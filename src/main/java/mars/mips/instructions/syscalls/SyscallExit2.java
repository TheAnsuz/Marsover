

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.Globals;
import mars.ProgramStatement;

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
