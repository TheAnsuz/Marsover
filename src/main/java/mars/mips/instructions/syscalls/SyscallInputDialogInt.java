

package mars.mips.instructions.syscalls;

import javax.swing.JOptionPane;
import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class SyscallInputDialogInt extends AbstractSyscall
{
    public SyscallInputDialogInt() {
        super(51, "InputDialogInt");
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
        String inputValue = null;
        inputValue = JOptionPane.showInputDialog(message);
        if (inputValue == null) {
            RegisterFile.updateRegister(4, 0);
            RegisterFile.updateRegister(5, -2);
        }
        else if (inputValue.length() == 0) {
            RegisterFile.updateRegister(4, 0);
            RegisterFile.updateRegister(5, -3);
        }
        else {
            try {
                final int i = Integer.parseInt(inputValue);
                RegisterFile.updateRegister(4, i);
                RegisterFile.updateRegister(5, 0);
            }
            catch (NumberFormatException e2) {
                RegisterFile.updateRegister(4, 0);
                RegisterFile.updateRegister(5, -1);
            }
        }
    }
}
