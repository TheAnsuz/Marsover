

package mars.mips.instructions;

import java.util.StringTokenizer;
import mars.assembler.Symbol;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;
import mars.Globals;
import mars.assembler.TokenList;
import mars.MIPSprogram;
import java.util.ArrayList;

public class ExtendedInstruction extends Instruction
{
    private ArrayList translationStrings;
    private ArrayList compactTranslationStrings;
    
    public ExtendedInstruction(final String example, final String translation, final String compactTranslation, final String description) {
        this.exampleFormat = example;
        this.description = description;
        this.mnemonic = this.extractOperator(example);
        this.createExampleTokenList();
        this.translationStrings = this.buildTranslationList(translation);
        this.compactTranslationStrings = this.buildTranslationList(compactTranslation);
    }
    
    public ExtendedInstruction(final String example, final String translation, final String description) {
        this.exampleFormat = example;
        this.description = description;
        this.mnemonic = this.extractOperator(example);
        this.createExampleTokenList();
        this.translationStrings = this.buildTranslationList(translation);
        this.compactTranslationStrings = null;
    }
    
    public ExtendedInstruction(final String example, final String translation) {
        this(example, translation, "");
    }
    
    @Override
    public int getInstructionLength() {
        return this.getInstructionLength(this.translationStrings);
    }
    
    public ArrayList getBasicIntructionTemplateList() {
        return this.translationStrings;
    }
    
    public int getCompactInstructionLength() {
        return this.getInstructionLength(this.compactTranslationStrings);
    }
    
    public boolean hasCompactTranslation() {
        return this.compactTranslationStrings != null;
    }
    
    public ArrayList getCompactBasicIntructionTemplateList() {
        return this.compactTranslationStrings;
    }
    
