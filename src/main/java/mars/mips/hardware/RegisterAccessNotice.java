

package mars.mips.hardware;

public class RegisterAccessNotice extends AccessNotice
{
    private final String registerName;
    private final int id;
    
    RegisterAccessNotice(final int type, final String registerName, final int id) {
        super(type);
        this.registerName = registerName;
        this.id = id;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getRegisterName() {
        return this.registerName;
    }
    
    @Override
    public String toString() {
        return ((this.getAccessType() == 0) ? "R " : "W ") + "Reg " + this.registerName;
    }
}
