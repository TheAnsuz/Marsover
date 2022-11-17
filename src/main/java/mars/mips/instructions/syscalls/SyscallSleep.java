

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallSleep extends AbstractSyscall
{
    public SyscallSleep() {
        super(32, "Sleep");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        try {
            Thread.sleep(RegisterFile.getValue(4));
        }
        catch (InterruptedException e) {}
    }
}
