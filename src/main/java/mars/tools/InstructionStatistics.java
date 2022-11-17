

package mars.tools;

import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.hardware.AccessNotice;
import java.util.Observable;
import mars.ProgramStatement;
import mars.mips.hardware.Memory;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class InstructionStatistics extends AbstractMarsToolAndApplication
{
    private static String NAME;
    private static String VERSION;
    private static String HEADING;
    private static final int MAX_CATEGORY = 5;
    private static final int CATEGORY_ALU = 0;
    private static final int CATEGORY_JUMP = 1;
    private static final int CATEGORY_BRANCH = 2;
    private static final int CATEGORY_MEM = 3;
    private static final int CATEGORY_OTHER = 4;
    private JTextField m_tfTotalCounter;
    private JTextField[] m_tfCounters;
    private JProgressBar[] m_pbCounters;
    private int m_totalCounter;
    private int[] m_counters;
    private String[] m_categoryLabels;
    protected int lastAddress;
    
    public InstructionStatistics(final String title, final String heading) {
        super(title, heading);
        this.m_totalCounter = 0;
        this.m_counters = new int[5];
        this.m_categoryLabels = new String[] { "ALU", "Jump", "Branch", "Memory", "Other" };
        this.lastAddress = -1;
    }
    
    public InstructionStatistics() {
        super(InstructionStatistics.NAME + ", " + InstructionStatistics.VERSION, InstructionStatistics.HEADING);
        this.m_totalCounter = 0;
        this.m_counters = new int[5];
        this.m_categoryLabels = new String[] { "ALU", "Jump", "Branch", "Memory", "Other" };
        this.lastAddress = -1;
    }
    
    @Override
    public String getName() {
        return InstructionStatistics.NAME;
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        final JPanel panel = new JPanel(new GridBagLayout());
        (this.m_tfTotalCounter = new JTextField("0", 10)).setEditable(false);
        this.m_tfCounters = new JTextField[5];
        this.m_pbCounters = new JProgressBar[5];
        for (int i = 0; i < 5; ++i) {
            (this.m_tfCounters[i] = new JTextField("0", 10)).setEditable(false);
            (this.m_pbCounters[i] = new JProgressBar(0)).setStringPainted(true);
        }
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = 21;
        final GridBagConstraints gridBagConstraints = c;
        final GridBagConstraints gridBagConstraints2 = c;
        final int n = 1;
        gridBagConstraints2.gridwidth = n;
        gridBagConstraints.gridheight = n;
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(new JLabel("Total: "), c);
        c.gridx = 3;
        panel.add(this.m_tfTotalCounter, c);
        c.insets = new Insets(3, 3, 3, 3);
        for (int j = 0; j < 5; ++j) {
            final GridBagConstraints gridBagConstraints3 = c;
            ++gridBagConstraints3.gridy;
            c.gridx = 2;
            panel.add(new JLabel(this.m_categoryLabels[j] + ":   "), c);
            c.gridx = 3;
            panel.add(this.m_tfCounters[j], c);
            c.gridx = 4;
            panel.add(this.m_pbCounters[j], c);
        }
        return panel;
    }
    
    @Override
    protected void addAsObserver() {
        this.addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }
    
    protected int getInstructionCategory(final ProgramStatement stmt) {
        final int opCode = stmt.getBinaryStatement() >>> 26;
        final int funct = stmt.getBinaryStatement() & 0x1F;
        if (opCode == 0) {
            if (funct == 0) {
                return 0;
            }
            if (2 <= funct && funct <= 7) {
                return 0;
            }
            if (funct == 8 || funct == 9) {
                return 1;
            }
            if (16 <= funct && funct <= 47) {
                return 0;
            }
            return 4;
        }
        else if (opCode == 1) {
            if (0 <= funct && funct <= 7) {
                return 2;
            }
            if (16 <= funct && funct <= 19) {
                return 2;
            }
            return 4;
        }
        else {
            if (opCode == 2 || opCode == 3) {
                return 1;
            }
            if (4 <= opCode && opCode <= 7) {
                return 2;
            }
            if (8 <= opCode && opCode <= 15) {
                return 0;
            }
            if (20 <= opCode && opCode <= 23) {
                return 2;
            }
            if (32 <= opCode && opCode <= 38) {
                return 3;
            }
            if (40 <= opCode && opCode <= 46) {
                return 3;
            }
            return 4;
        }
    }
    
    @Override
    protected void processMIPSUpdate(final Observable resource, final AccessNotice notice) {
        if (!notice.accessIsFromMIPS()) {
            return;
        }
        if (notice.getAccessType() == 0 && notice instanceof MemoryAccessNotice) {
            final MemoryAccessNotice memAccNotice = (MemoryAccessNotice)notice;
            final int a = memAccNotice.getAddress();
            if (a == this.lastAddress) {
                return;
            }
            this.lastAddress = a;
            try {
                final ProgramStatement stmt = Memory.getInstance().getStatementNoNotify(memAccNotice.getAddress());
                if (stmt != null) {
                    final int category = this.getInstructionCategory(stmt);
                    ++this.m_totalCounter;
                    final int[] counters = this.m_counters;
                    final int n = category;
                    ++counters[n];
                    this.updateDisplay();
                }
            }
            catch (AddressErrorException ex) {}
        }
    }
    
    @Override
    protected void initializePreGUI() {
        this.m_totalCounter = 0;
        this.lastAddress = -1;
        for (int i = 0; i < 5; ++i) {
            this.m_counters[i] = 0;
        }
    }
    
    @Override
    protected void reset() {
        this.m_totalCounter = 0;
        this.lastAddress = -1;
        for (int i = 0; i < 5; ++i) {
            this.m_counters[i] = 0;
        }
        this.updateDisplay();
    }
    
    @Override
    protected void updateDisplay() {
        this.m_tfTotalCounter.setText(String.valueOf(this.m_totalCounter));
        for (int i = 0; i < 5; ++i) {
            this.m_tfCounters[i].setText(String.valueOf(this.m_counters[i]));
            this.m_pbCounters[i].setMaximum(this.m_totalCounter);
            this.m_pbCounters[i].setValue(this.m_counters[i]);
        }
    }
    
    static {
        InstructionStatistics.NAME = "Instruction Statistics";
        InstructionStatistics.VERSION = "Version 1.0 (Ingo Kofler)";
        InstructionStatistics.HEADING = "";
    }
}
