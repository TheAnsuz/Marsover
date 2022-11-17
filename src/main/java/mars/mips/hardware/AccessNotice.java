

package mars.mips.hardware;

public abstract class AccessNotice
{
    public static final int READ = 0;
    public static final int WRITE = 1;
    private int accessType;
    private Thread thread;
    
    protected AccessNotice(final int type) {
        if (type != 0 && type != 1) {
            throw new IllegalArgumentException();
        }
        this.accessType = type;
        this.thread = Thread.currentThread();
    }
    
    public int getAccessType() {
        return this.accessType;
    }
    
    public Thread getThread() {
        return this.thread;
    }
    
    public boolean accessIsFromGUI() {
        return this.thread.getName().startsWith("AWT");
    }
    
    public boolean accessIsFromMIPS() {
        return this.thread.getName().startsWith("MIPS");
    }
}
