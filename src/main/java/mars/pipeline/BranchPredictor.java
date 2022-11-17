

package mars.pipeline;

import mars.mips.hardware.RegisterFile;

public abstract class BranchPredictor
{
    protected BranchPredictor_type type;
    
    public abstract boolean getPrediction(final int p0);
    
    public abstract boolean isPredictionCorrect(final int p0);
    
    public boolean is_branch_taken(final int instruction) {
        final int opCode = Decode.getCode(instruction);
        final int rs = Decode.getRs(instruction);
        final int rsData = RegisterFile.getValue(rs);
        final int rt = Decode.getRt(instruction);
        final int rtData = RegisterFile.getValue(rt);
        final int auxCode = rt;
        switch (opCode) {
            case 4: {
                return rsData == rtData;
            }
            case 5: {
                return rsData != rtData;
            }
            case 6: {
                return rsData <= 0;
            }
            case 7: {
                return rsData > 0;
            }
            case 1: {
                if (auxCode == 1 || auxCode == 17) {
                    return rsData >= 0;
                }
                return (auxCode == 0 || auxCode == 16) && rsData < 0;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public String toString() {
        return this.type.toString();
    }
    
    public enum BranchPredictor_type
    {
        ideal("Perfect predictor"), 
        stop("Stop pipeline"), 
        notTaken("Predict not taken"), 
        taken("Predict Taken"), 
        delayedBranch("delayed Branch"), 
        btb("BTB");
        
        private final String name;
        
        private BranchPredictor_type(final String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
    }
}
