

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.SystemIO;

public class SyscallPrintChar extends AbstractSyscall
{
    public SyscallPrintChar() {
        super(11, "PrintChar");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final char t = (char)(RegisterFile.getValue(4) & 0xFF);
        SystemIO.printString(new Character(t).toString());
    }
}
