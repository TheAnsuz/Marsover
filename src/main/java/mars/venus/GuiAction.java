

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class GuiAction extends AbstractAction
{
    protected VenusUI mainUI;
    
    protected GuiAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon);
        this.putValue("ShortDescription", descrip);
        this.putValue("MnemonicKey", mnemonic);
        this.putValue("AcceleratorKey", accel);
        this.mainUI = gui;
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
    }
}
