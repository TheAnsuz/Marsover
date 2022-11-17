

package mars.assembler;

import mars.Globals;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.Register;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;

public final class TokenTypes
{
    public static final String TOKEN_DELIMITERS = "\t ,()";
    public static final TokenTypes COMMENT;
    public static final TokenTypes DIRECTIVE;
    public static final TokenTypes OPERATOR;
    public static final TokenTypes DELIMITER;
    public static final TokenTypes REGISTER_NAME;
    public static final TokenTypes REGISTER_NUMBER;
    public static final TokenTypes FP_REGISTER_NAME;
    public static final TokenTypes IDENTIFIER;
    public static final TokenTypes LEFT_PAREN;
    public static final TokenTypes RIGHT_PAREN;
    public static final TokenTypes INTEGER_5;
    public static final TokenTypes INTEGER_16;
    public static final TokenTypes INTEGER_16U;
    public static final TokenTypes INTEGER_32;
    public static final TokenTypes REAL_NUMBER;
    public static final TokenTypes QUOTED_STRING;
    public static final TokenTypes PLUS;
    public static final TokenTypes MINUS;
    public static final TokenTypes COLON;
    public static final TokenTypes ERROR;
    public static final TokenTypes MACRO_PARAMETER;
    private String descriptor;
    
    private TokenTypes() {
        this.descriptor = "generic";
    }
    
    private TokenTypes(final String name) {
        this.descriptor = name;
    }
    
    @Override
    public String toString() {
        return this.descriptor;
    }
    
    public static TokenTypes matchTokenType(final String value) {
        final TokenTypes type = null;
        if (value.charAt(0) == '\'') {
            return TokenTypes.ERROR;
        }
        if (value.charAt(0) == '#') {
            return TokenTypes.COMMENT;
        }
        if (value.length() == 1) {
            switch (value.charAt(0)) {
                case '(': {
                    return TokenTypes.LEFT_PAREN;
                }
                case ')': {
                    return TokenTypes.RIGHT_PAREN;
                }
                case ':': {
                    return TokenTypes.COLON;
                }
                case '+': {
                    return TokenTypes.PLUS;
                }
                case '-': {
                    return TokenTypes.MINUS;
                }
            }
        }
        if (Macro.tokenIsMacroParameter(value, false)) {
            return TokenTypes.MACRO_PARAMETER;
        }
        Register reg = RegisterFile.getUserRegister(value);
        if (reg != null) {
            if (reg.getName().equals(value)) {
                return TokenTypes.REGISTER_NAME;
            }
            return TokenTypes.REGISTER_NUMBER;
        }
        else {
            reg = Coprocessor1.getRegister(value);
            if (reg != null) {
                return TokenTypes.FP_REGISTER_NAME;
            }
            try {
                final int i = Binary.stringToInt(value);
                if (i >= 0 && i <= 31) {
                    return TokenTypes.INTEGER_5;
                }
                if (i >= 0 && i <= 65535) {
                    return TokenTypes.INTEGER_16U;
                }
                if (i >= -32768 && i <= 32767) {
                    return TokenTypes.INTEGER_16;
                }
                return TokenTypes.INTEGER_32;
            }
            catch (NumberFormatException ex) {
                try {
                    Double.parseDouble(value);
                    return TokenTypes.REAL_NUMBER;
                }
                catch (NumberFormatException ex2) {
                    if (Globals.instructionSet.matchOperator(value) != null) {
                        return TokenTypes.OPERATOR;
                    }
                    if (value.charAt(0) == '.' && Directives.matchDirective(value) != null) {
                        return TokenTypes.DIRECTIVE;
                    }
                    if (value.charAt(0) == '\"') {
                        return TokenTypes.QUOTED_STRING;
                    }
                    if (isValidIdentifier(value)) {
                        return TokenTypes.IDENTIFIER;
                    }
                    return TokenTypes.ERROR;
                }
            }
        }
    }
    
    public static boolean isIntegerTokenType(final TokenTypes type) {
        return type == TokenTypes.INTEGER_5 || type == TokenTypes.INTEGER_16 || type == TokenTypes.INTEGER_16U || type == TokenTypes.INTEGER_32;
    }
    
    public static boolean isFloatingTokenType(final TokenTypes type) {
        return type == TokenTypes.REAL_NUMBER;
    }
    
    public static boolean isValidIdentifier(final String value) {
        boolean result = Character.isLetter(value.charAt(0)) || value.charAt(0) == '_' || value.charAt(0) == '.' || value.charAt(0) == '$';
        for (int index = 1; result && index < value.length(); ++index) {
            if (!Character.isLetterOrDigit(value.charAt(index)) && value.charAt(index) != '_' && value.charAt(index) != '.' && value.charAt(index) != '$') {
                result = false;
            }
        }
        return result;
    }
    
    static {
        COMMENT = new TokenTypes("COMMENT");
        DIRECTIVE = new TokenTypes("DIRECTIVE");
        OPERATOR = new TokenTypes("OPERATOR");
        DELIMITER = new TokenTypes("DELIMITER");
        REGISTER_NAME = new TokenTypes("REGISTER_NAME");
        REGISTER_NUMBER = new TokenTypes("REGISTER_NUMBER");
        FP_REGISTER_NAME = new TokenTypes("FP_REGISTER_NAME");
        IDENTIFIER = new TokenTypes("IDENTIFIER");
        LEFT_PAREN = new TokenTypes("LEFT_PAREN");
        RIGHT_PAREN = new TokenTypes("RIGHT_PAREN");
        INTEGER_5 = new TokenTypes("INTEGER_5");
        INTEGER_16 = new TokenTypes("INTEGER_16");
        INTEGER_16U = new TokenTypes("INTEGER_16U");
        INTEGER_32 = new TokenTypes("INTEGER_32");
        REAL_NUMBER = new TokenTypes("REAL_NUMBER");
        QUOTED_STRING = new TokenTypes("QUOTED_STRING");
        PLUS = new TokenTypes("PLUS");
        MINUS = new TokenTypes("MINUS");
        COLON = new TokenTypes("COLON");
        ERROR = new TokenTypes("ERROR");
        MACRO_PARAMETER = new TokenTypes("MACRO_PARAMETER");
    }
}
