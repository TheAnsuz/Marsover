

package mars.simulator;

import mars.mips.hardware.Register;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.Globals;
import mars.mips.hardware.Memory;
import java.util.StringTokenizer;
import java.util.ArrayList;

public class ProgramArgumentList
{
    ArrayList programArgumentList;
    
    public ProgramArgumentList(final String args) {
        final StringTokenizer st = new StringTokenizer(args);
        this.programArgumentList = new ArrayList(st.countTokens());
        while (st.hasMoreTokens()) {
            this.programArgumentList.add(st.nextToken());
        }
    }
    
    public ProgramArgumentList(final String[] list) {
        this(list, 0);
    }
    
    public ProgramArgumentList(final String[] list, final int startPosition) {
        this.programArgumentList = new ArrayList(list.length - startPosition);
        for (int i = startPosition; i < list.length; ++i) {
            this.programArgumentList.add(list[i]);
        }
    }
    
    public ProgramArgumentList(final ArrayList list) {
        this(list, 0);
    }
    
    public ProgramArgumentList(final ArrayList list, final int startPosition) {
        if (list == null || list.size() < startPosition) {
            this.programArgumentList = new ArrayList(0);
        }
        else {
            this.programArgumentList = new ArrayList(list.size() - startPosition);
            for (int i = startPosition; i < list.size(); ++i) {
                this.programArgumentList.add(list.get(i));
            }
        }
    }
    
    public void storeProgramArguments() {
        if (this.programArgumentList == null || this.programArgumentList.size() == 0) {
            return;
        }
        int highAddress = Memory.stackBaseAddress;
        final int[] argStartAddress = new int[this.programArgumentList.size()];
        try {
            for (int i = 0; i < this.programArgumentList.size(); ++i) {
                final String programArgument = this.programArgumentList.get(i);
                Globals.memory.set(highAddress, 0, 1);
                --highAddress;
                for (int j = programArgument.length() - 1; j >= 0; --j) {
                    Globals.memory.set(highAddress, programArgument.charAt(j), 1);
                    --highAddress;
                }
                argStartAddress[i] = highAddress + 1;
            }
            int stackAddress = Memory.stackPointer;
            if (highAddress < Memory.stackPointer) {
                stackAddress = highAddress - highAddress % 4 - 4;
            }
            Globals.memory.set(stackAddress, 0, 4);
            stackAddress -= 4;
            for (int k = argStartAddress.length - 1; k >= 0; --k) {
                Globals.memory.set(stackAddress, argStartAddress[k], 4);
                stackAddress -= 4;
            }
            Globals.memory.set(stackAddress, argStartAddress.length, 4);
            stackAddress -= 4;
            final Register[] registers = RegisterFile.getRegisters();
            RegisterFile.getUserRegister("$sp").setValue(stackAddress + 4);
            RegisterFile.getUserRegister("$a0").setValue(argStartAddress.length);
            RegisterFile.getUserRegister("$a1").setValue(stackAddress + 4 + 4);
        }
        catch (AddressErrorException aee) {
            System.out.println("Internal Error: Memory write error occurred while storing program arguments! " + aee);
            System.exit(0);
        }
    }
}
