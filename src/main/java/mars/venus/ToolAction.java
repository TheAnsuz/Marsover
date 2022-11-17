

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import mars.tools.MarsTool;

public class ToolAction extends AbstractAction
{
    private final Class<? extends MarsTool> toolClass;
    
    public ToolAction(final Class toolClass, final String toolName) {
        super(toolName, null);
        this.toolClass = toolClass;
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            this.toolClass.newInstance().action();
        }
        catch (Exception ex) {}
    }
}
