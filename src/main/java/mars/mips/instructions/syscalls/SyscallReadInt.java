package mars.mips.instructions.syscalls;

import mars.*;
import mars.mips.hardware.*;
import mars.simulator.*;
import mars.util.*;

/**
 * Service to read an integer from input console into $v0.
 *
 */
public class SyscallReadInt extends AbstractSyscall {

    /**
     * Build an instance of the Read Integer syscall. Default service number is
     * 5 and name is "ReadInt".
     */
    public SyscallReadInt() {
        super(5, "ReadInt");
    }

    /**
     * Performs syscall function to read an integer from input console into $v0
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        int value = 0;
        try {
            value = SystemIO.readInteger(this.getNumber());
        } catch (NumberFormatException e) {
            throw new ProcessingException(statement,
                    "invalid integer input (syscall " + this.getNumber() + ")",
                    Exceptions.SYSCALL_EXCEPTION);
        }
        RegisterFile.updateRegister(2, value);
    }

}
