

package mars.venus;

import mars.Globals;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class SettingsProgramArgumentsAction extends GuiAction
{
    public SettingsProgramArgumentsAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final boolean selected = ((JCheckBoxMenuItem)e.getSource()).isSelected();
        Globals.getSettings().setProgramArguments(selected);
        if (selected) {
            Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().addProgramArgumentsPanel();
        }
        else {
            Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().removeProgramArgumentsPanel();
        }
    }
}
