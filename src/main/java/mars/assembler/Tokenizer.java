

package mars.assembler;

import mars.Globals;
import java.util.Collection;
import mars.ErrorMessage;
import java.io.File;
import mars.ProcessingException;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import mars.MIPSprogram;
import mars.ErrorList;

public class Tokenizer
{
    private ErrorList errors;
    private MIPSprogram sourceMIPSprogram;
    private HashMap<String, String> equivalents;
    private static final String escapedCharacters = "'\"\\ntbrf0";
    private static final String[] escapedCharactersValues;
    
    public Tokenizer() {
        this(null);
    }
    
    public Tokenizer(final MIPSprogram program) {
        this.errors = new ErrorList();
        this.sourceMIPSprogram = program;
    }
    
    public ArrayList tokenize(final MIPSprogram p) throws ProcessingException {
        this.sourceMIPSprogram = p;
        this.equivalents = new HashMap<String, String>();
        final ArrayList tokenList = new ArrayList();
        final ArrayList<SourceLine> source = this.processIncludes(p, new HashMap<String, String>());
        p.setSourceLineList(source);
        for (int i = 0; i < source.size(); ++i) {
            final String sourceLine = source.get(i).getSource();
            final TokenList currentLineTokens = this.tokenizeLine(i + 1, sourceLine);
            tokenList.add(currentLineTokens);
            if (sourceLine.length() > 0 && sourceLine != currentLineTokens.getProcessedLine()) {
                source.set(i, new SourceLine(currentLineTokens.getProcessedLine(), source.get(i).getMIPSprogram(), source.get(i).getLineNumber()));
            }
        }
        if (this.errors.errorsOccurred()) {
            throw new ProcessingException(this.errors);
        }
        return tokenList;
    }
    
    private ArrayList<SourceLine> processIncludes(final MIPSprogram program, final Map<String, String> inclFiles) throws ProcessingException {
        final ArrayList source = program.getSourceList();
        final ArrayList<SourceLine> result = new ArrayList<SourceLine>(source.size());
        for (int i = 0; i < source.size(); ++i) {
            final String line = source.get(i);
            final TokenList tl = this.tokenizeLine(program, i + 1, line, false);
            boolean hasInclude = false;
            int ii = 0;
            while (ii < tl.size()) {
                if (tl.get(ii).getValue().equalsIgnoreCase(Directives.INCLUDE.getName()) && tl.size() > ii + 1 && tl.get(ii + 1).getType() == TokenTypes.QUOTED_STRING) {
                    String filename = tl.get(ii + 1).getValue();
                    filename = filename.substring(1, filename.length() - 1);
                    if (!new File(filename).isAbsolute()) {
                        filename = new File(program.getFilename()).getParent() + File.separator + filename;
                    }
                    if (inclFiles.containsKey(filename)) {
                        final Token t = tl.get(ii + 1);
                        this.errors.add(new ErrorMessage(program, t.getSourceLine(), t.getStartPos(), "Recursive include of file " + filename));
                        throw new ProcessingException(this.errors);
                    }
                    inclFiles.put(filename, filename);
                    final MIPSprogram incl = new MIPSprogram();
                    try {
                        incl.readSource(filename);
                    }
                    catch (ProcessingException p) {
                        final Token t2 = tl.get(ii + 1);
                        this.errors.add(new ErrorMessage(program, t2.getSourceLine(), t2.getStartPos(), "Error reading include file " + filename));
                        throw new ProcessingException(this.errors);
                    }
                    final ArrayList<SourceLine> allLines = this.processIncludes(incl, inclFiles);
                    result.addAll(allLines);
                    hasInclude = true;
                    break;
                }
                else {
                    ++ii;
                }
            }
            if (!hasInclude) {
                result.add(new SourceLine(line, program, i + 1));
            }
        }
        return result;
    }
    
    public TokenList tokenizeExampleInstruction(final String example) throws ProcessingException {
        TokenList result = new TokenList();
        result = this.tokenizeLine(this.sourceMIPSprogram, 0, example, false);
        if (this.errors.errorsOccurred()) {
            throw new ProcessingException(this.errors);
        }
        return result;
    }
    
    public TokenList tokenizeLine(final int lineNum, final String theLine) {
        return this.tokenizeLine(this.sourceMIPSprogram, lineNum, theLine, true);
    }
    
