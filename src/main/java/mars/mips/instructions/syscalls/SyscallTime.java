

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;
import java.util.Date;
import mars.ProgramStatement;

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
