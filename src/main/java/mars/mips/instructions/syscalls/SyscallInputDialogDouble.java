

package mars.mips.instructions.syscalls;

import mars.mips.hardware.InvalidRegisterAccessException;
import mars.mips.hardware.Coprocessor1;
import javax.swing.JOptionPane;
import mars.mips.hardware.AddressErrorException;
import mars.ProcessingException;
import mars.Globals;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallInputDialogDouble extends AbstractSyscall
{
    public SyscallInputDialogDouble() {
        super(53, "InputDialogDouble");
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
        try {
            Coprocessor1.setRegisterPairToDouble(0, 0.0);
            if (inputValue == null) {
                RegisterFile.updateRegister(5, -2);
            }
            else if (inputValue.length() == 0) {
                RegisterFile.updateRegister(5, -3);
            }
            else {
                final double doubleValue = Double.parseDouble(inputValue);
                Coprocessor1.setRegisterPairToDouble(0, doubleValue);
                RegisterFile.updateRegister(5, 0);
            }
        }
        catch (InvalidRegisterAccessException e2) {
            RegisterFile.updateRegister(5, -1);
            throw new ProcessingException(statement, "invalid int reg. access during double input (syscall " + this.getNumber() + ")", 8);
        }
        catch (NumberFormatException e3) {
            RegisterFile.updateRegister(5, -1);
        }
    }
}
