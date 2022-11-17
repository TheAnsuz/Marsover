

package mars.mips.instructions.syscalls;

import java.util.Date;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;

public class SyscallTime extends AbstractSyscall
{
    public SyscallTime() {
        super(30, "Time");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final long value = new Date().getTime();
        RegisterFile.updateRegister(4, Binary.lowOrderLongToInt(value));
        RegisterFile.updateRegister(5, Binary.highOrderLongToInt(value));
    }
}
