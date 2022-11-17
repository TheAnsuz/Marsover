

package mars.venus;

import mars.simulator.Simulator;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class SettingsDelayedBranchingAction extends GuiAction
{
    public SettingsDelayedBranchingAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        Globals.getSettings().setDelayedBranchingEnabled(((AbstractButton)e.getSource()).isSelected());
        if (Globals.getGui() != null && (FileStatus.get() == 5 || FileStatus.get() == 6 || FileStatus.get() == 7)) {
            if (FileStatus.get() == 6) {
                Simulator.getInstance().stopExecution(this);
            }
            Globals.getGui().getRunAssembleAction().actionPerformed(null);
        }
    }
}
