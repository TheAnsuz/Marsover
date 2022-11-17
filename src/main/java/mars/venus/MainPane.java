

package mars.venus;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import mars.Globals;

public class MainPane extends JTabbedPane
{
    EditPane editTab;
    ExecutePane executeTab;
    EditTabbedPane editTabbedPane;
    private final VenusUI mainUI;
    
    public MainPane(final VenusUI appFrame, final Editor editor, final RegistersWindow regs, final Coprocessor1Window cop1Regs, final Coprocessor0Window cop0Regs) {
        this.mainUI = appFrame;
        this.setTabPlacement(1);
        if (this.getUI() instanceof BasicTabbedPaneUI) {
            final BasicTabbedPaneUI basicTabbedPaneUI = (BasicTabbedPaneUI)this.getUI();
        }
        this.editTabbedPane = new EditTabbedPane(appFrame, editor, this);
        this.executeTab = new ExecutePane(appFrame, regs, cop1Regs, cop0Regs);
        final String editTabTitle = "Edit";
        final String executeTabTitle = "Execute";
        final Icon editTabIcon = null;
        final Icon executeTabIcon = null;
        this.setTabLayoutPolicy(1);
        this.addTab(editTabTitle, editTabIcon, this.editTabbedPane);
        this.addTab(executeTabTitle, executeTabIcon, this.executeTab);
        this.setToolTipTextAt(0, "Text editor for composing MIPS programs.");
        this.setToolTipTextAt(1, "View and control assembly language program execution.  Enabled upon successful assemble.");
        this.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent ce) {
                final JTabbedPane tabbedPane = (JTabbedPane)ce.getSource();
                final int index = tabbedPane.getSelectedIndex();
                final Component c = tabbedPane.getComponentAt(index);
                final ExecutePane executePane = Globals.getGui().getMainPane().getExecutePane();
                if (c == executePane) {
                    executePane.setWindowBounds();
                    Globals.getGui().getMainPane().removeChangeListener(this);
                }
            }
        });
    }
    
    public EditPane getEditPane() {
        return this.editTabbedPane.getCurrentEditTab();
    }
    
    public JComponent getEditTabbedPane() {
        return this.editTabbedPane;
    }
    
    public ExecutePane getExecutePane() {
        return this.executeTab;
    }
    
    public ExecutePane getExecuteTab() {
        return this.executeTab;
    }
}
