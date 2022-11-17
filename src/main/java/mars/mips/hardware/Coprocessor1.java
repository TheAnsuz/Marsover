

package mars.mips.hardware;

import java.util.Observer;
import mars.Globals;
import mars.util.Binary;

public class Coprocessor1
{
    private static Register[] registers;
    private static Register condition;
    private static int numConditionFlags;
    
    public static void showRegisters() {
        for (int i = 0; i < Coprocessor1.registers.length; ++i) {
            System.out.println("Name: " + Coprocessor1.registers[i].getName());
            System.out.println("Number: " + Coprocessor1.registers[i].getNumber());
            System.out.println("Value: " + Coprocessor1.registers[i].getValue());
            System.out.println("");
        }
    }
    
    public static void setRegisterToFloat(final String reg, final float val) {
        setRegisterToFloat(getRegisterNumber(reg), val);
    }
    
    public static void setRegisterToFloat(final int reg, final float val) {
        if (reg >= 0 && reg < Coprocessor1.registers.length) {
            Coprocessor1.registers[reg].setValue(Float.floatToRawIntBits(val));
        }
    }
    
    public static void setRegisterToInt(final String reg, final int val) {
        setRegisterToInt(getRegisterNumber(reg), val);
    }
    
    public static void setRegisterToInt(final int reg, final int val) {
        if (reg >= 0 && reg < Coprocessor1.registers.length) {
            Coprocessor1.registers[reg].setValue(val);
        }
    }
    
