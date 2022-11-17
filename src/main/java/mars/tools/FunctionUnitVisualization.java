

package mars.tools;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FunctionUnitVisualization extends JFrame
{
    private final JPanel contentPane;
    private final String instruction;
    private final int register;
    private final int control;
    private final int aluControl;
    private final int alu;
    private int currentUnit;
    
    public FunctionUnitVisualization(final String instruction, final int functionalUnit) {
        this.register = 1;
        this.control = 2;
        this.aluControl = 3;
        this.alu = 4;
        this.instruction = instruction;
        this.setBounds(100, 100, 840, 575);
        (this.contentPane = new JPanel()).setBorder(new EmptyBorder(5, 5, 5, 5));
        this.contentPane.setLayout(new BorderLayout(0, 0));
        this.setContentPane(this.contentPane);
        if (functionalUnit == this.register) {
            this.currentUnit = this.register;
            final UnitAnimation reg = new UnitAnimation(instruction, this.register);
            this.contentPane.add(reg);
            reg.startAnimation(instruction);
        }
        else if (functionalUnit == this.control) {
            this.currentUnit = this.control;
            final UnitAnimation reg = new UnitAnimation(instruction, this.control);
            this.contentPane.add(reg);
            reg.startAnimation(instruction);
        }
        else if (functionalUnit == this.aluControl) {
            this.currentUnit = this.aluControl;
            final UnitAnimation reg = new UnitAnimation(instruction, this.aluControl);
            this.contentPane.add(reg);
            reg.startAnimation(instruction);
        }
    }
    
    public void run() {
        try {
            final FunctionUnitVisualization frame = new FunctionUnitVisualization(this.instruction, this.currentUnit);
            frame.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