    public static String makeTemplateSubstitutions(final MIPSprogram program, final String template, final TokenList theTokenList) {
        String instruction = template;
        if (instruction.indexOf("DBNOP") >= 0) {
            return Globals.getSettings().getDelayedBranchingEnabled() ? "nop" : "";
        }
        for (int op = 1; op < theTokenList.size(); ++op) {
            instruction = substitute(instruction, "RG" + op, theTokenList.get(op).getValue());
            instruction = substitute(instruction, "OP" + op, theTokenList.get(op).getValue());
            int index;
            if ((index = instruction.indexOf("LH" + op + "P")) >= 0) {
                final String label = theTokenList.get(op).getValue();
                int addr = 0;
                final int add = instruction.charAt(index + 4) - '0';
                try {
                    addr = Binary.stringToInt(label) + add;
                }
                catch (NumberFormatException ex) {}
                final int extra = Binary.bitValue(addr, 15);
                instruction = substitute(instruction, "LH" + op + "P" + add, String.valueOf((addr >> 16) + extra));
            }
            if (instruction.indexOf("LH" + op) >= 0) {
                final String label = theTokenList.get(op).getValue();
                int addr = 0;
                try {
                    addr = Binary.stringToInt(label);
                }
                catch (NumberFormatException ex2) {}
                final int extra2 = Binary.bitValue(addr, 15);
                instruction = substitute(instruction, "LH" + op, String.valueOf((addr >> 16) + extra2));
            }
            if ((index = instruction.indexOf("LL" + op + "P")) >= 0) {
                final String label = theTokenList.get(op).getValue();
                int addr = 0;
                final int add = instruction.charAt(index + 4) - '0';
                try {
                    addr = Binary.stringToInt(label) + add;
                }
                catch (NumberFormatException ex3) {}
                instruction = substitute(instruction, "LL" + op + "P" + add, String.valueOf(addr << 16 >> 16));
            }
            if ((index = instruction.indexOf("LL" + op)) >= 0) {
                final String label = theTokenList.get(op).getValue();
                int addr = 0;
                try {
                    addr = Binary.stringToInt(label);
                }
                catch (NumberFormatException ex4) {}
                if (instruction.length() > index + 3 && instruction.charAt(index + 3) == 'U') {
                    instruction = substitute(instruction, "LL" + op + "U", String.valueOf(addr & 0xFFFF));
                }
                else {
                    instruction = substitute(instruction, "LL" + op, String.valueOf(addr << 16 >> 16));
                }
            }
            if ((index = instruction.indexOf("VHL" + op + "P")) >= 0) {
                final String value = theTokenList.get(op).getValue();
                int val = 0;
                final int add = instruction.charAt(index + 5) - '0';
                try {
                    val = Binary.stringToInt(value) + add;
                }
                catch (NumberFormatException ex5) {}
                instruction = substitute(instruction, "VHL" + op + "P" + add, String.valueOf(val >> 16));
            }
            if ((index = instruction.indexOf("VH" + op + "P")) >= 0) {
                final String value = theTokenList.get(op).getValue();
                int val = 0;
                final int add = instruction.charAt(index + 4) - '0';
                try {
                    val = Binary.stringToInt(value) + add;
                }
                catch (NumberFormatException ex6) {}
                final int extra = Binary.bitValue(val, 15);
                instruction = substitute(instruction, "VH" + op + "P" + add, String.valueOf((val >> 16) + extra));
            }
            if (instruction.indexOf("VH" + op) >= 0) {
                final String value = theTokenList.get(op).getValue();
                int val = 0;
                try {
                    val = Binary.stringToInt(value);
                }
                catch (NumberFormatException ex7) {}
                final int extra2 = Binary.bitValue(val, 15);
                instruction = substitute(instruction, "VH" + op, String.valueOf((val >> 16) + extra2));
            }
            if ((index = instruction.indexOf("VL" + op + "P")) >= 0) {
                final String value = theTokenList.get(op).getValue();
                int val = 0;
                final int add = instruction.charAt(index + 4) - '0';
                try {
                    val = Binary.stringToInt(value) + add;
                }
                catch (NumberFormatException ex8) {}
                if (instruction.length() > index + 5 && instruction.charAt(index + 5) == 'U') {
                    instruction = substitute(instruction, "VL" + op + "P" + add + "U", String.valueOf(val & 0xFFFF));
                }
                else {
                    instruction = substitute(instruction, "VL" + op + "P" + add, String.valueOf(val << 16 >> 16));
                }
            }
            if ((index = instruction.indexOf("VL" + op)) >= 0) {
                final String value = theTokenList.get(op).getValue();
                int val = 0;
                try {
                    val = Binary.stringToInt(value);
                }
                catch (NumberFormatException ex9) {}
                if (instruction.length() > index + 3 && instruction.charAt(index + 3) == 'U') {
                    instruction = substitute(instruction, "VL" + op + "U", String.valueOf(val & 0xFFFF));
                }
                else {
                    instruction = substitute(instruction, "VL" + op, String.valueOf(val << 16 >> 16));
                }
            }
            if (instruction.indexOf("VHL" + op) >= 0) {
                final String value = theTokenList.get(op).getValue();
                int val = 0;
                try {
                    val = Binary.stringToInt(value);
                }
                catch (NumberFormatException ex10) {}
                instruction = substitute(instruction, "VHL" + op, String.valueOf(val >> 16));
            }
        }
        if (instruction.indexOf("LHL") >= 0) {
            final String label2 = theTokenList.get(2).getValue();
            int addr2 = 0;
            try {
                addr2 = Binary.stringToInt(label2);
            }
            catch (NumberFormatException ex11) {}
            instruction = substitute(instruction, "LHL", String.valueOf(addr2 >> 16));
        }
        int index;
        if ((index = instruction.indexOf("LHPAP")) >= 0) {
            final String label2 = theTokenList.get(2).getValue();
            final String addend = theTokenList.get(4).getValue();
            int addr = 0;
            final int add = instruction.charAt(index + 5) - '0';
            try {
                addr = Binary.stringToInt(label2) + Binary.stringToInt(addend) + add;
            }
            catch (NumberFormatException ex12) {}
            final int extra = Binary.bitValue(addr, 15);
            instruction = substitute(instruction, "LHPAP" + add, String.valueOf((addr >> 16) + extra));
        }
        if (instruction.indexOf("LHPA") >= 0) {
            final String label2 = theTokenList.get(2).getValue();
            final String addend = theTokenList.get(4).getValue();
            int addr = 0;
            try {
                addr = Binary.stringToInt(label2) + Binary.stringToInt(addend);
            }
            catch (NumberFormatException ex13) {}
            final int extra2 = Binary.bitValue(addr, 15);
            instruction = substitute(instruction, "LHPA", String.valueOf((addr >> 16) + extra2));
        }
        if (instruction.indexOf("LHPN") >= 0) {
            final String label2 = theTokenList.get(2).getValue();
            final String addend = theTokenList.get(4).getValue();
            int addr = 0;
            try {
                addr = Binary.stringToInt(label2) + Binary.stringToInt(addend);
            }
            catch (NumberFormatException ex14) {}
            instruction = substitute(instruction, "LHPN", String.valueOf(addr >> 16));
        }
        if ((index = instruction.indexOf("LLPP")) >= 0) {
            final String label2 = theTokenList.get(2).getValue();
            final String addend = theTokenList.get(4).getValue();
            int addr = 0;
            final int add = instruction.charAt(index + 4) - '0';
            try {
                addr = Binary.stringToInt(label2) + Binary.stringToInt(addend) + add;
            }
            catch (NumberFormatException ex15) {}
            instruction = substitute(instruction, "LLPP" + add, String.valueOf(addr << 16 >> 16));
        }
        if ((index = instruction.indexOf("LLP")) >= 0) {
            final String label2 = theTokenList.get(2).getValue();
            final String addend = theTokenList.get(4).getValue();
            int addr = 0;
            try {
                addr = Binary.stringToInt(label2) + Binary.stringToInt(addend);
            }
            catch (NumberFormatException ex16) {}
            if (instruction.length() > index + 3 && instruction.charAt(index + 3) == 'U') {
                instruction = substitute(instruction, "LLPU", String.valueOf(addr & 0xFFFF));
            }
            else {
                instruction = substitute(instruction, "LLP", String.valueOf(addr << 16 >> 16));
            }
        }
        if ((index = instruction.indexOf("BROFF")) >= 0) {
            try {
                final String disabled = instruction.substring(index + 5, index + 6);
                final String enabled = instruction.substring(index + 6, index + 7);
                instruction = substitute(instruction, "BROFF" + disabled + enabled, Globals.getSettings().getDelayedBranchingEnabled() ? enabled : disabled);
            }
            catch (IndexOutOfBoundsException iooe) {
                instruction = substitute(instruction, "BROFF", "BAD_PSEUDO_OP_SPEC");
            }
        }
        if (instruction.indexOf("NR") >= 0) {
            for (int op = 1; op < theTokenList.size(); ++op) {
                final String token = theTokenList.get(op).getValue();
                try {
                    final int regNumber = RegisterFile.getUserRegister(token).getNumber();
                    if (regNumber >= 0) {
                        instruction = substitute(instruction, "NR" + op, "$" + (regNumber + 1));
                    }
                }
                catch (NullPointerException e) {
                    final int regNumber = Coprocessor1.getRegisterNumber(token);
                    if (regNumber >= 0) {
                        instruction = substitute(instruction, "NR" + op, "$f" + (regNumber + 1));
                    }
                }
            }
        }
        if (instruction.indexOf("S32") >= 0) {
            final String value2 = theTokenList.get(theTokenList.size() - 1).getValue();
            int val2 = 0;
            try {
                val2 = Binary.stringToInt(value2);
            }
            catch (NumberFormatException ex17) {}
            instruction = substitute(instruction, "S32", Integer.toString(32 - val2));
        }
        if (instruction.indexOf("LAB") >= 0) {
            final String label2 = theTokenList.get(theTokenList.size() - 1).getValue();
            final Symbol sym = program.getLocalSymbolTable().getSymbolGivenAddressLocalOrGlobal(label2);
            if (sym != null) {
                instruction = substituteFirst(instruction, "LAB", sym.getName());
            }
        }
        return instruction;
    }
    
