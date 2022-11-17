

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import mars.Globals;

public class RunClearBreakpointsAction extends GuiAction implements TableModelListener
{
    public RunClearBreakpointsAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
        Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().registerTableModelListener(this);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().clearAllBreakpoints();
    }
    
    @Override
    public void tableChanged(final TableModelEvent e) {
        this.setEnabled(Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().getBreakpointCount() > 0);
    }
}
