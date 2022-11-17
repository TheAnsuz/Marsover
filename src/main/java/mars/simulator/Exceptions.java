

package mars.simulator;

import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;

public class Exceptions
{
    public static final int EXTERNAL_INTERRUPT_KEYBOARD = 64;
    public static final int EXTERNAL_INTERRUPT_DISPLAY = 128;
    public static final int ADDRESS_EXCEPTION_LOAD = 4;
    public static final int ADDRESS_EXCEPTION_STORE = 5;
    public static final int SYSCALL_EXCEPTION = 8;
    public static final int BREAKPOINT_EXCEPTION = 9;
    public static final int RESERVED_INSTRUCTION_EXCEPTION = 10;
    public static final int ARITHMETIC_OVERFLOW_EXCEPTION = 12;
    public static final int TRAP_EXCEPTION = 13;
    public static final int DIVIDE_BY_ZERO_EXCEPTION = 15;
    public static final int FLOATING_POINT_OVERFLOW = 16;
    public static final int FLOATING_POINT_UNDERFLOW = 17;
    
    public static void setRegisters(final int cause) {
        Coprocessor0.updateRegister(13, (Coprocessor0.getValue(13) & 0xFFFFFC83) | cause << 2);
        Coprocessor0.updateRegister(14, RegisterFile.getProgramCounter() - 4);
        Coprocessor0.updateRegister(12, Binary.setBit(Coprocessor0.getValue(12), 1));
    }
    
    public static void setRegisters(final int cause, final int addr) {
        Coprocessor0.updateRegister(8, addr);
        setRegisters(cause);
    }
}
