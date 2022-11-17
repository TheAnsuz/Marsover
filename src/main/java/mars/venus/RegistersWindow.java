

package mars.venus;

import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.Color;
import javax.swing.table.TableModel;
import mars.util.Binary;
import javax.swing.table.AbstractTableModel;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.table.DefaultTableCellRenderer;
import mars.mips.hardware.RegisterAccessNotice;
import mars.simulator.SimulatorNotice;
import java.util.Observable;
import javax.swing.event.TableModelEvent;
import mars.mips.hardware.RegisterFile;
import java.awt.Component;
import javax.swing.JScrollPane;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.table.TableCellRenderer;
import mars.Globals;
import mars.simulator.Simulator;
import mars.Settings;
import mars.mips.hardware.Register;
import javax.swing.JTable;
import java.util.Observer;
import javax.swing.JPanel;

public class RegistersWindow extends JPanel implements Observer
{
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private boolean highlighting;
    private int highlightRow;
    private ExecutePane executePane;
    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static Settings settings;
    
    public RegistersWindow() {
        Simulator.getInstance().addObserver(this);
        RegistersWindow.settings = Globals.getSettings();
        this.highlighting = false;
        RegistersWindow.table = new MyTippedJTable(new RegTableModel(this.setupWindow()));
        RegistersWindow.table.getColumnModel().getColumn(0).setPreferredWidth(25);
        RegistersWindow.table.getColumnModel().getColumn(1).setPreferredWidth(25);
        RegistersWindow.table.getColumnModel().getColumn(2).setPreferredWidth(60);
        RegistersWindow.table.getColumnModel().getColumn(0).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 2));
        RegistersWindow.table.getColumnModel().getColumn(1).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 4));
        RegistersWindow.table.getColumnModel().getColumn(2).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 4));
        RegistersWindow.table.setPreferredScrollableViewportSize(new Dimension(200, 700));
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(RegistersWindow.table, 20, 31));
    }
    
    public Object[][] setupWindow() {
        final int valueBase = NumberDisplayBaseChooser.getBase(RegistersWindow.settings.getDisplayValuesInHex());
        this.tableData = new Object[35][3];
        RegistersWindow.registers = RegisterFile.getRegisters();
        for (int i = 0; i < RegistersWindow.registers.length; ++i) {
            this.tableData[i][0] = RegistersWindow.registers[i].getName();
            this.tableData[i][1] = new Integer(RegistersWindow.registers[i].getNumber());
            this.tableData[i][2] = NumberDisplayBaseChooser.formatNumber(RegistersWindow.registers[i].getValue(), valueBase);
        }
        this.tableData[32][0] = "pc";
        this.tableData[32][1] = "";
        this.tableData[32][2] = NumberDisplayBaseChooser.formatUnsignedInteger(RegisterFile.getProgramCounter(), valueBase);
        this.tableData[33][0] = "hi";
        this.tableData[33][1] = "";
        this.tableData[33][2] = NumberDisplayBaseChooser.formatNumber(RegisterFile.getValue(33), valueBase);
        this.tableData[34][0] = "lo";
        this.tableData[34][1] = "";
        this.tableData[34][2] = NumberDisplayBaseChooser.formatNumber(RegisterFile.getValue(34), valueBase);
        return this.tableData;
    }
    
    public void clearWindow() {
        this.clearHighlighting();
        RegisterFile.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }
    
    public void clearHighlighting() {
        this.highlighting = false;
        if (RegistersWindow.table != null) {
            RegistersWindow.table.tableChanged(new TableModelEvent(RegistersWindow.table.getModel()));
        }
        this.highlightRow = -1;
    }
    
    public void refresh() {
        if (RegistersWindow.table != null) {
            RegistersWindow.table.tableChanged(new TableModelEvent(RegistersWindow.table.getModel()));
        }
    }
    
    public void updateRegisters() {
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }
    
    public void updateRegisters(final int base) {
        RegistersWindow.registers = RegisterFile.getRegisters();
        for (int i = 0; i < RegistersWindow.registers.length; ++i) {
            this.updateRegisterValue(RegistersWindow.registers[i].getNumber(), RegistersWindow.registers[i].getValue(), base);
        }
        this.updateRegisterUnsignedValue(32, RegisterFile.getProgramCounter(), base);
        this.updateRegisterValue(33, RegisterFile.getValue(33), base);
        this.updateRegisterValue(34, RegisterFile.getValue(34), base);
    }
    
    public void updateRegisterValue(final int number, final int val, final int base) {
        ((RegTableModel)RegistersWindow.table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(val, base), number, 2);
    }
    
    private void updateRegisterUnsignedValue(final int number, final int val, final int base) {
        ((RegTableModel)RegistersWindow.table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatUnsignedInteger(val, base), number, 2);
    }
    
    @Override
    public void update(final Observable observable, final Object obj) {
        if (observable == Simulator.getInstance()) {
            final SimulatorNotice notice = (SimulatorNotice)obj;
            if (notice.getAction() == 0) {
                if (notice.getRunSpeed() != 40.0 || notice.getMaxSteps() == 1) {
                    RegisterFile.addRegistersObserver(this);
                    this.highlighting = true;
                }
            }
            else {
                RegisterFile.deleteRegistersObserver(this);
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
        RegistersWindow.table.tableChanged(new TableModelEvent(RegistersWindow.table.getModel()));
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
            if (RegistersWindow.settings.getRegistersHighlighting() && RegistersWindow.this.highlighting && row == RegistersWindow.this.highlightRow) {
                cell.setBackground(RegistersWindow.settings.getColorSettingByPosition(10));
                cell.setForeground(RegistersWindow.settings.getColorSettingByPosition(11));
                cell.setFont(RegistersWindow.settings.getFontByPosition(6));
            }
            else if (row % 2 == 0) {
                cell.setBackground(RegistersWindow.settings.getColorSettingByPosition(0));
                cell.setForeground(RegistersWindow.settings.getColorSettingByPosition(1));
                cell.setFont(RegistersWindow.settings.getFontByPosition(1));
            }
            else {
                cell.setBackground(RegistersWindow.settings.getColorSettingByPosition(2));
                cell.setForeground(RegistersWindow.settings.getColorSettingByPosition(3));
                cell.setFont(RegistersWindow.settings.getFontByPosition(2));
            }
            return cell;
        }
    }
    
    class RegTableModel extends AbstractTableModel
    {
        final String[] columnNames;
        Object[][] data;
        
        public RegTableModel(final Object[][] d) {
            this.columnNames = new String[] { "Name", "Number", "Value" };
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
            return col == 2 && row != 0 && row != 32 && row != 31;
        }
        
        @Override
        public void setValueAt(final Object value, final int row, final int col) {
            int val = 0;
            try {
                val = Binary.stringToInt((String)value);
            }
            catch (NumberFormatException nfe) {
                this.data[row][col] = "INVALID";
                this.fireTableCellUpdated(row, col);
                return;
            }
            synchronized (Globals.memoryAndRegistersLock) {
                RegisterFile.updateRegister(row, val);
            }
            final int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
            this.data[row][col] = NumberDisplayBaseChooser.formatNumber(val, valueBase);
            this.fireTableCellUpdated(row, col);
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
            this.regToolTips = new String[] { "constant 0", "reserved for assembler", "expression evaluation and results of a function", "expression evaluation and results of a function", "argument 1", "argument 2", "argument 3", "argument 4", "temporary (not preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "saved temporary (preserved across call)", "temporary (not preserved across call)", "temporary (not preserved across call)", "reserved for OS kernel", "reserved for OS kernel", "pointer to global area", "stack pointer", "frame pointer", "return address (used by function call)", "program counter", "high-order word of multiply product, or divide remainder", "low-order word of multiply product, or divide quotient" };
            this.columnToolTips = new String[] { "Each register has a tool tip describing its usage convention", "Corresponding register number", "Current 32 bit value" };
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
