

package mars.simulator;

import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.RegisterFile;
import mars.Globals;

public class BackStepper
{
    private static final int MEMORY_RESTORE_RAW_WORD = 0;
    private static final int MEMORY_RESTORE_WORD = 1;
    private static final int MEMORY_RESTORE_HALF = 2;
    private static final int MEMORY_RESTORE_BYTE = 3;
    private static final int REGISTER_RESTORE = 4;
    private static final int PC_RESTORE = 5;
    private static final int COPROC0_REGISTER_RESTORE = 6;
    private static final int COPROC1_REGISTER_RESTORE = 7;
    private static final int COPROC1_CONDITION_CLEAR = 8;
    private static final int COPROC1_CONDITION_SET = 9;
    private static final int DO_NOTHING = 10;
    private static final int NOT_PC_VALUE = -1;
    private boolean engaged;
    private BackstepStack backSteps;
    
    public BackStepper() {
        this.engaged = true;
        this.backSteps = new BackstepStack(Globals.maximumBacksteps);
    }
    
    public boolean enabled() {
        return this.engaged;
    }
    
    public void setEnabled(final boolean state) {
        this.engaged = state;
    }
    
    public boolean empty() {
        return this.backSteps.empty();
    }
    
    public boolean inDelaySlot() {
        return !this.empty() && this.backSteps.peek().inDelaySlot;
    }
    
    public void backStep() {
        if (this.engaged && !this.backSteps.empty()) {
            final ProgramStatement statement = this.backSteps.peek().ps;
            this.engaged = false;
            do {
                final BackStep step = this.backSteps.pop();
                if (step.pc != -1) {
                    RegisterFile.setProgramCounter(step.pc);
                }
                try {
                    switch (step.action) {
                        case 0: {
                            Globals.memory.setRawWord(step.param1, step.param2);
                            continue;
                        }
                        case 1: {
                            Globals.memory.setWord(step.param1, step.param2);
                            continue;
                        }
                        case 2: {
                            Globals.memory.setHalf(step.param1, step.param2);
                            continue;
                        }
                        case 3: {
                            Globals.memory.setByte(step.param1, step.param2);
                            continue;
                        }
                        case 4: {
                            RegisterFile.updateRegister(step.param1, step.param2);
                            continue;
                        }
                        case 5: {
                            RegisterFile.setProgramCounter(step.param1);
                            continue;
                        }
                        case 6: {
                            Coprocessor0.updateRegister(step.param1, step.param2);
                            continue;
                        }
                        case 7: {
                            Coprocessor1.updateRegister(step.param1, step.param2);
                            continue;
                        }
                        case 8: {
                            Coprocessor1.clearConditionFlag(step.param1);
                            continue;
                        }
                        case 9: {
                            Coprocessor1.setConditionFlag(step.param1);
                            continue;
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Internal MARS error: address exception while back-stepping.");
                    System.exit(0);
                }
            } while (!this.backSteps.empty() && statement == this.backSteps.peek().ps);
            this.engaged = true;
        }
    }
    
    private int pc() {
        return RegisterFile.getProgramCounter() - 4;
    }
    
    public int addMemoryRestoreRawWord(final int address, final int value) {
        this.backSteps.push(0, this.pc(), address, value);
        return value;
    }
    
    public int addMemoryRestoreWord(final int address, final int value) {
        this.backSteps.push(1, this.pc(), address, value);
        return value;
    }
    
    public int addMemoryRestoreHalf(final int address, final int value) {
        this.backSteps.push(2, this.pc(), address, value);
        return value;
    }
    
    public int addMemoryRestoreByte(final int address, final int value) {
        this.backSteps.push(3, this.pc(), address, value);
        return value;
    }
    
    public int addRegisterFileRestore(final int register, final int value) {
        this.backSteps.push(4, this.pc(), register, value);
        return value;
    }
    
    public int addPCRestore(int value) {
        value -= 4;
        this.backSteps.push(5, value, value);
        return value;
    }
    
    public int addCoprocessor0Restore(final int register, final int value) {
        this.backSteps.push(6, this.pc(), register, value);
        return value;
    }
    
    public int addCoprocessor1Restore(final int register, final int value) {
        this.backSteps.push(7, this.pc(), register, value);
        return value;
    }
    
    public int addConditionFlagSet(final int flag) {
        this.backSteps.push(9, this.pc(), flag);
        return flag;
    }
    
    public int addConditionFlagClear(final int flag) {
        this.backSteps.push(8, this.pc(), flag);
        return flag;
    }
    
    public int addDoNothing(final int pc) {
        if (this.backSteps.empty() || this.backSteps.peek().pc != pc) {
            this.backSteps.push(10, pc);
        }
        return 0;
    }
    
    private class BackStep
    {
        private int action;
        private int pc;
        private ProgramStatement ps;
        private int param1;
        private int param2;
        private boolean inDelaySlot;
        
        private void assign(final int act, final int programCounter, final int parm1, final int parm2) {
            this.action = act;
            this.pc = programCounter;
            try {
                this.ps = Globals.memory.getStatementNoNotify(programCounter);
            }
            catch (Exception e) {
                this.ps = null;
                this.pc = -1;
            }
            this.param1 = parm1;
            this.param2 = parm2;
            this.inDelaySlot = Simulator.inDelaySlot();
        }
    }
    
    private class BackstepStack
    {
        private int capacity;
        private int size;
        private int top;
        private BackStep[] stack;
        
        private BackstepStack(final int capacity) {
            this.capacity = capacity;
            this.size = 0;
            this.top = -1;
            this.stack = new BackStep[capacity];
            for (int i = 0; i < capacity; ++i) {
                this.stack[i] = new BackStep();
            }
        }
        
        private synchronized boolean empty() {
            return this.size == 0;
        }
        
        private synchronized void push(final int act, final int programCounter, final int parm1, final int parm2) {
            if (this.size == 0) {
                this.top = 0;
                ++this.size;
            }
            else if (this.size < this.capacity) {
                this.top = (this.top + 1) % this.capacity;
                ++this.size;
            }
            else {
                this.top = (this.top + 1) % this.capacity;
            }
            this.stack[this.top].assign(act, programCounter, parm1, parm2);
        }
        
        private synchronized void push(final int act, final int programCounter, final int parm1) {
            this.push(act, programCounter, parm1, 0);
        }
        
        private synchronized void push(final int act, final int programCounter) {
            this.push(act, programCounter, 0, 0);
        }
        
        private synchronized BackStep pop() {
            final BackStep bs = this.stack[this.top];
            if (this.size == 1) {
                this.top = -1;
            }
            else {
                this.top = (this.top + this.capacity - 1) % this.capacity;
            }
            --this.size;
            return bs;
        }
        
        private synchronized BackStep peek() {
            return this.stack[this.top];
        }
    }
}
