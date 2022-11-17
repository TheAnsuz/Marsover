

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.mips.hardware.Coprocessor1;
import java.util.Random;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallRandFloat extends AbstractSyscall
{
    public SyscallRandFloat() {
        super(43, "RandFloat");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = new Integer(RegisterFile.getValue(4));
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random();
            RandomStreams.randomStreams.put(index, stream);
        }
        Coprocessor1.setRegisterToFloat(0, stream.nextFloat());
    }
}
