

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;

public class SyscallExit extends AbstractSyscall
{
    public SyscallExit() {
        super(10, "Exit");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        throw new ProcessingException();
    }
}
