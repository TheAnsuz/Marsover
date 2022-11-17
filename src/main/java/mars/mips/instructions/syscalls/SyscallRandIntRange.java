

package mars.mips.instructions.syscalls;

import java.util.Random;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;

public class SyscallRandIntRange extends AbstractSyscall
{
    public SyscallRandIntRange() {
        super(42, "RandIntRange");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = RegisterFile.getValue(4);
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random();
            RandomStreams.randomStreams.put(index, stream);
        }
        try {
            RegisterFile.updateRegister(4, stream.nextInt(RegisterFile.getValue(5)));
        }
        catch (IllegalArgumentException iae) {
            throw new ProcessingException(statement, "Upper bound of range cannot be negative (syscall " + this.getNumber() + ")", 8);
        }
    }
}
