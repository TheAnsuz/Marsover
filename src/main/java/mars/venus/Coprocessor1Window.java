

package mars.venus;

import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.table.TableModel;
import mars.util.Binary;
import javax.swing.table.AbstractTableModel;
import java.awt.Font;
import javax.swing.table.DefaultTableCellRenderer;
import mars.mips.hardware.RegisterAccessNotice;
import mars.simulator.SimulatorNotice;
import java.util.Observable;
import javax.swing.event.TableModelEvent;
import mars.mips.hardware.InvalidRegisterAccessException;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.GridLayout;
import mars.mips.hardware.Coprocessor1;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellRenderer;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import mars.Globals;
import mars.simulator.Simulator;
import mars.Settings;
import javax.swing.JCheckBox;
import mars.mips.hardware.Register;
import javax.swing.JTable;
import java.util.Observer;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

public class Coprocessor1Window extends JPanel implements ActionListener, Observer
{
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private boolean highlighting;
    private int highlightRow;
    private ExecutePane executePane;
    private JCheckBox[] conditionFlagCheckBox;
    private static final int NAME_COLUMN = 0;
    private static final int FLOAT_COLUMN = 1;
    private static final int DOUBLE_COLUMN = 2;
    private static Settings settings;
    
    public Coprocessor1Window() {
        Simulator.getInstance().addObserver(this);
        Coprocessor1Window.settings = Globals.getSettings();
        this.setLayout(new BorderLayout());
        Coprocessor1Window.table = new MyTippedJTable(new RegTableModel(this.setupWindow()));
        Coprocessor1Window.table.getColumnModel().getColumn(0).setPreferredWidth(20);
        Coprocessor1Window.table.getColumnModel().getColumn(1).setPreferredWidth(70);
        Coprocessor1Window.table.getColumnModel().getColumn(2).setPreferredWidth(130);
        Coprocessor1Window.table.getColumnModel().getColumn(0).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 2));
        Coprocessor1Window.table.getColumnModel().getColumn(1).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 4));
        Coprocessor1Window.table.getColumnModel().getColumn(2).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 4));
        this.add(new JScrollPane(Coprocessor1Window.table, 20, 31));
        final JPanel flagsPane = new JPanel(new BorderLayout());
        flagsPane.setToolTipText("flags are used by certain floating point instructions, default flag is 0");
        flagsPane.add(new JLabel("Condition Flags", 0), "North");
        final int numFlags = Coprocessor1.getConditionFlagCount();
        this.conditionFlagCheckBox = new JCheckBox[numFlags];
        final JPanel checksPane = new JPanel(new GridLayout(2, numFlags / 2));
        for (int i = 0; i < numFlags; ++i) {
            (this.conditionFlagCheckBox[i] = new JCheckBox(Integer.toString(i))).addActionListener(this);
            this.conditionFlagCheckBox[i].setBackground(Color.WHITE);
            this.conditionFlagCheckBox[i].setToolTipText("checked == 1, unchecked == 0");
            checksPane.add(this.conditionFlagCheckBox[i]);
        }
        flagsPane.add(checksPane, "Center");
        this.add(flagsPane, "South");
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final JCheckBox checkBox = (JCheckBox)e.getSource();
        final int i = Integer.parseInt(checkBox.getText());
        if (checkBox.isSelected()) {
            checkBox.setSelected(true);
            Coprocessor1.setConditionFlag(i);
        }
        else {
            checkBox.setSelected(false);
            Coprocessor1.clearConditionFlag(i);
        }
    }
    
    public Object[][] setupWindow() {
        Coprocessor1Window.registers = Coprocessor1.getRegisters();
        this.highlighting = false;
        this.tableData = new Object[Coprocessor1Window.registers.length][3];
        for (int i = 0; i < Coprocessor1Window.registers.length; ++i) {
            this.tableData[i][0] = Coprocessor1Window.registers[i].getName();
            this.tableData[i][1] = NumberDisplayBaseChooser.formatFloatNumber(Coprocessor1Window.registers[i].getValue(), NumberDisplayBaseChooser.getBase(Coprocessor1Window.settings.getDisplayValuesInHex()));
            if (i % 2 == 0) {
                long longValue = 0L;
                try {
                    longValue = Coprocessor1.getLongFromRegisterPair(Coprocessor1Window.registers[i].getName());
                }
                catch (InvalidRegisterAccessException ex) {}
                this.tableData[i][2] = NumberDisplayBaseChooser.formatDoubleNumber(longValue, NumberDisplayBaseChooser.getBase(Coprocessor1Window.settings.getDisplayValuesInHex()));
            }
            else {
                this.tableData[i][2] = "";
            }
        }
        return this.tableData;
    }
    
    public void clearWindow() {
        this.clearHighlighting();
        Coprocessor1.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
        Coprocessor1.clearConditionFlags();
        this.updateConditionFlagDisplay();
    }
    
    public void clearHighlighting() {
        this.highlighting = false;
        if (Coprocessor1Window.table != null) {
            Coprocessor1Window.table.tableChanged(new TableModelEvent(Coprocessor1Window.table.getModel()));
        }
        this.highlightRow = -1;
    }
    
    public void refresh() {
        if (Coprocessor1Window.table != null) {
            Coprocessor1Window.table.tableChanged(new TableModelEvent(Coprocessor1Window.table.getModel()));
        }
    }
    
    public void updateRegisters() {
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }
    
    public void updateRegisters(final int base) {
        Coprocessor1Window.registers = Coprocessor1.getRegisters();
        for (int i = 0; i < Coprocessor1Window.registers.length; ++i) {
            this.updateFloatRegisterValue(Coprocessor1Window.registers[i].getNumber(), Coprocessor1Window.registers[i].getValue(), base);
            if (i % 2 == 0) {
                this.updateDoubleRegisterValue(i, base);
            }
        }
        this.updateConditionFlagDisplay();
    }
    
    private void updateConditionFlagDisplay() {
        for (int i = 0; i < this.conditionFlagCheckBox.length; ++i) {
            this.conditionFlagCheckBox[i].setSelected(Coprocessor1.getConditionFlag(i) != 0);
        }
    }
    
    public void updateFloatRegisterValue(final int number, final int val, final int base) {
        ((RegTableModel)Coprocessor1Window.table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatFloatNumber(val, base), number, 1);
    }
    
    public void updateDoubleRegisterValue(final int number, final int base) {
        long val = 0L;
        try {
            val = Coprocessor1.getLongFromRegisterPair(Coprocessor1Window.registers[number].getName());
        }
        catch (InvalidRegisterAccessException ex) {}
        ((RegTableModel)Coprocessor1Window.table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatDoubleNumber(val, base), number, 2);
    }
    
    @Override
    public void update(final Observable observable, final Object obj) {
        if (observable == Simulator.getInstance()) {
            final SimulatorNotice notice = (SimulatorNotice)obj;
            if (notice.getAction() == 0) {
                if (notice.getRunSpeed() != 40.0 || notice.getMaxSteps() == 1) {
                    Coprocessor1.addRegistersObserver(this);
                    this.highlighting = true;
                }
            }
            else {
                Coprocessor1.deleteRegistersObserver(this);
            }
        }
        else if (obj instanceof RegisterAccessNotice) {
            final RegisterAccessNotice access = (RegisterAccessNotice)obj;
            if (access.getAccessType() == 1) {
                this.highlighting = true;
                this.highlightCellForRegister((Register)observable);
                Globals.getGui().getRegistersPane().setSelectedComponent(this);
            }
        }
    }
    
    void highlightCellForRegister(final Register register) {
        this.highlightRow = register.getNumber();
        Coprocessor1Window.table.tableChanged(new TableModelEvent(Coprocessor1Window.table.getModel()));
    }
    
    private class RegisterCellRenderer extends DefaultTableCellRenderer
    {
        private Font font;
        private int alignment;
        
        public RegisterCellRenderer(final Font font, final int alignment) {
            this.font = font;
            this.alignment = alignment;
        }
        
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setFont(this.font);
            cell.setHorizontalAlignment(this.alignment);
            if (Coprocessor1Window.settings.getRegistersHighlighting() && Coprocessor1Window.this.highlighting && row == Coprocessor1Window.this.highlightRow) {
                cell.setBackground(Coprocessor1Window.settings.getColorSettingByPosition(10));
                cell.setForeground(Coprocessor1Window.settings.getColorSettingByPosition(11));
                cell.setFont(Coprocessor1Window.settings.getFontByPosition(6));
            }
            else if (row % 2 == 0) {
                cell.setBackground(Coprocessor1Window.settings.getColorSettingByPosition(0));
                cell.setForeground(Coprocessor1Window.settings.getColorSettingByPosition(1));
                cell.setFont(Coprocessor1Window.settings.getFontByPosition(1));
            }
            else {
                cell.setBackground(Coprocessor1Window.settings.getColorSettingByPosition(2));
                cell.setForeground(Coprocessor1Window.settings.getColorSettingByPosition(3));
                cell.setFont(Coprocessor1Window.settings.getFontByPosition(2));
            }
            return cell;
        }
    }
    
    class RegTableModel extends AbstractTableModel
    {
        final String[] columnNames;
        Object[][] data;
        
        public RegTableModel(final Object[][] d) {
            this.columnNames = new String[] { "Name", "Float", "Double" };
            this.data = d;
        }
        
        @Override
        public int getColumnCount() {
            return this.columnNames.length;
        }
        
        @Override
        public int getRowCount() {
            return this.data.length;
        }
        
        @Override
        public String getColumnName(final int col) {
            return this.columnNames[col];
        }
        
        @Override
        public Object getValueAt(final int row, final int col) {
            return this.data[row][col];
        }
        
        @Override
        public Class getColumnClass(final int c) {
            return this.getValueAt(0, c).getClass();
        }
        
        @Override
        public boolean isCellEditable(final int row, final int col) {
            return col == 1 || (col == 2 && row % 2 == 0);
        }
        
        @Override
        public void setValueAt(final Object value, final int row, final int col) {
            final int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
            final String sVal = (String)value;
            try {
                if (col == 1) {
                    if (Binary.isHex(sVal)) {
                        final int iVal = Binary.stringToInt(sVal);
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.updateRegister(row, iVal);
                        }
                        this.data[row][col] = NumberDisplayBaseChooser.formatFloatNumber(iVal, valueBase);
                    }
                    else {
                        final float fVal = Float.parseFloat(sVal);
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.setRegisterToFloat(row, fVal);
                        }
                        this.data[row][col] = NumberDisplayBaseChooser.formatNumber(fVal, valueBase);
                    }
                    final int dReg = row - row % 2;
                    this.setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatDoubleNumber(Coprocessor1.getLongFromRegisterPair(dReg), valueBase), dReg, 2);
                }
                else if (col == 2) {
                    if (Binary.isHex(sVal)) {
                        final long lVal = Binary.stringToLong(sVal);
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.setRegisterPairToLong(row, lVal);
                        }
                        this.setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatDoubleNumber(lVal, valueBase), row, col);
                    }
                    else {
                        final double dVal = Double.parseDouble(sVal);
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.setRegisterPairToDouble(row, dVal);
                        }
                        this.setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(dVal, valueBase), row, col);
                    }
                    this.setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(Coprocessor1.getValue(row), valueBase), row, 1);
                    this.setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(Coprocessor1.getValue(row + 1), valueBase), row + 1, 1);
                }
            }
            catch (NumberFormatException nfe) {
                this.data[row][col] = "INVALID";
                this.fireTableCellUpdated(row, col);
            }
            catch (InvalidRegisterAccessException e) {
                this.fireTableCellUpdated(row, col);
            }
        }
        
        private void setDisplayAndModelValueAt(final Object value, final int row, final int col) {
            this.data[row][col] = value;
            this.fireTableCellUpdated(row, col);
        }
        
        private void printDebugData() {
            final int numRows = this.getRowCount();
            final int numCols = this.getColumnCount();
            for (int i = 0; i < numRows; ++i) {
                System.out.print("    row " + i + ":");
                for (int j = 0; j < numCols; ++j) {
                    System.out.print("  " + this.data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }
    
    private class MyTippedJTable extends JTable
    {
        private String[] regToolTips;
        private String[] columnToolTips;
        
        MyTippedJTable(final RegTableModel m) {
            super(m);
            this.regToolTips = new String[] { "floating point subprogram return value", "should not be referenced explicitly in your program", "floating point subprogram return value", "should not be referenced explicitly in your program", "temporary (not preserved across call)", "should not be referenced explicitly in your program", "temporary (not preserved across call)", "should not be referenced explicitly in your program", "temporary (not preserved across call)", "should not be referenced explicitly in your program", "temporary (not preserved across call)", "should not be referenced explicitly in your program", "floating point subprogram argument 1", "should not be referenced explicitly in your program", "floating point subprogram argument 2", "should not be referenced explicitly in your program", "temporary (not preserved across call)", "should not be referenced explicitly in your program", "temporary (not preserved across call)", "should not be referenced explicitly in your program", "saved temporary (preserved across call)", "should not be referenced explicitly in your program", "saved temporary (preserved across call)", "should not be referenced explicitly in your program", "saved temporary (preserved across call)", "should not be referenced explicitly in your program", "saved temporary (preserved across call)", "should not be referenced explicitly in your program", "saved temporary (preserved across call)", "should not be referenced explicitly in your program", "saved temporary (preserved across call)", "should not be referenced explicitly in your program" };
            this.columnToolTips = new String[] { "Each register has a tool tip describing its usage convention", "32-bit single precision IEEE 754 floating point register", "64-bit double precision IEEE 754 floating point register (uses a pair of 32-bit registers)" };
            this.setRowSelectionAllowed(true);
            this.setSelectionBackground(Color.GREEN);
        }
        
        @Override
        public String getToolTipText(final MouseEvent e) {
            String tip = null;
            final Point p = e.getPoint();
            final int rowIndex = this.rowAtPoint(p);
            final int colIndex = this.columnAtPoint(p);
            final int realColumnIndex = this.convertColumnIndexToModel(colIndex);
            if (realColumnIndex == 0) {
                tip = this.regToolTips[rowIndex];
            }
            else {
                tip = super.getToolTipText(e);
            }
            return tip;
        }
        
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(this.columnModel) {
                @Override
                public String getToolTipText(final MouseEvent e) {
                    final String tip = null;
                    final Point p = e.getPoint();
                    final int index = this.columnModel.getColumnIndexAtX(p.x);
                    final int realIndex = this.columnModel.getColumn(index).getModelIndex();
                    return MyTippedJTable.this.columnToolTips[realIndex];
                }
            };
        }
    }
}
