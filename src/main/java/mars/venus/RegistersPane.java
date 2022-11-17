

package mars.venus;

import javax.swing.JTabbedPane;

public class RegistersPane extends JTabbedPane
{
    RegistersWindow regsTab;
    Coprocessor1Window cop1Tab;
    Coprocessor0Window cop0Tab;
    private final VenusUI mainUI;
    
    public RegistersPane(final VenusUI appFrame, final RegistersWindow regs, final Coprocessor1Window cop1, final Coprocessor0Window cop0) {
        this.mainUI = appFrame;
        this.regsTab = regs;
        this.cop1Tab = cop1;
        this.cop0Tab = cop0;
        this.regsTab.setVisible(true);
        this.cop1Tab.setVisible(true);
        this.cop0Tab.setVisible(true);
        this.addTab("Registers", this.regsTab);
        this.addTab("Coproc 1", this.cop1Tab);
        this.addTab("Coproc 0", this.cop0Tab);
        this.setToolTipTextAt(0, "CPU registers");
        this.setToolTipTextAt(1, "Coprocessor 1 (floating point unit) registers");
        this.setToolTipTextAt(2, "selected Coprocessor 0 (exceptions and interrupts) registers");
    }
    
    public RegistersWindow getRegistersWindow() {
        return this.regsTab;
    }
    
    public Coprocessor1Window getCoprocessor1Window() {
        return this.cop1Tab;
    }
    
    public Coprocessor0Window getCoprocessor0Window() {
        return this.cop0Tab;
    }
}
