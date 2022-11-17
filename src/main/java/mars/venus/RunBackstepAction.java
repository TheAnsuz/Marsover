

package mars.venus;

import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.RegisterFile;
import java.util.Observer;
import mars.mips.hardware.Memory;
import mars.Globals;
import java.awt.Component;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class RunBackstepAction extends GuiAction
{
    String name;
    ExecutePane executePane;
    
    public RunBackstepAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.name = this.getValue("Name").toString();
        this.executePane = this.mainUI.getMainPane().getExecutePane();
        final boolean done = false;
        if (!FileStatus.isAssembled()) {
            JOptionPane.showMessageDialog(this.mainUI, "The program must be assembled before it can be run.");
            return;
        }
        final VenusUI mainUI = this.mainUI;
        VenusUI.setStarted(true);
        this.mainUI.messagesPane.setSelectedComponent(this.mainUI.messagesPane.runTab);
        this.executePane.getTextSegmentWindow().setCodeHighlighting(true);
        if (Globals.getSettings().getBackSteppingEnabled()) {
            final boolean inDelaySlot = Globals.program.getBackStepper().inDelaySlot();
            Memory.getInstance().addObserver(this.executePane.getDataSegmentWindow());
            RegisterFile.addRegistersObserver(this.executePane.getRegistersWindow());
            Coprocessor0.addRegistersObserver(this.executePane.getCoprocessor0Window());
            Coprocessor1.addRegistersObserver(this.executePane.getCoprocessor1Window());
            Globals.program.getBackStepper().backStep();
            Memory.getInstance().deleteObserver(this.executePane.getDataSegmentWindow());
            RegisterFile.deleteRegistersObserver(this.executePane.getRegistersWindow());
            this.executePane.getRegistersWindow().updateRegisters();
            this.executePane.getCoprocessor1Window().updateRegisters();
            this.executePane.getCoprocessor0Window().updateRegisters();
            this.executePane.getDataSegmentWindow().updateValues();
            this.executePane.getTextSegmentWindow().highlightStepAtPC(inDelaySlot);
            FileStatus.set(5);
            final VenusUI mainUI2 = this.mainUI;
            VenusUI.setReset(false);
        }
    }
}
