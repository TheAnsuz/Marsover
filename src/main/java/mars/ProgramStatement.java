

package mars;

import mars.venus.NumberDisplayBaseChooser;
import java.util.ArrayList;
import mars.assembler.Token;
import mars.util.Binary;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.assembler.TokenTypes;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.Instruction;
import mars.assembler.TokenList;

public class ProgramStatement
{
    private MIPSprogram sourceMIPSprogram;
    private String source;
    private String basicAssemblyStatement;
    private String machineStatement;
    private TokenList originalTokenList;
    private TokenList strippedTokenList;
    private BasicStatementList basicStatementList;
    private int[] operands;
    private int numOperands;
    private Instruction instruction;
    private int textAddress;
    private int sourceLine;
    private int binaryStatement;
    private boolean altered;
    private static final String invalidOperator = "<INVALID>";
    
    public ProgramStatement(final MIPSprogram sourceMIPSprogram, final String source, final TokenList origTokenList, final TokenList strippedTokenList, final Instruction inst, final int textAddress, final int sourceLine) {
        this.sourceMIPSprogram = sourceMIPSprogram;
        this.source = source;
        this.originalTokenList = origTokenList;
        this.strippedTokenList = strippedTokenList;
        this.operands = new int[4];
        this.numOperands = 0;
        this.instruction = inst;
        this.textAddress = textAddress;
        this.sourceLine = sourceLine;
        this.basicAssemblyStatement = null;
        this.basicStatementList = new BasicStatementList();
        this.machineStatement = null;
        this.binaryStatement = 0;
        this.altered = false;
    }
    
    public ProgramStatement(final int binaryStatement, final int textAddress) {
        this.sourceMIPSprogram = null;
        this.binaryStatement = binaryStatement;
        this.textAddress = textAddress;
        final TokenList list = null;
        this.strippedTokenList = list;
        this.originalTokenList = list;
        this.source = "";
        final String s = null;
        this.basicAssemblyStatement = s;
        this.machineStatement = s;
        final BasicInstruction instr = Globals.instructionSet.findByBinaryCode(binaryStatement);
        if (instr == null) {
            this.operands = null;
            this.numOperands = 0;
            this.instruction = ((binaryStatement == 0) ? Globals.instructionSet.matchOperator("nop").get(0) : null);
        }
        else {
            this.operands = new int[4];
            this.numOperands = 0;
            this.instruction = instr;
            final String opandCodes = "fst";
            final String fmt = instr.getOperationMask();
            final BasicInstructionFormat instrFormat = instr.getInstructionFormat();
            int numOps = 0;
            for (int i = 0; i < opandCodes.length(); ++i) {
                final int code = opandCodes.charAt(i);
                final int j = fmt.indexOf(code);
                if (j >= 0) {
                    final int k0 = 31 - fmt.lastIndexOf(code);
                    final int k2 = 31 - j;
                    int opand = binaryStatement >> k0 & (1 << k2 - k0 + 1) - 1;
                    if (instrFormat.equals(BasicInstructionFormat.I_BRANCH_FORMAT) && numOps == 2) {
                        opand = opand << 16 >> 16;
                    }
                    else if (instrFormat.equals(BasicInstructionFormat.J_FORMAT) && numOps == 0) {
                        opand |= (textAddress >> 2 & 0x3C000000);
                    }
                    this.operands[numOps] = opand;
                    ++numOps;
                }
            }
            this.numOperands = numOps;
        }
        this.altered = false;
        this.basicStatementList = this.buildBasicStatementListFromBinaryCode(binaryStatement, instr, this.operands, this.numOperands);
    }
    
