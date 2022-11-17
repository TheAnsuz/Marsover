

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import mars.Globals;
import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.simulator.ProgramArgumentList;
import mars.util.SystemIO;

public class RunGoAction extends GuiAction
{
    public static int defaultMaxSteps;
    public static int maxSteps;
    private String name;
    private ExecutePane executePane;
    
    public RunGoAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.name = this.getValue("Name").toString();
        this.executePane = this.mainUI.getMainPane().getExecutePane();
        if (FileStatus.isAssembled()) {
            final VenusUI mainUI = this.mainUI;
            if (!VenusUI.getStarted()) {
                this.processProgramArgumentsIfAny();
            }
            final VenusUI mainUI2 = this.mainUI;
            if (!VenusUI.getReset()) {
                final VenusUI mainUI3 = this.mainUI;
                if (!VenusUI.getStarted()) {
                    final VenusUI mainUI4 = this.mainUI;
                    final StringBuilder append = new StringBuilder().append("reset ");
                    final VenusUI mainUI5 = this.mainUI;
                    final StringBuilder append2 = append.append(VenusUI.getReset()).append(" started ");
                    final VenusUI mainUI6 = this.mainUI;
                    JOptionPane.showMessageDialog(mainUI4, append2.append(VenusUI.getStarted()).toString());
                    return;
                }
            }
            final VenusUI mainUI7 = this.mainUI;
            VenusUI.setStarted(true);
            this.mainUI.messagesPane.postMarsMessage(this.name + ": running " + FileStatus.getFile().getName() + "\n\n");
            this.mainUI.getMessagesPane().selectRunMessageTab();
            this.executePane.getTextSegmentWindow().setCodeHighlighting(false);
            this.executePane.getTextSegmentWindow().unhighlightAllSteps();
            this.mainUI.setMenuState(6);
            try {
                final int[] breakPoints = this.executePane.getTextSegmentWindow().getSortedBreakPointsArray();
                Globals.program.simulateFromPC(breakPoints, RunGoAction.maxSteps, this);
            }
            catch (ProcessingException ex) {}
        }
        else {
            JOptionPane.showMessageDialog(this.mainUI, "The program must be assembled before it can be run.");
        }
    }
    
    public void paused(final boolean done, final int pauseReason, final ProcessingException pe) {
        if (done) {
            this.stopped(pe, 4);
            return;
        }
        if (pauseReason == 1) {
            this.mainUI.messagesPane.postMarsMessage(this.name + ": execution paused at breakpoint: " + FileStatus.getFile().getName() + "\n\n");
        }
        else {
            this.mainUI.messagesPane.postMarsMessage(this.name + ": execution paused by user: " + FileStatus.getFile().getName() + "\n\n");
        }
        this.mainUI.getMessagesPane().selectMarsMessageTab();
        this.executePane.getTextSegmentWindow().setCodeHighlighting(true);
        this.executePane.getTextSegmentWindow().highlightStepAtPC();
        this.executePane.getRegistersWindow().updateRegisters();
        this.executePane.getCoprocessor1Window().updateRegisters();
        this.executePane.getCoprocessor0Window().updateRegisters();
        this.executePane.getDataSegmentWindow().updateValues();
        FileStatus.set(5);
        final VenusUI mainUI = this.mainUI;
        VenusUI.setReset(false);
    }
    
    public void stopped(final ProcessingException pe, final int reason) {
        this.executePane.getRegistersWindow().updateRegisters();
        this.executePane.getCoprocessor1Window().updateRegisters();
        this.executePane.getCoprocessor0Window().updateRegisters();
        this.executePane.getDataSegmentWindow().updateValues();
        FileStatus.set(7);
        SystemIO.resetFiles();
        if (pe != null) {
            this.mainUI.getRegistersPane().setSelectedComponent(this.executePane.getCoprocessor0Window());
            this.executePane.getTextSegmentWindow().setCodeHighlighting(true);
            this.executePane.getTextSegmentWindow().unhighlightAllSteps();
            this.executePane.getTextSegmentWindow().highlightStepAtAddress(RegisterFile.getProgramCounter() - 4);
        }
        switch (reason) {
            case 4: {
                this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution completed successfully.\n\n");
                this.mainUI.getMessagesPane().postRunMessage("\n-- program is finished running --\n\n");
                this.mainUI.getMessagesPane().selectRunMessageTab();
                break;
            }
            case 5: {
                this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution terminated by null instruction.\n\n");
                this.mainUI.getMessagesPane().postRunMessage("\n-- program is finished running (dropped off bottom) --\n\n");
                this.mainUI.getMessagesPane().selectRunMessageTab();
                break;
            }
            case 2: {
                this.mainUI.getMessagesPane().postMarsMessage(pe.errors().generateErrorReport());
                this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution terminated with errors.\n\n");
                break;
            }
            case 6: {
                this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution terminated by user.\n\n");
                this.mainUI.getMessagesPane().selectMarsMessageTab();
                break;
            }
            case 3: {
                this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution step limit of " + RunGoAction.maxSteps + " exceeded.\n\n");
                this.mainUI.getMessagesPane().selectMarsMessageTab();
                break;
            }
        }
        resetMaxSteps();
        final VenusUI mainUI = this.mainUI;
        VenusUI.setReset(false);
    }
    
    public static void resetMaxSteps() {
        RunGoAction.maxSteps = RunGoAction.defaultMaxSteps;
    }
    
    private void processProgramArgumentsIfAny() {
        final String programArguments = this.executePane.getTextSegmentWindow().getProgramArguments();
        if (programArguments == null || programArguments.length() == 0 || !Globals.getSettings().getProgramArguments()) {
            return;
        }
        new ProgramArgumentList(programArguments).storeProgramArguments();
    }
    
    static {
        RunGoAction.defaultMaxSteps = -1;
        RunGoAction.maxSteps = RunGoAction.defaultMaxSteps;
    }
}
