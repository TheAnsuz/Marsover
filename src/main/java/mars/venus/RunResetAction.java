

package mars.venus;

import mars.util.SystemIO;
import java.awt.Component;
import mars.mips.hardware.Memory;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.ProcessingException;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class RunResetAction extends GuiAction
{
    public RunResetAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        RunGoAction.resetMaxSteps();
        final String name = this.getValue("Name").toString();
        final ExecutePane executePane = this.mainUI.getMainPane().getExecutePane();
        try {
            Globals.program.assemble(RunAssembleAction.getMIPSprogramsToAssemble(), RunAssembleAction.getExtendedAssemblerEnabled(), RunAssembleAction.getWarningsAreErrors());
        }
        catch (ProcessingException pe) {
            this.mainUI.getMessagesPane().postMarsMessage("Unable to reset.  Please close file then re-open and re-assemble.\n");
            return;
        }
        RegisterFile.resetRegisters();
        Coprocessor1.resetRegisters();
        Coprocessor0.resetRegisters();
        executePane.getRegistersWindow().clearHighlighting();
        executePane.getRegistersWindow().updateRegisters();
        executePane.getCoprocessor1Window().clearHighlighting();
        executePane.getCoprocessor1Window().updateRegisters();
        executePane.getCoprocessor0Window().clearHighlighting();
        executePane.getCoprocessor0Window().updateRegisters();
        executePane.getDataSegmentWindow().highlightCellForAddress(Memory.dataBaseAddress);
        executePane.getDataSegmentWindow().clearHighlighting();
        executePane.getTextSegmentWindow().resetModifiedSourceCode();
        executePane.getTextSegmentWindow().setCodeHighlighting(true);
        executePane.getTextSegmentWindow().highlightStepAtPC();
        this.mainUI.getRegistersPane().setSelectedComponent(executePane.getRegistersWindow());
        FileStatus.set(5);
        final VenusUI mainUI = this.mainUI;
        VenusUI.setReset(true);
        final VenusUI mainUI2 = this.mainUI;
        VenusUI.setStarted(false);
        SystemIO.resetFiles();
        this.mainUI.getMessagesPane().postRunMessage("\n" + name + ": reset completed.\n\n");
    }
}
