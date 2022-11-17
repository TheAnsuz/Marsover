

package mars.venus;

import mars.Globals;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class SettingsPopupInputAction extends GuiAction
{
    public SettingsPopupInputAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final boolean usePopup = ((JCheckBoxMenuItem)e.getSource()).isSelected();
        Globals.getSettings().setBooleanSetting(17, usePopup);
    }
}
