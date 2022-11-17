

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class EditCopyAction extends GuiAction
{
    public EditCopyAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.mainUI.getMainPane().getEditPane().copyText();
    }
}
