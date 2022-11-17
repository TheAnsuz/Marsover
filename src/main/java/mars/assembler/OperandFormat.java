

package mars.assembler;

import java.util.ArrayList;
import mars.ErrorList;
import mars.ErrorMessage;
import mars.Globals;
import mars.mips.instructions.Instruction;
import mars.util.Binary;

public class OperandFormat
{
    private OperandFormat() {
    }
    
    static boolean tokenOperandMatch(final TokenList candidateList, final Instruction inst, final ErrorList errors) {
        return numOperandsCheck(candidateList, inst, errors) && operandTypeCheck(candidateList, inst, errors);
    }
    
    static Instruction bestOperandMatch(final TokenList tokenList, final ArrayList<Instruction> instrMatches) {
        if (instrMatches == null) {
            return null;
        }
        if (instrMatches.size() == 1) {
            return instrMatches.get(0);
        }
        for (int i = 0; i < instrMatches.size(); ++i) {
            final Instruction potentialMatch = instrMatches.get(i);
            if (tokenOperandMatch(tokenList, potentialMatch, new ErrorList())) {
                return potentialMatch;
            }
        }
        return instrMatches.get(0);
    }
    
    private static boolean numOperandsCheck(final TokenList cand, final Instruction spec, final ErrorList errors) {
        final int numOperands = cand.size() - 1;
        final int reqNumOperands = spec.getTokenList().size() - 1;
        final Token operator = cand.get(0);
        if (numOperands == reqNumOperands) {
            return true;
        }
        if (numOperands < reqNumOperands) {
            final String mess = "Too few or incorrectly formatted operands. Expected: " + spec.getExampleFormat();
            generateMessage(operator, mess, errors);
        }
        else {
            final String mess = "Too many or incorrectly formatted operands. Expected: " + spec.getExampleFormat();
            generateMessage(operator, mess, errors);
        }
        return false;
    }
    
    private static boolean operandTypeCheck(final TokenList cand, final Instruction spec, final ErrorList errors) {
        for (int i = 1; i < spec.getTokenList().size(); ++i) {
            final Token candToken = cand.get(i);
            final Token specToken = spec.getTokenList().get(i);
            final TokenTypes candType = candToken.getType();
            final TokenTypes specType = specToken.getType();
            if (specType == TokenTypes.IDENTIFIER && candType == TokenTypes.OPERATOR) {
                final Token replacement = new Token(TokenTypes.IDENTIFIER, candToken.getValue(), candToken.getSourceMIPSprogram(), candToken.getSourceLine(), candToken.getStartPos());
                cand.set(i, replacement);
            }
            else if ((specType == TokenTypes.REGISTER_NAME || specType == TokenTypes.REGISTER_NUMBER) && candType == TokenTypes.REGISTER_NAME) {
                if (Globals.getSettings().getBareMachineEnabled()) {
                    generateMessage(candToken, "Use register number instead of name.  See Settings.", errors);
                    return false;
                }
            }
            else if (specType != TokenTypes.REGISTER_NAME || candType != TokenTypes.REGISTER_NUMBER) {
                if ((specType != TokenTypes.INTEGER_16 || candType != TokenTypes.INTEGER_5) && (specType != TokenTypes.INTEGER_16U || candType != TokenTypes.INTEGER_5) && (specType != TokenTypes.INTEGER_32 || candType != TokenTypes.INTEGER_5) && (specType != TokenTypes.INTEGER_32 || candType != TokenTypes.INTEGER_16U)) {
                    if (specType != TokenTypes.INTEGER_32 || candType != TokenTypes.INTEGER_16) {
                        if (candType == TokenTypes.INTEGER_16U || candType == TokenTypes.INTEGER_16) {
                            final int temp = Binary.stringToInt(candToken.getValue());
                            if (specType == TokenTypes.INTEGER_16 && candType == TokenTypes.INTEGER_16U && temp >= -32768 && temp <= 32767) {
                                continue;
                            }
                            if (specType == TokenTypes.INTEGER_16U && candType == TokenTypes.INTEGER_16 && temp >= 0 && temp <= 65535) {
                                continue;
                            }
                        }
                        if ((specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_16) || (specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_16U) || (specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_32) || (specType == TokenTypes.INTEGER_16 && candType == TokenTypes.INTEGER_16U) || (specType == TokenTypes.INTEGER_16U && candType == TokenTypes.INTEGER_16) || (specType == TokenTypes.INTEGER_16U && candType == TokenTypes.INTEGER_32) || (specType == TokenTypes.INTEGER_16 && candType == TokenTypes.INTEGER_32)) {
                            generateMessage(candToken, "operand is out of range", errors);
                            return false;
                        }
                        if (candType != specType) {
                            generateMessage(candToken, "operand is of incorrect type", errors);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private static void generateMessage(final Token token, final String mess, final ErrorList errors) {
        errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "\"" + token.getValue() + "\": " + mess));
    }
}
