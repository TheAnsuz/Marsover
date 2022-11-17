

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import java.util.Random;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallRandSeed extends AbstractSyscall
{
    public SyscallRandSeed() {
        super(40, "RandSeed");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = new Integer(RegisterFile.getValue(4));
        final Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            RandomStreams.randomStreams.put(index, new Random(RegisterFile.getValue(5)));
        }
        else {
            stream.setSeed(RegisterFile.getValue(5));
        }
    }
}
