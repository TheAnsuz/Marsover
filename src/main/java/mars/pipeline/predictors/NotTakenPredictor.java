

package mars.pipeline.predictors;

import mars.pipeline.BranchPredictor;

public class NotTakenPredictor extends BranchPredictor
{
    public NotTakenPredictor() {
        this.type = BranchPredictor_type.notTaken;
    }
    
    @Override
    public boolean isPredictionCorrect(final int instruction) {
        return !this.is_branch_taken(instruction);
    }
    
    @Override
    public boolean getPrediction(final int instruction) {
        return false;
    }
}
