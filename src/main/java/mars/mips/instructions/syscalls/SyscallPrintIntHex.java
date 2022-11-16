package mars.mips.instructions.syscalls;

import mars.*;
import mars.mips.hardware.*;
import mars.util.*;

/**
 * Service to display integer stored in $a0 on the console.
 *
 */
public class SyscallPrintIntHex extends AbstractSyscall {

    /**
     * Build an instance of the Print Integer syscall. Default service number is
     * 1 and name is "PrintInt".
     */
    public SyscallPrintIntHex() {
        super(34, "PrintIntHex");
    }

    /**
     * Performs syscall function to print on the console the integer stored in
     * $a0, in hexadecimal format.
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        SystemIO.printString(Binary.intToHexString(RegisterFile.getValue(4)));
    }
}
