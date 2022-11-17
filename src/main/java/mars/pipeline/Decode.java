

package mars.pipeline;

import mars.Globals;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;

public class Decode
{
    private static final String[] registerNames;
    
    public static int getCode(final int instruction) {
        return instruction >>> 26;
    }
    
    public static boolean isLoad(final int instruction) {
        final int code = getCode(instruction);
        return (code >= 32 && code <= 38) || code == 48 || code == 49 || code == 53;
    }
    
    public static boolean isStore(final int instruction) {
        final int code = getCode(instruction);
        return (code >= 40 && code <= 43) || code == 46 || code == 56 || code == 57 || code == 61;
    }
    
    private static boolean hasSource1(final int instruction) {
        final BasicInstructionFormat type = Globals.instructionSet.findByBinaryCode(instruction).getInstructionFormat();
        return type != BasicInstructionFormat.J_FORMAT;
    }
    
    private static boolean hasSource2(final int instruction) {
        final BasicInstructionFormat type = Globals.instructionSet.findByBinaryCode(instruction).getInstructionFormat();
        if (type == BasicInstructionFormat.R_FORMAT) {
            return true;
        }
        if (type == BasicInstructionFormat.I_BRANCH_FORMAT) {
            final int code = getCode(instruction);
            return code == 4 || code == 5;
        }
        return type == BasicInstructionFormat.I_FORMAT && isStore(instruction);
    }
    
    public static int getDestination(final int instruction) {
        final BasicInstructionFormat type = Globals.instructionSet.findByBinaryCode(instruction).getInstructionFormat();
        final int code = getCode(instruction);
        if (type == BasicInstructionFormat.R_FORMAT) {
            final int func = getFunct(instruction);
            if (instruction == 0 || func == 12 || (code == 0 && func == 8)) {
                return 0;
            }
            if (code == 0 && func >= 24 && func <= 27) {
                return 0;
            }
            return getRd(instruction);
        }
        else {
            if (type == BasicInstructionFormat.I_FORMAT && !isStore(instruction)) {
                return getRt(instruction);
            }
            return 0;
        }
    }
    
    public static boolean isBranch(final int instruction) {
        final int code = getCode(instruction);
        return (code >= 4 && code <= 7) || code == 1;
    }
    
    public static int getRs(final int instruction) {
        final BasicInstructionFormat type = getFormat(instruction);
        final int code = getCode(instruction);
        if (type == BasicInstructionFormat.R_FORMAT) {
            final int func = getFunct(instruction);
            if (instruction == 0 || func == 12) {
                return 0;
            }
            if (code == 0) {
                if (func == 16) {
                    return 32;
                }
                if (func == 18) {
                    return 33;
                }
                if (func == 0 || func == 2 || func == 3) {
                    return 0;
                }
            }
        }
        else {
            if (type == BasicInstructionFormat.J_FORMAT) {
                return 0;
            }
            if (type == BasicInstructionFormat.I_FORMAT && code == 15) {
                return 0;
            }
        }
        return instruction >>> 21 & 0x1F;
    }
    
    public static int getRt(final int instruction) {
        final BasicInstructionFormat type = getFormat(instruction);
        final int code = getCode(instruction);
        if (type == BasicInstructionFormat.R_FORMAT) {
            final int func = getFunct(instruction);
            if (instruction == 0 || func == 12) {
                return 0;
            }
            if (code == 0 && (func == 16 || func == 18 || func == 8)) {
                return 0;
            }
        }
        else {
            if (type == BasicInstructionFormat.J_FORMAT) {
                return 0;
            }
            if (type == BasicInstructionFormat.I_BRANCH_FORMAT && code != 4 && code != 5) {
                return 0;
            }
        }
        return instruction >>> 16 & 0x1F;
    }
    
    private static int getRd(final int instruction) {
        return instruction >>> 11 & 0x1F;
    }
    
    public static int getInm(final int instruction) {
        return instruction & 0xFFFF;
    }
    
    public static int getInm_ExtensionSigno(final int instruction) {
        int inm = instruction & 0xFFFF;
        if ((instruction & 0x8000) != 0x0) {
            inm |= 0xFFFF0000;
        }
        return inm;
    }
    
    public static int getPseudoDir(final int instruction) {
        return instruction & 0xFFFFFFF;
    }
    
    public static int getFunct(final int instruction) {
        return instruction & 0x3F;
    }
    