    public void buildBasicStatementFromBasicInstruction(final ErrorList errors) {
        Token token = this.strippedTokenList.get(0);
        String basic;
        String basicStatementElement = basic = token.getValue() + " ";
        this.basicStatementList.addString(basicStatementElement);
        this.numOperands = 0;
        for (int i = 1; i < this.strippedTokenList.size(); ++i) {
            token = this.strippedTokenList.get(i);
            final TokenTypes tokenType = token.getType();
            final String tokenValue = token.getValue();
            if (tokenType == TokenTypes.REGISTER_NUMBER) {
                basicStatementElement = tokenValue;
                basic += basicStatementElement;
                this.basicStatementList.addString(basicStatementElement);
                int registerNumber;
                try {
                    registerNumber = RegisterFile.getUserRegister(tokenValue).getNumber();
                }
                catch (Exception e) {
                    errors.add(new ErrorMessage(this.sourceMIPSprogram, token.getSourceLine(), token.getStartPos(), "invalid register name"));
                    return;
                }
                this.operands[this.numOperands++] = registerNumber;
            }
            else if (tokenType == TokenTypes.REGISTER_NAME) {
                final int registerNumber = RegisterFile.getNumber(tokenValue);
                basicStatementElement = "$" + registerNumber;
                basic += basicStatementElement;
                this.basicStatementList.addString(basicStatementElement);
                if (registerNumber < 0) {
                    errors.add(new ErrorMessage(this.sourceMIPSprogram, token.getSourceLine(), token.getStartPos(), "invalid register name"));
                    return;
                }
                this.operands[this.numOperands++] = registerNumber;
            }
            else if (tokenType == TokenTypes.FP_REGISTER_NAME) {
                final int registerNumber = Coprocessor1.getRegisterNumber(tokenValue);
                basicStatementElement = "$f" + registerNumber;
                basic += basicStatementElement;
                this.basicStatementList.addString(basicStatementElement);
                if (registerNumber < 0) {
                    errors.add(new ErrorMessage(this.sourceMIPSprogram, token.getSourceLine(), token.getStartPos(), "invalid FPU register name"));
                    return;
                }
                this.operands[this.numOperands++] = registerNumber;
            }
            else if (tokenType == TokenTypes.IDENTIFIER) {
                int address = this.sourceMIPSprogram.getLocalSymbolTable().getAddressLocalOrGlobal(tokenValue);
                if (address == -1) {
                    errors.add(new ErrorMessage(this.sourceMIPSprogram, token.getSourceLine(), token.getStartPos(), "Symbol \"" + tokenValue + "\" not found in symbol table."));
                    return;
                }
                boolean absoluteAddress = true;
                if (this.instruction instanceof BasicInstruction) {
                    final BasicInstructionFormat format = ((BasicInstruction)this.instruction).getInstructionFormat();
                    if (format == BasicInstructionFormat.I_BRANCH_FORMAT) {
                        address = address - (this.textAddress + 4) >> 2;
                        absoluteAddress = false;
                    }
                }
                basic += address;
                if (absoluteAddress) {
                    this.basicStatementList.addAddress(address);
                }
                else {
                    this.basicStatementList.addValue(address);
                }
                this.operands[this.numOperands++] = address;
            }
            else if (tokenType == TokenTypes.INTEGER_5 || tokenType == TokenTypes.INTEGER_16 || tokenType == TokenTypes.INTEGER_16U || tokenType == TokenTypes.INTEGER_32) {
                final int tempNumeric = Binary.stringToInt(tokenValue);
                basic += tempNumeric;
                this.basicStatementList.addValue(tempNumeric);
                this.operands[this.numOperands++] = tempNumeric;
            }
            else {
                basicStatementElement = tokenValue;
                basic += basicStatementElement;
                this.basicStatementList.addString(basicStatementElement);
            }
            if (i < this.strippedTokenList.size() - 1) {
                final TokenTypes nextTokenType = this.strippedTokenList.get(i + 1).getType();
                if (tokenType != TokenTypes.LEFT_PAREN && tokenType != TokenTypes.RIGHT_PAREN && nextTokenType != TokenTypes.LEFT_PAREN && nextTokenType != TokenTypes.RIGHT_PAREN) {
                    basicStatementElement = ",";
                    basic += basicStatementElement;
                    this.basicStatementList.addString(basicStatementElement);
                }
            }
        }
        this.basicAssemblyStatement = basic;
    }
    
