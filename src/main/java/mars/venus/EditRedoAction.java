

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class EditRedoAction extends GuiAction
{
    public EditRedoAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
        this.setEnabled(false);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final EditPane editPane = this.mainUI.getMainPane().getEditPane();
        if (editPane != null) {
            editPane.redo();
            this.updateRedoState();
            this.mainUI.editUndoAction.updateUndoState();
        }
    }
    
    void updateRedoState() {
        final EditPane editPane = this.mainUI.getMainPane().getEditPane();
        this.setEnabled(editPane != null && editPane.getUndoManager().canRedo());
    }
}
