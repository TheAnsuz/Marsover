

package mars.venus;

import mars.tools.MarsTool;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.AbstractAction;

public class ToolAction extends AbstractAction
{
    private Class toolClass;
    
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
