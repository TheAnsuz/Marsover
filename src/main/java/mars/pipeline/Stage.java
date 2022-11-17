

package mars.pipeline;

public enum Stage
{
    IF("IF"), 
    ID("Is"), 
    EX("EX"), 
    MEM("M"), 
    WB("WB"), 
    COMMIT("C0"), 
    MULT("X"), 
    AC("AC");
    
    private final String name;
    private int cycle;
    
    private Stage(final String name) {
        this.name = name;
        this.cycle = 0;
    }
    
    @Override
    public String toString() {
        return this.name + ((this.cycle == 0) ? "" : Integer.valueOf(this.cycle));
    }
    
    public void nextCycle() {
        ++this.cycle;
    }
    
    public void clear() {
        this.cycle = 0;
    }
}
