

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.SystemIO;

public class SyscallPrintInt extends AbstractSyscall
{
    public SyscallPrintInt() {
        super(1, "PrintInt");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(Integer.toString(RegisterFile.getValue(4)));
    }
}
