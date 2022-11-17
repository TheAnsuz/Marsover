

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.util.SystemIO;
import mars.util.Binary;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallPrintIntUnsigned extends AbstractSyscall
{
    public SyscallPrintIntUnsigned() {
        super(36, "PrintIntUnsigned");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(Binary.unsignedIntToIntString(RegisterFile.getValue(4)));
    }
}
