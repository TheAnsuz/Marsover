

package mars.tools;

public class BHTEntry
{
    private boolean[] m_history;
    private boolean m_prediction;
    private int m_incorrect;
    private int m_correct;
    
    public BHTEntry(final int historySize, final boolean initVal) {
        this.m_prediction = initVal;
        this.m_history = new boolean[historySize];
        for (int i = 0; i < historySize; ++i) {
            this.m_history[i] = initVal;
        }
        final int n = 0;
        this.m_incorrect = n;
        this.m_correct = n;
    }
    
    public boolean getPrediction() {
        return this.m_prediction;
    }
    
    public void updatePrediction(final boolean branchTaken) {
        for (int i = 0; i < this.m_history.length - 1; ++i) {
            this.m_history[i] = this.m_history[i + 1];
        }
        if ((this.m_history[this.m_history.length - 1] = branchTaken) == this.m_prediction) {
            ++this.m_correct;
        }
        else {
            ++this.m_incorrect;
            boolean changePrediction = true;
            for (int j = 0; j < this.m_history.length; ++j) {
                if (this.m_history[j] != branchTaken) {
                    changePrediction = false;
                }
            }
            if (changePrediction) {
                this.m_prediction = !this.m_prediction;
            }
        }
    }
    
    public int getStatsPredIncorrect() {
        return this.m_incorrect;
    }
    
    public int getStatsPredCorrect() {
        return this.m_correct;
    }
    
    public double getStatsPredPrecision() {
        final int sum = this.m_incorrect + this.m_correct;
        return (sum == 0) ? 0.0 : (this.m_correct * 100.0 / sum);
    }
    
    public String getHistoryAsStr() {
        String result = "";
        for (int i = 0; i < this.m_history.length; ++i) {
            if (i > 0) {
                result += ", ";
            }
            result += (this.m_history[i] ? "T" : "NT");
        }
        return result;
    }
    
    public String getPredictionAsStr() {
        return this.m_prediction ? "TAKE" : "NOT TAKE";
    }
}
