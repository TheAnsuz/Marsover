

package mars.assembler;

import mars.util.Binary;
import mars.venus.NumberDisplayBaseChooser;
import mars.mips.instructions.Instruction;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import mars.util.SystemIO;
import mars.mips.hardware.AddressErrorException;
import mars.mips.instructions.ExtendedInstruction;
import mars.mips.instructions.BasicInstruction;
import mars.ProgramStatement;
import mars.ErrorMessage;
import java.util.Collection;
import mars.Globals;
import mars.mips.hardware.Memory;
import mars.ProcessingException;
import mars.MIPSprogram;
import mars.ErrorList;
import java.util.ArrayList;

public class Assembler
{
    private ArrayList machineList;
    private ErrorList errors;
    private boolean inDataSegment;
    private boolean inMacroSegment;
    private int externAddress;
    private boolean autoAlign;
    private Directives currentDirective;
    private Directives dataDirective;
    private MIPSprogram fileCurrentlyBeingAssembled;
    private TokenList globalDeclarationList;
    private UserKernelAddressSpace textAddress;
    private UserKernelAddressSpace dataAddress;
    private DataSegmentForwardReferences currentFileDataSegmentForwardReferences;
    private DataSegmentForwardReferences accumulatedDataSegmentForwardReferences;
    
    public ArrayList assemble(final MIPSprogram p, final boolean extendedAssemblerEnabled) throws ProcessingException {
        return this.assemble(p, extendedAssemblerEnabled, false);
    }
    
    public ArrayList assemble(final MIPSprogram p, final boolean extendedAssemblerEnabled, final boolean warningsAreErrors) throws ProcessingException {
        final ArrayList programFiles = new ArrayList();
        programFiles.add(p);
        return this.assemble(programFiles, extendedAssemblerEnabled, warningsAreErrors);
    }
    
    public ErrorList getErrorList() {
        return this.errors;
    }
    
    public ArrayList assemble(final ArrayList tokenizedProgramFiles, final boolean extendedAssemblerEnabled) throws ProcessingException {
        return this.assemble(tokenizedProgramFiles, extendedAssemblerEnabled, false);
    }
    
