

package mars.pipeline.predictors;

import mars.pipeline.BranchPredictor;

public class TakenPredictor extends BranchPredictor
{
    public TakenPredictor() {
        this.type = BranchPredictor_type.taken;
    }
    
    @Override
    public boolean isPredictionCorrect(final int instruction) {
        return this.is_branch_taken(instruction);
    }
    
    @Override
    public boolean getPrediction(final int instruction) {
        return true;
    }
}
