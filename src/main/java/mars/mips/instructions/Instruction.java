

package mars.mips.instructions;

import mars.ProcessingException;
import mars.assembler.Tokenizer;
import java.util.StringTokenizer;
import mars.assembler.TokenList;

public abstract class Instruction
{
    public static final int INSTRUCTION_LENGTH = 4;
    public static final int INSTRUCTION_LENGTH_BITS = 32;
    public static char[] operandMask;
    protected String mnemonic;
    protected String exampleFormat;
    protected String description;
    protected TokenList tokenList;
    
    public String getName() {
        return this.mnemonic;
    }
    
    public String getExampleFormat() {
        return this.exampleFormat;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public TokenList getTokenList() {
        return this.tokenList;
    }
    
    public int getInstructionLength() {
        return 4;
    }
    
    protected String extractOperator(final String example) {
        final StringTokenizer st = new StringTokenizer(example, " ,\t");
        return st.nextToken();
    }
    
    public void createExampleTokenList() {
        try {
            this.tokenList = new Tokenizer().tokenizeExampleInstruction(this.exampleFormat);
        }
        catch (ProcessingException pe) {
            System.out.println("CONFIGURATION ERROR: Instruction example \"" + this.exampleFormat + "\" contains invalid token(s).");
        }
    }
    
    static {
        Instruction.operandMask = new char[] { 'f', 's', 't' };
    }
}
