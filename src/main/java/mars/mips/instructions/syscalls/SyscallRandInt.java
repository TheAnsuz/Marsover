

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import java.util.Random;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallRandInt extends AbstractSyscall
{
    public SyscallRandInt() {
        super(41, "RandInt");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = new Integer(RegisterFile.getValue(4));
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random();
            RandomStreams.randomStreams.put(index, stream);
        }
        RegisterFile.updateRegister(4, stream.nextInt());
    }
}
