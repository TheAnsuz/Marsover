

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.SystemIO;

public class SyscallClose extends AbstractSyscall
{
    public SyscallClose() {
        super(16, "Close");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.closeFile(RegisterFile.getValue(4));
    }
}
