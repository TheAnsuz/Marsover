

package mars.mips.instructions.syscalls;

import mars.mips.hardware.AddressErrorException;
import mars.ProcessingException;
import mars.util.SystemIO;
import mars.Globals;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallPrintString extends AbstractSyscall
{
    public SyscallPrintString() {
        super(4, "PrintString");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        int byteAddress = RegisterFile.getValue(4);
        char ch = '\0';
        try {
            for (ch = (char)Globals.memory.getByte(byteAddress); ch != '\0'; ch = (char)Globals.memory.getByte(byteAddress)) {
                SystemIO.printString(new Character(ch).toString());
                ++byteAddress;
            }
        }
        catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
    }
}
