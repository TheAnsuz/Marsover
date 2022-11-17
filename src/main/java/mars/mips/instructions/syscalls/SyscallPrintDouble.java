

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.util.SystemIO;
import mars.util.Binary;
import mars.mips.hardware.Coprocessor1;
import mars.ProgramStatement;

public class SyscallPrintDouble extends AbstractSyscall
{
    public SyscallPrintDouble() {
        super(3, "PrintDouble");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(new Double(Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(13), Coprocessor1.getValue(12)))).toString());
    }
}
