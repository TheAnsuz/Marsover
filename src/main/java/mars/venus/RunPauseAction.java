

package mars.venus;

import javax.swing.AbstractAction;
import mars.simulator.Simulator;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class RunPauseAction extends GuiAction
{
    public RunPauseAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        Simulator.getInstance().stopExecution(this);
    }
}
