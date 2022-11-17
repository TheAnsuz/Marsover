

package mars.tools;

import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.BasicInstruction;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.hardware.AccessNotice;
import java.util.Observable;
import mars.mips.hardware.Memory;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class InstructionCounter extends AbstractMarsToolAndApplication
{
    private static String name;
    private static String version;
    private static String heading;
    protected int counter;
    private JTextField counterField;
    protected int counterR;
    private JTextField counterRField;
    private JProgressBar progressbarR;
    protected int counterI;
    private JTextField counterIField;
    private JProgressBar progressbarI;
    protected int counterJ;
    private JTextField counterJField;
    private JProgressBar progressbarJ;
    protected int lastAddress;
    
    public InstructionCounter(final String title, final String heading) {
        super(title, heading);
        this.counter = 0;
        this.counterR = 0;
        this.counterI = 0;
        this.counterJ = 0;
        this.lastAddress = -1;
    }
    
    public InstructionCounter() {
        super(InstructionCounter.name + ", " + InstructionCounter.version, InstructionCounter.heading);
        this.counter = 0;
        this.counterR = 0;
        this.counterI = 0;
        this.counterJ = 0;
        this.lastAddress = -1;
    }
    
    @Override
    public String getName() {
        return InstructionCounter.name;
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        final JPanel panel = new JPanel(new GridBagLayout());
        (this.counterField = new JTextField("0", 10)).setEditable(false);
        (this.counterRField = new JTextField("0", 10)).setEditable(false);
        (this.progressbarR = new JProgressBar(0)).setStringPainted(true);
        (this.counterIField = new JTextField("0", 10)).setEditable(false);
        (this.progressbarI = new JProgressBar(0)).setStringPainted(true);
        (this.counterJField = new JTextField("0", 10)).setEditable(false);
        (this.progressbarJ = new JProgressBar(0)).setStringPainted(true);
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = 21;
        final GridBagConstraints gridBagConstraints = c;
        final GridBagConstraints gridBagConstraints2 = c;
        final int n = 1;
        gridBagConstraints2.gridwidth = n;
        gridBagConstraints.gridheight = n;
        c.gridx = 3;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(this.counterField, c);
        c.insets = new Insets(0, 0, 0, 0);
        final GridBagConstraints gridBagConstraints3 = c;
        ++gridBagConstraints3.gridy;
        panel.add(this.counterRField, c);
        final GridBagConstraints gridBagConstraints4 = c;
        ++gridBagConstraints4.gridy;
        panel.add(this.counterIField, c);
        final GridBagConstraints gridBagConstraints5 = c;
        ++gridBagConstraints5.gridy;
        panel.add(this.counterJField, c);
        c.anchor = 22;
        c.gridx = 1;
        c.gridwidth = 2;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(new JLabel("Instructions so far: "), c);
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 2;
        c.gridwidth = 1;
        final GridBagConstraints gridBagConstraints6 = c;
        ++gridBagConstraints6.gridy;
        panel.add(new JLabel("R-type: "), c);
        final GridBagConstraints gridBagConstraints7 = c;
        ++gridBagConstraints7.gridy;
        panel.add(new JLabel("I-type: "), c);
        final GridBagConstraints gridBagConstraints8 = c;
        ++gridBagConstraints8.gridy;
        panel.add(new JLabel("J-type: "), c);
        c.insets = new Insets(3, 3, 3, 3);
        c.gridx = 4;
        c.gridy = 2;
        panel.add(this.progressbarR, c);
        final GridBagConstraints gridBagConstraints9 = c;
        ++gridBagConstraints9.gridy;
        panel.add(this.progressbarI, c);
        final GridBagConstraints gridBagConstraints10 = c;
        ++gridBagConstraints10.gridy;
        panel.add(this.progressbarJ, c);
        return panel;
    }
    
    @Override
    protected void addAsObserver() {
        this.addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }
    
    @Override
    protected void processMIPSUpdate(final Observable resource, final AccessNotice notice) {
        if (!notice.accessIsFromMIPS()) {
            return;
        }
        if (notice.getAccessType() != 0) {
            return;
        }
        final MemoryAccessNotice m = (MemoryAccessNotice)notice;
        final int a = m.getAddress();
        if (a == this.lastAddress) {
            return;
        }
        this.lastAddress = a;
        ++this.counter;
        try {
            final ProgramStatement stmt = Memory.getInstance().getStatement(a);
            final BasicInstruction instr = (BasicInstruction)stmt.getInstruction();
            final BasicInstructionFormat format = instr.getInstructionFormat();
            if (format == BasicInstructionFormat.R_FORMAT) {
                ++this.counterR;
            }
            else if (format == BasicInstructionFormat.I_FORMAT || format == BasicInstructionFormat.I_BRANCH_FORMAT) {
                ++this.counterI;
            }
            else if (format == BasicInstructionFormat.J_FORMAT) {
                ++this.counterJ;
            }
        }
        catch (AddressErrorException e) {
            e.printStackTrace();
        }
        this.updateDisplay();
    }
    
    @Override
    protected void initializePreGUI() {
        final int n = 0;
        this.counterJ = n;
        this.counterI = n;
        this.counterR = n;
        this.counter = n;
        this.lastAddress = -1;
    }
    
    @Override
    protected void reset() {
        final int n = 0;
        this.counterJ = n;
        this.counterI = n;
        this.counterR = n;
        this.counter = n;
        this.lastAddress = -1;
        this.updateDisplay();
    }
    
    @Override
    protected void updateDisplay() {
        this.counterField.setText(String.valueOf(this.counter));
        this.counterRField.setText(String.valueOf(this.counterR));
        this.progressbarR.setMaximum(this.counter);
        this.progressbarR.setValue(this.counterR);
        this.counterIField.setText(String.valueOf(this.counterI));
        this.progressbarI.setMaximum(this.counter);
        this.progressbarI.setValue(this.counterI);
        this.counterJField.setText(String.valueOf(this.counterJ));
        this.progressbarJ.setMaximum(this.counter);
        this.progressbarJ.setValue(this.counterJ);
        if (this.counter == 0) {
            this.progressbarR.setString("0%");
            this.progressbarI.setString("0%");
            this.progressbarJ.setString("0%");
        }
        else {
            this.progressbarR.setString(this.counterR * 100 / this.counter + "%");
            this.progressbarI.setString(this.counterI * 100 / this.counter + "%");
            this.progressbarJ.setString(this.counterJ * 100 / this.counter + "%");
        }
    }
    
    static {
        InstructionCounter.name = "Instruction Counter";
        InstructionCounter.version = "Version 1.0 (Felipe Lessa)";
        InstructionCounter.heading = "Counting the number of instructions executed";
    }
}
