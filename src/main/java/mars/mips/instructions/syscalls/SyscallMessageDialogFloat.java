

package mars.mips.instructions.syscalls;

import javax.swing.JOptionPane;
import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;

public class SyscallMessageDialogFloat extends AbstractSyscall
{
    public SyscallMessageDialogFloat() {
        super(57, "MessageDialogFloat");
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
        JOptionPane.showMessageDialog(null, message + Float.toString(Coprocessor1.getFloatFromRegister("$f12")), null, 1);
    }
}
