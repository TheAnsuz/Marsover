

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.util.SystemIO;

public class SyscallReadChar extends AbstractSyscall
{
    public SyscallReadChar() {
        super(12, "ReadChar");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        int value = 0;
        try {
            value = SystemIO.readChar(this.getNumber());
        }
        catch (IndexOutOfBoundsException e) {
            throw new ProcessingException(statement, "invalid char input (syscall " + this.getNumber() + ")", 8);
        }
        RegisterFile.updateRegister(2, value);
    }
}