    public void buildMachineStatementFromBasicStatement(final ErrorList errors) {
        try {
            this.machineStatement = ((BasicInstruction)this.instruction).getOperationMask();
        }
        catch (ClassCastException cce) {
            errors.add(new ErrorMessage(this.sourceMIPSprogram, this.sourceLine, 0, "INTERNAL ERROR: pseudo-instruction expansion contained a pseudo-instruction"));
            return;
        }
        final BasicInstructionFormat format = ((BasicInstruction)this.instruction).getInstructionFormat();
        if (format == BasicInstructionFormat.J_FORMAT) {
            if ((this.textAddress & 0xF0000000) != (this.operands[0] & 0xF0000000)) {
                errors.add(new ErrorMessage(this.sourceMIPSprogram, this.sourceLine, 0, "Jump target word address beyond 26-bit range"));
                return;
            }
            this.insertBinaryCode(this.operands[0] >>>= 2, Instruction.operandMask[0], errors);
        }
        else if (format == BasicInstructionFormat.I_BRANCH_FORMAT) {
            for (int i = 0; i < this.numOperands - 1; ++i) {
                this.insertBinaryCode(this.operands[i], Instruction.operandMask[i], errors);
            }
            this.insertBinaryCode(this.operands[this.numOperands - 1], Instruction.operandMask[this.numOperands - 1], errors);
        }
        else {
            for (int i = 0; i < this.numOperands; ++i) {
                this.insertBinaryCode(this.operands[i], Instruction.operandMask[i], errors);
            }
        }
        this.binaryStatement = Binary.binaryStringToInt(this.machineStatement);
    }
    
    @Override
    public String toString() {
        final String blanks = "                               ";
        String result = "[" + this.textAddress + "]";
        if (this.basicAssemblyStatement != null) {
            final int firstSpace = this.basicAssemblyStatement.indexOf(" ");
            result = result + blanks.substring(0, 16 - result.length()) + this.basicAssemblyStatement.substring(0, firstSpace);
            result = result + blanks.substring(0, 24 - result.length()) + this.basicAssemblyStatement.substring(firstSpace + 1);
        }
        else {
            result = result + blanks.substring(0, 16 - result.length()) + "0x" + Integer.toString(this.binaryStatement, 16);
        }
        result = result + blanks.substring(0, 40 - result.length()) + ";  ";
        if (this.operands != null) {
            for (int i = 0; i < this.numOperands; ++i) {
                result = result + Integer.toString(this.operands[i], 16) + " ";
            }
        }
        if (this.machineStatement != null) {
            result = result + "[" + Binary.binaryStringToHexString(this.machineStatement) + "]";
            result = result + "  " + this.machineStatement.substring(0, 6) + "|" + this.machineStatement.substring(6, 11) + "|" + this.machineStatement.substring(11, 16) + "|" + this.machineStatement.substring(16, 21) + "|" + this.machineStatement.substring(21, 26) + "|" + this.machineStatement.substring(26, 32);
        }
        return result;
    }
    
    public void setBasicAssemblyStatement(final String statement) {
        this.basicAssemblyStatement = statement;
    }
    
    public void setMachineStatement(final String statement) {
        this.machineStatement = statement;
    }
    
    public void setBinaryStatement(final int binaryCode) {
        this.binaryStatement = binaryCode;
    }
    
    public void setSource(final String src) {
        this.source = src;
    }
    
    public MIPSprogram getSourceMIPSprogram() {
        return this.sourceMIPSprogram;
    }
    
    public String getSourceFile() {
        return (this.sourceMIPSprogram == null) ? "" : this.sourceMIPSprogram.getFilename();
    }
    
    public String getSource() {
        return this.source;
    }
    
    public int getSourceLine() {
        return this.sourceLine;
    }
    
    public String getBasicAssemblyStatement() {
        return this.basicAssemblyStatement;
    }
    
    public String getPrintableBasicAssemblyStatement() {
        return this.basicStatementList.toString();
    }
    
    public String getMachineStatement() {
        return this.machineStatement;
    }
    
    public int getBinaryStatement() {
        return this.binaryStatement;
    }
    
    public TokenList getOriginalTokenList() {
        return this.originalTokenList;
    }
    
    public TokenList getStrippedTokenList() {
        return this.strippedTokenList;
    }
    
    public Instruction getInstruction() {
        return this.instruction;
    }
    
    public int getAddress() {
        return this.textAddress;
    }
    
    public int[] getOperands() {
        return this.operands;
    }
    
    public int getOperand(final int i) {
        if (i >= 0 && i < this.numOperands) {
            return this.operands[i];
        }
        return -1;
    }
    
