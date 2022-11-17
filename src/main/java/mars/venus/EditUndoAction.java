

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;

public class EditUndoAction extends GuiAction
{
    public EditUndoAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
        this.setEnabled(false);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final EditPane editPane = this.mainUI.getMainPane().getEditPane();
        if (editPane != null) {
            editPane.undo();
            this.updateUndoState();
            this.mainUI.editRedoAction.updateRedoState();
        }
    }
    
    void updateUndoState() {
        final EditPane editPane = this.mainUI.getMainPane().getEditPane();
        this.setEnabled(editPane != null && editPane.getUndoManager().canUndo());
    }
}
