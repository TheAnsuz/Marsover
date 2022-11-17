

package mars.venus;

import mars.simulator.ProgramArgumentList;
import mars.mips.hardware.RegisterFile;
import javax.swing.JOptionPane;
import mars.ProcessingException;
import javax.swing.AbstractAction;
import mars.Globals;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class RunStepAction extends GuiAction
{
    String name;
    ExecutePane executePane;
    
    public RunStepAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.name = this.getValue("Name").toString();
        this.executePane = this.mainUI.getMainPane().getExecutePane();
        boolean done = false;
        if (FileStatus.isAssembled()) {
            final VenusUI mainUI = this.mainUI;
            if (!VenusUI.getStarted()) {
                this.processProgramArgumentsIfAny();
            }
            final VenusUI mainUI2 = this.mainUI;
            VenusUI.setStarted(true);
            this.mainUI.messagesPane.setSelectedComponent(this.mainUI.messagesPane.runTab);
            this.executePane.getTextSegmentWindow().setCodeHighlighting(true);
            try {
                done = Globals.program.simulateStepAtPC(this);
            }
            catch (ProcessingException ex) {}
        }
        else {
            JOptionPane.showMessageDialog(this.mainUI, "The program must be assembled before it can be run.");
        }
    }
    
    public void stepped(final boolean done, final int reason, final ProcessingException pe) {
        this.executePane.getRegistersWindow().updateRegisters();
        this.executePane.getCoprocessor1Window().updateRegisters();
        this.executePane.getCoprocessor0Window().updateRegisters();
        this.executePane.getDataSegmentWindow().updateValues();
        if (!done) {
            this.executePane.getTextSegmentWindow().highlightStepAtPC();
            FileStatus.set(5);
        }
        if (done) {
            RunGoAction.resetMaxSteps();
            this.executePane.getTextSegmentWindow().unhighlightAllSteps();
            FileStatus.set(7);
        }
        if (done && pe == null) {
            this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution " + ((reason == 5) ? "terminated due to null instruction." : "completed successfully.") + "\n\n");
            this.mainUI.getMessagesPane().postRunMessage("\n-- program is finished running " + ((reason == 5) ? "(dropped off bottom)" : "") + " --\n\n");
            this.mainUI.getMessagesPane().selectRunMessageTab();
        }
        if (pe != null) {
            RunGoAction.resetMaxSteps();
            this.mainUI.getMessagesPane().postMarsMessage(pe.errors().generateErrorReport());
            this.mainUI.getMessagesPane().postMarsMessage("\n" + this.name + ": execution terminated with errors.\n\n");
            this.mainUI.getRegistersPane().setSelectedComponent(this.executePane.getCoprocessor0Window());
            FileStatus.set(7);
            this.executePane.getTextSegmentWindow().setCodeHighlighting(true);
            this.executePane.getTextSegmentWindow().unhighlightAllSteps();
            this.executePane.getTextSegmentWindow().highlightStepAtAddress(RegisterFile.getProgramCounter() - 4);
        }
        final VenusUI mainUI = this.mainUI;
        VenusUI.setReset(false);
    }
    
    private void processProgramArgumentsIfAny() {
        final String programArguments = this.executePane.getTextSegmentWindow().getProgramArguments();
        if (programArguments == null || programArguments.length() == 0 || !Globals.getSettings().getProgramArguments()) {
            return;
        }
        new ProgramArgumentList(programArguments).storeProgramArguments();
    }
}
