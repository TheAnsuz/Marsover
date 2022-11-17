

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class EditSelectAllAction extends GuiAction
{
    public EditSelectAllAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.mainUI.getMainPane().getEditPane().selectAllText();
    }
}
