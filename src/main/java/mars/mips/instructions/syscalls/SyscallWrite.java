

package mars.mips.instructions.syscalls;

import mars.util.SystemIO;
import mars.mips.hardware.AddressErrorException;
import mars.ProcessingException;
import mars.Globals;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallWrite extends AbstractSyscall
{
    public SyscallWrite() {
        super(15, "Write");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        int byteAddress = RegisterFile.getValue(5);
        byte b = 0;
        final int reqLength = RegisterFile.getValue(6);
        int index = 0;
        final byte[] myBuffer = new byte[RegisterFile.getValue(6) + 1];
        try {
            for (b = (byte)Globals.memory.getByte(byteAddress); index < reqLength; myBuffer[index++] = b, ++byteAddress, b = (byte)Globals.memory.getByte(byteAddress)) {}
            myBuffer[index] = 0;
        }
        catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
        final int retValue = SystemIO.writeToFile(RegisterFile.getValue(4), myBuffer, RegisterFile.getValue(6));
        RegisterFile.updateRegister(2, retValue);
    }
}
