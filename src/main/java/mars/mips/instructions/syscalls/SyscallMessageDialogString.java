

package mars.mips.instructions.syscalls;

import javax.swing.JOptionPane;
import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class SyscallMessageDialogString extends AbstractSyscall
{
    public SyscallMessageDialogString() {
        super(59, "MessageDialogString");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        String message = new String();
        int byteAddress = RegisterFile.getValue(4);
        final char[] ch = { ' ' };
        try {
            ch[0] = (char)Globals.memory.getByte(byteAddress);
            while (ch[0] != '\0') {
                message = message.concat(new String(ch));
                ++byteAddress;
                ch[0] = (char)Globals.memory.getByte(byteAddress);
            }
        }
        catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
        String message2 = new String();
        byteAddress = RegisterFile.getValue(5);
        try {
            ch[0] = (char)Globals.memory.getByte(byteAddress);
            while (ch[0] != '\0') {
                message2 = message2.concat(new String(ch));
                ++byteAddress;
                ch[0] = (char)Globals.memory.getByte(byteAddress);
            }
        }
        catch (AddressErrorException e2) {
            throw new ProcessingException(statement, e2);
        }
        JOptionPane.showMessageDialog(null, message + message2, null, 1);
    }
}