    public TokenList tokenizeLine(final int lineNum, final String theLine, final ErrorList callerErrorList) {
        final ErrorList saveList = this.errors;
        this.errors = callerErrorList;
        final TokenList tokens = this.tokenizeLine(lineNum, theLine);
        this.errors = saveList;
        return tokens;
    }
    
    public TokenList tokenizeLine(final int lineNum, final String theLine, final ErrorList callerErrorList, final boolean doEqvSubstitutes) {
        final ErrorList saveList = this.errors;
        this.errors = callerErrorList;
        final TokenList tokens = this.tokenizeLine(this.sourceMIPSprogram, lineNum, theLine, doEqvSubstitutes);
        this.errors = saveList;
        return tokens;
    }
    
    public TokenList tokenizeLine(final MIPSprogram program, final int lineNum, final String theLine, final boolean doEqvSubstitutes) {
        TokenList result = new TokenList();
        if (theLine.length() == 0) {
            return result;
        }
        final char[] line = theLine.toCharArray();
        int linePos = 0;
        final char[] token = new char[line.length];
        int tokenPos = 0;
        int tokenStartPos = 1;
        boolean insideQuotedString = false;
        if (Globals.debug) {
            System.out.println("source line --->" + theLine + "<---");
        }
        while (linePos < line.length) {
            char c = line[linePos];
            if (insideQuotedString) {
                token[tokenPos++] = c;
                if (c == '\"' && token[tokenPos - 2] != '\\') {
                    this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                    tokenPos = 0;
                    insideQuotedString = false;
                }
            }
            else {
                switch (c) {
                    case '#': {
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        tokenPos = line.length - linePos;
                        System.arraycopy(line, linePos, token, 0, tokenPos);
                        this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        linePos = line.length;
                        tokenPos = 0;
                        break;
                    }
                    case '\t':
                    case ' ':
                    case ',': {
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                            break;
                        }
                        break;
                    }
                    case '+':
                    case '-': {
                        if (tokenPos > 0 && line.length >= linePos + 2 && Character.isDigit(line[linePos + 1]) && (line[linePos - 1] == 'e' || line[linePos - 1] == 'E')) {
                            token[tokenPos++] = c;
                            break;
                        }
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        if ((!result.isEmpty() && result.get(result.size() - 1).getType() == TokenTypes.IDENTIFIER) || line.length < linePos + 2 || !Character.isDigit(line[linePos + 1])) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                            break;
                        }
                        break;
                    }
                    case '(':
                    case ')':
                    case ':': {
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        tokenPos = 0;
                        break;
                    }
                    case '\"': {
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        insideQuotedString = true;
                        break;
                    }
                    case '\'': {
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        final int lookaheadChars = line.length - linePos - 1;
                        if (lookaheadChars < 2) {
                            break;
                        }
                        c = line[++linePos];
                        if ((token[tokenPos++] = c) == '\'') {
                            break;
                        }
                        c = line[++linePos];
                        token[tokenPos++] = c;
                        if ((c == '\'' && token[1] != '\\') || lookaheadChars == 2) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                            tokenStartPos = linePos + 1;
                            break;
                        }
                        c = line[++linePos];
                        token[tokenPos++] = c;
                        if (c == '\'' || lookaheadChars == 3) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                            tokenStartPos = linePos + 1;
                            break;
                        }
                        if (lookaheadChars >= 5) {
                            c = line[++linePos];
                            if ((token[tokenPos++] = c) != '\'') {
                                c = line[++linePos];
                                token[tokenPos++] = c;
                            }
                        }
                        this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        tokenPos = 0;
                        tokenStartPos = linePos + 1;
                        break;
                    }
                    default: {
                        if (tokenPos == 0) {
                            tokenStartPos = linePos + 1;
                        }
                        token[tokenPos++] = c;
                        break;
                    }
                }
            }
            ++linePos;
        }
        if (tokenPos > 0) {
            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
            tokenPos = 0;
        }
        if (doEqvSubstitutes) {
            result = this.processEqv(program, lineNum, theLine, result);
        }
        return result;
    }
    
    private TokenList processEqv(final MIPSprogram program, final int lineNum, String theLine, final TokenList tokens) {
        if (tokens.size() > 2 && (tokens.get(0).getType() == TokenTypes.DIRECTIVE || tokens.get(2).getType() == TokenTypes.DIRECTIVE)) {
            final int dirPos = (tokens.get(0).getType() == TokenTypes.DIRECTIVE) ? 0 : 2;
            if (Directives.matchDirective(tokens.get(dirPos).getValue()) == Directives.EQV) {
                final int tokenPosLastOperand = tokens.size() - ((tokens.get(tokens.size() - 1).getType() == TokenTypes.COMMENT) ? 2 : 1);
                if (tokenPosLastOperand < dirPos + 2) {
                    this.errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos).getStartPos(), "Too few operands for " + Directives.EQV.getName() + " directive"));
                    return tokens;
                }
                if (tokens.get(dirPos + 1).getType() != TokenTypes.IDENTIFIER) {
                    this.errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos).getStartPos(), "Malformed " + Directives.EQV.getName() + " directive"));
                    return tokens;
                }
                final String symbol = tokens.get(dirPos + 1).getValue();
                for (int i = dirPos + 2; i < tokens.size(); ++i) {
                    if (tokens.get(i).getValue().equals(symbol)) {
                        this.errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos).getStartPos(), "Cannot substitute " + symbol + " for itself in " + Directives.EQV.getName() + " directive"));
                        return tokens;
                    }
                }
                final int startExpression = tokens.get(dirPos + 2).getStartPos();
                final int endExpression = tokens.get(tokenPosLastOperand).getStartPos() + tokens.get(tokenPosLastOperand).getValue().length();
                final String expression = theLine.substring(startExpression - 1, endExpression - 1);
                if (this.equivalents.containsKey(symbol) && !this.equivalents.get(symbol).equals(expression)) {
                    this.errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos + 1).getStartPos(), "\"" + symbol + "\" is already defined"));
                    return tokens;
                }
                this.equivalents.put(symbol, expression);
                return tokens;
            }
        }
        boolean substitutionMade = false;
        for (int j = 0; j < tokens.size(); ++j) {
            final Token token = tokens.get(j);
            if (token.getType() == TokenTypes.IDENTIFIER && this.equivalents != null && this.equivalents.containsKey(token.getValue())) {
                final String sub = this.equivalents.get(token.getValue());
                final int startPos = token.getStartPos();
                theLine = theLine.substring(0, startPos - 1) + sub + theLine.substring(startPos + token.getValue().length() - 1);
                substitutionMade = true;
                break;
            }
        }
        tokens.setProcessedLine(theLine);
        return substitutionMade ? this.tokenizeLine(lineNum, theLine) : tokens;
    }
    
    public ErrorList getErrors() {
        return this.errors;
    }
    
    private void processCandidateToken(final char[] token, final MIPSprogram program, final int line, final String theLine, final int tokenPos, final int tokenStartPos, final TokenList tokenList) {
        String value = new String(token, 0, tokenPos);
        if (value.length() > 0 && value.charAt(0) == '\'') {
            value = this.preprocessCharacterLiteral(value);
        }
        final TokenTypes type = TokenTypes.matchTokenType(value);
        if (type == TokenTypes.ERROR) {
            this.errors.add(new ErrorMessage(program, line, tokenStartPos, theLine + "\nInvalid language element: " + value));
        }
        final Token toke = new Token(type, value, program, line, tokenStartPos);
        tokenList.add(toke);
    }
    
    private String preprocessCharacterLiteral(final String value) {
        if (value.length() < 3 || value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'') {
            return value;
        }
        final String quotesRemoved = value.substring(1, value.length() - 1);
        if (quotesRemoved.charAt(0) != '\\') {
            return (quotesRemoved.length() == 1) ? Integer.toString(quotesRemoved.charAt(0)) : value;
        }
        if (quotesRemoved.length() == 2) {
            final int escapedCharacterIndex = "'\"\\ntbrf0".indexOf(quotesRemoved.charAt(1));
            return (escapedCharacterIndex >= 0) ? Tokenizer.escapedCharactersValues[escapedCharacterIndex] : value;
        }
        if (quotesRemoved.length() == 4) {
            try {
                final int intValue = Integer.parseInt(quotesRemoved.substring(1), 8);
                if (intValue >= 0 && intValue <= 255) {
                    return Integer.toString(intValue);
                }
            }
            catch (NumberFormatException ex) {}
        }
        return value;
    }
    
    static {
        escapedCharactersValues = new String[] { "39", "34", "92", "10", "9", "8", "13", "12", "0" };
    }
}
