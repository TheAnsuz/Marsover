

package mars.pipeline.tomasulo;

import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import mars.mips.instructions.Instruction;
import mars.util.Binary;
import mars.mips.hardware.AddressErrorException;
import mars.Globals;
import mars.mips.instructions.BasicInstruction;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.instructions.SimulationCode;
import mars.mips.instructions.BasicInstructionFormat;
import java.util.ArrayList;

public class InstructionSetTomasulo
{
    private static InstructionSetTomasulo instructionSet;
    private ArrayList<Instruction> instructionList;
    private ArrayList<MatchMap> opcodeMatchMaps;
    private static int op1;
    private static int op2;
    private static int result;
    private static int HI;
    private static int LOW;
    
    private InstructionSetTomasulo() {
        this.instructionList = new ArrayList();
    }
    
    public static InstructionSetTomasulo getInstance() {
        if (InstructionSetTomasulo.instructionSet == null) {
            (InstructionSetTomasulo.instructionSet = new InstructionSetTomasulo()).populate();
        }
        return InstructionSetTomasulo.instructionSet;
    }
    
    public ArrayList getInstructionList() {
        return this.instructionList;
    }
    
    private void populate() {
        this.instructionList.add(new BasicInstruction("nop", "Null operation : machine code is all zeroes", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 00000 00000 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
            }
        }));
        this.instructionList.add(new BasicInstruction("add $t1,$t2,$t3", "Addition with overflow : set $t1 to ($t2 plus $t3)", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int add1 = InstructionSetTomasulo.op1;
                final int add2 = InstructionSetTomasulo.op2;
                final int sum = add1 + add2;
                if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                    throw new ProcessingException(statement, "arithmetic overflow", 12);
                }
                InstructionSetTomasulo.result = sum;
            }
        }));
        this.instructionList.add(new BasicInstruction("sub $t1,$t2,$t3", "Subtraction with overflow : set $t1 to ($t2 minus $t3)", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int sub1 = InstructionSetTomasulo.op1;
                final int sub2 = InstructionSetTomasulo.op2;
                final int dif = sub1 - sub2;
                if ((sub1 >= 0 && sub2 < 0 && dif < 0) || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                    throw new ProcessingException(statement, "arithmetic overflow", 12);
                }
                InstructionSetTomasulo.result = dif;
            }
        }));
        this.instructionList.add(new BasicInstruction("addi $t1,$t2,-100", "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)", BasicInstructionFormat.I_FORMAT, "001000 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int add1 = InstructionSetTomasulo.op1;
                final int add2 = InstructionSetTomasulo.op2;
                final int sum = add1 + add2;
                if ((add1 >= 0 && add2 >= 0 && sum < 0) || (add1 < 0 && add2 < 0 && sum >= 0)) {
                    throw new ProcessingException(statement, "arithmetic overflow", 12);
                }
                InstructionSetTomasulo.result = sum;
            }
        }));
        this.instructionList.add(new BasicInstruction("addu $t1,$t2,$t3", "Addition unsigned without overflow : set $t1 to ($t2 plus $t3), no overflow", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 + InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("subu $t1,$t2,$t3", "Subtraction unsigned without overflow : set $t1 to ($t2 minus $t3), no overflow", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 - InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("addiu $t1,$t2,-100", "Addition immediate unsigned without overflow : set $t1 to ($t2 plus signed 16-bit immediate), no overflow", BasicInstructionFormat.I_FORMAT, "001001 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 + InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("mult $t1,$t2", "Multiplication : Set hi to high-order 32 bits, lo to low-order 32 bits of the product of $t1 and $t2 (use mfhi to access hi, mflo to access lo)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final long product = InstructionSetTomasulo.op1 * (long)InstructionSetTomasulo.op2;
                InstructionSetTomasulo.HI = (int)(product >> 32);
                InstructionSetTomasulo.LOW = (int)(product << 32 >> 32);
            }
        }));
        this.instructionList.add(new BasicInstruction("multu $t1,$t2", "Multiplication unsigned : Set HI to high-order 32 bits, LO to low-order 32 bits of the product of unsigned $t1 and $t2 (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final long product = ((long)InstructionSetTomasulo.op1 << 32 >>> 32) * ((long)InstructionSetTomasulo.op2 << 32 >>> 32);
                InstructionSetTomasulo.HI = (int)(product >> 32);
                InstructionSetTomasulo.LOW = (int)(product << 32 >> 32);
            }
        }));
        this.instructionList.add(new BasicInstruction("mul $t1,$t2,$t3", "Multiplication without overflow  : Set HI to high-order 32 bits, LO and $t1 to low-order 32 bits of the product of $t2 and $t3 (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "011100 sssss ttttt fffff 00000 000010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final long product = InstructionSetTomasulo.op1 * (long)InstructionSetTomasulo.op2;
                InstructionSetTomasulo.result = (int)(product << 32 >> 32);
                InstructionSetTomasulo.HI = (int)(product >> 32);
                InstructionSetTomasulo.LOW = (int)(product << 32 >> 32);
            }
        }));
        this.instructionList.add(new BasicInstruction("div $t1,$t2", "Division with overflow : Divide $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                if (InstructionSetTomasulo.op2 == 0) {
                    return;
                }
                InstructionSetTomasulo.HI = InstructionSetTomasulo.op1 % InstructionSetTomasulo.op2;
                InstructionSetTomasulo.LOW = InstructionSetTomasulo.op1 / InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("divu $t1,$t2", "Division unsigned without overflow : Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)", BasicInstructionFormat.R_FORMAT, "000000 fffff sssss 00000 00000 011011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                if (InstructionSetTomasulo.op1 == 0) {
                    return;
                }
                final long oper1 = (long)InstructionSetTomasulo.op1 << 32 >>> 32;
                final long oper2 = (long)InstructionSetTomasulo.op2 << 32 >>> 32;
                InstructionSetTomasulo.HI = (int)(oper1 % oper2 << 32 >> 32);
                InstructionSetTomasulo.LOW = (int)(oper1 / oper2 << 32 >> 32);
            }
        }));
        this.instructionList.add(new BasicInstruction("mfhi $t1", "Move from HI register : Set $t1 to contents of HI (see multiply and divide operations)", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 fffff 00000 010000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.HI;
            }
        }));
        this.instructionList.add(new BasicInstruction("mflo $t1", "Move from LO register : Set $t1 to contents of LO (see multiply and divide operations)", BasicInstructionFormat.R_FORMAT, "000000 00000 00000 fffff 00000 010010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.LOW;
            }
        }));
        this.instructionList.add(new BasicInstruction("and $t1,$t2,$t3", "Bitwise AND : Set $t1 to bitwise AND of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = (InstructionSetTomasulo.op1 & InstructionSetTomasulo.op2);
            }
        }));
        this.instructionList.add(new BasicInstruction("or $t1,$t2,$t3", "Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100101", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = (InstructionSetTomasulo.op1 | InstructionSetTomasulo.op2);
            }
        }));
        this.instructionList.add(new BasicInstruction("andi $t1,$t2,100", "Bitwise AND immediate : Set $t1 to bitwise AND of $t2 and zero-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "001100 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = (InstructionSetTomasulo.op1 & (InstructionSetTomasulo.op2 & 0xFFFF));
            }
        }));
        this.instructionList.add(new BasicInstruction("ori $t1,$t2,100", "Bitwise OR immediate : Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "001101 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = (InstructionSetTomasulo.op1 | (InstructionSetTomasulo.op2 & 0xFFFF));
            }
        }));
        this.instructionList.add(new BasicInstruction("nor $t1,$t2,$t3", "Bitwise NOR : Set $t1 to bitwise NOR of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = ~(InstructionSetTomasulo.op1 | InstructionSetTomasulo.op2);
            }
        }));
        this.instructionList.add(new BasicInstruction("xor $t1,$t2,$t3", "Bitwise XOR (exclusive OR) : Set $t1 to bitwise XOR of $t2 and $t3", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = (InstructionSetTomasulo.op1 ^ InstructionSetTomasulo.op2);
            }
        }));
        this.instructionList.add(new BasicInstruction("xori $t1,$t2,100", "Bitwise XOR immediate : Set $t1 to bitwise XOR of $t2 and zero-extended 16-bit immediate", BasicInstructionFormat.I_FORMAT, "001110 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = (InstructionSetTomasulo.op1 ^ (InstructionSetTomasulo.op2 & 0xFFFF));
            }
        }));
        this.instructionList.add(new BasicInstruction("sll $t1,$t2,10", "Shift left logical : Set $t1 to result of shifting $t2 left by number of bits specified by immediate", BasicInstructionFormat.R_FORMAT, "000000 00000 sssss fffff ttttt 000000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 << InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("sllv $t1,$t2,$t3", "Shift left logical variable : Set $t1 to result of shifting $t2 left by number of bits specified by value in low-order 5 bits of $t3", BasicInstructionFormat.R_FORMAT, "000000 ttttt sssss fffff 00000 000100", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 << (InstructionSetTomasulo.op2 & 0x1F);
            }
        }));
        this.instructionList.add(new BasicInstruction("srl $t1,$t2,10", "Shift right logical : Set $t1 to result of shifting $t2 right by number of bits specified by immediate", BasicInstructionFormat.R_FORMAT, "000000 00000 sssss fffff ttttt 000010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 >>> InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("sra $t1,$t2,10", "Shift right arithmetic : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by immediate", BasicInstructionFormat.R_FORMAT, "000000 00000 sssss fffff ttttt 000011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 >> InstructionSetTomasulo.op2;
            }
        }));
        this.instructionList.add(new BasicInstruction("srav $t1,$t2,$t3", "Shift right arithmetic variable : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3", BasicInstructionFormat.R_FORMAT, "000000 ttttt sssss fffff 00000 000111", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 >> (InstructionSetTomasulo.op2 & 0x1F);
            }
        }));
        this.instructionList.add(new BasicInstruction("srlv $t1,$t2,$t3", "Shift right logical variable : Set $t1 to result of shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3", BasicInstructionFormat.R_FORMAT, "000000 ttttt sssss fffff 00000 000110", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op1 >>> (InstructionSetTomasulo.op2 & 0x1F);
            }
        }));
        this.instructionList.add(new BasicInstruction("lw $t1,-100($t2)", "Load word : Set $t1 to contents of effective memory word address", BasicInstructionFormat.I_FORMAT, "100011 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                try {
                    InstructionSetTomasulo.result = Globals.memory.getWord(InstructionSetTomasulo.op2 + InstructionSetTomasulo.op1);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lui $t1,100", "Load upper immediate : Set high-order 16 bits of $t1 to 16-bit immediate and low-order 16 bits to 0", BasicInstructionFormat.I_FORMAT, "001111 00000 fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = InstructionSetTomasulo.op2 << 16;
            }
        }));
        this.instructionList.add(new BasicInstruction("slt $t1,$t2,$t3", "Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 101010", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = ((InstructionSetTomasulo.op1 < InstructionSetTomasulo.op2) ? 1 : 0);
            }
        }));
        this.instructionList.add(new BasicInstruction("sltu $t1,$t2,$t3", "Set less than unsigned : If $t2 is less than $t3 using unsigned comparision, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 101011", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int first = InstructionSetTomasulo.op1;
                final int second = InstructionSetTomasulo.op2;
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    InstructionSetTomasulo.result = ((first < second) ? 1 : 0);
                }
                else {
                    InstructionSetTomasulo.result = ((first >= 0) ? 1 : 0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("slti $t1,$t2,-100", "Set less than immediate : If $t2 is less than sign-extended 16-bit immediate, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.I_FORMAT, "001010 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                InstructionSetTomasulo.result = ((InstructionSetTomasulo.op1 < InstructionSetTomasulo.op2 << 16 >> 16) ? 1 : 0);
            }
        }));
        this.instructionList.add(new BasicInstruction("sltiu $t1,$t2,-100", "Set less than immediate unsigned : If $t2 is less than  sign-extended 16-bit immediate using unsigned comparison, then set $t1 to 1 else set $t1 to 0", BasicInstructionFormat.I_FORMAT, "001011 sssss fffff tttttttttttttttt", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int first = InstructionSetTomasulo.op1;
                final int second = InstructionSetTomasulo.op2 << 16 >> 16;
                if ((first >= 0 && second >= 0) || (first < 0 && second < 0)) {
                    InstructionSetTomasulo.result = ((first < second) ? 1 : 0);
                }
                else {
                    InstructionSetTomasulo.result = ((first >= 0) ? 1 : 0);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lb $t1,-100($t2)", "Load byte : Set $t1 to sign-extended 8-bit value from effective memory byte address", BasicInstructionFormat.I_FORMAT, "100000 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                try {
                    InstructionSetTomasulo.result = Globals.memory.getByte(InstructionSetTomasulo.op2 + (InstructionSetTomasulo.op1 << 16 >> 16)) << 24 >> 24;
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lh $t1,-100($t2)", "Load halfword : Set $t1 to sign-extended 16-bit value from effective memory halfword address", BasicInstructionFormat.I_FORMAT, "100001 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                try {
                    InstructionSetTomasulo.result = Globals.memory.getHalf(InstructionSetTomasulo.op2 + (InstructionSetTomasulo.op1 << 16 >> 16)) << 16 >> 16;
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lhu $t1,-100($t2)", "Load halfword unsigned : Set $t1 to zero-extended 16-bit value from effective memory halfword address", BasicInstructionFormat.I_FORMAT, "100101 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                try {
                    InstructionSetTomasulo.result = (Globals.memory.getHalf(InstructionSetTomasulo.op2 + (InstructionSetTomasulo.op1 << 16 >> 16)) & 0xFFFF);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("lbu $t1,-100($t2)", "Load byte unsigned : Set $t1 to zero-extended 8-bit value from effective memory byte address", BasicInstructionFormat.I_FORMAT, "100100 ttttt fffff ssssssssssssssss", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                try {
                    InstructionSetTomasulo.result = (Globals.memory.getByte(InstructionSetTomasulo.op2 + (InstructionSetTomasulo.op1 << 16 >> 16)) & 0xFF);
                }
                catch (AddressErrorException e) {
                    throw new ProcessingException(statement, e);
                }
            }
        }));
        this.instructionList.add(new BasicInstruction("clo $t1,$t2", "Count number of leading ones : Set $t1 to the count of leading one bits in $t2 starting at most significant bit position", BasicInstructionFormat.R_FORMAT, "011100 sssss 00000 fffff 00000 100001", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int value = InstructionSetTomasulo.op1;
                int leadingOnes = 0;
                for (int bitPosition = 31; Binary.bitValue(value, bitPosition) == 1 && bitPosition >= 0; --bitPosition) {
                    ++leadingOnes;
                }
                InstructionSetTomasulo.result = leadingOnes;
            }
        }));
        this.instructionList.add(new BasicInstruction("clz $t1,$t2", "Count number of leading zeroes : Set $t1 to the count of leading zero bits in $t2 starting at most significant bit positio", BasicInstructionFormat.R_FORMAT, "011100 sssss 00000 fffff 00000 100000", new SimulationCode() {
            @Override
            public void simulate(final ProgramStatement statement) throws ProcessingException {
                final int value = InstructionSetTomasulo.op1;
                int leadingZeros = 0;
                for (int bitPosition = 31; Binary.bitValue(value, bitPosition) == 0 && bitPosition >= 0; --bitPosition) {
                    ++leadingZeros;
                }
                InstructionSetTomasulo.result = leadingZeros;
            }
        }));
        for (int i = 0; i < this.instructionList.size(); ++i) {
            final Instruction inst = this.instructionList.get(i);
            inst.createExampleTokenList();
        }
        final HashMap<Integer,HashMap<Integer,BasicInstruction>> maskMap = new HashMap();
        final ArrayList matchMaps = new ArrayList();
        for (int j = 0; j < this.instructionList.size(); ++j) {
            final Instruction rawInstr = this.instructionList.get(j);
            if (rawInstr instanceof BasicInstruction) {
                final BasicInstruction basic = (BasicInstruction)rawInstr;
                final Integer mask = basic.getOpcodeMask();
                final Integer match = basic.getOpcodeMatch();
                HashMap<Integer,BasicInstruction> matchMap = maskMap.get(mask);
                if (matchMap == null) {
                    matchMap = new HashMap();
                    maskMap.put(mask, matchMap);
                    matchMaps.add(new MatchMap(mask, matchMap));
                }
                matchMap.put(match, basic);
            }
        }
        Collections.sort((List<Comparable>)matchMaps);
        this.opcodeMatchMaps = matchMaps;
    }
    
    public BasicInstruction findByBinaryCode(final int binaryInstr) {
        for (int i = 0; i < this.opcodeMatchMaps.size(); ++i) {
            final MatchMap map = this.opcodeMatchMaps.get(i);
            final BasicInstruction ret = map.find(binaryInstr);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }
    
    public ArrayList matchOperator(final String name) {
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
    
    public int getResult() {
        return InstructionSetTomasulo.result;
    }
    
    public int getHI() {
        return InstructionSetTomasulo.HI;
    }
    
    public int getLOW() {
        return InstructionSetTomasulo.LOW;
    }
    
    public void setOp1(final int op1) {
        InstructionSetTomasulo.op1 = op1;
    }
    
    public void setOp2(final int op2) {
        InstructionSetTomasulo.op2 = op2;
    }
    
    static {
        InstructionSetTomasulo.instructionSet = null;
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
