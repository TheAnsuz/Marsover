

package mars.pipeline.predictors;

import mars.pipeline.BranchPredictor;

public class StopPredictor extends BranchPredictor
{
    public StopPredictor() {
        this.type = BranchPredictor_type.stop;
    }
    
    @Override
    public boolean isPredictionCorrect(final int instruction) {
        return false;
    }
    
    @Override
    public boolean getPrediction(final int instruction) {
        return !this.is_branch_taken(instruction);
    }
}
