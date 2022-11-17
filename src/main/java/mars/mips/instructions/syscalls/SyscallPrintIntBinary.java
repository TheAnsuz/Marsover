

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;
import mars.util.SystemIO;

public class SyscallPrintIntBinary extends AbstractSyscall
{
    public SyscallPrintIntBinary() {
        super(35, "PrintIntBinary");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(Binary.intToBinaryString(RegisterFile.getValue(4)));
    }
}
