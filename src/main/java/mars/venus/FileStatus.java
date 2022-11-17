

package mars.venus;

import mars.Globals;
import java.io.File;

public class FileStatus
{
    public static final int NO_FILE = 0;
    public static final int NEW_NOT_EDITED = 1;
    public static final int NEW_EDITED = 2;
    public static final int NOT_EDITED = 3;
    public static final int EDITED = 4;
    public static final int RUNNABLE = 5;
    public static final int RUNNING = 6;
    public static final int TERMINATED = 7;
    public static final int OPENING = 8;
    private static int systemStatus;
    private static boolean systemAssembled;
    private static boolean systemSaved;
    private static boolean systemEdited;
    private static String systemName;
    private static File systemFile;
    private int status;
    private File file;
    
    public static void set(final int newStatus) {
        FileStatus.systemStatus = newStatus;
        Globals.getGui().setMenuState(FileStatus.systemStatus);
    }
    
    public static int get() {
        return FileStatus.systemStatus;
    }
    
    public static void setAssembled(final boolean b) {
        FileStatus.systemAssembled = b;
    }
    
    public static void setSaved(final boolean b) {
        FileStatus.systemSaved = b;
    }
    
    public static void setEdited(final boolean b) {
        FileStatus.systemEdited = b;
    }
    
    public static void setName(final String s) {
        FileStatus.systemName = s;
    }
    
    public static void setFile(final File f) {
        FileStatus.systemFile = f;
    }
    
    public static File getFile() {
        return FileStatus.systemFile;
    }
    
    public static String getName() {
        return FileStatus.systemName;
    }
    
    public static boolean isAssembled() {
        return FileStatus.systemAssembled;
    }
    
    public static boolean isSaved() {
        return FileStatus.systemSaved;
    }
    
    public static boolean isEdited() {
        return FileStatus.systemEdited;
    }
    
    public static void reset() {
        FileStatus.systemStatus = 0;
        FileStatus.systemName = "";
        FileStatus.systemAssembled = false;
        FileStatus.systemSaved = false;
        FileStatus.systemEdited = false;
        FileStatus.systemFile = null;
    }
    
    public FileStatus() {
        this(0, null);
    }
    
    public FileStatus(final int status, final String pathname) {
        this.status = status;
        if (pathname == null) {
            this.file = null;
        }
        else {
            this.setPathname(pathname);
        }
    }
    
    public void setFileStatus(final int newStatus) {
        this.status = newStatus;
    }
    
    public int getFileStatus() {
        return this.status;
    }
    
    public boolean isNew() {
        return this.status == 1 || this.status == 2;
    }
    
    public boolean hasUnsavedEdits() {
        return this.status == 2 || this.status == 4;
    }
    
    public void setPathname(final String newPath) {
        this.file = new File(newPath);
    }
    
    public void setPathname(final String parent, final String name) {
        this.file = new File(parent, name);
    }
    
    public String getPathname() {
        return (this.file == null) ? null : this.file.getPath();
    }
    
    public String getFilename() {
        return (this.file == null) ? null : this.file.getName();
    }
    
    public String getParent() {
        return (this.file == null) ? null : this.file.getParent();
    }
    
    public void updateStaticFileStatus() {
        FileStatus.systemStatus = this.status;
        FileStatus.systemName = this.file.getPath();
        FileStatus.systemAssembled = false;
        FileStatus.systemSaved = (this.status == 3 || this.status == 5 || this.status == 6 || this.status == 7);
        FileStatus.systemEdited = (this.status == 2 || this.status == 4);
        FileStatus.systemFile = this.file;
    }
}
