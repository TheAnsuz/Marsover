

package mars.mips.instructions;

public class BasicInstructionFormat
{
    public static final BasicInstructionFormat R_FORMAT;
    public static final BasicInstructionFormat I_FORMAT;
    public static final BasicInstructionFormat I_BRANCH_FORMAT;
    public static final BasicInstructionFormat J_FORMAT;
    
    private BasicInstructionFormat() {
    }
    
    static {
        R_FORMAT = new BasicInstructionFormat();
        I_FORMAT = new BasicInstructionFormat();
        I_BRANCH_FORMAT = new BasicInstructionFormat();
        J_FORMAT = new BasicInstructionFormat();
    }
}
