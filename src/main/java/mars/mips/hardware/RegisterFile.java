

package mars.mips.hardware;

import java.util.Observer;
import mars.Globals;
import mars.assembler.SymbolTable;
import mars.util.Binary;

public class RegisterFile
{
    public static final int GLOBAL_POINTER_REGISTER = 28;
    public static final int STACK_POINTER_REGISTER = 29;
    private static Register[] regFile;
    private static Register programCounter;
    private static Register hi;
    private static Register lo;
    
    public static void showRegisters() {
        for (int i = 0; i < RegisterFile.regFile.length; ++i) {
            System.out.println("Name: " + RegisterFile.regFile[i].getName());
            System.out.println("Number: " + RegisterFile.regFile[i].getNumber());
            System.out.println("Value: " + RegisterFile.regFile[i].getValue());
            System.out.println("");
        }
    }
    
    public static int updateRegister(final int num, final int val) {
        int old = 0;
        if (num != 0) {
            for (int i = 0; i < RegisterFile.regFile.length; ++i) {
                if (RegisterFile.regFile[i].getNumber() == num) {
                    old = (Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addRegisterFileRestore(num, RegisterFile.regFile[i].setValue(val)) : RegisterFile.regFile[i].setValue(val));
                    break;
                }
            }
        }
        if (num == 33) {
            old = (Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addRegisterFileRestore(num, RegisterFile.hi.setValue(val)) : RegisterFile.hi.setValue(val));
        }
        else if (num == 34) {
            old = (Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addRegisterFileRestore(num, RegisterFile.lo.setValue(val)) : RegisterFile.lo.setValue(val));
        }
        return old;
    }
    
    public static void updateRegister(final String reg, final int val) {
        if (!reg.equals("zero")) {
            for (int i = 0; i < RegisterFile.regFile.length; ++i) {
                if (RegisterFile.regFile[i].getName().equals(reg)) {
                    updateRegister(i, val);
                    break;
                }
            }
        }
    }
    
    public static int getValue(final int num) {
        if (num == 33) {
            return RegisterFile.hi.getValue();
        }
        if (num == 34) {
            return RegisterFile.lo.getValue();
        }
        return RegisterFile.regFile[num].getValue();
    }
    
    public static int getNumber(final String n) {
        int j = -1;
        for (int i = 0; i < RegisterFile.regFile.length; ++i) {
            if (RegisterFile.regFile[i].getName().equals(n)) {
                j = RegisterFile.regFile[i].getNumber();
                break;
            }
        }
        return j;
    }
    
    public static Register[] getRegisters() {
        return RegisterFile.regFile;
    }
    
    public static Register getUserRegister(final String Rname) {
        Register reg = null;
        if (Rname.charAt(0) == '$') {
            try {
                reg = RegisterFile.regFile[Binary.stringToInt(Rname.substring(1))];
            }
            catch (Exception e) {
                reg = null;
                Block_5: {
                    for (int i = 0; i < RegisterFile.regFile.length; ++i) {
                        if (Rname.equals(RegisterFile.regFile[i].getName())) {
                            break Block_5;
                        }
                    }
                    return reg;
                }
                int i = 0;
                reg = RegisterFile.regFile[i];
            }
        }
        return reg;
    }
    
    public static void initializeProgramCounter(final int value) {
        RegisterFile.programCounter.setValue(value);
    }
    
    public static void initializeProgramCounter(final boolean startAtMain) {
        final int mainAddr = Globals.symbolTable.getAddress(SymbolTable.getStartLabel());
        if (startAtMain && mainAddr != -1 && (Memory.inTextSegment(mainAddr) || Memory.inKernelTextSegment(mainAddr))) {
            initializeProgramCounter(mainAddr);
        }
        else {
            initializeProgramCounter(RegisterFile.programCounter.getResetValue());
        }
    }
    
    public static int setProgramCounter(final int value) {
        final int old = RegisterFile.programCounter.getValue();
        RegisterFile.programCounter.setValue(value);
        if (Globals.getSettings().getBackSteppingEnabled()) {
            Globals.program.getBackStepper().addPCRestore(old);
        }
        return old;
    }
    
    public static int getProgramCounter() {
        return RegisterFile.programCounter.getValue();
    }
    
    public static Register getProgramCounterRegister() {
        return RegisterFile.programCounter;
    }
    
    public static int getInitialProgramCounter() {
        return RegisterFile.programCounter.getResetValue();
    }
    
    public static void resetRegisters() {
        for (int i = 0; i < RegisterFile.regFile.length; ++i) {
            RegisterFile.regFile[i].resetValue();
        }
        initializeProgramCounter(Globals.getSettings().getStartAtMain());
        RegisterFile.hi.resetValue();
        RegisterFile.lo.resetValue();
    }
    
    public static void incrementPC() {
        RegisterFile.programCounter.setValue(RegisterFile.programCounter.getValue() + 4);
    }
    
    public static void addRegistersObserver(final Observer observer) {
        for (int i = 0; i < RegisterFile.regFile.length; ++i) {
            RegisterFile.regFile[i].addObserver(observer);
        }
        RegisterFile.hi.addObserver(observer);
        RegisterFile.lo.addObserver(observer);
    }
    
    public static void addRegistersObserverSinHi(final Observer observer) {
        for (int i = 0; i < RegisterFile.regFile.length; ++i) {
            RegisterFile.regFile[i].addObserver(observer);
        }
        RegisterFile.lo.addObserver(observer);
    }
    
    public static void deleteRegistersObserver(final Observer observer) {
        for (int i = 0; i < RegisterFile.regFile.length; ++i) {
            RegisterFile.regFile[i].deleteObserver(observer);
        }
        RegisterFile.hi.deleteObserver(observer);
        RegisterFile.lo.deleteObserver(observer);
    }
    
    static {
        RegisterFile.regFile = new Register[] { new Register("$zero", 0, 0), new Register("$at", 1, 0), new Register("$v0", 2, 0), new Register("$v1", 3, 0), new Register("$a0", 4, 0), new Register("$a1", 5, 0), new Register("$a2", 6, 0), new Register("$a3", 7, 0), new Register("$t0", 8, 0), new Register("$t1", 9, 0), new Register("$t2", 10, 0), new Register("$t3", 11, 0), new Register("$t4", 12, 0), new Register("$t5", 13, 0), new Register("$t6", 14, 0), new Register("$t7", 15, 0), new Register("$s0", 16, 0), new Register("$s1", 17, 0), new Register("$s2", 18, 0), new Register("$s3", 19, 0), new Register("$s4", 20, 0), new Register("$s5", 21, 0), new Register("$s6", 22, 0), new Register("$s7", 23, 0), new Register("$t8", 24, 0), new Register("$t9", 25, 0), new Register("$k0", 26, 0), new Register("$k1", 27, 0), new Register("$gp", 28, Memory.globalPointer), new Register("$sp", 29, Memory.stackPointer), new Register("$fp", 30, 0), new Register("$ra", 31, 0) };
        RegisterFile.programCounter = new Register("pc", 32, Memory.textBaseAddress);
        RegisterFile.hi = new Register("hi", 33, 0);
        RegisterFile.lo = new Register("lo", 34, 0);
    }
}
