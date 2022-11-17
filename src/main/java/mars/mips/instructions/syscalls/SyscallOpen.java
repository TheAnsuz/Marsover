

package mars.mips.instructions.syscalls;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.util.SystemIO;

public class SyscallOpen extends AbstractSyscall
{
    public SyscallOpen() {
        super(13, "Open");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        String filename = new String();
        int byteAddress = RegisterFile.getValue(4);
        final char[] ch = { ' ' };
        try {
            ch[0] = (char)Globals.memory.getByte(byteAddress);
            while (ch[0] != '\0') {
                filename = filename.concat(new String(ch));
                ++byteAddress;
                ch[0] = (char)Globals.memory.getByte(byteAddress);
            }
        }
        catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
        final int retValue = SystemIO.openFile(filename, RegisterFile.getValue(5));
        RegisterFile.updateRegister(2, retValue);
    }
}
