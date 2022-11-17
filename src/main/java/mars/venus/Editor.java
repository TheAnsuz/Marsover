

package mars.venus;

import java.io.File;

public class Editor
{
    public static final int MIN_TAB_SIZE = 1;
    public static final int MAX_TAB_SIZE = 32;
    public static final int MIN_BLINK_RATE = 0;
    public static final int MAX_BLINK_RATE = 1000;
    private VenusUI mainUI;
    private EditTabbedPane editTabbedPane;
    private String mainUIbaseTitle;
    private int newUsageCount;
    private String defaultOpenDirectory;
    private String currentOpenDirectory;
    private String defaultSaveDirectory;
    private String currentSaveDirectory;
    
    public Editor(final VenusUI ui) {
        this.mainUI = ui;
        FileStatus.reset();
        this.mainUIbaseTitle = this.mainUI.getTitle();
        this.newUsageCount = 0;
        this.defaultOpenDirectory = System.getProperty("user.dir");
        this.defaultSaveDirectory = System.getProperty("user.dir");
        this.currentOpenDirectory = this.defaultOpenDirectory;
        this.currentSaveDirectory = this.defaultSaveDirectory;
    }
    
    public void setEditTabbedPane(final EditTabbedPane editTabbedPane) {
        this.editTabbedPane = editTabbedPane;
    }
    
    public String getCurrentOpenDirectory() {
        return this.currentOpenDirectory;
    }
    
    void setCurrentOpenDirectory(final String currentOpenDirectory) {
        final File file = new File(currentOpenDirectory);
        if (!file.exists() || !file.isDirectory()) {
            this.currentOpenDirectory = this.defaultOpenDirectory;
        }
        else {
            this.currentOpenDirectory = currentOpenDirectory;
        }
    }
    
    public String getCurrentSaveDirectory() {
        return this.currentSaveDirectory;
    }
    
    void setCurrentSaveDirectory(final String currentSaveDirectory) {
        final File file = new File(currentSaveDirectory);
        if (!file.exists() || !file.isDirectory()) {
            this.currentSaveDirectory = this.defaultSaveDirectory;
        }
        else {
            this.currentSaveDirectory = currentSaveDirectory;
        }
    }
    
    public String getNextDefaultFilename() {
        ++this.newUsageCount;
        return "mips" + this.newUsageCount + ".asm";
    }
    
    public void setTitle(final String path, final String name, final int status) {
        if (status == 0 || name == null || name.length() == 0) {
            this.mainUI.setTitle(this.mainUIbaseTitle);
        }
        else {
            final String edited = (status == 2 || status == 4) ? "*" : " ";
            final String titleName = (status == 2 || status == 1) ? name : path;
            this.mainUI.setTitle(titleName + edited + " - " + this.mainUIbaseTitle);
            this.editTabbedPane.setTitleAt(this.editTabbedPane.getSelectedIndex(), name + edited);
        }
    }
    
    public void newFile() {
        this.editTabbedPane.newFile();
    }
    
    public boolean close() {
        return this.editTabbedPane.closeCurrentFile();
    }
    
    public boolean closeAll() {
        return this.editTabbedPane.closeAllFiles();
    }
    
    public boolean save() {
        return this.editTabbedPane.saveCurrentFile();
    }
    
    public boolean saveAs() {
        return this.editTabbedPane.saveAsCurrentFile();
    }
    
    public boolean saveAll() {
        return this.editTabbedPane.saveAllFiles();
    }
    
    public boolean open() {
        return this.editTabbedPane.openFile();
    }
    
    public boolean editsSavedOrAbandoned() {
        return this.editTabbedPane.editsSavedOrAbandoned();
    }
}
