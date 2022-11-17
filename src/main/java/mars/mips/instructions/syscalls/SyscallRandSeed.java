

package mars.mips.instructions.syscalls;

import java.util.Random;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;

public class SyscallRandSeed extends AbstractSyscall
{
    public SyscallRandSeed() {
        super(40, "RandSeed");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = RegisterFile.getValue(4);
        final Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            RandomStreams.randomStreams.put(index, new Random(RegisterFile.getValue(5)));
        }
        else {
            stream.setSeed(RegisterFile.getValue(5));
        }
    }
}
