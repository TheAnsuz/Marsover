

package mars.venus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import mars.Globals;
import mars.Settings;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Register;
import mars.mips.hardware.RegisterAccessNotice;
import mars.simulator.Simulator;
import mars.simulator.SimulatorNotice;
import mars.util.Binary;

public class Coprocessor0Window extends JPanel implements Observer
{
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private boolean highlighting;
    private int highlightRow;
    private ExecutePane executePane;
    private int[] rowGivenRegNumber;
    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static Settings settings;
    
    public Coprocessor0Window() {
        Simulator.getInstance().addObserver(this);
        Coprocessor0Window.settings = Globals.getSettings();
        this.highlighting = false;
        Coprocessor0Window.table = new MyTippedJTable(new RegTableModel(this.setupWindow()));
        Coprocessor0Window.table.getColumnModel().getColumn(0).setPreferredWidth(50);
        Coprocessor0Window.table.getColumnModel().getColumn(1).setPreferredWidth(25);
        Coprocessor0Window.table.getColumnModel().getColumn(2).setPreferredWidth(60);
        Coprocessor0Window.table.getColumnModel().getColumn(0).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 2));
        Coprocessor0Window.table.getColumnModel().getColumn(1).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 4));
        Coprocessor0Window.table.getColumnModel().getColumn(2).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, 4));
        Coprocessor0Window.table.setPreferredScrollableViewportSize(new Dimension(200, 700));
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(Coprocessor0Window.table, 20, 30));
    }
    
    public Object[][] setupWindow() {
        Coprocessor0Window.registers = Coprocessor0.getRegisters();
        this.tableData = new Object[Coprocessor0Window.registers.length][3];
        this.rowGivenRegNumber = new int[32];
        for (int i = 0; i < Coprocessor0Window.registers.length; ++i) {
            this.rowGivenRegNumber[Coprocessor0Window.registers[i].getNumber()] = i;
            this.tableData[i][0] = Coprocessor0Window.registers[i].getName();
            this.tableData[i][1] = Coprocessor0Window.registers[i].getNumber();
            this.tableData[i][2] = NumberDisplayBaseChooser.formatNumber(Coprocessor0Window.registers[i].getValue(), NumberDisplayBaseChooser.getBase(Coprocessor0Window.settings.getDisplayValuesInHex()));
        }
        return this.tableData;
    }
    
    public void clearWindow() {
        this.clearHighlighting();
        Coprocessor0.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }
    
    public void clearHighlighting() {
        this.highlighting = false;
        if (Coprocessor0Window.table != null) {
            Coprocessor0Window.table.tableChanged(new TableModelEvent(Coprocessor0Window.table.getModel()));
        }
        this.highlightRow = -1;
    }
    
    public void refresh() {
        if (Coprocessor0Window.table != null) {
            Coprocessor0Window.table.tableChanged(new TableModelEvent(Coprocessor0Window.table.getModel()));
        }
    }
    
    public void updateRegisters() {
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }
    
    public void updateRegisters(final int base) {
        Coprocessor0Window.registers = Coprocessor0.getRegisters();
        for (int i = 0; i < Coprocessor0Window.registers.length; ++i) {
            this.updateRegisterValue(Coprocessor0Window.registers[i].getNumber(), Coprocessor0Window.registers[i].getValue(), base);
        }
    }
    
    public void updateRegisterValue(final int number, final int val, final int base) {
        ((RegTableModel)Coprocessor0Window.table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(val, base), this.rowGivenRegNumber[number], 2);
    }
    
    @Override
    public void update(final Observable observable, final Object obj) {
        if (observable == Simulator.getInstance()) {
            final SimulatorNotice notice = (SimulatorNotice)obj;
            if (notice.getAction() == 0) {
                if (notice.getRunSpeed() != 40.0 || notice.getMaxSteps() == 1) {
                    Coprocessor0.addRegistersObserver(this);
                    this.highlighting = true;
                }
            }
            else {
                Coprocessor0.deleteRegistersObserver(this);
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
        final int registerRow = Coprocessor0.getRegisterPosition(register);
        if (registerRow < 0) {
            return;
        }
        this.highlightRow = registerRow;
        Coprocessor0Window.table.tableChanged(new TableModelEvent(Coprocessor0Window.table.getModel()));
    }
    
    private class RegisterCellRenderer extends DefaultTableCellRenderer
    {
        private final Font font;
        private final int alignment;
        
        public RegisterCellRenderer(final Font font, final int alignment) {
            this.font = font;
            this.alignment = alignment;
        }
        
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setFont(this.font);
            cell.setHorizontalAlignment(this.alignment);
            if (Coprocessor0Window.settings.getRegistersHighlighting() && Coprocessor0Window.this.highlighting && row == Coprocessor0Window.this.highlightRow) {
                cell.setBackground(Coprocessor0Window.settings.getColorSettingByPosition(10));
                cell.setForeground(Coprocessor0Window.settings.getColorSettingByPosition(11));
                cell.setFont(Coprocessor0Window.settings.getFontByPosition(6));
            }
            else if (row % 2 == 0) {
                cell.setBackground(Coprocessor0Window.settings.getColorSettingByPosition(0));
                cell.setForeground(Coprocessor0Window.settings.getColorSettingByPosition(1));
                cell.setFont(Coprocessor0Window.settings.getFontByPosition(1));
            }
            else {
                cell.setBackground(Coprocessor0Window.settings.getColorSettingByPosition(2));
                cell.setForeground(Coprocessor0Window.settings.getColorSettingByPosition(3));
                cell.setFont(Coprocessor0Window.settings.getFontByPosition(2));
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
            return col == 2;
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
                Coprocessor0.updateRegister(Coprocessor0Window.registers[row].getNumber(), val);
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
        private final String[] regToolTips;
        private final String[] columnToolTips;
        
        MyTippedJTable(final RegTableModel m) {
            super(m);
            this.regToolTips = new String[] { "Memory address at which address exception occurred", "Interrupt mask and enable bits", "Exception type and pending interrupt bits", "Address of instruction that caused exception" };
            this.columnToolTips = new String[] { "Each register has a tool tip describing its usage convention", "Register number.  In your program, precede it with $", "Current 32 bit value" };
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
