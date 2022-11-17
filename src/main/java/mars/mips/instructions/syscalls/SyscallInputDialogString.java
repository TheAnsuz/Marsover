

package mars.mips.instructions.syscalls;

import javax.swing.JOptionPane;
import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class SyscallInputDialogString extends AbstractSyscall
{
    public SyscallInputDialogString() {
        super(54, "InputDialogString");
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
        String inputString = null;
        inputString = JOptionPane.showInputDialog(message);
        byteAddress = RegisterFile.getValue(5);
        final int maxLength = RegisterFile.getValue(6);
        try {
            if (inputString == null) {
                RegisterFile.updateRegister(5, -2);
            }
            else if (inputString.length() == 0) {
                RegisterFile.updateRegister(5, -3);
            }
            else {
                for (int index = 0; index < inputString.length() && index < maxLength - 1; ++index) {
                    Globals.memory.setByte(byteAddress + index, inputString.charAt(index));
                }
                if (inputString.length() < maxLength - 1) {
                    Globals.memory.setByte(byteAddress + Math.min(inputString.length(), maxLength - 2), 10);
                }
                Globals.memory.setByte(byteAddress + Math.min(inputString.length() + 1, maxLength - 1), 0);
                if (inputString.length() > maxLength - 1) {
                    RegisterFile.updateRegister(5, -4);
                }
                else {
                    RegisterFile.updateRegister(5, 0);
                }
            }
        }
        catch (AddressErrorException e2) {
            throw new ProcessingException(statement, e2);
        }
    }
}
