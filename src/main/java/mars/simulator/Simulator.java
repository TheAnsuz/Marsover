

package mars.simulator;

import mars.venus.RunGoAction;
import mars.venus.RunStepAction;
import mars.ProgramStatement;
import javax.swing.SwingUtilities;
import mars.mips.hardware.Memory;
import mars.mips.instructions.BasicInstruction;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Coprocessor0;
import mars.ErrorMessage;
import mars.util.Binary;
import mars.ErrorList;
import mars.mips.hardware.RegisterFile;
import java.util.Arrays;
import mars.venus.RunSpeedPanel;
import java.util.Iterator;
import mars.ProcessingException;
import mars.util.SystemIO;
import javax.swing.AbstractAction;
import mars.MIPSprogram;
import mars.Globals;
import java.util.ArrayList;
import java.util.Observable;

public class Simulator extends Observable
{
    private SimThread simulatorThread;
    private static Simulator simulator;
    private static Runnable interactiveGUIUpdater;
    public static final int NO_DEVICE = 0;
    public static volatile int externalInterruptingDevice;
    public static final int BREAKPOINT = 1;
    public static final int EXCEPTION = 2;
    public static final int MAX_STEPS = 3;
    public static final int NORMAL_TERMINATION = 4;
    public static final int CLIFF_TERMINATION = 5;
    public static final int PAUSE_OR_STOP = 6;
    private ArrayList<StopListener> stopListeners;
    
    public static Simulator getInstance() {
        if (Simulator.simulator == null) {
            Simulator.simulator = new Simulator();
        }
        return Simulator.simulator;
    }
    
    private Simulator() {
        this.stopListeners = new ArrayList<StopListener>(1);
        this.simulatorThread = null;
        if (Globals.getGui() != null) {
            Simulator.interactiveGUIUpdater = new UpdateGUI();
        }
    }
    
    public static boolean inDelaySlot() {
        return DelayedBranch.isTriggered();
    }
    
    public boolean simulate(final MIPSprogram p, final int pc, final int maxSteps, final int[] breakPoints, final AbstractAction actor) throws ProcessingException {
        (this.simulatorThread = new SimThread(p, pc, maxSteps, breakPoints, actor)).start();
        if (actor != null) {
            return true;
        }
        final Object dun = this.simulatorThread.get();
        final ProcessingException pe = this.simulatorThread.pe;
        final boolean done = this.simulatorThread.done;
        if (done) {
            SystemIO.resetFiles();
        }
        this.simulatorThread = null;
        if (pe != null) {
            throw pe;
        }
        return done;
    }
    
    public void stopExecution(final AbstractAction actor) {
        if (this.simulatorThread != null) {
            this.simulatorThread.setStop(actor);
            for (final StopListener l : this.stopListeners) {
                l.stopped(this);
            }
            this.simulatorThread = null;
        }
    }
    
    public void addStopListener(final StopListener l) {
        this.stopListeners.add(l);
    }
    
    public void removeStopListener(final StopListener l) {
        this.stopListeners.remove(l);
    }
    
    private void notifyObserversOfExecutionStart(final int maxSteps, final int programCounter) {
        this.setChanged();
        this.notifyObservers(new SimulatorNotice(0, maxSteps, RunSpeedPanel.getInstance().getRunSpeed(), programCounter));
    }
    
    private void notifyObserversOfExecutionStop(final int maxSteps, final int programCounter) {
        this.setChanged();
        this.notifyObservers(new SimulatorNotice(1, maxSteps, RunSpeedPanel.getInstance().getRunSpeed(), programCounter));
    }
    
    static {
        Simulator.simulator = null;
        Simulator.interactiveGUIUpdater = null;
        Simulator.externalInterruptingDevice = 0;
    }
    
    class SimThread extends SwingWorker
    {
        private MIPSprogram p;
        private int pc;
        private int maxSteps;
        private int[] breakPoints;
        private boolean done;
        private ProcessingException pe;
        private volatile boolean stop;
        private volatile AbstractAction stopper;
        private AbstractAction starter;
        private int constructReturnReason;
        
