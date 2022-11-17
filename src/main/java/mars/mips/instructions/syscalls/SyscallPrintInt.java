

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.util.SystemIO;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallPrintInt extends AbstractSyscall
{
    public SyscallPrintInt() {
        super(1, "PrintInt");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(new Integer(RegisterFile.getValue(4)).toString());
    }
}
