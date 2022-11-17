

package mars.venus;

import javax.swing.JCheckBoxMenuItem;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class SettingsWarningsAreErrorsAction extends GuiAction
{
    public SettingsWarningsAreErrorsAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        Globals.getSettings().setWarningsAreErrors(((JCheckBoxMenuItem)e.getSource()).isSelected());
    }
}
