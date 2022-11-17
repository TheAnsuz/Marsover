

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.util.SystemIO;
import mars.mips.hardware.Coprocessor1;
import mars.ProgramStatement;

public class SyscallPrintFloat extends AbstractSyscall
{
    public SyscallPrintFloat() {
        super(2, "PrintFloat");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(new Float(Float.intBitsToFloat(Coprocessor1.getValue(12))).toString());
    }
}