        SimThread(final MIPSprogram p, final int pc, final int maxSteps, final int[] breakPoints, final AbstractAction starter) {
            super(Globals.getGui() != null);
            this.stop = false;
            this.p = p;
            this.pc = pc;
            this.maxSteps = maxSteps;
            this.breakPoints = breakPoints;
            this.done = false;
            this.pe = null;
            this.starter = starter;
            this.stopper = null;
        }
        
        public void setStop(final AbstractAction actor) {
            this.stop = true;
            this.stopper = actor;
        }
        
        @Override
        public Object construct() {
            Thread.currentThread().setPriority(4);
            Thread.yield();
            if (this.breakPoints == null || this.breakPoints.length == 0) {
                this.breakPoints = null;
            }
            else {
                Arrays.sort(this.breakPoints);
            }
            Simulator.getInstance().notifyObserversOfExecutionStart(this.maxSteps, this.pc);
            RegisterFile.initializeProgramCounter(this.pc);
            ProgramStatement statement = null;
            try {
                statement = Globals.memory.getStatement(RegisterFile.getProgramCounter());
            }
            catch (AddressErrorException e) {
                final ErrorList el = new ErrorList();
                el.add(new ErrorMessage((MIPSprogram)null, 0, 0, "invalid program counter value: " + Binary.intToHexString(RegisterFile.getProgramCounter())));
                this.pe = new ProcessingException(el, e);
                Coprocessor0.updateRegister(14, RegisterFile.getProgramCounter());
                this.constructReturnReason = 2;
                this.done = true;
                SystemIO.resetFiles();
                Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, this.pc);
                return new Boolean(this.done);
            }
            int steps = 0;
            int pc = 0;
            while (statement != null) {
                pc = RegisterFile.getProgramCounter();
                RegisterFile.incrementPC();
                synchronized (Globals.memoryAndRegistersLock) {
                    try {
                        if (Simulator.externalInterruptingDevice != 0) {
                            final int deviceInterruptCode = Simulator.externalInterruptingDevice;
                            Simulator.externalInterruptingDevice = 0;
                            throw new ProcessingException(statement, "External Interrupt", deviceInterruptCode);
                        }
                        final BasicInstruction instruction = (BasicInstruction)statement.getInstruction();
                        if (instruction == null) {
                            throw new ProcessingException(statement, "undefined instruction (" + Binary.intToHexString(statement.getBinaryStatement()) + ")", 10);
                        }
                        instruction.getSimulationCode().simulate(statement);
                        if (Globals.getSettings().getBackSteppingEnabled()) {
                            Globals.program.getBackStepper().addDoNothing(pc);
                        }
                    }
                    catch (ProcessingException pe) {
                        if (pe.errors() == null) {
                            this.constructReturnReason = 4;
                            this.done = true;
                            SystemIO.resetFiles();
                            Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
                            return new Boolean(this.done);
                        }
                        ProgramStatement exceptionHandler = null;
                        try {
                            exceptionHandler = Globals.memory.getStatement(Memory.exceptionHandlerAddress);
                        }
                        catch (AddressErrorException ex) {}
                        if (exceptionHandler == null) {
                            this.constructReturnReason = 2;
                            this.pe = pe;
                            this.done = true;
                            SystemIO.resetFiles();
                            Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
                            return new Boolean(this.done);
                        }
                        RegisterFile.setProgramCounter(Memory.exceptionHandlerAddress);
                    }
                }
                if (DelayedBranch.isTriggered()) {
                    RegisterFile.setProgramCounter(DelayedBranch.getBranchTargetAddress());
                    DelayedBranch.clear();
                }
                else if (DelayedBranch.isRegistered()) {
                    DelayedBranch.trigger();
                }
                if (this.stop) {
                    this.constructReturnReason = 6;
                    this.done = false;
                    Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
                    return new Boolean(this.done);
                }
                if (this.breakPoints != null && Arrays.binarySearch(this.breakPoints, RegisterFile.getProgramCounter()) >= 0) {
                    this.constructReturnReason = 1;
                    this.done = false;
                    Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
                    return new Boolean(this.done);
                }
                if (this.maxSteps > 0 && ++steps >= this.maxSteps) {
                    this.constructReturnReason = 3;
                    this.done = false;
                    Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
                    return new Boolean(this.done);
                }
                if (Simulator.interactiveGUIUpdater != null && this.maxSteps != 1 && RunSpeedPanel.getInstance().getRunSpeed() < 40.0) {
                    SwingUtilities.invokeLater(Simulator.interactiveGUIUpdater);
                }
                if ((Globals.getGui() != null || Globals.runSpeedPanelExists) && this.maxSteps != 1 && RunSpeedPanel.getInstance().getRunSpeed() < 40.0) {
                    try {
                        Thread.sleep((int)(1000.0 / RunSpeedPanel.getInstance().getRunSpeed()));
                    }
                    catch (InterruptedException ex2) {}
                }
                try {
                    statement = Globals.memory.getStatement(RegisterFile.getProgramCounter());
                    continue;
                }
                catch (AddressErrorException e2) {
                    final ErrorList el2 = new ErrorList();
                    el2.add(new ErrorMessage((MIPSprogram)null, 0, 0, "invalid program counter value: " + Binary.intToHexString(RegisterFile.getProgramCounter())));
                    this.pe = new ProcessingException(el2, e2);
                    Coprocessor0.updateRegister(14, RegisterFile.getProgramCounter());
                    this.constructReturnReason = 2;
                    this.done = true;
                    SystemIO.resetFiles();
                    Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
                    return new Boolean(this.done);
                }
                break;
            }
            if (DelayedBranch.isTriggered() || DelayedBranch.isRegistered()) {
                DelayedBranch.clear();
            }
            this.constructReturnReason = 5;
            this.done = true;
            SystemIO.resetFiles();
            Simulator.getInstance().notifyObserversOfExecutionStop(this.maxSteps, pc);
            return new Boolean(this.done);
        }
        