    private static String substitute(final String original, final String find, final String replacement) {
        if (original.indexOf(find) < 0 || find.equals(replacement)) {
            return original;
        }
        String modified;
        int i;
        for (modified = original; (i = modified.indexOf(find)) >= 0; modified = modified.substring(0, i) + replacement + modified.substring(i + find.length())) {}
        return modified;
    }
    
    private static String substituteFirst(final String original, final String find, final String replacement) {
        if (original.indexOf(find) < 0 || find.equals(replacement)) {
            return original;
        }
        String modified = original;
        final int i;
        if ((i = modified.indexOf(find)) >= 0) {
            modified = modified.substring(0, i) + replacement + modified.substring(i + find.length());
        }
        return modified;
    }
    
    private ArrayList buildTranslationList(final String translation) {
        if (translation == null || translation.length() == 0) {
            return null;
        }
        final ArrayList translationList = new ArrayList();
        final StringTokenizer st = new StringTokenizer(translation, "\n");
        while (st.hasMoreTokens()) {
            translationList.add(st.nextToken());
        }
        return translationList;
    }
    
    private int getInstructionLength(final ArrayList translationList) {
        if (translationList == null || translationList.size() == 0) {
            return 0;
        }
        int instructionCount = 0;
        for (int i = 0; i < translationList.size(); ++i) {
            if (translationList.get(i).indexOf("DBNOP") < 0 || Globals.getSettings().getDelayedBranchingEnabled()) {
                ++instructionCount;
            }
        }
        return 4 * instructionCount;
    }
}
