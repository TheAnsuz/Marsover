

package mars.venus.editors.jeditsyntax;

import java.util.Date;
import java.awt.Component;
import javax.swing.text.Segment;
import java.awt.event.MouseEvent;

class InstructionMouseEvent extends MouseEvent
{
    private Segment line;
    
    public InstructionMouseEvent(final Component component, final int x, final int y, final Segment line) {
        super(component, 503, new Date().getTime(), 0, x, y, 0, false);
        System.out.println("Create InstructionMouseEvent " + x + " " + y + " " + (Object)line);
        this.line = line;
    }
    
    public Segment getLine() {
        return this.line;
    }
}
