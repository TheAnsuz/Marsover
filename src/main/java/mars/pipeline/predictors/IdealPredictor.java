

package mars.pipeline.predictors;

import mars.pipeline.BranchPredictor;

public class IdealPredictor extends BranchPredictor
{
    public IdealPredictor() {
        this.type = BranchPredictor_type.ideal;
    }
    
    @Override
    public boolean isPredictionCorrect(final int instruction) {
        return true;
    }
    
    @Override
    public boolean getPrediction(final int instruction) {
        return this.is_branch_taken(instruction);
    }
}
