

package mars.venus;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

public class PopupListener extends MouseAdapter
{
    private final JPopupMenu popup;
    
    public PopupListener(final JPopupMenu p) {
        this.popup = p;
    }
    
    @Override
    public void mousePressed(final MouseEvent e) {
        this.maybeShowPopup(e);
    }
    
    @Override
    public void mouseReleased(final MouseEvent e) {
        this.maybeShowPopup(e);
    }
    
    private void maybeShowPopup(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
