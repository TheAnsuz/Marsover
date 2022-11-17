

package mars.mips.instructions.syscalls;

import mars.mips.hardware.InvalidRegisterAccessException;
import mars.ProcessingException;
import mars.mips.hardware.Coprocessor1;
import java.util.Random;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallRandDouble extends AbstractSyscall
{
    public SyscallRandDouble() {
        super(44, "RandDouble");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        final Integer index = new Integer(RegisterFile.getValue(4));
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random();
            RandomStreams.randomStreams.put(index, stream);
        }
        try {
            Coprocessor1.setRegisterPairToDouble(0, stream.nextDouble());
        }
        catch (InvalidRegisterAccessException e) {
            throw new ProcessingException(statement, "Internal error storing double to register (syscall " + this.getNumber() + ")", 8);
        }
    }
}
