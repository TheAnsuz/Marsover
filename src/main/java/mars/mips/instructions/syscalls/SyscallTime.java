package mars.mips.instructions.syscalls;

import mars.*;
import mars.mips.hardware.*;
import mars.util.*;

/**
 * Service to read a character from input console into $a0.
 *
 */
public class SyscallTime extends AbstractSyscall {

    /**
     * Build an instance of the Read Char syscall. Default service number is 12
     * and name is "ReadChar".
     */
    public SyscallTime() {
        super(30, "Time");
    }

    /**
     * Performs syscall function to place current system time into $a0 (low
     * order 32 bits) and $a1 (high order 32 bits).
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        long value = new java.util.Date().getTime();
        RegisterFile.updateRegister(4, Binary.lowOrderLongToInt(value)); // $a0 
        RegisterFile.updateRegister(5, Binary.highOrderLongToInt(value)); // $a1
    }

}
