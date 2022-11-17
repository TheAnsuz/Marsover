

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;

public abstract class AbstractSyscall implements Syscall
{
    private int serviceNumber;
    private final String serviceName;
    
    public AbstractSyscall(final int number, final String name) {
        this.serviceNumber = number;
        this.serviceName = name;
    }
    
    @Override
    public String getName() {
        return this.serviceName;
    }
    
    @Override
    public void setNumber(final int num) {
        this.serviceNumber = num;
    }
    
    @Override
    public int getNumber() {
        return this.serviceNumber;
    }
    
    @Override
    public abstract void simulate(final ProgramStatement p0) throws ProcessingException;
}
