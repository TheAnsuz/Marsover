

package mars.mips.instructions.syscalls;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;

public class SyscallExit2 extends AbstractSyscall
{
    public SyscallExit2() {
        super(17, "Exit2");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        if (Globals.getGui() == null) {
            Globals.exitCode = RegisterFile.getValue(4);
        }
        throw new ProcessingException();
    }
}
