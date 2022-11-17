

package mars.venus;

import mars.ErrorList;
import mars.ProcessingException;
import mars.ErrorMessage;
import mars.util.SystemIO;
import java.awt.Component;
import mars.mips.hardware.Memory;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.util.FilenameFinder;
import java.io.File;
import mars.MIPSprogram;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import java.util.ArrayList;

public class RunAssembleAction extends GuiAction
{
    private static ArrayList MIPSprogramsToAssemble;
    private static boolean extendedAssemblerEnabled;
    private static boolean warningsAreErrors;
    private static final int LINE_LENGTH_LIMIT = 60;
    
    public RunAssembleAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    static ArrayList getMIPSprogramsToAssemble() {
        return RunAssembleAction.MIPSprogramsToAssemble;
    }
    
    static boolean getExtendedAssemblerEnabled() {
        return RunAssembleAction.extendedAssemblerEnabled;
    }
    
    static boolean getWarningsAreErrors() {
        return RunAssembleAction.warningsAreErrors;
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final String name = this.getValue("Name").toString();
        final Component editPane = this.mainUI.getMainPane().getEditPane();
        final ExecutePane executePane = this.mainUI.getMainPane().getExecutePane();
        final RegistersPane registersPane = this.mainUI.getRegistersPane();
        RunAssembleAction.extendedAssemblerEnabled = Globals.getSettings().getExtendedAssemblerEnabled();
        RunAssembleAction.warningsAreErrors = Globals.getSettings().getWarningsAreErrors();
        if (FileStatus.getFile() != null) {
            if (FileStatus.get() == 4) {
                this.mainUI.editor.save();
            }
            try {
                Globals.program = new MIPSprogram();
                ArrayList filesToAssemble;
                if (Globals.getSettings().getAssembleAllEnabled()) {
                    filesToAssemble = FilenameFinder.getFilenameList(new File(FileStatus.getName()).getParent(), Globals.fileExtensions);
                }
                else {
                    filesToAssemble = new ArrayList();
                    filesToAssemble.add(FileStatus.getName());
                }
                String exceptionHandler = null;
                if (Globals.getSettings().getExceptionHandlerEnabled() && Globals.getSettings().getExceptionHandler() != null && Globals.getSettings().getExceptionHandler().length() > 0) {
                    exceptionHandler = Globals.getSettings().getExceptionHandler();
                }
                RunAssembleAction.MIPSprogramsToAssemble = Globals.program.prepareFilesForAssembly(filesToAssemble, FileStatus.getFile().getPath(), exceptionHandler);
                this.mainUI.messagesPane.postMarsMessage(this.buildFileNameList(name + ": assembling ", RunAssembleAction.MIPSprogramsToAssemble));
                final ErrorList warnings = Globals.program.assemble(RunAssembleAction.MIPSprogramsToAssemble, RunAssembleAction.extendedAssemblerEnabled, RunAssembleAction.warningsAreErrors);
                if (warnings.warningsOccurred()) {
                    this.mainUI.messagesPane.postMarsMessage(warnings.generateWarningReport());
                }
                this.mainUI.messagesPane.postMarsMessage(name + ": operation completed successfully.\n\n");
                FileStatus.setAssembled(true);
                FileStatus.set(5);
                RegisterFile.resetRegisters();
                Coprocessor1.resetRegisters();
                Coprocessor0.resetRegisters();
                executePane.getTextSegmentWindow().setupTable();
                executePane.getDataSegmentWindow().setupTable();
                executePane.getDataSegmentWindow().highlightCellForAddress(Memory.dataBaseAddress);
                executePane.getDataSegmentWindow().clearHighlighting();
                executePane.getLabelsWindow().setupTable();
                executePane.getTextSegmentWindow().setCodeHighlighting(true);
                executePane.getTextSegmentWindow().highlightStepAtPC();
                registersPane.getRegistersWindow().clearWindow();
                registersPane.getCoprocessor1Window().clearWindow();
                registersPane.getCoprocessor0Window().clearWindow();
                final VenusUI mainUI = this.mainUI;
                VenusUI.setReset(true);
                final VenusUI mainUI2 = this.mainUI;
                VenusUI.setStarted(false);
                this.mainUI.getMainPane().setSelectedComponent(executePane);
                SystemIO.resetFiles();
            }
            catch (ProcessingException pe) {
                final String errorReport = pe.errors().generateErrorAndWarningReport();
                this.mainUI.messagesPane.postMarsMessage(errorReport);
                this.mainUI.messagesPane.postMarsMessage(name + ": operation completed with errors.\n\n");
                final ArrayList<ErrorMessage> errorMessages = pe.errors().getErrorMessages();
                for (int i = 0; i < errorMessages.size(); ++i) {
                    final ErrorMessage em = errorMessages.get(i);
                    if (em.getLine() != 0 || em.getPosition() != 0) {
                        if (!em.isWarning() || RunAssembleAction.warningsAreErrors) {
                            Globals.getGui().getMessagesPane().selectErrorMessage(em.getFilename(), em.getLine(), em.getPosition());
                            if (e != null) {
                                Globals.getGui().getMessagesPane().selectEditorTextLine(em.getFilename(), em.getLine(), em.getPosition());
                                break;
                            }
                            break;
                        }
                    }
                }
                FileStatus.setAssembled(false);
                FileStatus.set(3);
            }
        }
    }
    
    private String buildFileNameList(final String preamble, final ArrayList<MIPSprogram> programList) {
        String result = preamble;
        int lineLength = result.length();
        for (int i = 0; i < programList.size(); ++i) {
            final String filename = programList.get(i).getFilename();
            result = result + filename + ((i < programList.size() - 1) ? ", " : "");
            lineLength += filename.length();
            if (lineLength > 60) {
                result += "\n";
                lineLength = 0;
            }
        }
        return result + ((lineLength == 0) ? "" : "\n") + "\n";
    }
}
