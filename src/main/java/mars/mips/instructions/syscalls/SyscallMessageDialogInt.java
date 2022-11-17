

package mars.mips.instructions.syscalls;

import java.awt.Component;
import javax.swing.JOptionPane;
import mars.mips.hardware.AddressErrorException;
import mars.ProcessingException;
import mars.Globals;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallMessageDialogInt extends AbstractSyscall
{
    public SyscallMessageDialogInt() {
        super(56, "MessageDialogInt");
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
        JOptionPane.showMessageDialog(null, message + Integer.toString(RegisterFile.getValue(5)), null, 1);
    }
}
