

package mars.mips.instructions.syscalls;

public class SyscallNumberOverride
{
    private String serviceName;
    private int newServiceNumber;
    
    public SyscallNumberOverride(final String serviceName, final String value) {
        this.serviceName = serviceName;
        try {
            this.newServiceNumber = Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e) {
            System.out.println("Error processing Syscall number override: '" + value.trim() + "' is not a valid integer");
            System.exit(0);
        }
    }
    
    public String getName() {
        return this.serviceName;
    }
    
    public int getNumber() {
        return this.newServiceNumber;
    }
}
