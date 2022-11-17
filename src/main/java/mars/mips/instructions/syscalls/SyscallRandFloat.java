

package mars.mips.instructions.syscalls;

import java.util.Random;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;

public class SyscallRandFloat extends AbstractSyscall
{
    public SyscallRandFloat() {
        super(43, "RandFloat");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = RegisterFile.getValue(4);
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random();
            RandomStreams.randomStreams.put(index, stream);
        }
        Coprocessor1.setRegisterToFloat(0, stream.nextFloat());
    }
}