    private void insertBinaryCode(final int value, final char mask, final ErrorList errors) {
        final int startPos = this.machineStatement.indexOf(mask);
        final int endPos = this.machineStatement.lastIndexOf(mask);
        if (startPos == -1 || endPos == -1) {
            errors.add(new ErrorMessage(this.sourceMIPSprogram, this.sourceLine, 0, "INTERNAL ERROR: mismatch in number of operands in statement vs mask"));
            return;
        }
        final String bitString = Binary.intToBinaryString(value, endPos - startPos + 1);
        String state = this.machineStatement.substring(0, startPos) + bitString;
        if (endPos < this.machineStatement.length() - 1) {
            state += this.machineStatement.substring(endPos + 1);
        }
        this.machineStatement = state;
    }
    
    private BasicStatementList buildBasicStatementListFromBinaryCode(final int binary, final BasicInstruction instr, final int[] operands, final int numOperands) {
        final BasicStatementList statementList = new BasicStatementList();
        int tokenListCounter = 1;
        if (instr == null) {
            statementList.addString("<INVALID>");
            return statementList;
        }
        statementList.addString(instr.getName() + " ");
        for (int i = 0; i < numOperands; ++i) {
            if (tokenListCounter > 1 && tokenListCounter < instr.getTokenList().size()) {
                final TokenTypes thisTokenType = instr.getTokenList().get(tokenListCounter).getType();
                if (thisTokenType != TokenTypes.LEFT_PAREN && thisTokenType != TokenTypes.RIGHT_PAREN) {
                    statementList.addString(",");
                }
            }
            for (boolean notOperand = true; notOperand && tokenListCounter < instr.getTokenList().size(); ++tokenListCounter) {
                final TokenTypes tokenType = instr.getTokenList().get(tokenListCounter).getType();
                if (tokenType.equals(TokenTypes.LEFT_PAREN)) {
                    statementList.addString("(");
                }
                else if (tokenType.equals(TokenTypes.RIGHT_PAREN)) {
                    statementList.addString(")");
                }
                else if (tokenType.toString().contains("REGISTER")) {
                    final String marker = tokenType.toString().contains("FP_REGISTER") ? "$f" : "$";
                    statementList.addString(marker + operands[i]);
                    notOperand = false;
                }
                else {
                    statementList.addValue(operands[i]);
                    notOperand = false;
                }
            }
        }
        while (tokenListCounter < instr.getTokenList().size()) {
            final TokenTypes tokenType2 = instr.getTokenList().get(tokenListCounter).getType();
            if (tokenType2.equals(TokenTypes.LEFT_PAREN)) {
                statementList.addString("(");
            }
            else if (tokenType2.equals(TokenTypes.RIGHT_PAREN)) {
                statementList.addString(")");
            }
            ++tokenListCounter;
        }
        return statementList;
    }
    
    private class BasicStatementList
    {
        private ArrayList list;
        
        BasicStatementList() {
            this.list = new ArrayList();
        }
        
        void addString(final String string) {
            this.list.add(new ListElement(0, string, 0));
        }
        
        void addAddress(final int address) {
            this.list.add(new ListElement(1, null, address));
        }
        
        void addValue(final int value) {
            this.list.add(new ListElement(2, null, value));
        }
        
        @Override
        public String toString() {
            final int addressBase = Globals.getSettings().getBooleanSetting(5) ? 16 : 10;
            final int valueBase = Globals.getSettings().getBooleanSetting(6) ? 16 : 10;
            final StringBuffer result = new StringBuffer();
            for (int i = 0; i < this.list.size(); ++i) {
                final ListElement e = this.list.get(i);
                switch (e.type) {
                    case 0: {
                        result.append(e.sValue);
                        break;
                    }
                    case 1: {
                        result.append(NumberDisplayBaseChooser.formatNumber(e.iValue, addressBase));
                        break;
                    }
                    case 2: {
                        if (valueBase == 16) {
                            result.append(Binary.intToHexString(e.iValue));
                            break;
                        }
                        result.append(NumberDisplayBaseChooser.formatNumber(e.iValue, valueBase));
                        break;
                    }
                }
            }
            return result.toString();
        }
        
        private class ListElement
        {
            int type;
            String sValue;
            int iValue;
            
            ListElement(final int type, final String sValue, final int iValue) {
                this.type = type;
                this.sValue = sValue;
                this.iValue = iValue;
            }
        }
    }
}
