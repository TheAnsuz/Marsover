

package mars.venus;

import mars.Globals;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class SettingsAddressDisplayBaseAction extends GuiAction
{
    public SettingsAddressDisplayBaseAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final boolean isHex = ((JCheckBoxMenuItem)e.getSource()).isSelected();
        Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBaseChooser().setSelected(isHex);
        Globals.getSettings().setDisplayAddressesInHex(isHex);
    }
}