    public ArrayList assemble(final ArrayList tokenizedProgramFiles, final boolean extendedAssemblerEnabled, final boolean warningsAreErrors) throws ProcessingException {
        if (tokenizedProgramFiles == null || tokenizedProgramFiles.size() == 0) {
            return null;
        }
        this.textAddress = new UserKernelAddressSpace(Memory.textBaseAddress, Memory.kernelTextBaseAddress);
        this.dataAddress = new UserKernelAddressSpace(Memory.dataBaseAddress, Memory.kernelDataBaseAddress);
        this.externAddress = Memory.externBaseAddress;
        this.currentFileDataSegmentForwardReferences = new DataSegmentForwardReferences();
        this.accumulatedDataSegmentForwardReferences = new DataSegmentForwardReferences();
        Globals.symbolTable.clear();
        Globals.memory.clear();
        this.machineList = new ArrayList();
        this.errors = new ErrorList();
        if (Globals.debug) {
            System.out.println("Assembler first pass begins:");
        }
        for (int fileIndex = 0; fileIndex < tokenizedProgramFiles.size() && !this.errors.errorLimitExceeded(); ++fileIndex) {
            this.fileCurrentlyBeingAssembled = tokenizedProgramFiles.get(fileIndex);
            this.globalDeclarationList = new TokenList();
            this.inDataSegment = false;
            this.inMacroSegment = false;
            this.autoAlign = true;
            this.dataDirective = Directives.WORD;
            this.fileCurrentlyBeingAssembled.getLocalSymbolTable().clear();
            this.currentFileDataSegmentForwardReferences.clear();
            final ArrayList<SourceLine> sourceLineList = this.fileCurrentlyBeingAssembled.getSourceLineList();
            final ArrayList tokenList = this.fileCurrentlyBeingAssembled.getTokenList();
            final ArrayList parsedList = this.fileCurrentlyBeingAssembled.createParsedList();
            final MacroPool macroPool = this.fileCurrentlyBeingAssembled.createMacroPool();
            for (int i = 0; i < tokenList.size() && !this.errors.errorLimitExceeded(); ++i) {
                for (int z = 0; z < tokenList.get(i).size(); ++z) {
                    final Token t = tokenList.get(i).get(z);
                    t.setOriginal(sourceLineList.get(i).getMIPSprogram(), sourceLineList.get(i).getLineNumber());
                }
                final ArrayList<ProgramStatement> statements = this.parseLine(tokenList.get(i), sourceLineList.get(i).getSource(), sourceLineList.get(i).getLineNumber(), extendedAssemblerEnabled);
                if (statements != null) {
                    parsedList.addAll(statements);
                }
            }
            if (this.inMacroSegment) {
                this.errors.add(new ErrorMessage(this.fileCurrentlyBeingAssembled, this.fileCurrentlyBeingAssembled.getLocalMacroPool().getCurrent().getFromLine(), 0, "Macro started but not ended (no .end_macro directive)"));
            }
            this.transferGlobals();
            this.currentFileDataSegmentForwardReferences.resolve(this.fileCurrentlyBeingAssembled.getLocalSymbolTable());
            this.accumulatedDataSegmentForwardReferences.add(this.currentFileDataSegmentForwardReferences);
            this.currentFileDataSegmentForwardReferences.clear();
        }
        this.accumulatedDataSegmentForwardReferences.resolve(Globals.symbolTable);
        this.accumulatedDataSegmentForwardReferences.generateErrorMessages(this.errors);
        if (this.errors.errorsOccurred()) {
            throw new ProcessingException(this.errors);
        }
        if (Globals.debug) {
            System.out.println("Assembler second pass begins");
        }
        for (int fileIndex = 0; fileIndex < tokenizedProgramFiles.size() && !this.errors.errorLimitExceeded(); ++fileIndex) {
            this.fileCurrentlyBeingAssembled = tokenizedProgramFiles.get(fileIndex);
            final ArrayList parsedList2 = this.fileCurrentlyBeingAssembled.getParsedList();
            for (int j = 0; j < parsedList2.size(); ++j) {
                final ProgramStatement statement = parsedList2.get(j);
                statement.buildBasicStatementFromBasicInstruction(this.errors);
                if (this.errors.errorsOccurred()) {
                    throw new ProcessingException(this.errors);
                }
                if (statement.getInstruction() instanceof BasicInstruction) {
                    this.machineList.add(statement);
                }
                else {
                    final ExtendedInstruction inst = (ExtendedInstruction)statement.getInstruction();
                    final String basicAssembly = statement.getBasicAssemblyStatement();
                    final int sourceLine = statement.getSourceLine();
                    final TokenList theTokenList = new Tokenizer().tokenizeLine(sourceLine, basicAssembly, this.errors, false);
                    ArrayList templateList;
                    if (this.compactTranslationCanBeApplied(statement)) {
                        templateList = inst.getCompactBasicIntructionTemplateList();
                    }
                    else {
                        templateList = inst.getBasicIntructionTemplateList();
                    }
                    this.textAddress.set(statement.getAddress());
                    for (int instrNumber = 0; instrNumber < templateList.size(); ++instrNumber) {
                        final String instruction = ExtendedInstruction.makeTemplateSubstitutions(this.fileCurrentlyBeingAssembled, templateList.get(instrNumber), theTokenList);
                        if (instruction != null) {
                            if (instruction != "") {
                                if (Globals.debug) {
                                    System.out.println("PSEUDO generated: " + instruction);
                                }
                                final TokenList newTokenList = new Tokenizer().tokenizeLine(sourceLine, instruction, this.errors, false);
                                final ArrayList instrMatches = this.matchInstruction(newTokenList.get(0));
                                final Instruction instr = OperandFormat.bestOperandMatch(newTokenList, instrMatches);
                                final ProgramStatement ps = new ProgramStatement(this.fileCurrentlyBeingAssembled, (instrNumber == 0) ? statement.getSource() : "", newTokenList, newTokenList, instr, this.textAddress.get(), statement.getSourceLine());
                                this.textAddress.increment(4);
                                ps.buildBasicStatementFromBasicInstruction(this.errors);
                                this.machineList.add(ps);
                            }
                        }
                    }
                }
            }
        }
        if (Globals.debug) {
            System.out.println("Code generation begins");
        }
        for (int k = 0; k < this.machineList.size() && !this.errors.errorLimitExceeded(); ++k) {
            final ProgramStatement statement2 = this.machineList.get(k);
            statement2.buildMachineStatementFromBasicStatement(this.errors);
            if (Globals.debug) {
                System.out.println(statement2);
            }
            try {
                Globals.memory.setStatement(statement2.getAddress(), statement2);
            }
            catch (AddressErrorException e) {
                final Token t2 = statement2.getOriginalTokenList().get(0);
                this.errors.add(new ErrorMessage(t2.getSourceMIPSprogram(), t2.getSourceLine(), t2.getStartPos(), "Invalid address for text segment: " + e.getAddress()));
            }
        }
        SystemIO.resetFiles();
        Collections.sort((List<Object>)this.machineList, new ProgramStatementComparator());
        this.catchDuplicateAddresses(this.machineList, this.errors);
        if (this.errors.errorsOccurred() || (this.errors.warningsOccurred() && warningsAreErrors)) {
            throw new ProcessingException(this.errors);
        }
        return this.machineList;
    }
    
