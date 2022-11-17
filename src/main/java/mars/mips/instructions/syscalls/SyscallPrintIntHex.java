

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;
import mars.util.SystemIO;

public class SyscallPrintIntHex extends AbstractSyscall
{
    public SyscallPrintIntHex() {
        super(34, "PrintIntHex");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(Binary.intToHexString(RegisterFile.getValue(4)));
    }
}
