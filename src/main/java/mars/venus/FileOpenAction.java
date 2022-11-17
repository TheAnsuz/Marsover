

package mars.venus;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

public class FileOpenAction extends GuiAction
{
    private File mostRecentlyOpenedFile;
    private JFileChooser fileChooser;
    private int fileFilterCount;
    private ArrayList fileFilterList;
    private PropertyChangeListener listenForUserAddedFileFilter;
    
    public FileOpenAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.mainUI.editor.open();
    }
}
