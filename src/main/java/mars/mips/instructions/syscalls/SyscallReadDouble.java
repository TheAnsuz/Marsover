

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor1;
import mars.util.Binary;
import mars.util.SystemIO;

public class SyscallReadDouble extends AbstractSyscall
{
    public SyscallReadDouble() {
        super(7, "ReadDouble");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        double doubleValue = 0.0;
        try {
            doubleValue = SystemIO.readDouble(this.getNumber());
        }
        catch (NumberFormatException e) {
            throw new ProcessingException(statement, "invalid double input (syscall " + this.getNumber() + ")", 8);
        }
        final long longValue = Double.doubleToRawLongBits(doubleValue);
        Coprocessor1.updateRegister(1, Binary.highOrderLongToInt(longValue));
        Coprocessor1.updateRegister(0, Binary.lowOrderLongToInt(longValue));
    }
}