    public static void setRegisterPairToDouble(final int reg, final double val) throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        final long bits = Double.doubleToRawLongBits(val);
        Coprocessor1.registers[reg + 1].setValue(Binary.highOrderLongToInt(bits));
        Coprocessor1.registers[reg].setValue(Binary.lowOrderLongToInt(bits));
    }
    
    public static void setRegisterPairToDouble(final String reg, final double val) throws InvalidRegisterAccessException {
        setRegisterPairToDouble(getRegisterNumber(reg), val);
    }
    
    public static void setRegisterPairToLong(final int reg, final long val) throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        Coprocessor1.registers[reg + 1].setValue(Binary.highOrderLongToInt(val));
        Coprocessor1.registers[reg].setValue(Binary.lowOrderLongToInt(val));
    }
    
    public static void setRegisterPairToLong(final String reg, final long val) throws InvalidRegisterAccessException {
        setRegisterPairToLong(getRegisterNumber(reg), val);
    }
    
    public static float getFloatFromRegister(final int reg) {
        float result = 0.0f;
        if (reg >= 0 && reg < Coprocessor1.registers.length) {
            result = Float.intBitsToFloat(Coprocessor1.registers[reg].getValue());
        }
        return result;
    }
    
    public static float getFloatFromRegister(final String reg) {
        return getFloatFromRegister(getRegisterNumber(reg));
    }
    
    public static int getIntFromRegister(final int reg) {
        int result = 0;
        if (reg >= 0 && reg < Coprocessor1.registers.length) {
            result = Coprocessor1.registers[reg].getValue();
        }
        return result;
    }
    
    public static int getIntFromRegister(final String reg) {
        return getIntFromRegister(getRegisterNumber(reg));
    }
    
    public static double getDoubleFromRegisterPair(final int reg) throws InvalidRegisterAccessException {
        final double result = 0.0;
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        final long bits = Binary.twoIntsToLong(Coprocessor1.registers[reg + 1].getValue(), Coprocessor1.registers[reg].getValue());
        return Double.longBitsToDouble(bits);
    }
    
    public static double getDoubleFromRegisterPair(final String reg) throws InvalidRegisterAccessException {
        return getDoubleFromRegisterPair(getRegisterNumber(reg));
    }
    
    public static long getLongFromRegisterPair(final int reg) throws InvalidRegisterAccessException {
        final double result = 0.0;
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        return Binary.twoIntsToLong(Coprocessor1.registers[reg + 1].getValue(), Coprocessor1.registers[reg].getValue());
    }
    
    public static long getLongFromRegisterPair(final String reg) throws InvalidRegisterAccessException {
        return getLongFromRegisterPair(getRegisterNumber(reg));
    }
    
    public static int updateRegister(final int num, final int val) {
        int old = 0;
        for (int i = 0; i < Coprocessor1.registers.length; ++i) {
            if (Coprocessor1.registers[i].getNumber() == num) {
                old = (Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addCoprocessor1Restore(num, Coprocessor1.registers[i].setValue(val)) : Coprocessor1.registers[i].setValue(val));
                break;
            }
        }
        return old;
    }
    
    public static int getValue(final int num) {
        return Coprocessor1.registers[num].getValue();
    }
    
    public static int getRegisterNumber(final String n) {
        int j = -1;
        for (int i = 0; i < Coprocessor1.registers.length; ++i) {
            if (Coprocessor1.registers[i].getName().equals(n)) {
                j = Coprocessor1.registers[i].getNumber();
                break;
            }
        }
        return j;
    }
    
    public static Register[] getRegisters() {
        return Coprocessor1.registers;
    }
    
    public static Register getRegister(final String rName) {
        Register reg = null;
        if (rName.charAt(0) == '$' && rName.length() > 1 && rName.charAt(1) == 'f') {
            try {
                reg = Coprocessor1.registers[Binary.stringToInt(rName.substring(2))];
            }
            catch (Exception e) {
                reg = null;
            }
        }
        return reg;
    }
    
    public static void resetRegisters() {
        for (int i = 0; i < Coprocessor1.registers.length; ++i) {
            Coprocessor1.registers[i].resetValue();
        }
        clearConditionFlags();
    }
    
    public static void addRegistersObserver(final Observer observer) {
        for (int i = 0; i < Coprocessor1.registers.length; ++i) {
            Coprocessor1.registers[i].addObserver(observer);
        }
    }
    
    public static void deleteRegistersObserver(final Observer observer) {
        for (int i = 0; i < Coprocessor1.registers.length; ++i) {
            Coprocessor1.registers[i].deleteObserver(observer);
        }
    }
    
    public static int setConditionFlag(final int flag) {
        int old = 0;
        if (flag >= 0 && flag < Coprocessor1.numConditionFlags) {
            old = getConditionFlag(flag);
            Coprocessor1.condition.setValue(Binary.setBit(Coprocessor1.condition.getValue(), flag));
            if (Globals.getSettings().getBackSteppingEnabled()) {
                if (old == 0) {
                    Globals.program.getBackStepper().addConditionFlagClear(flag);
                }
                else {
                    Globals.program.getBackStepper().addConditionFlagSet(flag);
                }
            }
        }
        return old;
    }
    
    public static int clearConditionFlag(final int flag) {
        int old = 0;
        if (flag >= 0 && flag < Coprocessor1.numConditionFlags) {
            old = getConditionFlag(flag);
            Coprocessor1.condition.setValue(Binary.clearBit(Coprocessor1.condition.getValue(), flag));
            if (Globals.getSettings().getBackSteppingEnabled()) {
                if (old == 0) {
                    Globals.program.getBackStepper().addConditionFlagClear(flag);
                }
                else {
                    Globals.program.getBackStepper().addConditionFlagSet(flag);
                }
            }
        }
        return old;
    }
    
    public static int getConditionFlag(int flag) {
        if (flag < 0 || flag >= Coprocessor1.numConditionFlags) {
            flag = 0;
        }
        return Binary.bitValue(Coprocessor1.condition.getValue(), flag);
    }
    
    public static int getConditionFlags() {
        return Coprocessor1.condition.getValue();
    }
    
    public static void clearConditionFlags() {
        Coprocessor1.condition.setValue(0);
    }
    
    public static void setConditionFlags() {
        Coprocessor1.condition.setValue(-1);
    }
    
    public static int getConditionFlagCount() {
        return Coprocessor1.numConditionFlags;
    }
    
    static {
        Coprocessor1.registers = new Register[] { new Register("$f0", 0, 0), new Register("$f1", 1, 0), new Register("$f2", 2, 0), new Register("$f3", 3, 0), new Register("$f4", 4, 0), new Register("$f5", 5, 0), new Register("$f6", 6, 0), new Register("$f7", 7, 0), new Register("$f8", 8, 0), new Register("$f9", 9, 0), new Register("$f10", 10, 0), new Register("$f11", 11, 0), new Register("$f12", 12, 0), new Register("$f13", 13, 0), new Register("$f14", 14, 0), new Register("$f15", 15, 0), new Register("$f16", 16, 0), new Register("$f17", 17, 0), new Register("$f18", 18, 0), new Register("$f19", 19, 0), new Register("$f20", 20, 0), new Register("$f21", 21, 0), new Register("$f22", 22, 0), new Register("$f23", 23, 0), new Register("$f24", 24, 0), new Register("$f25", 25, 0), new Register("$f26", 26, 0), new Register("$f27", 27, 0), new Register("$f28", 28, 0), new Register("$f29", 29, 0), new Register("$f30", 30, 0), new Register("$f31", 31, 0) };
        Coprocessor1.condition = new Register("cf", 32, 0);
        Coprocessor1.numConditionFlags = 8;
    }
}
