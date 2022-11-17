

package mars.mips.instructions.syscalls;

import mars.mips.hardware.InvalidRegisterAccessException;
import java.awt.Component;
import javax.swing.JOptionPane;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.AddressErrorException;
import mars.ProcessingException;
import mars.Globals;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallMessageDialogDouble extends AbstractSyscall
{
    public SyscallMessageDialogDouble() {
        super(58, "MessageDialogDouble");
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
        try {
            JOptionPane.showMessageDialog(null, message + Double.toString(Coprocessor1.getDoubleFromRegisterPair("$f12")), null, 1);
        }
        catch (InvalidRegisterAccessException e2) {
            RegisterFile.updateRegister(5, -1);
            throw new ProcessingException(statement, "invalid int reg. access during double input (syscall " + this.getNumber() + ")", 8);
        }
    }
}
