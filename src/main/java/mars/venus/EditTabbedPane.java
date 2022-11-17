

package mars.venus;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import mars.Globals;
import mars.MIPSprogram;
import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.util.FilenameFinder;

public class EditTabbedPane extends JTabbedPane
{
    EditPane editTab;
    MainPane mainPane;
    private final VenusUI mainUI;
    private final Editor editor;
    private final FileOpener fileOpener;
    
    public EditTabbedPane(final VenusUI appFrame, final Editor editor, final MainPane mainPane) {
        this.mainUI = appFrame;
        this.editor = editor;
        this.fileOpener = new FileOpener(editor);
        this.mainPane = mainPane;
        this.editor.setEditTabbedPane(this);
        this.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                final EditPane editPane = (EditPane)EditTabbedPane.this.getSelectedComponent();
                if (editPane != null) {
                    if (Globals.getSettings().getBooleanSetting(3)) {
                        EditTabbedPane.this.updateTitles(editPane);
                    }
                    else {
                        EditTabbedPane.this.updateTitlesAndMenuState(editPane);
                        EditTabbedPane.this.mainPane.getExecutePane().clearPane();
                    }
                    editPane.tellEditingComponentToRequestFocusInWindow();
                }
            }
        });
    }
    
    public EditPane getCurrentEditTab() {
        return (EditPane)this.getSelectedComponent();
    }
    
    public void setCurrentEditTab(final EditPane editPane) {
        this.setSelectedComponent(editPane);
    }
    
    public EditPane getCurrentEditTabForFile(final File file) {
        EditPane result = null;
        final EditPane tab = this.getEditPaneForFile(file.getPath());
        if (tab != null) {
            if (tab != this.getCurrentEditTab()) {
                this.setCurrentEditTab(tab);
            }
            return tab;
        }
        if (this.openFile(file)) {
            result = this.getCurrentEditTab();
        }
        return result;
    }
    
    public void newFile() {
        final EditPane editPane = new EditPane(this.mainUI);
        editPane.setSourceCode("", true);
        editPane.setShowLineNumbersEnabled(true);
        editPane.setFileStatus(1);
        final String name = this.editor.getNextDefaultFilename();
        editPane.setPathname(name);
        this.addTab(name, editPane);
        FileStatus.reset();
        FileStatus.setName(name);
        FileStatus.set(1);
        RegisterFile.resetRegisters();
        final VenusUI mainUI = this.mainUI;
        VenusUI.setReset(true);
        this.mainPane.getExecutePane().clearPane();
        this.mainPane.setSelectedComponent(this);
        editPane.displayCaretPosition(new Point(1, 1));
        this.setSelectedComponent(editPane);
        this.updateTitlesAndMenuState(editPane);
        editPane.tellEditingComponentToRequestFocusInWindow();
    }
    
    public boolean openFile() {
        return this.fileOpener.openFile();
    }
    
    public boolean openFile(final File file) {
        return this.fileOpener.openFile(file);
    }
    
    public boolean closeCurrentFile() {
        final EditPane editPane = this.getCurrentEditTab();
        if (editPane != null) {
            if (!this.editsSavedOrAbandoned()) {
                return false;
            }
            this.remove(editPane);
            this.mainPane.getExecutePane().clearPane();
            this.mainPane.setSelectedComponent(this);
        }
        return true;
    }
    
    public boolean closeAllFiles() {
        final boolean result = true;
        boolean unsavedChanges = false;
        final int tabCount = this.getTabCount();
        if (tabCount > 0) {
            this.mainPane.getExecutePane().clearPane();
            this.mainPane.setSelectedComponent(this);
            final EditPane[] tabs = new EditPane[tabCount];
            for (int i = 0; i < tabCount; ++i) {
                tabs[i] = (EditPane)this.getComponentAt(i);
                if (tabs[i].hasUnsavedEdits()) {
                    unsavedChanges = true;
                }
            }
            if (unsavedChanges) {
                switch (this.confirm("one or more files")) {
                    case 0: {
                        boolean removedAll = true;
                        for (int j = 0; j < tabCount; ++j) {
                            if (tabs[j].hasUnsavedEdits()) {
                                this.setSelectedComponent(tabs[j]);
                                final boolean saved = this.saveCurrentFile();
                                if (saved) {
                                    this.remove(tabs[j]);
                                }
                                else {
                                    removedAll = false;
                                }
                            }
                            else {
                                this.remove(tabs[j]);
                            }
                        }
                        return removedAll;
                    }
                    case 1: {
                        for (int j = 0; j < tabCount; ++j) {
                            this.remove(tabs[j]);
                        }
                        return true;
                    }
                    case 2: {
                        return false;
                    }
                    default: {
                        return false;
                    }
                }
            }
            else {
                for (int i = 0; i < tabCount; ++i) {
                    this.remove(tabs[i]);
                }
            }
        }
        return result;
    }
    
    public boolean saveCurrentFile() {
        final EditPane editPane = this.getCurrentEditTab();
        if (this.saveFile(editPane)) {
            FileStatus.setSaved(true);
            FileStatus.setEdited(false);
            FileStatus.set(3);
            editPane.setFileStatus(3);
            this.updateTitlesAndMenuState(editPane);
            return true;
        }
        return false;
    }
    
    private boolean saveFile(final EditPane editPane) {
        if (editPane == null) {
            return false;
        }
        if (editPane.isNew()) {
            final File theFile = this.saveAsFile(editPane);
            if (theFile != null) {
                editPane.setPathname(theFile.getPath());
            }
            return theFile != null;
        }
        final File theFile = new File(editPane.getPathname());
        try {
            final BufferedWriter outFileStream = new BufferedWriter(new FileWriter(theFile));
            outFileStream.write(editPane.getSource(), 0, editPane.getSource().length());
            outFileStream.close();
        }
        catch (IOException c) {
            JOptionPane.showMessageDialog(null, "Save operation could not be completed due to an error:\n" + c, "Save Operation Failed", 0);
            return false;
        }
        return true;
    }
    
    public boolean saveAsCurrentFile() {
        final EditPane editPane = this.getCurrentEditTab();
        final File theFile = this.saveAsFile(editPane);
        if (theFile != null) {
            FileStatus.setFile(theFile);
            FileStatus.setName(theFile.getPath());
            FileStatus.setSaved(true);
            FileStatus.setEdited(false);
            FileStatus.set(3);
            this.editor.setCurrentSaveDirectory(theFile.getParent());
            editPane.setPathname(theFile.getPath());
            editPane.setFileStatus(3);
            this.updateTitlesAndMenuState(editPane);
            return true;
        }
        return false;
    }
    
    private File saveAsFile(final EditPane editPane) {
        File theFile = null;
        if (editPane != null) {
            JFileChooser saveDialog = null;
            boolean operationOK = false;
            while (!operationOK) {
                if (editPane.isNew()) {
                    saveDialog = new JFileChooser(this.editor.getCurrentSaveDirectory());
                }
                else {
                    final File f = new File(editPane.getPathname());
                    if (f != null) {
                        saveDialog = new JFileChooser(f.getParent());
                    }
                    else {
                        saveDialog = new JFileChooser(this.editor.getCurrentSaveDirectory());
                    }
                }
                final String paneFile = editPane.getFilename();
                if (paneFile != null) {
                    saveDialog.setSelectedFile(new File(paneFile));
                }
                saveDialog.setDialogTitle("Save As");
                final int decision = saveDialog.showSaveDialog(this.mainUI);
                if (decision != 0) {
                    return null;
                }
                theFile = saveDialog.getSelectedFile();
                operationOK = true;
                if (!theFile.exists()) {
                    continue;
                }
                final int overwrite = JOptionPane.showConfirmDialog(this.mainUI, "File " + theFile.getName() + " already exists.  Do you wish to overwrite it?", "Overwrite existing file?", 1, 2);
                switch (overwrite) {
                    case 0: {
                        operationOK = true;
                        continue;
                    }
                    case 1: {
                        operationOK = false;
                        continue;
                    }
                    case 2: {
                        return null;
                    }
                    default: {
                        return null;
                    }
                }
            }
            try {
                final BufferedWriter outFileStream = new BufferedWriter(new FileWriter(theFile));
                outFileStream.write(editPane.getSource(), 0, editPane.getSource().length());
                outFileStream.close();
            }
            catch (IOException c) {
                JOptionPane.showMessageDialog(null, "Save As operation could not be completed due to an error:\n" + c, "Save As Operation Failed", 0);
                return null;
            }
        }
        return theFile;
    }
    
    public boolean saveAllFiles() {
        boolean result = false;
        final int tabCount = this.getTabCount();
        if (tabCount > 0) {
            result = true;
            final EditPane[] tabs = new EditPane[tabCount];
            final EditPane savedPane = this.getCurrentEditTab();
            for (int i = 0; i < tabCount; ++i) {
                tabs[i] = (EditPane)this.getComponentAt(i);
                if (tabs[i].hasUnsavedEdits()) {
                    this.setCurrentEditTab(tabs[i]);
                    if (this.saveFile(tabs[i])) {
                        tabs[i].setFileStatus(3);
                        this.editor.setTitle(tabs[i].getPathname(), tabs[i].getFilename(), tabs[i].getFileStatus());
                    }
                    else {
                        result = false;
                    }
                }
            }
            this.setCurrentEditTab(savedPane);
            if (result) {
                final EditPane editPane = this.getCurrentEditTab();
                FileStatus.setSaved(true);
                FileStatus.setEdited(false);
                FileStatus.set(3);
                editPane.setFileStatus(3);
                this.updateTitlesAndMenuState(editPane);
            }
        }
        return result;
    }
    
    public void remove(EditPane editPane) {
        super.remove(editPane);
        editPane = this.getCurrentEditTab();
        if (editPane == null) {
            FileStatus.set(0);
            this.editor.setTitle("", "", 0);
            Globals.getGui().setMenuState(0);
        }
        else {
            FileStatus.set(editPane.getFileStatus());
            this.updateTitlesAndMenuState(editPane);
        }
        if (this.getTabCount() == 0) {
            this.mainUI.haveMenuRequestFocus();
        }
    }
    
    private void updateTitlesAndMenuState(final EditPane editPane) {
        this.editor.setTitle(editPane.getPathname(), editPane.getFilename(), editPane.getFileStatus());
        editPane.updateStaticFileStatus();
        Globals.getGui().setMenuState(editPane.getFileStatus());
    }
    
    private void updateTitles(final EditPane editPane) {
        this.editor.setTitle(editPane.getPathname(), editPane.getFilename(), editPane.getFileStatus());
        final boolean assembled = FileStatus.isAssembled();
        editPane.updateStaticFileStatus();
        FileStatus.setAssembled(assembled);
    }
    
    public EditPane getEditPaneForFile(final String pathname) {
        EditPane openPane = null;
        for (int i = 0; i < this.getTabCount(); ++i) {
            final EditPane pane = (EditPane)this.getComponentAt(i);
            if (pane.getPathname().equals(pathname)) {
                openPane = pane;
                break;
            }
        }
        return openPane;
    }
    
    public boolean editsSavedOrAbandoned() {
        final EditPane currentPane = this.getCurrentEditTab();
        if (currentPane == null || !currentPane.hasUnsavedEdits()) {
            return true;
        }
        switch (this.confirm(currentPane.getFilename())) {
            case 0: {
                return this.saveCurrentFile();
            }
            case 1: {
                return true;
            }
            case 2: {
                return false;
            }
            default: {
                return false;
            }
        }
    }
    
    private int confirm(final String name) {
        return JOptionPane.showConfirmDialog(this.mainUI, "Changes to " + name + " will be lost unless you save.  Do you wish to save all changes now?", "Save program changes?", 1, 2);
    }
    
    private class FileOpener
    {
        private File mostRecentlyOpenedFile;
        private final JFileChooser fileChooser;
        private int fileFilterCount;
        private final ArrayList<FileFilter> fileFilterList;
        private final PropertyChangeListener listenForUserAddedFileFilter;
        private final Editor theEditor;
        
        public FileOpener(final Editor theEditor) {
            this.mostRecentlyOpenedFile = null;
            this.theEditor = theEditor;
            this.fileChooser = new JFileChooser();
            this.listenForUserAddedFileFilter = new ChoosableFileFilterChangeListener();
            this.fileChooser.addPropertyChangeListener(this.listenForUserAddedFileFilter);
            (this.fileFilterList = new ArrayList()).add(this.fileChooser.getAcceptAllFileFilter());
            this.fileFilterList.add(FilenameFinder.getFileFilter(Globals.fileExtensions, "Assembler Files", true));
            this.fileFilterCount = 0;
            this.setChoosableFileFilters();
        }
        
        private boolean openFile() {
            this.setChoosableFileFilters();
            this.fileChooser.setCurrentDirectory(new File(this.theEditor.getCurrentOpenDirectory()));
            if (Globals.getSettings().getAssembleOnOpenEnabled() && this.mostRecentlyOpenedFile != null) {
                this.fileChooser.setSelectedFile(this.mostRecentlyOpenedFile);
            }
            if (this.fileChooser.showOpenDialog(EditTabbedPane.this.mainUI) == 0) {
                final File theFile = this.fileChooser.getSelectedFile();
                this.theEditor.setCurrentOpenDirectory(theFile.getParent());
                if (!this.openFile(theFile)) {
                    return false;
                }
                if (theFile.canRead() && Globals.getSettings().getAssembleOnOpenEnabled()) {
                    EditTabbedPane.this.mainUI.getRunAssembleAction().actionPerformed(null);
                }
            }
            return true;
        }
        
        private boolean openFile(File theFile) {
            try {
                theFile = theFile.getCanonicalFile();
            }
            catch (IOException ex) {}
            final String currentFilePath = theFile.getPath();
            EditPane editPane = EditTabbedPane.this.getEditPaneForFile(currentFilePath);
            if (editPane != null) {
                EditTabbedPane.this.setSelectedComponent(editPane);
                EditTabbedPane.this.updateTitles(editPane);
                return false;
            }
            editPane = new EditPane(EditTabbedPane.this.mainUI);
            editPane.setPathname(currentFilePath);
            FileStatus.setName(currentFilePath);
            FileStatus.setFile(theFile);
            FileStatus.set(8);
            if (theFile.canRead()) {
                Globals.program = new MIPSprogram();
                try {
                    Globals.program.readSource(currentFilePath);
                }
                catch (ProcessingException ex2) {}
                final StringBuffer fileContents = new StringBuffer((int)theFile.length());
                int lineNumber = 1;
                for (String line = Globals.program.getSourceLine(lineNumber++); line != null; line = Globals.program.getSourceLine(lineNumber++)) {
                    fileContents.append(line).append("\n");
                }
                editPane.setSourceCode(fileContents.toString(), true);
                editPane.discardAllUndoableEdits();
                editPane.setShowLineNumbersEnabled(true);
                editPane.setFileStatus(3);
                EditTabbedPane.this.addTab(editPane.getFilename(), editPane);
                EditTabbedPane.this.setToolTipTextAt(EditTabbedPane.this.indexOfComponent(editPane), editPane.getPathname());
                EditTabbedPane.this.setSelectedComponent(editPane);
                FileStatus.setSaved(true);
                FileStatus.setEdited(false);
                FileStatus.set(3);
                if (Globals.getSettings().getBooleanSetting(3)) {
                    EditTabbedPane.this.updateTitles(editPane);
                }
                else {
                    EditTabbedPane.this.updateTitlesAndMenuState(editPane);
                    EditTabbedPane.this.mainPane.getExecutePane().clearPane();
                }
                EditTabbedPane.this.mainPane.setSelectedComponent(EditTabbedPane.this);
                editPane.tellEditingComponentToRequestFocusInWindow();
                this.mostRecentlyOpenedFile = theFile;
            }
            return true;
        }
        
        private void setChoosableFileFilters() {
            if (this.fileFilterCount < this.fileFilterList.size() || this.fileFilterList.size() != this.fileChooser.getChoosableFileFilters().length) {
                this.fileFilterCount = this.fileFilterList.size();
                boolean activeListener = false;
                if (this.fileChooser.getPropertyChangeListeners().length > 0) {
                    this.fileChooser.removePropertyChangeListener(this.listenForUserAddedFileFilter);
                    activeListener = true;
                }
                this.fileChooser.resetChoosableFileFilters();
                for (int i = 0; i < this.fileFilterList.size(); ++i) {
                    this.fileChooser.addChoosableFileFilter(this.fileFilterList.get(i));
                }
                if (activeListener) {
                    this.fileChooser.addPropertyChangeListener(this.listenForUserAddedFileFilter);
                }
            }
        }
        
        private class ChoosableFileFilterChangeListener implements PropertyChangeListener
        {
            @Override
            public void propertyChange(final PropertyChangeEvent e) {
                if ("ChoosableFileFilterChangedProperty".equals(e.getPropertyName())) {
                    final FileFilter[] newFilters = (FileFilter[])e.getNewValue();
                    final FileFilter[] oldFilters = (FileFilter[])e.getOldValue();
                    if (newFilters.length > FileOpener.this.fileFilterList.size()) {
                        FileOpener.this.fileFilterList.add(newFilters[newFilters.length - 1]);
                    }
                }
            }
        }
    }
}
