

package mars.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

public class BHTSimGUI extends JPanel
{
    private JTextField m_tfInstruction;
    private JTextField m_tfAddress;
    private JTextField m_tfIndex;
    private JComboBox m_cbBHTentries;
    private JComboBox m_cbBHThistory;
    private JComboBox m_cbBHTinitVal;
    private final JTable m_tabBHT;
    private JTextArea m_taLog;
    public static final Color COLOR_PREPREDICTION;
    public static final Color COLOR_PREDICTION_CORRECT;
    public static final Color COLOR_PREDICTION_INCORRECT;
    public static final String BHT_TAKE_BRANCH = "TAKE";
    public static final String BHT_DO_NOT_TAKE_BRANCH = "NOT TAKE";
    
    public BHTSimGUI() {
        final BorderLayout layout = new BorderLayout();
        layout.setVgap(10);
        layout.setHgap(10);
        this.setLayout(layout);
        this.m_tabBHT = this.createAndInitTable();
        this.add(this.buildConfigPanel(), "North");
        this.add(this.buildInfoPanel(), "West");
        this.add(new JScrollPane(this.m_tabBHT), "Center");
        this.add(this.buildLogPanel(), "South");
    }
    
    private JTable createAndInitTable() {
        final JTable theTable = new JTable();
        final DefaultTableCellRenderer doubleRenderer = new DefaultTableCellRenderer() {
            private final DecimalFormat formatter = new DecimalFormat("##0.00");
            
            public void setValue(final Object value) {
                this.setText((value == null) ? "" : this.formatter.format(value));
            }
        };
        doubleRenderer.setHorizontalAlignment(0);
        final DefaultTableCellRenderer defRenderer = new DefaultTableCellRenderer();
        defRenderer.setHorizontalAlignment(0);
        theTable.setDefaultRenderer(Double.class, doubleRenderer);
        theTable.setDefaultRenderer(Integer.class, defRenderer);
        theTable.setDefaultRenderer(String.class, defRenderer);
        theTable.setSelectionBackground(BHTSimGUI.COLOR_PREPREDICTION);
        theTable.setSelectionMode(1);
        return theTable;
    }
    
    private JPanel buildInfoPanel() {
        this.m_tfInstruction = new JTextField();
        this.m_tfAddress = new JTextField();
        this.m_tfIndex = new JTextField();
        this.m_tfInstruction.setColumns(10);
        this.m_tfInstruction.setEditable(false);
        this.m_tfInstruction.setHorizontalAlignment(0);
        this.m_tfAddress.setColumns(10);
        this.m_tfAddress.setEditable(false);
        this.m_tfAddress.setHorizontalAlignment(0);
        this.m_tfIndex.setColumns(10);
        this.m_tfIndex.setEditable(false);
        this.m_tfIndex.setHorizontalAlignment(0);
        final JPanel panel = new JPanel();
        final JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        final GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 2, 5);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(new JLabel("Instruction"), c);
        final GridBagConstraints gridBagConstraints = c;
        ++gridBagConstraints.gridy;
        panel.add(this.m_tfInstruction, c);
        final GridBagConstraints gridBagConstraints2 = c;
        ++gridBagConstraints2.gridy;
        panel.add(new JLabel("@ Address"), c);
        final GridBagConstraints gridBagConstraints3 = c;
        ++gridBagConstraints3.gridy;
        panel.add(this.m_tfAddress, c);
        final GridBagConstraints gridBagConstraints4 = c;
        ++gridBagConstraints4.gridy;
        panel.add(new JLabel("-> Index"), c);
        final GridBagConstraints gridBagConstraints5 = c;
        ++gridBagConstraints5.gridy;
        panel.add(this.m_tfIndex, c);
        outerPanel.add(panel, "North");
        return outerPanel;
    }
    
    private JPanel buildConfigPanel() {
        final JPanel panel = new JPanel();
        final Vector sizes = new Vector();
        sizes.add(8);
        sizes.add(16);
        sizes.add(32);
        final Vector bits = new Vector();
        bits.add(1);
        bits.add(2);
        final Vector initVals = new Vector();
        initVals.add("NOT TAKE");
        initVals.add("TAKE");
        this.m_cbBHTentries = new JComboBox(sizes);
        this.m_cbBHThistory = new JComboBox(bits);
        this.m_cbBHTinitVal = new JComboBox(initVals);
        panel.add(new JLabel("# of BHT entries"));
        panel.add(this.m_cbBHTentries);
        panel.add(new JLabel("BHT history size"));
        panel.add(this.m_cbBHThistory);
        panel.add(new JLabel("Initial value"));
        panel.add(this.m_cbBHTinitVal);
        return panel;
    }
    
    private JPanel buildLogPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        (this.m_taLog = new JTextArea()).setRows(6);
        this.m_taLog.setEditable(false);
        panel.add(new JLabel("Log"), "North");
        panel.add(new JScrollPane(this.m_taLog), "Center");
        return panel;
    }
    
    public JComboBox getCbBHTentries() {
        return this.m_cbBHTentries;
    }
    
    public JComboBox getCbBHThistory() {
        return this.m_cbBHThistory;
    }
    
    public JComboBox getCbBHTinitVal() {
        return this.m_cbBHTinitVal;
    }
    
    public JTable getTabBHT() {
        return this.m_tabBHT;
    }
    
    public JTextArea getTaLog() {
        return this.m_taLog;
    }
    
    public JTextField getTfInstruction() {
        return this.m_tfInstruction;
    }
    
    public JTextField getTfAddress() {
        return this.m_tfAddress;
    }
    
    public JTextField getTfIndex() {
        return this.m_tfIndex;
    }
    
    static {
        COLOR_PREPREDICTION = Color.yellow;
        COLOR_PREDICTION_CORRECT = Color.green;
        COLOR_PREDICTION_INCORRECT = Color.red;
    }
}
