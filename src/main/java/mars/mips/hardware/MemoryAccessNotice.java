

package mars.mips.hardware;

public class MemoryAccessNotice extends AccessNotice
{
    private int address;
    private int length;
    private int value;
    
    MemoryAccessNotice(final int type, final int address, final int length, final int value) {
        super(type);
        this.address = address;
        this.length = length;
        this.value = value;
    }
    
    public MemoryAccessNotice(final int type, final int address, final int value) {
        super(type);
        this.address = address;
        this.length = 4;
        this.value = value;
    }
    
    public int getAddress() {
        return this.address;
    }
    
    public int getLength() {
        return this.length;
    }
    
    public int getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return ((this.getAccessType() == 0) ? "R " : "W ") + "Mem " + this.address + " " + this.length + "B = " + this.value;
    }
}
