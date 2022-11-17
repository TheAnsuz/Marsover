

package mars.mips.instructions.syscalls;

import mars.mips.hardware.RegisterFile;
import mars.ProcessingException;
import mars.util.SystemIO;
import mars.ProgramStatement;

public class SyscallReadInt extends AbstractSyscall
{
    public SyscallReadInt() {
        super(5, "ReadInt");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        int value = 0;
        try {
            value = SystemIO.readInteger(this.getNumber());
        }
        catch (NumberFormatException e) {
            throw new ProcessingException(statement, "invalid integer input (syscall " + this.getNumber() + ")", 8);
        }
        RegisterFile.updateRegister(2, value);
    }
}
