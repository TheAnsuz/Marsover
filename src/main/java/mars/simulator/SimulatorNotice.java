

package mars.simulator;

public class SimulatorNotice
{
    private final int action;
    private final int maxSteps;
    private final double runSpeed;
    private final int programCounter;
    public static final int SIMULATOR_START = 0;
    public static final int SIMULATOR_STOP = 1;
    
    public SimulatorNotice(final int action, final int maxSteps, final double runSpeed, final int programCounter) {
        this.action = action;
        this.maxSteps = maxSteps;
        this.runSpeed = runSpeed;
        this.programCounter = programCounter;
    }
    
    public int getAction() {
        return this.action;
    }
    
    public int getMaxSteps() {
        return this.maxSteps;
    }
    
    public double getRunSpeed() {
        return this.runSpeed;
    }
    
    public int getProgramCounter() {
        return this.programCounter;
    }
    
    @Override
    public String toString() {
        return ((this.getAction() == 0) ? "START " : "STOP  ") + "Max Steps " + this.maxSteps + " Speed " + ((this.runSpeed == 40.0) ? "unlimited " : ("" + this.runSpeed + " inst/sec")) + "Prog Ctr " + this.programCounter;
    }
}
