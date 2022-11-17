

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class FileExitAction extends GuiAction
{
    public FileExitAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (this.mainUI.editor.closeAll()) {
            System.exit(0);
        }
    }
}