    public static boolean hasShamt(final int instruction) {
        if (getFormat(instruction) == BasicInstructionFormat.R_FORMAT && getCode(instruction) == 0) {
            final int func = getFunct(instruction);
            return func == 0 || func == 2 || func == 3;
        }
        return false;
    }
    
    public static int getShamt(final int instruction) {
        return instruction >>> 6 & 0x1F;
    }
    
    public static boolean isMult(final int instruction) {
        final int code = getCode(instruction);
        final int func = getFunct(instruction);
        if (code == 0) {
            return func == 24 || func == 25;
        }
        return code == 28 && (func <= 3 || func == 4 || func == 5);
    }
    
    public static boolean isLowHi(final int instruction) {
        final int func = getFunct(instruction);
        return getCode(instruction) == 0 && (func == 16 || func == 18);
    }
    
    public static boolean isDiv(final int instruction) {
        final int code = getCode(instruction);
        final int func = getFunct(instruction);
        return code == 0 && (func == 26 || func == 27);
    }
    
    public static String toString(final int instruction) {
        final BasicInstruction ins = Globals.instructionSet.findByBinaryCode(instruction);
        String str = ins.getName();
        final BasicInstructionFormat type = ins.getInstructionFormat();
        final int code = getCode(instruction);
        if (type == BasicInstructionFormat.R_FORMAT) {
            final int func = getFunct(instruction);
            if (instruction != 0 && func != 12) {
                if (code == 0) {
                    if (func >= 24 && func <= 27) {
                        str = str + " " + Decode.registerNames[getRs(instruction)] + ", " + Decode.registerNames[getRt(instruction)];
                    }
                    else if (func >= 16 && func <= 19) {
                        str = str + " " + Decode.registerNames[getRd(instruction)];
                    }
                    else if (func == 0 || func == 2 || func == 3) {
                        str = str + " " + Decode.registerNames[getRd(instruction)] + ", " + Decode.registerNames[getRt(instruction)] + ", " + getShamt(instruction);
                    }
                    else if (func == 8) {
                        str = str + " " + Decode.registerNames[getRs(instruction)];
                    }
                    else {
                        str = str + " " + Decode.registerNames[getRd(instruction)] + ", " + Decode.registerNames[getRs(instruction)] + ", " + Decode.registerNames[getRt(instruction)];
                    }
                }
                else {
                    str = str + " " + Decode.registerNames[getRd(instruction)] + ", " + Decode.registerNames[getRs(instruction)] + ", " + Decode.registerNames[getRt(instruction)];
                }
            }
        }
        else if (type == BasicInstructionFormat.J_FORMAT) {
            str = str + " Ox" + Integer.toHexString(getPseudoDir(instruction));
        }
        else if (type == BasicInstructionFormat.I_BRANCH_FORMAT) {
            final String inm = Integer.toHexString(getInm(instruction));
            if (code == 4 || code == 5) {
                str = str + " " + Decode.registerNames[getRs(instruction)] + ", " + Decode.registerNames[getRt(instruction)] + ", 0x" + inm;
            }
            else {
                str = str + " " + Decode.registerNames[getRs(instruction)] + ", 0x" + inm;
            }
        }
        else {
            final int inm2 = getInm_ExtensionSigno(instruction);
            if (isLoad(instruction) || isStore(instruction)) {
                str = str + " " + Decode.registerNames[getRt(instruction)] + ", " + inm2 + "(" + Decode.registerNames[getRs(instruction)] + ")";
            }
            else if (code != 15) {
                str = str + " " + Decode.registerNames[getRt(instruction)] + ", " + Decode.registerNames[getRs(instruction)] + ", " + inm2;
            }
            else {
                str = str + " " + Decode.registerNames[getRt(instruction)] + ", 0x" + Integer.toHexString(inm2);
            }
        }
        while (str.length() < 24) {
            str += " ";
        }
        return str;
    }
    
    public static String getRegisterName(final int register) {
        return Decode.registerNames[register];
    }
    
    public static BasicInstructionFormat getFormat(final int instruction) {
        final BasicInstruction ins = Globals.instructionSet.findByBinaryCode(instruction);
        return ins.getInstructionFormat();
    }
    
    public static String normaliza(String str, final int lenght) {
        while (str.length() < lenght) {
            str += " ";
        }
        return str;
    }
    
    static {
        registerNames = new String[] { "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra", "$hi", "$low" };
    }
}
