

package mars.mips.instructions.syscalls;

import mars.mips.hardware.AddressErrorException;
import mars.ProcessingException;
import mars.Globals;
import mars.util.SystemIO;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallReadString extends AbstractSyscall
{
    public SyscallReadString() {
        super(8, "ReadString");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        String inputString = "";
        final int buf = RegisterFile.getValue(4);
        int maxLength = RegisterFile.getValue(5) - 1;
        boolean addNullByte = true;
        if (maxLength < 0) {
            maxLength = 0;
            addNullByte = false;
        }
        inputString = SystemIO.readString(this.getNumber(), maxLength);
        int stringLength = Math.min(maxLength, inputString.length());
        try {
            for (int index = 0; index < stringLength; ++index) {
                Globals.memory.setByte(buf + index, inputString.charAt(index));
            }
            if (stringLength < maxLength) {
                Globals.memory.setByte(buf + stringLength, 10);
                ++stringLength;
            }
            if (addNullByte) {
                Globals.memory.setByte(buf + stringLength, 0);
            }
        }
        catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
    }
}
