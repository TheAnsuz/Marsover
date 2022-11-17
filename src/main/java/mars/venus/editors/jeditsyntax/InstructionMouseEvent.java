

package mars.venus.editors.jeditsyntax;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Date;
import javax.swing.text.Segment;

class InstructionMouseEvent extends MouseEvent
{
    private final Segment line;
    
    public InstructionMouseEvent(final Component component, final int x, final int y, final Segment line) {
        super(component, 503, new Date().getTime(), 0, x, y, 0, false);
        System.out.println("Create InstructionMouseEvent " + x + " " + y + " " + line);
        this.line = line;
    }
    
    public Segment getLine() {
        return this.line;
    }
}
