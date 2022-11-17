

package mars.mips.instructions.syscalls;

import java.util.Random;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;

public class SyscallRandInt extends AbstractSyscall
{
    public SyscallRandInt() {
        super(41, "RandInt");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = RegisterFile.getValue(4);
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random();
            RandomStreams.randomStreams.put(index, stream);
        }
        RegisterFile.updateRegister(4, stream.nextInt());
    }
}