        @Override
        public void finished() {
            if (Globals.getGui() == null) {
                return;
            }
            final String starterName = (String)this.starter.getValue("Name");
            if (starterName.equals("Step")) {
                ((RunStepAction)this.starter).stepped(this.done, this.constructReturnReason, this.pe);
            }
            if (starterName.equals("Go")) {
                if (this.done) {
                    ((RunGoAction)this.starter).stopped(this.pe, this.constructReturnReason);
                }
                else if (this.constructReturnReason == 1) {
                    ((RunGoAction)this.starter).paused(this.done, this.constructReturnReason, this.pe);
                }
                else {
                    final String stopperName = (String)this.stopper.getValue("Name");
                    if ("Pause".equals(stopperName)) {
                        ((RunGoAction)this.starter).paused(this.done, this.constructReturnReason, this.pe);
                    }
                    else if ("Stop".equals(stopperName)) {
                        ((RunGoAction)this.starter).stopped(this.pe, this.constructReturnReason);
                    }
                }
            }
        }
    }
    
    private class UpdateGUI implements Runnable
    {
        @Override
        public void run() {
            if (Globals.getGui().getRegistersPane().getSelectedComponent() == Globals.getGui().getMainPane().getExecutePane().getRegistersWindow()) {
                Globals.getGui().getMainPane().getExecutePane().getRegistersWindow().updateRegisters();
            }
            else {
                Globals.getGui().getMainPane().getExecutePane().getCoprocessor1Window().updateRegisters();
            }
            Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().updateValues();
            Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().setCodeHighlighting(true);
            Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().highlightStepAtPC();
        }
    }
    
    public interface StopListener
    {
        void stopped(final Simulator p0);
    }
}
