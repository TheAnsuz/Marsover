

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import mars.Globals;

public class SettingsSelfModifyingCodeAction extends GuiAction
{
    public SettingsSelfModifyingCodeAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        Globals.getSettings().setBooleanSetting(20, ((AbstractButton)e.getSource()).isSelected());
    }
}
