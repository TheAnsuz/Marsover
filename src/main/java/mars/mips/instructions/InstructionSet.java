

package mars.mips.instructions;

import mars.simulator.DelayedBranch;
import mars.mips.instructions.syscalls.Syscall;
import java.io.InputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.Memory;
import mars.mips.hardware.AddressErrorException;
import mars.Globals;
import mars.util.Binary;
import mars.mips.hardware.RegisterFile;
import mars.ProcessingException;
import mars.ProgramStatement;
import java.util.ArrayList;

public class InstructionSet
{
    private ArrayList<Instruction> instructionList;
    private ArrayList opcodeMatchMaps;
    private SyscallLoader syscallLoader;
    
    public InstructionSet() {
        this.instructionList = new ArrayList();
    }
    
    public ArrayList getInstructionList() {
        return this.instructionList;
    }
    
    public void populate() {
        this.instructionList.add(new BasicInstruction("nop", "Null operation : machine code is all zeroes", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 00000 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
            }
        }));
        this.instructionList.add(new BasicInstruction("add $t1,$t2,$t3", "Addition with overflow : set $t1 to ($t2 plus $t3)", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int add1 = RegisterFile.getValue(operands[1]);
                final int add2 = RegisterFile.getValue(operands[2]);
                final int sum = add1 + add2;
                if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                    throw new ProcessingException(statement, "arithmetic overflow", 12);
                }
                RegisterFile.updateRegister(operands[0], sum);
            }
        }));
        this.instructionList.add(new BasicInstruction("sub $t1,$t2,$t3", "Subtraction with overflow : set $t1 to ($t2 minus $t3)", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int sub1 = RegisterFile.getValue(operands[1]);
                final int sub2 = RegisterFile.getValue(operands[2]);
                final int dif = sub1 - sub2;
                if ((sub1 >= 0 && sub2 < 0 && dif < 0) || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                    throw new ProcessingException(statement, "arithmetic overflow", 12);
                }
                RegisterFile.updateRegister(operands[0], dif);
            }
        }));
        this.instructionList.add(new BasicInstruction("addi $t1,$t2,-100", "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)", BasicInstructionFormat.I_FORMAT, "001000 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int add1 = RegisterFile.getValue(operands[1]);
                final int add2 = operands[2] << 16 >> 16;
                final int sum = add1 + add2;
                if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                    throw new ProcessingException(statement, "arithmetic overflow", 12);
                }
                RegisterFile.updateRegister(operands[0], sum);
            }
        }));
        this.instructionList.add(new BasicInstruction("addu $t1,$t2,$t3", "Addition unsigned without overflow : set $t1 to ($t2 plus $t3), no overflow", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) + RegisterFile.getValue(operands[2]));
            }
        }));
        this.instructionList.add(new BasicInstruction("subu $t1,$t2,$t3", "Subtraction unsigned without overflow : set $t1 to ($t2 minus $t3), no overflow", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) - RegisterFile.getValue(operands[2]));
            }
        }));
        this.instructionList.add(new BasicInstruction("addiu $t1,$t2,-100", "Addition immediate unsigned without overflow : set $t1 to ($t2 plus signed 16-bit immediate), no overflow", BasicInstructionFormat.I_FORMAT, "001001 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) + (operands[2] << 16 >> 16));
            }
        }));
        this.instructionList.add(new BasicInstruction("mult $t1,$t2", "Multiplication : Set hi to high-order 32 bits, lo to low-order 32 bits of the product of $t1 and $t2 (use mfhi to access hi, mflo to access lo)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = RegisterFile.getValue(operands[0]) * (long)RegisterFile.getValue(operands[1]);
                RegisterFile.updateRegister(33, (int)(product >> 32));
                RegisterFile.updateRegister(34, (int)(product << 32 >> 32));
            }
        }));
        this.instructionList.add(new BasicInstruction("multu $t1,$t2", "Multiplication unsigned : Set HI to high-order 32 bits, LO to low-order 32 bits of the product of unsigned $t1 and $t2 (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = ((long)RegisterFile.getValue(operands[0]) << 32 >>> 32) * ((long)RegisterFile.getValue(operands[1]) << 32 >>> 32);
                RegisterFile.updateRegister(33, (int)(product >> 32));
                RegisterFile.updateRegister(34, (int)(product << 32 >> 32));
            }
        }));
        this.instructionList.add(new BasicInstruction("mul $t1,$t2,$t3", "Multiplication without overflow  : Set HI to high-order 32 bits, LO and $t1 to low-order 32 bits of the product of $t2 and $t3 (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "011100 sssss ttttt fffff 00000 000010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = RegisterFile.getValue(operands[1]) * (long)RegisterFile.getValue(operands[2]);
                RegisterFile.updateRegister(operands[0], (int)(product << 32 >> 32));
                RegisterFile.updateRegister(33, (int)(product >> 32));
                RegisterFile.updateRegister(34, (int)(product << 32 >> 32));
            }
        }));
        this.instructionList.add(new BasicInstruction("madd $t1,$t2", "Multiply add : Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "011100 fffff sssss 00000 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = RegisterFile.getValue(operands[0]) * (long)RegisterFile.getValue(operands[1]);
                final long contentsHiLo = Binary.twoIntsToLong(RegisterFile.getValue(33), RegisterFile.getValue(34));
                final long sum = contentsHiLo + product;
                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(sum));
                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(sum));
            }
        }));
        this.instructionList.add(new BasicInstruction("maddu $t1,$t2", "Multiply add unsigned : Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product, unsigned (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "011100 fffff sssss 00000 00000 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = ((long)RegisterFile.getValue(operands[0]) << 32 >>> 32) * ((long)RegisterFile.getValue(operands[1]) << 32 >>> 32);
                final long contentsHiLo = Binary.twoIntsToLong(RegisterFile.getValue(33), RegisterFile.getValue(34));
                final long sum = contentsHiLo + product;
                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(sum));
                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(sum));
            }
        }));
        this.instructionList.add(new BasicInstruction("msub $t1,$t2", "Multiply subtract : Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decrement LO by low-order 32 bits of product (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "011100 fffff sssss 00000 00000 000100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = RegisterFile.getValue(operands[0]) * (long)RegisterFile.getValue(operands[1]);
                final long contentsHiLo = Binary.twoIntsToLong(RegisterFile.getValue(33), RegisterFile.getValue(34));
                final long diff = contentsHiLo - product;
                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(diff));
                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(diff));
            }
        }));
        this.instructionList.add(new BasicInstruction("msubu $t1,$t2", "Multiply subtract unsigned : Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decement LO by low-order 32 bits of product, unsigned (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "011100 fffff sssss 00000 00000 000101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final long product = ((long)RegisterFile.getValue(operands[0]) << 32 >>> 32) * ((long)RegisterFile.getValue(operands[1]) << 32 >>> 32);
                final long contentsHiLo = Binary.twoIntsToLong(RegisterFile.getValue(33), RegisterFile.getValue(34));
                final long diff = contentsHiLo - product;
                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(diff));
                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(diff));
            }
        }));
        this.instructionList.add(new BasicInstruction("div $t1,$t2", "Division with overflow : Divide $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[1]) == 0) {
                    return;
                }
                RegisterFile.updateRegister(33, RegisterFile.getValue(operands[0]) % RegisterFile.getValue(operands[1]));
                RegisterFile.updateRegister(34, RegisterFile.getValue(operands[0]) / RegisterFile.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("divu $t1,$t2", "Division unsigned without overflow : Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[1]) == 0) {
                    return;
                }
                final long oper1 = (long)RegisterFile.getValue(operands[0]) << 32 >>> 32;
                final long oper2 = (long)RegisterFile.getValue(operands[1]) << 32 >>> 32;
                RegisterFile.updateRegister(33, (int)(oper1 % oper2 << 32 >> 32));
                RegisterFile.updateRegister(34, (int)(oper1 / oper2 << 32 >> 32));
            }
        }));
        this.instructionList.add(new BasicInstruction("mfhi $t1", "Move from HI register : Set $t1 to contents of HI (see multiply and divide operations)", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 fffff 00000 010000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(33));
            }
        }));
        this.instructionList.add(new BasicInstruction("mflo $t1", "Move from LO register : Set $t1 to contents of LO (see multiply and divide operations)", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 fffff 00000 010010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(34));
            }
        }));
        this.instructionList.add(new BasicInstruction("mthi $t1", "Move to HI registerr : Set HI to contents of $t1 (see multiply and divide operations)", BasicInstructionFormat.R_FORMAT, "000000 fffff 00000 00000 00000 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(33, RegisterFile.getValue(operands[0]));
            }
        }));
        this.instructionList.add(new BasicInstruction("mtlo $t1", "Move to LO register : Set LO to contents of $t1 (see multiply and divide operations)", BasicInstructionFormat.R_FORMAT, "000000 fffff 00000 00000 00000 010011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(34, RegisterFile.getValue(operands[0]));
            }
        }));
        this.instructionList.add(new BasicInstruction("and $t1,$t2,$t3", "Bitwise AND : Set $t1 to bitwise AND of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) & RegisterFile.getValue(operands[2]));
            }
        }));
        this.instructionList.add(new BasicInstruction("or $t1,$t2,$t3", "Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) | RegisterFile.getValue(operands[2]));
            }
        }));
        this.instructionList.add(new BasicInstruction("andi $t1,$t2,100", "Bitwise AND immediate : Set $t1 to bitwise AND of $t2 and zero-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "001100 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) & (operands[2] & 0xFFFF));
            }
        }));
        this.instructionList.add(new BasicInstruction("ori $t1,$t2,100", "Bitwise OR immediate : Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "001101 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) | (operands[2] & 0xFFFF));
            }
        }));
        this.instructionList.add(new BasicInstruction("nor $t1,$t2,$t3", "Bitwise NOR : Set $t1 to bitwise NOR of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], ~(RegisterFile.getValue(operands[1]) | RegisterFile.getValue(operands[2])));
            }
        }));
        this.instructionList.add(new BasicInstruction("xor $t1,$t2,$t3", "Bitwise XOR (exclusive OR) : Set $t1 to bitwise XOR of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) ^ RegisterFile.getValue(operands[2]));
            }
        }));
        this.instructionList.add(new BasicInstruction("xori $t1,$t2,100", "Bitwise XOR immediate : Set $t1 to bitwise XOR of $t2 and zero-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "001110 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) ^ (operands[2] & 0xFFFF));
            }
        }));
        this.instructionList.add(new BasicInstruction("sll $t1,$t2,10", "Shift left logical : Set $t1 to result of shifting $t2 left by number of bits specified by immediate", BasicInstructionFormat.R_FORMAT, "000000 00000 sssss fffff ttttt 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) << operands[2]);
            }
        }));
        this.instructionList.add(new BasicInstruction("sllv $t1,$t2,$t3", "Shift left logical variable : Set $t1 to result of shifting $t2 left by number of bits specified by value in low-order 5 bits of $t3", BasicInstructionFormat.R_FORMAT, "000000 ttttt sssss fffff 00000 000100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) << (RegisterFile.getValue(operands[2]) & 0x1F));
            }
        }));
        this.instructionList.add(new BasicInstruction("srl $t1,$t2,10", "Shift right logical : Set $t1 to result of shifting $t2 right by number of bits specified by immediate", BasicInstructionFormat.R_FORMAT, "000000 00000 sssss fffff ttttt 000010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >>> operands[2]);
            }
        }));
        this.instructionList.add(new BasicInstruction("sra $t1,$t2,10", "Shift right arithmetic : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by immediate", BasicInstructionFormat.R_FORMAT, "000000 00000 sssss fffff ttttt 000011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >> operands[2]);
            }
        }));
        this.instructionList.add(new BasicInstruction("srav $t1,$t2,$t3", "Shift right arithmetic variable : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3", BasicInstructionFormat.R_FORMAT, "000000 ttttt sssss fffff 00000 000111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >> (RegisterFile.getValue(operands[2]) & 0x1F));
            }
        }));
        this.instructionList.add(new BasicInstruction("srlv $t1,$t2,$t3", "Shift right logical variable : Set $t1 to result of shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3", BasicInstructionFormat.R_FORMAT, "000000 ttttt sssss fffff 00000 000110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >>> (RegisterFile.getValue(operands[2]) & 0x1F));
            }
        }));
        this.instructionList.add(new BasicInstruction("lw $t1,-100($t2)", "Load word : Set $t1 to contents of effective memory word address", BasicInstructionFormat.I_FORMAT, "100011 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    RegisterFile.updateRegister(operands[0], Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("ll $t1,-100($t2)", "Load linked : Paired with Store Conditional (sc) to perform atomic read-modify-write.  Treated as equivalent to Load Word (lw) because MARS does not simulate multiple processors.", BasicInstructionFormat.I_FORMAT, "110000 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    RegisterFile.updateRegister(operands[0], Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lwl $t1,-100($t2)", "Load word left : Load from 1 to 4 bytes left-justified into $t1, starting with effective memory byte address and continuing through the low-order byte of its word", BasicInstructionFormat.I_FORMAT, "100010 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    final int address = RegisterFile.getValue(operands[2]) + operands[1];
                    int result = RegisterFile.getValue(operands[0]);
                    int i = 0;
                    while (true) {
                        final int n = i;
                        final int n2 = address;
                        final Memory memory = Globals.memory;
                        if (n > n2 % 4) {
                            break;
                        }
                        result = Binary.setByte(result, 3 - i, Globals.memory.getByte(address - i));
                        ++i;
                    }
                    RegisterFile.updateRegister(operands[0], result);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lwr $t1,-100($t2)", "Load word right : Load from 1 to 4 bytes right-justified into $t1, starting with effective memory byte address and continuing through the high-order byte of its word", BasicInstructionFormat.I_FORMAT, "100110 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    final int address = RegisterFile.getValue(operands[2]) + operands[1];
                    int result = RegisterFile.getValue(operands[0]);
                    int i = 0;
                    while (true) {
                        final int n = i;
                        final int n2 = 3;
                        final int n3 = address;
                        final Memory memory = Globals.memory;
                        if (n > n2 - n3 % 4) {
                            break;
                        }
                        result = Binary.setByte(result, i, Globals.memory.getByte(address + i));
                        ++i;
                    }
                    RegisterFile.updateRegister(operands[0], result);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("sw $t1,-100($t2)", "Store word : Store contents of $t1 into effective memory word address", BasicInstructionFormat.I_FORMAT, "101011 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    Globals.memory.setWord(RegisterFile.getValue(operands[2]) + operands[1], RegisterFile.getValue(operands[0]));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("sc $t1,-100($t2)", "Store conditional : Paired with Load Linked (ll) to perform atomic read-modify-write.  Stores $t1 value into effective address, then sets $t1 to 1 for success.  Always succeeds because MARS does not simulate multiple processors.", BasicInstructionFormat.I_FORMAT, "111000 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    Globals.memory.setWord(RegisterFile.getValue(operands[2]) + operands[1], RegisterFile.getValue(operands[0]));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
                RegisterFile.updateRegister(operands[0], 1);
            }
        }));
        this.instructionList.add(new BasicInstruction("swl $t1,-100($t2)", "Store word left : Store high-order 1 to 4 bytes of $t1 into memory, starting with effective byte address and continuing through the low-order byte of its word", BasicInstructionFormat.I_FORMAT, "101010 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    final int address = RegisterFile.getValue(operands[2]) + operands[1];
                    final int source = RegisterFile.getValue(operands[0]);
                    int i = 0;
                    while (true) {
                        final int n = i;
                        final int n2 = address;
                        final Memory memory = Globals.memory;
                        if (n > n2 % 4) {
                            break;
                        }
                        Globals.memory.setByte(address - i, Binary.getByte(source, 3 - i));
                        ++i;
                    }
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("swr $t1,-100($t2)", "Store word right : Store low-order 1 to 4 bytes of $t1 into memory, starting with high-order byte of word containing effective byte address and continuing through that byte address", BasicInstructionFormat.I_FORMAT, "101110 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    final int address = RegisterFile.getValue(operands[2]) + operands[1];
                    final int source = RegisterFile.getValue(operands[0]);
                    int i = 0;
                    while (true) {
                        final int n = i;
                        final int n2 = 3;
                        final int n3 = address;
                        final Memory memory = Globals.memory;
                        if (n > n2 - n3 % 4) {
                            break;
                        }
                        Globals.memory.setByte(address + i, Binary.getByte(source, i));
                        ++i;
                    }
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lui $t1,100", "Load upper immediate : Set high-order 16 bits of $t1 to 16-bit immediate and low-order 16 bits to 0", BasicInstructionFormat.I_FORMAT, "001111 00000 fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], operands[1] << 16);
            }
        }));
        this.instructionList.add(new BasicInstruction("beq $t1,$t2,label", "Branch if equal : Branch to statement at label's address if $t1 and $t2 are equal", BasicInstructionFormat.I_BRANCH_FORMAT, "000100 fffff sssss tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) == RegisterFile.getValue(operands[1])) {
                    InstructionSet.this.processBranch(operands[2]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bne $t1,$t2,label", "Branch if not equal : Branch to statement at label's address if $t1 and $t2 are not equal", BasicInstructionFormat.I_BRANCH_FORMAT, "000101 fffff sssss tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) != RegisterFile.getValue(operands[1])) {
                    InstructionSet.this.processBranch(operands[2]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bgez $t1,label", "Branch if greater than or equal to zero : Branch to statement at label's address if $t1 is greater than or equal to zero", BasicInstructionFormat.I_BRANCH_FORMAT, "000001 fffff 00001 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) >= 0) {
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bgezal $t1,label", "Branch if greater then or equal to zero and link : If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address", BasicInstructionFormat.I_BRANCH_FORMAT, "000001 fffff 10001 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) >= 0) {
                    InstructionSet.this.processReturnAddress(31);
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bgtz $t1,label", "Branch if greater than zero : Branch to statement at label's address if $t1 is greater than zero", BasicInstructionFormat.I_BRANCH_FORMAT, "000111 fffff 00000 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) > 0) {
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("blez $t1,label", "Branch if less than or equal to zero : Branch to statement at label's address if $t1 is less than or equal to zero", BasicInstructionFormat.I_BRANCH_FORMAT, "000110 fffff 00000 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) <= 0) {
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bltz $t1,label", "Branch if less than zero : Branch to statement at label's address if $t1 is less than zero", BasicInstructionFormat.I_BRANCH_FORMAT, "000001 fffff 00000 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) < 0) {
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bltzal $t1,label", "Branch if less than zero and link : If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address", BasicInstructionFormat.I_BRANCH_FORMAT, "000001 fffff 10000 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) < 0) {
                    InstructionSet.this.processReturnAddress(31);
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("slt $t1,$t2,$t3", "Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 101010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], (RegisterFile.getValue(operands[1]) < RegisterFile.getValue(operands[2])) ? 1 : 0);
            }
        }));
        this.instructionList.add(new BasicInstruction("sltu $t1,$t2,$t3", "Set less than unsigned : If $t2 is less than $t3 using unsigned comparision, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 101011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int first = RegisterFile.getValue(operands[1]);
                final int second = RegisterFile.getValue(operands[2]);
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    RegisterFile.updateRegister(operands[0], (first < second) ? 1 : 0);
                }
                else {
                    RegisterFile.updateRegister(operands[0], (first >= 0) ? 1 : 0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("slti $t1,$t2,-100", "Set less than immediate : If $t2 is less than sign-extended 16-bit immediate, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.I_FORMAT, "001010 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], (RegisterFile.getValue(operands[1]) < operands[2] << 16 >> 16) ? 1 : 0);
            }
        }));
        this.instructionList.add(new BasicInstruction("sltiu $t1,$t2,-100", "Set less than immediate unsigned : If $t2 is less than  sign-extended 16-bit immediate using unsigned comparison, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.I_FORMAT, "001011 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int first = RegisterFile.getValue(operands[1]);
                final int second = operands[2] << 16 >> 16;
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    RegisterFile.updateRegister(operands[0], (first < second) ? 1 : 0);
                }
                else {
                    RegisterFile.updateRegister(operands[0], (first >= 0) ? 1 : 0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movn $t1,$t2,$t3", "Move conditional not zero : Set $t1 to $t2 if $t3 is not zero", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 001011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[2]) != 0) {
                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movz $t1,$t2,$t3", "Move conditional zero : Set $t1 to $t2 if $t3 is zero", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 001010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[2]) == 0) {
                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movf $t1,$t2", "Move if FP condition flag 0 false : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag 0 is false (zero)", BasicInstructionFormat.R_FORMAT, "000000 sssss 000 00 fffff 00000 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(0) == 0) {
                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movf $t1,$t2,1", "Move if specified FP condition flag false : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag specified by the immediate is false (zero)", BasicInstructionFormat.R_FORMAT, "000000 sssss ttt 00 fffff 00000 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(operands[2]) == 0) {
                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movt $t1,$t2", "Move if FP condition flag 0 true : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag 0 is true (one)", BasicInstructionFormat.R_FORMAT, "000000 sssss 000 01 fffff 00000 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(0) == 1) {
                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movt $t1,$t2,1", "Move if specfied FP condition flag true : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag specified by the immediate is true (one)", BasicInstructionFormat.R_FORMAT, "000000 sssss ttt 01 fffff 00000 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(operands[2]) == 1) {
                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("break 100", "Break execution with code : Terminate program execution with specified exception code", BasicInstructionFormat.R_FORMAT, "000000 ffffffffffffffffffff 001101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                throw new ProcessingException(statement, "break instruction executed; code = " + operands[0] + ".", 9);
            }
        }));
        this.instructionList.add(new BasicInstruction("break", "Break execution : Terminate program execution with exception", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 00000 00000 001101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                throw new ProcessingException(statement, "break instruction executed; no code given.", 9);
            }
        }));
        this.instructionList.add(new BasicInstruction("syscall", "Issue a system call : Execute the system call specified by value in $v0", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 00000 00000 001100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSet.this.findAndSimulateSyscall(RegisterFile.getValue(2), statement);
            }
        }));
        this.instructionList.add(new BasicInstruction("j target", "Jump unconditionally : Jump to statement at target address", BasicInstructionFormat.J_FORMAT, "000010 ffffffffffffffffffffffffff", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                InstructionSet.this.processJump((RegisterFile.getProgramCounter() & 0xF0000000) | operands[0] << 2);
            }
        }));
        this.instructionList.add(new BasicInstruction("jr $t1", "Jump register unconditionally : Jump to statement whose address is in $t1", BasicInstructionFormat.R_FORMAT, "000000 fffff 00000 00000 00000 001000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                InstructionSet.this.processJump(RegisterFile.getValue(operands[0]));
            }
        }));
        this.instructionList.add(new BasicInstruction("jal target", "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address", BasicInstructionFormat.J_FORMAT, "000011 ffffffffffffffffffffffffff", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                InstructionSet.this.processReturnAddress(31);
                InstructionSet.this.processJump((RegisterFile.getProgramCounter() & 0xF0000000) | operands[0] << 2);
            }
        }));
        this.instructionList.add(new BasicInstruction("jalr $t1,$t2", "Jump and link register : Set $t1 to Program Counter (return address) then jump to statement whose address is in $t2", BasicInstructionFormat.R_FORMAT, "000000 sssss 00000 fffff 00000 001001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                InstructionSet.this.processReturnAddress(operands[0]);
                InstructionSet.this.processJump(RegisterFile.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("jalr $t1", "Jump and link register : Set $ra to Program Counter (return address) then jump to statement whose address is in $t1", BasicInstructionFormat.R_FORMAT, "000000 fffff 00000 11111 00000 001001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                InstructionSet.this.processReturnAddress(31);
                InstructionSet.this.processJump(RegisterFile.getValue(operands[0]));
            }
        }));
        this.instructionList.add(new BasicInstruction("lb $t1,-100($t2)", "Load byte : Set $t1 to sign-extended 8-bit value from effective memory byte address", BasicInstructionFormat.I_FORMAT, "100000 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    RegisterFile.updateRegister(operands[0], Globals.memory.getByte(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16)) << 24 >> 24);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lh $t1,-100($t2)", "Load halfword : Set $t1 to sign-extended 16-bit value from effective memory halfword address", BasicInstructionFormat.I_FORMAT, "100001 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    RegisterFile.updateRegister(operands[0], Globals.memory.getHalf(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16)) << 16 >> 16);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lhu $t1,-100($t2)", "Load halfword unsigned : Set $t1 to zero-extended 16-bit value from effective memory halfword address", BasicInstructionFormat.I_FORMAT, "100101 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    RegisterFile.updateRegister(operands[0], Globals.memory.getHalf(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16)) & 0xFFFF);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lbu $t1,-100($t2)", "Load byte unsigned : Set $t1 to zero-extended 8-bit value from effective memory byte address", BasicInstructionFormat.I_FORMAT, "100100 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    RegisterFile.updateRegister(operands[0], Globals.memory.getByte(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16)) & 0xFF);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("sb $t1,-100($t2)", "Store byte : Store the low-order 8 bits of $t1 into the effective memory byte address", BasicInstructionFormat.I_FORMAT, "101000 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    Globals.memory.setByte(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16), RegisterFile.getValue(operands[0]) & 0xFF);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("sh $t1,-100($t2)", "Store halfword : Store the low-order 16 bits of $t1 into the effective memory halfword address", BasicInstructionFormat.I_FORMAT, "101001 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    Globals.memory.setHalf(RegisterFile.getValue(operands[2]) + (operands[1] << 16 >> 16), RegisterFile.getValue(operands[0]) & 0xFFFF);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("clo $t1,$t2", "Count number of leading ones : Set $t1 to the count of leading one bits in $t2 starting at most significant bit position", BasicInstructionFormat.R_FORMAT, "011100 sssss 00000 fffff 00000 100001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int value = RegisterFile.getValue(operands[1]);
                int leadingOnes = 0;
                for (int bitPosition = 31; Binary.bitValue(value, bitPosition) == 1 && bitPosition >= 0; --bitPosition) {
                    ++leadingOnes;
                }
                RegisterFile.updateRegister(operands[0], leadingOnes);
            }
        }));
        this.instructionList.add(new BasicInstruction("clz $t1,$t2", "Count number of leading zeroes : Set $t1 to the count of leading zero bits in $t2 starting at most significant bit positio", BasicInstructionFormat.R_FORMAT, "011100 sssss 00000 fffff 00000 100000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int value = RegisterFile.getValue(operands[1]);
                int leadingZeros = 0;
                for (int bitPosition = 31; Binary.bitValue(value, bitPosition) == 0 && bitPosition >= 0; --bitPosition) {
                    ++leadingZeros;
                }
                RegisterFile.updateRegister(operands[0], leadingZeros);
            }
        }));
        this.instructionList.add(new BasicInstruction("mfc0 $t1,$8", "Move from Coprocessor 0 : Set $t1 to the value stored in Coprocessor 0 register $8", BasicInstructionFormat.R_FORMAT, "010000 00000 fffff sssss 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], Coprocessor0.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("mtc0 $t1,$8", "Move to Coprocessor 0 : Set Coprocessor 0 register $8 to value stored in $t1", BasicInstructionFormat.R_FORMAT, "010000 00100 fffff sssss 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                Coprocessor0.updateRegister(operands[1], RegisterFile.getValue(operands[0]));
            }
        }));
        this.instructionList.add(new BasicInstruction("add.s $f0,$f1,$f3", "Floating point addition single precision : Set $f0 to single-precision floating point value of $f1 plus $f3", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fffff 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float add1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float add2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                final float sum = add1 + add2;
                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(sum));
            }
        }));
        this.instructionList.add(new BasicInstruction("sub.s $f0,$f1,$f3", "Floating point subtraction single precision : Set $f0 to single-precision floating point value of $f1  minus $f3", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fffff 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float sub1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float sub2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                final float diff = sub1 - sub2;
                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(diff));
            }
        }));
        this.instructionList.add(new BasicInstruction("mul.s $f0,$f1,$f3", "Floating point multiplication single precision : Set $f0 to single-precision floating point value of $f1 times $f3", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fffff 000010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float mul1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float mul2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                final float prod = mul1 * mul2;
                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(prod));
            }
        }));
        this.instructionList.add(new BasicInstruction("div.s $f0,$f1,$f3", "Floating point division single precision : Set $f0 to single-precision floating point value of $f1 divided by $f3", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fffff 000011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float div1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float div2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                final float quot = div1 / div2;
                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(quot));
            }
        }));
        this.instructionList.add(new BasicInstruction("sqrt.s $f0,$f1", "Square root single precision : Set $f0 to single-precision floating point square root of $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 000100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float value = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                int floatSqrt = 0;
                if (value < 0.0f) {
                    floatSqrt = Float.floatToIntBits(Float.NaN);
                }
                else {
                    floatSqrt = Float.floatToIntBits((float)Math.sqrt(value));
                }
                Coprocessor1.updateRegister(operands[0], floatSqrt);
            }
        }));
        this.instructionList.add(new BasicInstruction("floor.w.s $f0,$f1", "Floor single precision to word : Set $f0 to 32-bit integer floor of single-precision float in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 001111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                int floor = (int)Math.floor(floatValue);
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || floatValue < -2.14748365E9f || floatValue > 2.14748365E9f) {
                    floor = Integer.MAX_VALUE;
                }
                Coprocessor1.updateRegister(operands[0], floor);
            }
        }));
        this.instructionList.add(new BasicInstruction("ceil.w.s $f0,$f1", "Ceiling single precision to word : Set $f0 to 32-bit integer ceiling of single-precision float in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 001110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                int ceiling = (int)Math.ceil(floatValue);
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || floatValue < -2.14748365E9f || floatValue > 2.14748365E9f) {
                    ceiling = Integer.MAX_VALUE;
                }
                Coprocessor1.updateRegister(operands[0], ceiling);
            }
        }));
        this.instructionList.add(new BasicInstruction("round.w.s $f0,$f1", "Round single precision to word : Set $f0 to 32-bit integer round of single-precision float in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 001100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                int below = 0;
                int above = 0;
                int round = Math.round(floatValue);
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || floatValue < -2.14748365E9f || floatValue > 2.14748365E9f) {
                    round = Integer.MAX_VALUE;
                }
                else {
                    final Float floatObj = new Float(floatValue);
                    if (floatValue < 0.0f) {
                        above = floatObj.intValue();
                        below = above - 1;
                    }
                    else {
                        below = floatObj.intValue();
                        above = below + 1;
                    }
                    if (floatValue - below == above - floatValue) {
                        round = ((above % 2 == 0) ? above : below);
                    }
                }
                Coprocessor1.updateRegister(operands[0], round);
            }
        }));
        this.instructionList.add(new BasicInstruction("trunc.w.s $f0,$f1", "Truncate single precision to word : Set $f0 to 32-bit integer truncation of single-precision float in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 001101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                int truncate = (int)floatValue;
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || floatValue < -2.14748365E9f || floatValue > 2.14748365E9f) {
                    truncate = Integer.MAX_VALUE;
                }
                Coprocessor1.updateRegister(operands[0], truncate);
            }
        }));
        this.instructionList.add(new BasicInstruction("add.d $f2,$f4,$f6", "Floating point addition double precision : Set $f2 to double-precision floating point value of $f4 plus $f6", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fffff 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "all registers must be even-numbered");
                }
                final double add1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double add2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                final double sum = add1 + add2;
                final long longSum = Double.doubleToLongBits(sum);
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longSum));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longSum));
            }
        }));
        this.instructionList.add(new BasicInstruction("sub.d $f2,$f4,$f6", "Floating point subtraction double precision : Set $f2 to double-precision floating point value of $f4 minus $f6", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fffff 000001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "all registers must be even-numbered");
                }
                final double sub1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double sub2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                final double diff = sub1 - sub2;
                final long longDiff = Double.doubleToLongBits(diff);
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longDiff));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longDiff));
            }
        }));
        this.instructionList.add(new BasicInstruction("mul.d $f2,$f4,$f6", "Floating point multiplication double precision : Set $f2 to double-precision floating point value of $f4 times $f6", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fffff 000010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "all registers must be even-numbered");
                }
                final double mul1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double mul2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                final double prod = mul1 * mul2;
                final long longProd = Double.doubleToLongBits(prod);
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longProd));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longProd));
            }
        }));
        this.instructionList.add(new BasicInstruction("div.d $f2,$f4,$f6", "Floating point division double precision : Set $f2 to double-precision floating point value of $f4 divided by $f6", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fffff 000011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "all registers must be even-numbered");
                }
                final double div1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double div2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                final double quot = div1 / div2;
                final long longQuot = Double.doubleToLongBits(quot);
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longQuot));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longQuot));
            }
        }));
        this.instructionList.add(new BasicInstruction("sqrt.d $f2,$f4", "Square root double precision : Set $f2 to double-precision floating point square root of $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 000100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double value = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                long longSqrt = 0L;
                if (value < 0.0) {
                    longSqrt = Double.doubleToLongBits(Double.NaN);
                }
                else {
                    longSqrt = Double.doubleToLongBits(Math.sqrt(value));
                }
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longSqrt));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longSqrt));
            }
        }));
        this.instructionList.add(new BasicInstruction("floor.w.d $f1,$f2", "Floor double precision to word : Set $f1 to 32-bit integer floor of double-precision float in $f2", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 001111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "second register must be even-numbered");
                }
                final double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                int floor = (int)Math.floor(doubleValue);
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || doubleValue < -2.147483648E9 || doubleValue > 2.147483647E9) {
                    floor = Integer.MAX_VALUE;
                }
                Coprocessor1.updateRegister(operands[0], floor);
            }
        }));
        this.instructionList.add(new BasicInstruction("ceil.w.d $f1,$f2", "Ceiling double precision to word : Set $f1 to 32-bit integer ceiling of double-precision float in $f2", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 001110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "second register must be even-numbered");
                }
                final double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                int ceiling = (int)Math.ceil(doubleValue);
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || doubleValue < -2.147483648E9 || doubleValue > 2.147483647E9) {
                    ceiling = Integer.MAX_VALUE;
                }
                Coprocessor1.updateRegister(operands[0], ceiling);
            }
        }));
        this.instructionList.add(new BasicInstruction("round.w.d $f1,$f2", "Round double precision to word : Set $f1 to 32-bit integer round of double-precision float in $f2", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 001100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "second register must be even-numbered");
                }
                final double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                int below = 0;
                int above = 0;
                int round = (int)Math.round(doubleValue);
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || doubleValue < -2.147483648E9 || doubleValue > 2.147483647E9) {
                    round = Integer.MAX_VALUE;
                }
                else {
                    final Double doubleObj = new Double(doubleValue);
                    if (doubleValue < 0.0) {
                        above = doubleObj.intValue();
                        below = above - 1;
                    }
                    else {
                        below = doubleObj.intValue();
                        above = below + 1;
                    }
                    if (doubleValue - below == above - doubleValue) {
                        round = ((above % 2 == 0) ? above : below);
                    }
                }
                Coprocessor1.updateRegister(operands[0], round);
            }
        }));
        this.instructionList.add(new BasicInstruction("trunc.w.d $f1,$f2", "Truncate double precision to word : Set $f1 to 32-bit integer truncation of double-precision float in $f2", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 001101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "second register must be even-numbered");
                }
                final double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                int truncate = (int)doubleValue;
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || doubleValue < -2.147483648E9 || doubleValue > 2.147483647E9) {
                    truncate = Integer.MAX_VALUE;
                }
                Coprocessor1.updateRegister(operands[0], truncate);
            }
        }));
        this.instructionList.add(new BasicInstruction("bc1t label", "Branch if FP condition flag 0 true (BC1T, not BCLT) : If Coprocessor 1 condition flag 0 is true (one) then branch to statement at label's address", BasicInstructionFormat.I_BRANCH_FORMAT, "010001 01000 00001 ffffffffffffffff", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(0) == 1) {
                    InstructionSet.this.processBranch(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bc1t 1,label", "Branch if specified FP condition flag true (BC1T, not BCLT) : If Coprocessor 1 condition flag specified by immediate is true (one) then branch to statement at label's address", BasicInstructionFormat.I_BRANCH_FORMAT, "010001 01000 fff 01 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(operands[0]) == 1) {
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bc1f label", "Branch if FP condition flag 0 false (BC1F, not BCLF) : If Coprocessor 1 condition flag 0 is false (zero) then branch to statement at label's address", BasicInstructionFormat.I_BRANCH_FORMAT, "010001 01000 00000 ffffffffffffffff", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(0) == 0) {
                    InstructionSet.this.processBranch(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("bc1f 1,label", "Branch if specified FP condition flag false (BC1F, not BCLF) : If Coprocessor 1 condition flag specified by immediate is false (zero) then branch to statement at label's address", BasicInstructionFormat.I_BRANCH_FORMAT, "010001 01000 fff 00 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(operands[0]) == 0) {
                    InstructionSet.this.processBranch(operands[1]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.eq.s $f0,$f1", "Compare equal single precision : If $f0 is equal to $f1, set Coprocessor 1 condition flag 0 true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10000 sssss fffff 00000 110010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[0]));
                final float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                if (op1 == op2) {
                    Coprocessor1.setConditionFlag(0);
                }
                else {
                    Coprocessor1.clearConditionFlag(0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.eq.s 1,$f0,$f1", "Compare equal single precision : If $f0 is equal to $f1, set Coprocessor 1 condition flag specied by immediate to true else set it to false", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fff 00 11 0010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                if (op1 == op2) {
                    Coprocessor1.setConditionFlag(operands[0]);
                }
                else {
                    Coprocessor1.clearConditionFlag(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.le.s $f0,$f1", "Compare less or equal single precision : If $f0 is less than or equal to $f1, set Coprocessor 1 condition flag 0 true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10000 sssss fffff 00000 111110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[0]));
                final float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                if (op1 <= op2) {
                    Coprocessor1.setConditionFlag(0);
                }
                else {
                    Coprocessor1.clearConditionFlag(0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.le.s 1,$f0,$f1", "Compare less or equal single precision : If $f0 is less than or equal to $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fff 00 111110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                if (op1 <= op2) {
                    Coprocessor1.setConditionFlag(operands[0]);
                }
                else {
                    Coprocessor1.clearConditionFlag(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.lt.s $f0,$f1", "Compare less than single precision : If $f0 is less than $f1, set Coprocessor 1 condition flag 0 true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10000 sssss fffff 00000 111100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[0]));
                final float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                if (op1 < op2) {
                    Coprocessor1.setConditionFlag(0);
                }
                else {
                    Coprocessor1.clearConditionFlag(0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.lt.s 1,$f0,$f1", "Compare less than single precision : If $f0 is less than $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fff 00 111100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                final float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                if (op1 < op2) {
                    Coprocessor1.setConditionFlag(operands[0]);
                }
                else {
                    Coprocessor1.clearConditionFlag(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.eq.d $f2,$f4", "Compare equal double precision : If $f2 is equal to $f4 (double-precision), set Coprocessor 1 condition flag 0 true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10001 sssss fffff 00000 110010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[0] + 1), Coprocessor1.getValue(operands[0])));
                final double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                if (op1 == op2) {
                    Coprocessor1.setConditionFlag(0);
                }
                else {
                    Coprocessor1.clearConditionFlag(0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.eq.d 1,$f2,$f4", "Compare equal double precision : If $f2 is equal to $f4 (double-precision), set Coprocessor 1 condition flag specified by immediate to true else set it to false", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fff 00 110010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                if (op1 == op2) {
                    Coprocessor1.setConditionFlag(operands[0]);
                }
                else {
                    Coprocessor1.clearConditionFlag(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.le.d $f2,$f4", "Compare less or equal double precision : If $f2 is less than or equal to $f4 (double-precision), set Coprocessor 1 condition flag 0 true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10001 sssss fffff 00000 111110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[0] + 1), Coprocessor1.getValue(operands[0])));
                final double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                if (op1 <= op2) {
                    Coprocessor1.setConditionFlag(0);
                }
                else {
                    Coprocessor1.clearConditionFlag(0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.le.d 1,$f2,$f4", "Compare less or equal double precision : If $f2 is less than or equal to $f4 (double-precision), set Coprocessor 1 condition flag specfied by immediate true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fff 00 111110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                if (op1 <= op2) {
                    Coprocessor1.setConditionFlag(operands[0]);
                }
                else {
                    Coprocessor1.clearConditionFlag(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.lt.d $f2,$f4", "Compare less than double precision : If $f2 is less than $f4 (double-precision), set Coprocessor 1 condition flag 0 true else set it false", BasicInstructionFormat.R_FORMAT, "010001 10001 sssss fffff 00000 111100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[0] + 1), Coprocessor1.getValue(operands[0])));
                final double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                if (op1 < op2) {
                    Coprocessor1.setConditionFlag(0);
                }
                else {
                    Coprocessor1.clearConditionFlag(0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("c.lt.d 1,$f2,$f4", "Compare less than double precision : If $f2 is less than $f4 (double-precision), set Coprocessor 1 condition flag specified by immediate to true else set it to false", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fff 00 111100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                final double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                if (op1 < op2) {
                    Coprocessor1.setConditionFlag(operands[0]);
                }
                else {
                    Coprocessor1.clearConditionFlag(operands[0]);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("abs.s $f0,$f1", "Floating point absolute value single precision : Set $f0 to absolute value of $f1, single precision", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 000101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]) & Integer.MAX_VALUE);
            }
        }));
        this.instructionList.add(new BasicInstruction("abs.d $f2,$f4", "Floating point absolute value double precision : Set $f2 to absolute value of $f4, double precision", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 000101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1) & Integer.MAX_VALUE);
                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("cvt.d.s $f2,$f1", "Convert from single precision to double precision : Set $f2 to double precision equivalent of single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 100001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1) {
                    throw new ProcessingException(statement, "first register must be even-numbered");
                }
                final long result = Double.doubleToLongBits(Float.intBitsToFloat(Coprocessor1.getValue(operands[1])));
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(result));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(result));
            }
        }));
        this.instructionList.add(new BasicInstruction("cvt.d.w $f2,$f1", "Convert from word to double precision : Set $f2 to double precision equivalent of 32-bit integer value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10100 00000 sssss fffff 100001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1) {
                    throw new ProcessingException(statement, "first register must be even-numbered");
                }
                final long result = Double.doubleToLongBits(Coprocessor1.getValue(operands[1]));
                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(result));
                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(result));
            }
        }));
        this.instructionList.add(new BasicInstruction("cvt.s.d $f1,$f2", "Convert from double precision to single precision : Set $f1 to single precision equivalent of double precision value in $f2", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 100000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "second register must be even-numbered");
                }
                final double val = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits((float)val));
            }
        }));
        this.instructionList.add(new BasicInstruction("cvt.s.w $f0,$f1", "Convert from word to single precision : Set $f0 to single precision equivalent of 32-bit integer value in $f2", BasicInstructionFormat.R_FORMAT, "010001 10100 00000 sssss fffff 100000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits((float)Coprocessor1.getValue(operands[1])));
            }
        }));
        this.instructionList.add(new BasicInstruction("cvt.w.d $f1,$f2", "Convert from double precision to word : Set $f1 to 32-bit integer equivalent of double precision value in $f2", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 100100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "second register must be even-numbered");
                }
                final double val = Double.longBitsToDouble(Binary.twoIntsToLong(Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                Coprocessor1.updateRegister(operands[0], (int)val);
            }
        }));
        this.instructionList.add(new BasicInstruction("cvt.w.s $f0,$f1", "Convert from single precision to word : Set $f0 to 32-bit integer equivalent of single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 100100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                Coprocessor1.updateRegister(operands[0], (int)Float.intBitsToFloat(Coprocessor1.getValue(operands[1])));
            }
        }));
        this.instructionList.add(new BasicInstruction("mov.d $f2,$f4", "Move floating point double precision : Set double precision $f2 to double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 000110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
            }
        }));
        this.instructionList.add(new BasicInstruction("movf.d $f2,$f4", "Move floating point double precision : If condition flag 0 false, set double precision $f2 to double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 000 00 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                if (Coprocessor1.getConditionFlag(0) == 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movf.d $f2,$f4,1", "Move floating point double precision : If condition flag specified by immediate is false, set double precision $f2 to double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 ttt 00 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                if (Coprocessor1.getConditionFlag(operands[2]) == 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movt.d $f2,$f4", "Move floating point double precision : If condition flag 0 true, set double precision $f2 to double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 000 01 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                if (Coprocessor1.getConditionFlag(0) == 1) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movt.d $f2,$f4,1", "Move floating point double precision : If condition flag specified by immediate is true, set double precision $f2 to double precision value in $f4e", BasicInstructionFormat.R_FORMAT, "010001 10001 ttt 01 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                if (Coprocessor1.getConditionFlag(operands[2]) == 1) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movn.d $f2,$f4,$t3", "Move floating point double precision : If $t3 is not zero, set double precision $f2 to double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fffff 010011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                if (RegisterFile.getValue(operands[2]) != 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movz.d $f2,$f4,$t3", "Move floating point double precision : If $t3 is zero, set double precision $f2 to double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 ttttt sssss fffff 010010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                if (RegisterFile.getValue(operands[2]) == 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("mov.s $f0,$f1", "Move floating point single precision : Set single precision $f0 to single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 000110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("movf.s $f0,$f1", "Move floating point single precision : If condition flag 0 is false, set single precision $f0 to single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 000 00 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(0) == 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movf.s $f0,$f1,1", "Move floating point single precision : If condition flag specified by immediate is false, set single precision $f0 to single precision value in $f1e", BasicInstructionFormat.R_FORMAT, "010001 10000 ttt 00 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(operands[2]) == 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movt.s $f0,$f1", "Move floating point single precision : If condition flag 0 is true, set single precision $f0 to single precision value in $f1e", BasicInstructionFormat.R_FORMAT, "010001 10000 000 01 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(0) == 1) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movt.s $f0,$f1,1", "Move floating point single precision : If condition flag specified by immediate is true, set single precision $f0 to single precision value in $f1e", BasicInstructionFormat.R_FORMAT, "010001 10000 ttt 01 sssss fffff 010001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (Coprocessor1.getConditionFlag(operands[2]) == 1) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movn.s $f0,$f1,$t3", "Move floating point single precision : If $t3 is not zero, set single precision $f0 to single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fffff 010011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[2]) != 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("movz.s $f0,$f1,$t3", "Move floating point single precision : If $t3 is zero, set single precision $f0 to single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 ttttt sssss fffff 010010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[2]) == 0) {
                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("mfc1 $t1,$f1", "Move from Coprocessor 1 (FPU) : Set $t1 to value in Coprocessor 1 register $f1", BasicInstructionFormat.R_FORMAT, "010001 00000 fffff sssss 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                RegisterFile.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("mtc1 $t1,$f1", "Move to Coprocessor 1 (FPU) : Set Coprocessor 1 register $f1 to value in $t1", BasicInstructionFormat.R_FORMAT, "010001 00100 fffff sssss 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                Coprocessor1.updateRegister(operands[1], RegisterFile.getValue(operands[0]));
            }
        }));
        this.instructionList.add(new BasicInstruction("neg.d $f2,$f4", "Floating point negate double precision : Set double precision $f2 to negation of double precision value in $f4", BasicInstructionFormat.R_FORMAT, "010001 10001 00000 sssss fffff 000111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                    throw new ProcessingException(statement, "both registers must be even-numbered");
                }
                final int value = Coprocessor1.getValue(operands[1] + 1);
                Coprocessor1.updateRegister(operands[0] + 1, (value < 0) ? (value & Integer.MAX_VALUE) : (value | Integer.MIN_VALUE));
                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
            }
        }));
        this.instructionList.add(new BasicInstruction("neg.s $f0,$f1", "Floating point negate single precision : Set single precision $f0 to negation of single precision value in $f1", BasicInstructionFormat.R_FORMAT, "010001 10000 00000 sssss fffff 000111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int value = Coprocessor1.getValue(operands[1]);
                Coprocessor1.updateRegister(operands[0], (value < 0) ? (value & Integer.MAX_VALUE) : (value | Integer.MIN_VALUE));
            }
        }));
        this.instructionList.add(new BasicInstruction("lwc1 $f1,-100($t2)", "Load word into Coprocessor 1 (FPU) : Set $f1 to 32-bit value from effective memory word address", BasicInstructionFormat.I_FORMAT, "110001 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    Coprocessor1.updateRegister(operands[0], Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("ldc1 $f2,-100($t2)", "Load double word Coprocessor 1 (FPU)) : Set $f2 to 64-bit value from effective memory doubleword address", BasicInstructionFormat.I_FORMAT, "110101 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1) {
                    throw new ProcessingException(statement, "first register must be even-numbered");
                }
                final Memory memory = Globals.memory;
                if (!Memory.doublewordAligned(RegisterFile.getValue(operands[2]) + operands[1])) {
                    throw new ProcessingException(statement, new AddressErrorException("address not aligned on doubleword boundary ", 4, RegisterFile.getValue(operands[2]) + operands[1]));
                }
                try {
                    Coprocessor1.updateRegister(operands[0], Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]));
                    Coprocessor1.updateRegister(operands[0] + 1, Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1] + 4));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("swc1 $f1,-100($t2)", "Store word from Coprocesor 1 (FPU) : Store 32 bit value in $f1 to effective memory word address", BasicInstructionFormat.I_FORMAT, "111001 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                try {
                    Globals.memory.setWord(RegisterFile.getValue(operands[2]) + operands[1], Coprocessor1.getValue(operands[0]));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("sdc1 $f2,-100($t2)", "Store double word from Coprocessor 1 (FPU)) : Store 64 bit value in $f2 to effective memory doubleword address", BasicInstructionFormat.I_FORMAT, "111101 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (operands[0] % 2 == 1) {
                    throw new ProcessingException(statement, "first register must be even-numbered");
                }
                final Memory memory = Globals.memory;
                if (!Memory.doublewordAligned(RegisterFile.getValue(operands[2]) + operands[1])) {
                    throw new ProcessingException(statement, new AddressErrorException("address not aligned on doubleword boundary ", 5, RegisterFile.getValue(operands[2]) + operands[1]));
                }
                try {
                    Globals.memory.setWord(RegisterFile.getValue(operands[2]) + operands[1], Coprocessor1.getValue(operands[0]));
                    Globals.memory.setWord(RegisterFile.getValue(operands[2]) + operands[1] + 4, Coprocessor1.getValue(operands[0] + 1));
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("teq $t1,$t2", "Trap if equal : Trap if $t1 is equal to $t2", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 110100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) == RegisterFile.getValue(operands[1])) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("teqi $t1,-100", "Trap if equal to immediate : Trap if $t1 is equal to sign-extended 16 bit immediate", BasicInstructionFormat.I_FORMAT, "000001 fffff 01100 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) == operands[1] << 16 >> 16) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tne $t1,$t2", "Trap if not equal : Trap if $t1 is not equal to $t2", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 110110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) != RegisterFile.getValue(operands[1])) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tnei $t1,-100", "Trap if not equal to immediate : Trap if $t1 is not equal to sign-extended 16 bit immediate", BasicInstructionFormat.I_FORMAT, "000001 fffff 01110 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) != operands[1] << 16 >> 16) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tge $t1,$t2", "Trap if greater or equal : Trap if $t1 is greater than or equal to $t2", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 110000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) >= RegisterFile.getValue(operands[1])) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tgeu $t1,$t2", "Trap if greater or equal unsigned : Trap if $t1 is greater than or equal to $t2 using unsigned comparision", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 110001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int first = RegisterFile.getValue(operands[0]);
                final int second = RegisterFile.getValue(operands[1]);
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    if (first < second) {
                        return;
                    }
                }
                else if (first >= 0) {
                    return;
                }
                throw new ProcessingException(statement, "trap", 13);
            }
        }));
        this.instructionList.add(new BasicInstruction("tgei $t1,-100", "Trap if greater than or equal to immediate : Trap if $t1 greater than or equal to sign-extended 16 bit immediate", BasicInstructionFormat.I_FORMAT, "000001 fffff 01000 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) >= operands[1] << 16 >> 16) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tgeiu $t1,-100", "Trap if greater or equal to immediate unsigned : Trap if $t1 greater than or equal to sign-extended 16 bit immediate, unsigned comparison", BasicInstructionFormat.I_FORMAT, "000001 fffff 01001 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int first = RegisterFile.getValue(operands[0]);
                final int second = operands[1] << 16 >> 16;
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    if (first < second) {
                        return;
                    }
                }
                else if (first >= 0) {
                    return;
                }
                throw new ProcessingException(statement, "trap", 13);
            }
        }));
        this.instructionList.add(new BasicInstruction("tlt $t1,$t2", "Trap if less than: Trap if $t1 less than $t2", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 110010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) < RegisterFile.getValue(operands[1])) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tltu $t1,$t2", "Trap if less than unsigned : Trap if $t1 less than $t2, unsigned comparison", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 110011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int first = RegisterFile.getValue(operands[0]);
                final int second = RegisterFile.getValue(operands[1]);
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    if (first >= second) {
                        return;
                    }
                }
                else if (first < 0) {
                    return;
                }
                throw new ProcessingException(statement, "trap", 13);
            }
        }));
        this.instructionList.add(new BasicInstruction("tlti $t1,-100", "Trap if less than immediate : Trap if $t1 less than sign-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "000001 fffff 01010 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                if (RegisterFile.getValue(operands[0]) < operands[1] << 16 >> 16) {
                    throw new ProcessingException(statement, "trap", 13);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("tltiu $t1,-100", "Trap if less than immediate unsigned : Trap if $t1 less than sign-extended 16-bit immediate, unsigned comparison", BasicInstructionFormat.I_FORMAT, "000001 fffff 01011 ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int[] operands = statement.getOperands();
                final int first = RegisterFile.getValue(operands[0]);
                final int second = operands[1] << 16 >> 16;
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    if (first >= second) {
                        return;
                    }
                }
                else if (first < 0) {
                    return;
                }
                throw new ProcessingException(statement, "trap", 13);
            }
        }));
        this.instructionList.add(new BasicInstruction("eret", "Exception return : Set Program Counter to Coprocessor 0 EPC register value, set Coprocessor Status register bit 1 (exception level) to zero", BasicInstructionFormat.R_FORMAT, "010000 1 0000000000000000000 011000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                Coprocessor0.updateRegister(12, Binary.clearBit(Coprocessor0.getValue(12), 1));
                RegisterFile.setProgramCounter(Coprocessor0.getValue(14));
            }
        }));
        this.addPseudoInstructions();
        (this.syscallLoader = new SyscallLoader()).loadSyscalls();
        for (int i = 0; i < this.instructionList.size(); ++i) {
            final Instruction inst = this.instructionList.get(i);
            inst.createExampleTokenList();
        }
        // Who did this?
        final HashMap<Integer,HashMap> maskMap = new HashMap();
        final ArrayList<MatchMap> matchMaps = new ArrayList();
        for (int j = 0; j < this.instructionList.size(); ++j) {
            final Instruction rawInstr = this.instructionList.get(j);
            if (rawInstr instanceof BasicInstruction) {
                final BasicInstruction basic = (BasicInstruction)rawInstr;
                final Integer mask = basic.getOpcodeMask();
                final Integer match = basic.getOpcodeMatch();
                HashMap matchMap = maskMap.get(mask);
                if (matchMap == null) {
                    matchMap = new HashMap();
                    maskMap.put(mask, matchMap);
                    matchMaps.add(new MatchMap(mask, matchMap));
                }
                matchMap.put(match, basic);
            }
        }
        Collections.sort(matchMaps);
        this.opcodeMatchMaps = matchMaps;
    }
    
    public BasicInstruction findByBinaryCode(final int binaryInstr) {
        final ArrayList<MatchMap> matchMaps = this.opcodeMatchMaps;
        for (int i = 0; i < matchMaps.size(); ++i) {
            final MatchMap map = matchMaps.get(i);
            final BasicInstruction ret = map.find(binaryInstr);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }
    
    private void addPseudoInstructions() {
        InputStream is = null;
        BufferedReader in = null;
        try {
            is = this.getClass().getResourceAsStream("/PseudoOps.txt");
            in = new BufferedReader(new InputStreamReader(is));
        }
        catch (NullPointerException e) {
            System.out.println("Error: MIPS pseudo-instruction file PseudoOps.txt not found.");
            System.exit(0);
        }
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.startsWith("#") && !line.startsWith(" ") && line.length() > 0) {
                    String description = "";
                    final StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                    final String pseudoOp = tokenizer.nextToken();
                    String template = "";
                    String firstTemplate = null;
                    while (tokenizer.hasMoreTokens()) {
                        final String token = tokenizer.nextToken();
                        if (token.startsWith("#")) {
                            description = token.substring(1);
                            break;
                        }
                        if (token.startsWith("COMPACT")) {
                            firstTemplate = template;
                            template = "";
                        }
                        else {
                            template += token;
                            if (!tokenizer.hasMoreTokens()) {
                                continue;
                            }
                            template += "\n";
                        }
                    }
                    final ExtendedInstruction inst = (firstTemplate == null) ? new ExtendedInstruction(pseudoOp, template, description) : new ExtendedInstruction(pseudoOp, firstTemplate, template, description);
                    this.instructionList.add(inst);
                }
            }
            in.close();
        }
        catch (IOException ioe) {
            System.out.println("Internal Error: MIPS pseudo-instructions could not be loaded.");
            System.exit(0);
        }
        catch (Exception ioe2) {
            System.out.println("Error: Invalid MIPS pseudo-instruction specification.");
            System.exit(0);
        }
    }
    
    public ArrayList<Instruction> matchOperator(final String name) {
        ArrayList matchingInstructions = null;
        for (int i = 0; i < this.instructionList.size(); ++i) {
            if (this.instructionList.get(i).getName().equalsIgnoreCase(name)) {
                if (matchingInstructions == null) {
                    matchingInstructions = new ArrayList();
                }
                matchingInstructions.add(this.instructionList.get(i));
            }
        }
        return matchingInstructions;
    }
    
    public ArrayList prefixMatchOperator(final String name) {
        ArrayList matchingInstructions = null;
        if (name != null) {
            for (int i = 0; i < this.instructionList.size(); ++i) {
                if (this.instructionList.get(i).getName().toLowerCase().startsWith(name.toLowerCase())) {
                    if (matchingInstructions == null) {
                        matchingInstructions = new ArrayList();
                    }
                    matchingInstructions.add(this.instructionList.get(i));
                }
            }
        }
        return matchingInstructions;
    }
    
    private void findAndSimulateSyscall(final int number, final ProgramStatement statement) throws ProcessingException {
        final Syscall service = this.syscallLoader.findSyscall(number);
        if (service != null) {
            service.simulate(statement);
            return;
        }
        throw new ProcessingException(statement, "invalid or unimplemented syscall service: " + number + " ", 8);
    }
    
    private void processBranch(final int displacement) {
        if (Globals.getSettings().getDelayedBranchingEnabled()) {
            DelayedBranch.register(RegisterFile.getProgramCounter() + (displacement << 2));
        }
        else {
            RegisterFile.setProgramCounter(RegisterFile.getProgramCounter() + (displacement << 2));
        }
    }
    
    private void processJump(final int targetAddress) {
        if (Globals.getSettings().getDelayedBranchingEnabled()) {
            DelayedBranch.register(targetAddress);
        }
        else {
            RegisterFile.setProgramCounter(targetAddress);
        }
    }
    
    private void processReturnAddress(final int register) {
        RegisterFile.updateRegister(register, RegisterFile.getProgramCounter() + (Globals.getSettings().getDelayedBranchingEnabled() ? 4 : 0));
    }
    
    private static class MatchMap implements Comparable
    {
        private int mask;
        private int maskLength;
        private HashMap<Integer,BasicInstruction> matchMap;
        
        public MatchMap(final int mask, final HashMap matchMap) {
            this.mask = mask;
            this.matchMap = matchMap;
            int k = 0;
            for (int n = mask; n != 0; n &= n - 1) {
                ++k;
            }
            this.maskLength = k;
        }
        
        @Override
        public boolean equals(final Object o) {
            return o instanceof MatchMap && this.mask == ((MatchMap)o).mask;
        }
        
        @Override
        public int compareTo(final Object other) {
            final MatchMap o = (MatchMap)other;
            int d = o.maskLength - this.maskLength;
            if (d == 0) {
                d = this.mask - o.mask;
            }
            return d;
        }
        
        public BasicInstruction find(final int instr) {
            final int match = instr & this.mask;
            return this.matchMap.get(match);
        }
    }
}