    private void catchDuplicateAddresses(final ArrayList instructions, final ErrorList errors) {
        for (int i = 0; i < instructions.size() - 1; ++i) {
            final ProgramStatement ps1 = instructions.get(i);
            final ProgramStatement ps2 = instructions.get(i + 1);
            if (ps1.getAddress() == ps2.getAddress()) {
                errors.add(new ErrorMessage(ps2.getSourceMIPSprogram(), ps2.getSourceLine(), 0, "Duplicate text segment address: " + NumberDisplayBaseChooser.formatUnsignedInteger(ps2.getAddress(), Globals.getSettings().getDisplayAddressesInHex() ? 16 : 10) + " already occupied by " + ps1.getSourceFile() + " line " + ps1.getSourceLine() + " (caused by use of " + (Memory.inTextSegment(ps2.getAddress()) ? ".text" : ".ktext") + " operand)"));
            }
        }
    }
    
    private ArrayList<ProgramStatement> parseLine(final TokenList tokenList, final String source, final int sourceLineNumber, final boolean extendedAssemblerEnabled) {
        final ArrayList<ProgramStatement> ret = new ArrayList<ProgramStatement>();
        TokenList tokens = this.stripComment(tokenList);
        final MacroPool macroPool = this.fileCurrentlyBeingAssembled.getLocalMacroPool();
        if (this.inMacroSegment) {
            this.detectLabels(tokens, macroPool.getCurrent());
        }
        else {
            this.stripLabels(tokens);
        }
        if (tokens.isEmpty()) {
            return null;
        }
        final Token token = tokens.get(0);
        final TokenTypes tokenType = token.getType();
        if (tokenType == TokenTypes.DIRECTIVE) {
            this.executeDirective(tokens);
            return null;
        }
        if (this.inMacroSegment) {
            return null;
        }
        TokenList parenFreeTokens = tokens;
        if (tokens.size() > 2 && tokens.get(1).getType() == TokenTypes.LEFT_PAREN && tokens.get(tokens.size() - 1).getType() == TokenTypes.RIGHT_PAREN) {
            parenFreeTokens = (TokenList)tokens.clone();
            parenFreeTokens.remove(tokens.size() - 1);
            parenFreeTokens.remove(1);
        }
        final Macro macro = macroPool.getMatchingMacro(parenFreeTokens, sourceLineNumber);
        if (macro != null) {
            tokens = parenFreeTokens;
            final int counter = macroPool.getNextCounter();
            if (macroPool.pushOnCallStack(token)) {
                this.errors.add(new ErrorMessage(this.fileCurrentlyBeingAssembled, tokens.get(0).getSourceLine(), 0, "Detected a macro expansion loop (recursive reference). "));
            }
            else {
                for (int i = macro.getFromLine() + 1; i < macro.getToLine(); ++i) {
                    String substituted = macro.getSubstitutedLine(i, tokens, counter, this.errors);
                    final TokenList tokenList2 = this.fileCurrentlyBeingAssembled.getTokenizer().tokenizeLine(i, substituted, this.errors);
                    if (tokenList2.getProcessedLine().length() > 0) {
                        substituted = tokenList2.getProcessedLine();
                    }
                    final ArrayList<ProgramStatement> statements = this.parseLine(tokenList2, "<" + (i - macro.getFromLine() + macro.getOriginalFromLine()) + "> " + substituted.trim(), sourceLineNumber, extendedAssemblerEnabled);
                    if (statements != null) {
                        ret.addAll(statements);
                    }
                }
                macroPool.popFromCallStack();
            }
            return ret;
        }
        if (tokenType == TokenTypes.IDENTIFIER && token.getValue().charAt(0) == '.') {
            this.errors.add(new ErrorMessage(true, token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "MARS does not recognize the " + token.getValue() + " directive.  Ignored."));
            return null;
        }
        if (this.inDataSegment && (tokenType == TokenTypes.PLUS || tokenType == TokenTypes.MINUS || tokenType == TokenTypes.QUOTED_STRING || tokenType == TokenTypes.IDENTIFIER || TokenTypes.isIntegerTokenType(tokenType) || TokenTypes.isFloatingTokenType(tokenType))) {
            this.executeDirectiveContinuation(tokens);
            return null;
        }
        if (!this.inDataSegment) {
            final ArrayList instrMatches = this.matchInstruction(token);
            if (instrMatches == null) {
                return ret;
            }
            final Instruction inst = OperandFormat.bestOperandMatch(tokens, instrMatches);
            if (inst instanceof ExtendedInstruction && !extendedAssemblerEnabled) {
                this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "Extended (pseudo) instruction or format not permitted.  See Settings."));
            }
            if (OperandFormat.tokenOperandMatch(tokens, inst, this.errors)) {
                final ProgramStatement programStatement = new ProgramStatement(this.fileCurrentlyBeingAssembled, source, tokenList, tokens, inst, this.textAddress.get(), sourceLineNumber);
                int instLength = inst.getInstructionLength();
                if (this.compactTranslationCanBeApplied(programStatement)) {
                    instLength = ((ExtendedInstruction)inst).getCompactInstructionLength();
                }
                this.textAddress.increment(instLength);
                ret.add(programStatement);
                return ret;
            }
        }
        return null;
    }
    
    private void detectLabels(final TokenList tokens, final Macro current) {
        if (this.tokenListBeginsWithLabel(tokens)) {
            current.addLabel(tokens.get(0).getValue());
        }
    }
    
    private boolean compactTranslationCanBeApplied(final ProgramStatement statement) {
        return statement.getInstruction() instanceof ExtendedInstruction && Globals.memory.usingCompactMemoryConfiguration() && ((ExtendedInstruction)statement.getInstruction()).hasCompactTranslation();
    }
    
    private TokenList stripComment(final TokenList tokenList) {
        if (tokenList.isEmpty()) {
            return tokenList;
        }
        final TokenList tokens = (TokenList)tokenList.clone();
        final int last = tokens.size() - 1;
        if (tokens.get(last).getType() == TokenTypes.COMMENT) {
            tokens.remove(last);
        }
        return tokens;
    }
    
    private void stripLabels(final TokenList tokens) {
        final boolean thereWasLabel = this.parseAndRecordLabel(tokens);
        if (thereWasLabel) {
            tokens.remove(0);
            tokens.remove(0);
        }
    }
    
    private boolean parseAndRecordLabel(final TokenList tokens) {
        if (tokens.size() < 2) {
            return false;
        }
        final Token token = tokens.get(0);
        if (this.tokenListBeginsWithLabel(tokens)) {
            if (token.getType() == TokenTypes.OPERATOR) {
                token.setType(TokenTypes.IDENTIFIER);
            }
            this.fileCurrentlyBeingAssembled.getLocalSymbolTable().addSymbol(token, this.inDataSegment ? this.dataAddress.get() : this.textAddress.get(), this.inDataSegment, this.errors);
            return true;
        }
        return false;
    }
    
    private boolean tokenListBeginsWithLabel(final TokenList tokens) {
        return tokens.size() >= 2 && (tokens.get(0).getType() == TokenTypes.IDENTIFIER || tokens.get(0).getType() == TokenTypes.OPERATOR) && tokens.get(1).getType() == TokenTypes.COLON;
    }
    
    private void executeDirective(final TokenList tokens) {
        final Token token = tokens.get(0);
        final Directives direct = Directives.matchDirective(token.getValue());
        if (Globals.debug) {
            System.out.println("line " + token.getSourceLine() + " is directive " + direct);
        }
        if (direct == null) {
            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive is invalid or not implemented in MARS"));
            return;
        }
        if (direct != Directives.EQV) {
            if (direct == Directives.MACRO) {
                if (tokens.size() < 2) {
                    this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive requires at least one argument."));
                    return;
                }
                if (tokens.get(1).getType() != TokenTypes.IDENTIFIER) {
                    this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), tokens.get(1).getStartPos(), "Invalid Macro name \"" + tokens.get(1).getValue() + "\""));
                    return;
                }
                if (this.inMacroSegment) {
                    this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "Nested macros are not allowed"));
                    return;
                }
                this.inMacroSegment = true;
                final MacroPool pool = this.fileCurrentlyBeingAssembled.getLocalMacroPool();
                pool.beginMacro(tokens.get(1));
                for (int i = 2; i < tokens.size(); ++i) {
                    final Token arg = tokens.get(i);
                    if (arg.getType() != TokenTypes.RIGHT_PAREN) {
                        if (arg.getType() != TokenTypes.LEFT_PAREN) {
                            if (!Macro.tokenIsMacroParameter(arg.getValue(), true)) {
                                this.errors.add(new ErrorMessage(arg.getSourceMIPSprogram(), arg.getSourceLine(), arg.getStartPos(), "Invalid macro argument '" + arg.getValue() + "'"));
                                return;
                            }
                            pool.getCurrent().addArg(arg.getValue());
                        }
                    }
                }
            }
            else if (direct == Directives.END_MACRO) {
                if (tokens.size() > 1) {
                    this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "invalid text after .END_MACRO"));
                    return;
                }
                if (!this.inMacroSegment) {
                    this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), ".END_MACRO without .MACRO"));
                    return;
                }
                this.inMacroSegment = false;
                this.fileCurrentlyBeingAssembled.getLocalMacroPool().commitMacro(token);
            }
            else {
                if (this.inMacroSegment) {
                    return;
                }
                if (direct == Directives.DATA || direct == Directives.KDATA) {
                    this.inDataSegment = true;
                    this.autoAlign = true;
                    this.dataAddress.setAddressSpace((direct != Directives.DATA) ? 1 : 0);
                    if (tokens.size() > 1 && TokenTypes.isIntegerTokenType(tokens.get(1).getType())) {
                        this.dataAddress.set(Binary.stringToInt(tokens.get(1).getValue()));
                    }
                }
                else if (direct == Directives.TEXT || direct == Directives.KTEXT) {
                    this.inDataSegment = false;
                    this.textAddress.setAddressSpace((direct != Directives.TEXT) ? 1 : 0);
                    if (tokens.size() > 1 && TokenTypes.isIntegerTokenType(tokens.get(1).getType())) {
                        this.textAddress.set(Binary.stringToInt(tokens.get(1).getValue()));
                    }
                }
                else if (direct == Directives.WORD || direct == Directives.HALF || direct == Directives.BYTE || direct == Directives.FLOAT || direct == Directives.DOUBLE) {
                    this.dataDirective = direct;
                    if (this.passesDataSegmentCheck(token) && tokens.size() > 1) {
                        this.storeNumeric(tokens, direct, this.errors);
                    }
                }
                else if (direct == Directives.ASCII || direct == Directives.ASCIIZ) {
                    this.dataDirective = direct;
                    if (this.passesDataSegmentCheck(token)) {
                        this.storeStrings(tokens, direct, this.errors);
                    }
                }
                else if (direct == Directives.ALIGN) {
                    if (this.passesDataSegmentCheck(token)) {
                        if (tokens.size() != 2) {
                            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" requires one operand"));
                            return;
                        }
                        if (!TokenTypes.isIntegerTokenType(tokens.get(1).getType()) || Binary.stringToInt(tokens.get(1).getValue()) < 0) {
                            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" requires a non-negative integer"));
                            return;
                        }
                        final int value = Binary.stringToInt(tokens.get(1).getValue());
                        if (value == 0) {
                            this.autoAlign = false;
                        }
                        else {
                            this.dataAddress.set(this.alignToBoundary(this.dataAddress.get(), (int)Math.pow(2.0, value)));
                        }
                    }
                }
                else if (direct == Directives.SPACE) {
                    if (this.passesDataSegmentCheck(token)) {
                        if (tokens.size() != 2) {
                            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" requires one operand"));
                            return;
                        }
                        if (!TokenTypes.isIntegerTokenType(tokens.get(1).getType()) || Binary.stringToInt(tokens.get(1).getValue()) < 0) {
                            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" requires a non-negative integer"));
                            return;
                        }
                        final int value = Binary.stringToInt(tokens.get(1).getValue());
                        this.dataAddress.increment(value);
                    }
                }
                else if (direct == Directives.EXTERN) {
                    if (tokens.size() != 3) {
                        this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive requires two operands (label and size)."));
                        return;
                    }
                    if (!TokenTypes.isIntegerTokenType(tokens.get(2).getType()) || Binary.stringToInt(tokens.get(2).getValue()) < 0) {
                        this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" requires a non-negative integer size"));
                        return;
                    }
                    final int size = Binary.stringToInt(tokens.get(2).getValue());
                    if (Globals.symbolTable.getAddress(tokens.get(1).getValue()) == -1) {
                        Globals.symbolTable.addSymbol(tokens.get(1), this.externAddress, true, this.errors);
                        this.externAddress += size;
                    }
                }
                else if (direct == Directives.SET) {
                    this.errors.add(new ErrorMessage(true, token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "MARS currently ignores the .set directive."));
                }
                else {
                    if (direct != Directives.GLOBL) {
                        this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive recognized but not yet implemented."));
                        return;
                    }
                    if (tokens.size() < 2) {
                        this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive requires at least one argument."));
                        return;
                    }
                    for (int j = 1; j < tokens.size(); ++j) {
                        final Token label = tokens.get(j);
                        if (label.getType() != TokenTypes.IDENTIFIER) {
                            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive argument must be label."));
                            return;
                        }
                        this.globalDeclarationList.add(label);
                    }
                }
            }
        }
    }
    
    private void transferGlobals() {
        for (int i = 0; i < this.globalDeclarationList.size(); ++i) {
            final Token label = this.globalDeclarationList.get(i);
            final Symbol symtabEntry = this.fileCurrentlyBeingAssembled.getLocalSymbolTable().getSymbol(label.getValue());
            if (symtabEntry == null) {
                this.errors.add(new ErrorMessage(this.fileCurrentlyBeingAssembled, label.getSourceLine(), label.getStartPos(), "\"" + label.getValue() + "\" declared global label but not defined."));
            }
            else if (Globals.symbolTable.getAddress(label.getValue()) != -1) {
                this.errors.add(new ErrorMessage(this.fileCurrentlyBeingAssembled, label.getSourceLine(), label.getStartPos(), "\"" + label.getValue() + "\" already defined as global in a different file."));
            }
            else {
                this.fileCurrentlyBeingAssembled.getLocalSymbolTable().removeSymbol(label);
                Globals.symbolTable.addSymbol(label, symtabEntry.getAddress(), symtabEntry.getType(), this.errors);
            }
        }
    }
    
    private void executeDirectiveContinuation(final TokenList tokens) {
        final Directives direct = this.dataDirective;
        if (direct == Directives.WORD || direct == Directives.HALF || direct == Directives.BYTE || direct == Directives.FLOAT || direct == Directives.DOUBLE) {
            if (tokens.size() > 0) {
                this.storeNumeric(tokens, direct, this.errors);
            }
        }
        else if ((direct == Directives.ASCII || direct == Directives.ASCIIZ) && this.passesDataSegmentCheck(tokens.get(0))) {
            this.storeStrings(tokens, direct, this.errors);
        }
    }
    
    private ArrayList matchInstruction(final Token token) {
        if (token.getType() != TokenTypes.OPERATOR) {
            if (token.getSourceMIPSprogram().getLocalMacroPool().matchesAnyMacroName(token.getValue())) {
                this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "forward reference or invalid parameters for macro \"" + token.getValue() + "\""));
            }
            else {
                this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is not a recognized operator"));
            }
            return null;
        }
        final ArrayList inst = Globals.instructionSet.matchOperator(token.getValue());
        if (inst == null) {
            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "Internal Assembler error: \"" + token.getValue() + "\" tokenized OPERATOR then not recognized"));
        }
        return inst;
    }
    
    private void storeNumeric(final TokenList tokens, final Directives directive, final ErrorList errors) {
        Token token = tokens.get(0);
        if (!this.passesDataSegmentCheck(token)) {
            return;
        }
        int tokenStart = 0;
        if (token.getType() == TokenTypes.DIRECTIVE) {
            tokenStart = 1;
        }
        final int lengthInBytes = DataTypes.getLengthInBytes(directive);
        if (tokens.size() != 4 || tokens.get(2).getType() != TokenTypes.COLON) {
            for (int i = tokenStart; i < tokens.size(); ++i) {
                token = tokens.get(i);
                if (Directives.isIntegerDirective(directive)) {
                    this.storeInteger(token, directive, errors);
                }
                if (Directives.isFloatingDirective(directive)) {
                    this.storeRealNumber(token, directive, errors);
                }
            }
            return;
        }
        final Token valueToken = tokens.get(1);
        final Token repetitionsToken = tokens.get(3);
        if (((!Directives.isIntegerDirective(directive) || !TokenTypes.isIntegerTokenType(valueToken.getType())) && (!Directives.isFloatingDirective(directive) || (!TokenTypes.isIntegerTokenType(valueToken.getType()) && !TokenTypes.isFloatingTokenType(valueToken.getType())))) || !TokenTypes.isIntegerTokenType(repetitionsToken.getType())) {
            errors.add(new ErrorMessage(this.fileCurrentlyBeingAssembled, valueToken.getSourceLine(), valueToken.getStartPos(), "malformed expression"));
            return;
        }
        final int repetitions = Binary.stringToInt(repetitionsToken.getValue());
        if (repetitions <= 0) {
            errors.add(new ErrorMessage(this.fileCurrentlyBeingAssembled, repetitionsToken.getSourceLine(), repetitionsToken.getStartPos(), "repetition factor must be positive"));
            return;
        }
        if (this.inDataSegment) {
            if (this.autoAlign) {
                this.dataAddress.set(this.alignToBoundary(this.dataAddress.get(), lengthInBytes));
            }
            for (int j = 0; j < repetitions; ++j) {
                if (Directives.isIntegerDirective(directive)) {
                    this.storeInteger(valueToken, directive, errors);
                }
                else {
                    this.storeRealNumber(valueToken, directive, errors);
                }
            }
        }
    }
    
    private void storeInteger(final Token token, final Directives directive, final ErrorList errors) {
        final int lengthInBytes = DataTypes.getLengthInBytes(directive);
        if (TokenTypes.isIntegerTokenType(token.getType())) {
            final int fullvalue;
            int value = fullvalue = Binary.stringToInt(token.getValue());
            if (directive == Directives.BYTE) {
                value &= 0xFF;
            }
            else if (directive == Directives.HALF) {
                value &= 0xFFFF;
            }
            if (DataTypes.outOfRange(directive, fullvalue)) {
                errors.add(new ErrorMessage(true, token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is out-of-range for a signed value and possibly truncated"));
            }
            if (this.inDataSegment) {
                this.writeToDataSegment(value, lengthInBytes, token, errors);
            }
            else {
                try {
                    Globals.memory.set(this.textAddress.get(), value, lengthInBytes);
                }
                catch (AddressErrorException e) {
                    errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + this.textAddress.get() + "\" is not a valid text segment address"));
                    return;
                }
                this.textAddress.increment(lengthInBytes);
            }
        }
        else if (token.getType() == TokenTypes.IDENTIFIER) {
            if (this.inDataSegment) {
                final int value = this.fileCurrentlyBeingAssembled.getLocalSymbolTable().getAddressLocalOrGlobal(token.getValue());
                if (value == -1) {
                    final int dataAddress = this.writeToDataSegment(0, lengthInBytes, token, errors);
                    this.currentFileDataSegmentForwardReferences.add(dataAddress, lengthInBytes, token);
                }
                else {
                    this.writeToDataSegment(value, lengthInBytes, token, errors);
                }
            }
            else {
                errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" label as directive operand not permitted in text segment"));
            }
        }
        else {
            errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is not a valid integer constant or label"));
        }
    }
    
    private void storeRealNumber(final Token token, final Directives directive, final ErrorList errors) {
        final int lengthInBytes = DataTypes.getLengthInBytes(directive);
        if (!TokenTypes.isIntegerTokenType(token.getType())) {
            if (!TokenTypes.isFloatingTokenType(token.getType())) {
                errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is not a valid floating point constant"));
                return;
            }
        }
        double value;
        try {
            value = Double.parseDouble(token.getValue());
        }
        catch (NumberFormatException nfe) {
            errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is not a valid floating point constant"));
            return;
        }
        if (DataTypes.outOfRange(directive, value)) {
            errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is an out-of-range value"));
            return;
        }
        if (directive == Directives.FLOAT) {
            this.writeToDataSegment(Float.floatToIntBits((float)value), lengthInBytes, token, errors);
        }
        if (directive == Directives.DOUBLE) {
            this.writeDoubleToDataSegment(value, token, errors);
        }
    }
    
    private void storeStrings(final TokenList tokens, final Directives direct, final ErrorList errors) {
        int tokenStart = 0;
        if (tokens.get(0).getType() == TokenTypes.DIRECTIVE) {
            tokenStart = 1;
        }
        for (int i = tokenStart; i < tokens.size(); ++i) {
            final Token token = tokens.get(i);
            if (token.getType() != TokenTypes.QUOTED_STRING) {
                errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" is not a valid character string"));
            }
            else {
                final String quote = token.getValue();
                for (int j = 1; j < quote.length() - 1; ++j) {
                    char theChar = quote.charAt(j);
                    if (theChar == '\\') {
                        theChar = quote.charAt(++j);
                        switch (theChar) {
                            case 'n': {
                                theChar = '\n';
                                break;
                            }
                            case 't': {
                                theChar = '\t';
                                break;
                            }
                            case 'r': {
                                theChar = '\r';
                                break;
                            }
                            case '\\': {
                                theChar = '\\';
                                break;
                            }
                            case '\'': {
                                theChar = '\'';
                                break;
                            }
                            case '\"': {
                                theChar = '\"';
                                break;
                            }
                            case 'b': {
                                theChar = '\b';
                                break;
                            }
                            case 'f': {
                                theChar = '\f';
                                break;
                            }
                            case '0': {
                                theChar = '\0';
                                break;
                            }
                        }
                    }
                    try {
                        Globals.memory.set(this.dataAddress.get(), theChar, 1);
                    }
                    catch (AddressErrorException e) {
                        errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + this.dataAddress.get() + "\" is not a valid data segment address"));
                    }
                    this.dataAddress.increment(1);
                }
                if (direct == Directives.ASCIIZ) {
                    try {
                        Globals.memory.set(this.dataAddress.get(), 0, 1);
                    }
                    catch (AddressErrorException e2) {
                        errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + this.dataAddress.get() + "\" is not a valid data segment address"));
                    }
                    this.dataAddress.increment(1);
                }
            }
        }
    }
    
    private boolean passesDataSegmentCheck(final Token token) {
        if (!this.inDataSegment) {
            this.errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\" directive cannot appear in text segment"));
            return false;
        }
        return true;
    }
    
    private int writeToDataSegment(final int value, final int lengthInBytes, final Token token, final ErrorList errors) {
        if (this.autoAlign) {
            this.dataAddress.set(this.alignToBoundary(this.dataAddress.get(), lengthInBytes));
        }
        try {
            Globals.memory.set(this.dataAddress.get(), value, lengthInBytes);
        }
        catch (AddressErrorException e) {
            errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + this.dataAddress.get() + "\" is not a valid data segment address"));
            return this.dataAddress.get();
        }
        final int address = this.dataAddress.get();
        this.dataAddress.increment(lengthInBytes);
        return address;
    }
    
    private void writeDoubleToDataSegment(final double value, final Token token, final ErrorList errors) {
        final int lengthInBytes = 8;
        if (this.autoAlign) {
            this.dataAddress.set(this.alignToBoundary(this.dataAddress.get(), lengthInBytes));
        }
        try {
            Globals.memory.setDouble(this.dataAddress.get(), value);
        }
        catch (AddressErrorException e) {
            errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + this.dataAddress.get() + "\" is not a valid data segment address"));
            return;
        }
        this.dataAddress.increment(lengthInBytes);
    }
    
    private int alignToBoundary(final int address, final int byteBoundary) {
        final int remainder = address % byteBoundary;
        if (remainder == 0) {
            return address;
        }
        final int alignedAddress = address + byteBoundary - remainder;
        this.fileCurrentlyBeingAssembled.getLocalSymbolTable().fixSymbolTableAddress(address, alignedAddress);
        return alignedAddress;
    }
    
    private class ProgramStatementComparator implements Comparator
    {
        @Override
        public int compare(final Object obj1, final Object obj2) {
            if (obj1 instanceof ProgramStatement && obj2 instanceof ProgramStatement) {
                final int addr1 = ((ProgramStatement)obj1).getAddress();
                final int addr2 = ((ProgramStatement)obj2).getAddress();
                return ((addr1 < 0 && addr2 >= 0) || (addr1 >= 0 && addr2 < 0)) ? addr2 : (addr1 - addr2);
            }
            throw new ClassCastException();
        }
        
        @Override
        public boolean equals(final Object obj) {
            return this == obj;
        }
    }
    
    private class UserKernelAddressSpace
    {
        int[] address;
        int currentAddressSpace;
        private final int USER = 0;
        private final int KERNEL = 1;
        
        private UserKernelAddressSpace(final int userBase, final int kernelBase) {
            (this.address = new int[2])[0] = userBase;
            this.address[1] = kernelBase;
            this.currentAddressSpace = 0;
        }
        
        private int get() {
            return this.address[this.currentAddressSpace];
        }
        
        private void set(final int value) {
            this.address[this.currentAddressSpace] = value;
        }
        
        private void increment(final int increment) {
            final int[] address = this.address;
            final int currentAddressSpace = this.currentAddressSpace;
            address[currentAddressSpace] += increment;
        }
        
        private void setAddressSpace(final int addressSpace) {
            if (addressSpace == 0 || addressSpace == 1) {
                this.currentAddressSpace = addressSpace;
                return;
            }
            throw new IllegalArgumentException();
        }
    }
    
    private class DataSegmentForwardReferences
    {
        private ArrayList forwardReferenceList;
        
        private DataSegmentForwardReferences() {
            this.forwardReferenceList = new ArrayList();
        }
        
        private int size() {
            return this.forwardReferenceList.size();
        }
        
        private void add(final int patchAddress, final int length, final Token token) {
            this.forwardReferenceList.add(new DataSegmentForwardReference(patchAddress, length, token));
        }
        
        private void add(final DataSegmentForwardReferences another) {
            this.forwardReferenceList.addAll(another.forwardReferenceList);
        }
        
        private void clear() {
            this.forwardReferenceList.clear();
        }
        
        private int resolve(final SymbolTable localSymtab) {
            int count = 0;
            for (int i = 0; i < this.forwardReferenceList.size(); ++i) {
                final DataSegmentForwardReference entry = this.forwardReferenceList.get(i);
                final int labelAddress = localSymtab.getAddressLocalOrGlobal(entry.token.getValue());
                if (labelAddress != -1) {
                    try {
                        Globals.memory.set(entry.patchAddress, labelAddress, entry.length);
                    }
                    catch (AddressErrorException ex) {}
                    this.forwardReferenceList.remove(i);
                    --i;
                    ++count;
                }
            }
            return count;
        }
        
        private void generateErrorMessages(final ErrorList errors) {
            for (int i = 0; i < this.forwardReferenceList.size(); ++i) {
                final DataSegmentForwardReference entry = this.forwardReferenceList.get(i);
                errors.add(new ErrorMessage(entry.token.getSourceMIPSprogram(), entry.token.getSourceLine(), entry.token.getStartPos(), "Symbol \"" + entry.token.getValue() + "\" not found in symbol table."));
            }
        }
        
        private class DataSegmentForwardReference
        {
            int patchAddress;
            int length;
            Token token;
            
            DataSegmentForwardReference(final int patchAddress, final int length, final Token token) {
                this.patchAddress = patchAddress;
                this.length = length;
                this.token = token;
            }
        }
    }
}
