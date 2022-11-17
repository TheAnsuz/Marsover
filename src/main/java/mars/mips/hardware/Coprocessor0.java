

package mars.mips.hardware;

import java.util.Observer;
import mars.Globals;

public class Coprocessor0
{
    public static final int VADDR = 8;
    public static final int STATUS = 12;
    public static final int CAUSE = 13;
    public static final int EPC = 14;
    public static final int EXCEPTION_LEVEL = 1;
    public static final int DEFAULT_STATUS_VALUE = 65297;
    private static Register[] registers;
    
    public static void showRegisters() {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            System.out.println("Name: " + Coprocessor0.registers[i].getName());
            System.out.println("Number: " + Coprocessor0.registers[i].getNumber());
            System.out.println("Value: " + Coprocessor0.registers[i].getValue());
            System.out.println("");
        }
    }
    
    public static int updateRegister(final String n, final int val) {
        int oldValue = 0;
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            if (("$" + Coprocessor0.registers[i].getNumber()).equals(n) || Coprocessor0.registers[i].getName().equals(n)) {
                oldValue = Coprocessor0.registers[i].getValue();
                Coprocessor0.registers[i].setValue(val);
                break;
            }
        }
        return oldValue;
    }
    
    public static int updateRegister(final int num, final int val) {
        int old = 0;
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            if (Coprocessor0.registers[i].getNumber() == num) {
                old = (Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addCoprocessor0Restore(num, Coprocessor0.registers[i].setValue(val)) : Coprocessor0.registers[i].setValue(val));
                break;
            }
        }
        return old;
    }
    
    public static int getValue(final int num) {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            if (Coprocessor0.registers[i].getNumber() == num) {
                return Coprocessor0.registers[i].getValue();
            }
        }
        return 0;
    }
    
    public static int getNumber(final String n) {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            if (("$" + Coprocessor0.registers[i].getNumber()).equals(n) || Coprocessor0.registers[i].getName().equals(n)) {
                return Coprocessor0.registers[i].getNumber();
            }
        }
        return -1;
    }
    
    public static Register[] getRegisters() {
        return Coprocessor0.registers;
    }
    
    public static int getRegisterPosition(final Register r) {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            if (Coprocessor0.registers[i] == r) {
                return i;
            }
        }
        return -1;
    }
    
    public static Register getRegister(final String rname) {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            if (("$" + Coprocessor0.registers[i].getNumber()).equals(rname) || Coprocessor0.registers[i].getName().equals(rname)) {
                return Coprocessor0.registers[i];
            }
        }
        return null;
    }
    
    public static void resetRegisters() {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            Coprocessor0.registers[i].resetValue();
        }
    }
    
    public static void addRegistersObserver(final Observer observer) {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            Coprocessor0.registers[i].addObserver(observer);
        }
    }
    
    public static void deleteRegistersObserver(final Observer observer) {
        for (int i = 0; i < Coprocessor0.registers.length; ++i) {
            Coprocessor0.registers[i].deleteObserver(observer);
        }
    }
    
    static {
        Coprocessor0.registers = new Register[] { new Register("$8 (vaddr)", 8, 0), new Register("$12 (status)", 12, 65297), new Register("$13 (cause)", 13, 0), new Register("$14 (epc)", 14, 0) };
    }
}
