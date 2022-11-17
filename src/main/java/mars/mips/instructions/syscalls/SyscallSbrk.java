

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.Globals;
import mars.ProgramStatement;

public class SyscallSbrk extends AbstractSyscall
{
    public SyscallSbrk() {
        super(9, "Sbrk");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        int address = 0;
        try {
            address = Globals.memory.allocateBytesFromHeap(RegisterFile.getValue(4));
        }
        catch (IllegalArgumentException iae) {
            throw new ProcessingException(statement, iae.getMessage() + " (syscall " + this.getNumber() + ")", 8);
        }
        RegisterFile.updateRegister(2, address);
    }
}
