

package mars.venus;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.JTableHeader;
import java.awt.Insets;
import javax.swing.border.EmptyBorder;
import javax.swing.UIManager;
import javax.swing.border.Border;
import mars.Settings;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import mars.mips.hardware.AddressErrorException;
import java.awt.event.MouseListener;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.awt.Point;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import mars.mips.hardware.RegisterFile;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import java.util.Arrays;
import java.util.Enumeration;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.simulator.SimulatorNotice;
import java.util.Observable;
import mars.util.Binary;
import java.util.ArrayList;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import mars.util.EditorFont;
import mars.ProgramStatement;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import mars.Globals;
import mars.simulator.Simulator;
import javax.swing.event.TableModelListener;
import java.awt.Font;
import java.awt.Container;
import java.util.Hashtable;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.util.Observer;
import javax.swing.JInternalFrame;

public class TextSegmentWindow extends JInternalFrame implements Observer
{
    private JPanel programArgumentsPanel;
    private JTextField programArgumentsTextField;
    private static final int PROGRAM_ARGUMENT_TEXTFIELD_COLUMNS = 40;
    private JTable table;
    private JScrollPane tableScroller;
    private Object[][] data;
    private int[] intAddresses;
    private Hashtable<Integer,Integer> addressRows;
    private Hashtable<Integer, ModifiedCode> executeMods;
    private Container contentPane;
    private TextTableModel tableModel;
    private Font tableCellFont;
    private boolean codeHighlighting;
    private boolean breakpointsEnabled;
    private int highlightAddress;
    private TableModelListener tableModelListener;
    private boolean inDelaySlot;
    private static String[] columnNames;
    private static final int BREAK_COLUMN = 0;
    private static final int ADDRESS_COLUMN = 1;
    private static final int CODE_COLUMN = 2;
    private static final int BASIC_COLUMN = 3;
    private static final int SOURCE_COLUMN = 4;
    private static final Font monospacedPlain12Point;
    private static final String modifiedCodeMarker = " ------ ";
    
    public TextSegmentWindow() {
        super("Text Segment", true, false, true, true);
        this.tableCellFont = new Font("Monospaced", 0, 12);
        Simulator.getInstance().addObserver(this);
        Globals.getSettings().addObserver(this);
        this.contentPane = this.getContentPane();
        this.codeHighlighting = true;
        this.breakpointsEnabled = true;
        (this.programArgumentsPanel = new JPanel(new FlowLayout(0))).add(new JLabel("Program Arguments: "));
        (this.programArgumentsTextField = new JTextField(40)).setToolTipText("Arguments provided to program at runtime via $a0 (argc) and $a1 (argv)");
        this.programArgumentsPanel.add(this.programArgumentsTextField);
    }
    
    public void setupTable() {
        final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
        this.codeHighlighting = true;
        this.breakpointsEnabled = true;
        final ArrayList<ProgramStatement> sourceStatementList = Globals.program.getMachineList();
        this.data = new Object[sourceStatementList.size()][TextSegmentWindow.columnNames.length];
        this.intAddresses = new int[this.data.length];
        this.addressRows = new Hashtable(this.data.length);
        this.executeMods = new Hashtable<Integer, ModifiedCode>(this.data.length);
        int maxSourceLineNumber = 0;
        for (int i = sourceStatementList.size() - 1; i >= 0; --i) {
            final ProgramStatement statement = sourceStatementList.get(i);
            if (statement.getSourceLine() > maxSourceLineNumber) {
                maxSourceLineNumber = statement.getSourceLine();
            }
        }
        final int sourceLineDigits = ("" + maxSourceLineNumber).length();
        int leadingSpaces = 0;
        int lastLine = -1;
        for (int j = 0; j < sourceStatementList.size(); ++j) {
            final ProgramStatement statement2 = sourceStatementList.get(j);
            this.intAddresses[j] = statement2.getAddress();
            this.addressRows.put(new Integer(this.intAddresses[j]), new Integer(j));
            this.data[j][0] = Boolean.FALSE;
            this.data[j][1] = NumberDisplayBaseChooser.formatUnsignedInteger(statement2.getAddress(), addressBase);
            this.data[j][2] = NumberDisplayBaseChooser.formatNumber(statement2.getBinaryStatement(), 16);
            this.data[j][3] = statement2.getPrintableBasicAssemblyStatement();
            String sourceString = "";
            if (!statement2.getSource().equals("")) {
                leadingSpaces = sourceLineDigits - ("" + statement2.getSourceLine()).length();
                String lineNumber = "          ".substring(0, leadingSpaces) + statement2.getSourceLine() + ": ";
                if (statement2.getSourceLine() == lastLine) {
                    lineNumber = "          ".substring(0, sourceLineDigits) + "  ";
                }
                sourceString = lineNumber + EditorFont.substituteSpacesForTabs(statement2.getSource());
            }
            this.data[j][4] = sourceString;
            lastLine = statement2.getSourceLine();
        }
        this.contentPane.removeAll();
        this.tableModel = new TextTableModel(this.data);
        if (this.tableModelListener != null) {
            this.tableModel.addTableModelListener(this.tableModelListener);
            this.tableModel.fireTableDataChanged();
        }
        (this.table = new MyTippedJTable(this.tableModel)).setRowSelectionAllowed(false);
        this.table.getColumnModel().getColumn(0).setMinWidth(40);
        this.table.getColumnModel().getColumn(1).setMinWidth(80);
        this.table.getColumnModel().getColumn(2).setMinWidth(80);
        this.table.getColumnModel().getColumn(0).setMaxWidth(50);
        this.table.getColumnModel().getColumn(1).setMaxWidth(90);
        this.table.getColumnModel().getColumn(2).setMaxWidth(90);
        this.table.getColumnModel().getColumn(3).setMaxWidth(200);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(40);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(80);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(160);
        this.table.getColumnModel().getColumn(4).setPreferredWidth(280);
        final CodeCellRenderer codeStepHighlighter = new CodeCellRenderer();
        this.table.getColumnModel().getColumn(3).setCellRenderer(codeStepHighlighter);
        this.table.getColumnModel().getColumn(4).setCellRenderer(codeStepHighlighter);
        this.table.getColumnModel().getColumn(1).setCellRenderer(new MonoRightCellRenderer());
        this.table.getColumnModel().getColumn(2).setCellRenderer(new MachineCodeCellRenderer());
        this.table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxTableCellRenderer());
        this.reorderColumns();
        this.table.getColumnModel().addColumnModelListener(new MyTableColumnMovingListener());
        this.tableScroller = new JScrollPane(this.table, 22, 32);
        this.contentPane.add(this.tableScroller);
        if (Globals.getSettings().getProgramArguments()) {
            this.addProgramArgumentsPanel();
        }
        this.deleteAsTextSegmentObserver();
        if (Globals.getSettings().getBooleanSetting(20)) {
            this.addAsTextSegmentObserver();
        }
    }
    
    public String getProgramArguments() {
        return this.programArgumentsTextField.getText();
    }
    
    public void addProgramArgumentsPanel() {
        if (this.contentPane != null && this.contentPane.getComponentCount() > 0) {
            this.contentPane.add(this.programArgumentsPanel, "North");
            this.contentPane.validate();
        }
    }
    
    public void removeProgramArgumentsPanel() {
        if (this.contentPane != null) {
            this.contentPane.remove(this.programArgumentsPanel);
            this.contentPane.validate();
        }
    }
    
    public void clearWindow() {
        this.contentPane.removeAll();
    }
    
    public void registerTableModelListener(final TableModelListener tml) {
        this.tableModelListener = tml;
    }
    
    public void updateCodeAddresses() {
        if (this.contentPane.getComponentCount() == 0) {
            return;
        }
        final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
        for (int i = 0; i < this.intAddresses.length; ++i) {
            final String formattedAddress = NumberDisplayBaseChooser.formatUnsignedInteger(this.intAddresses[i], addressBase);
            this.table.getModel().setValueAt(formattedAddress, i, 1);
        }
    }
    
    public void updateBasicStatements() {
        if (this.contentPane.getComponentCount() == 0) {
            return;
        }
        final ArrayList<ProgramStatement> sourceStatementList = Globals.program.getMachineList();
        for (int i = 0; i < sourceStatementList.size(); ++i) {
            if (this.executeMods.get(i) == null) {
                final ProgramStatement statement = sourceStatementList.get(i);
                this.table.getModel().setValueAt(statement.getPrintableBasicAssemblyStatement(), i, 3);
            }
            else {
                try {
                    final ProgramStatement statement = new ProgramStatement(Binary.stringToInt((String)this.table.getModel().getValueAt(i, 2)), Binary.stringToInt((String)this.table.getModel().getValueAt(i, 1)));
                    this.table.getModel().setValueAt(statement.getPrintableBasicAssemblyStatement(), i, 3);
                }
                catch (NumberFormatException e) {
                    this.table.getModel().setValueAt("", i, 3);
                }
            }
        }
    }
    
    @Override
    public void update(final Observable observable, final Object obj) {
        if (observable == Simulator.getInstance()) {
            final SimulatorNotice notice = (SimulatorNotice)obj;
            if (notice.getAction() == 0) {
                this.deleteAsTextSegmentObserver();
                if (Globals.getSettings().getBooleanSetting(20)) {
                    this.addAsTextSegmentObserver();
                }
            }
        }
        else if (observable == Globals.getSettings()) {
            this.deleteAsTextSegmentObserver();
            if (Globals.getSettings().getBooleanSetting(20)) {
                this.addAsTextSegmentObserver();
            }
        }
        else if (obj instanceof MemoryAccessNotice) {
            final MemoryAccessNotice access = (MemoryAccessNotice)obj;
            if (access.getAccessType() == 1) {
                final int address = access.getAddress();
                final int value = access.getValue();
                final String strValue = Binary.intToHexString(access.getValue());
                String strBasic = " ------ ";
                String strSource = " ------ ";
                int row = 0;
                try {
                    row = this.findRowForAddress(address);
                }
                catch (IllegalArgumentException e) {
                    return;
                }
                ModifiedCode mc = this.executeMods.get(row);
                if (mc == null) {
                    if (this.tableModel.getValueAt(row, 2).equals(strValue)) {
                        return;
                    }
                    mc = new ModifiedCode(Integer.valueOf(row), this.tableModel.getValueAt(row, 2), this.tableModel.getValueAt(row, 3), this.tableModel.getValueAt(row, 4));
                    this.executeMods.put(row, mc);
                    strBasic = new ProgramStatement(value, address).getPrintableBasicAssemblyStatement();
                }
                else if (mc.getCode().equals(strValue)) {
                    strBasic = (String)mc.getBasic();
                    strSource = (String)mc.getSource();
                    this.executeMods.remove(row);
                }
                else {
                    strBasic = new ProgramStatement(value, address).getPrintableBasicAssemblyStatement();
                }
                this.data[row][2] = strValue;
                this.tableModel.fireTableCellUpdated(row, 2);
                this.tableModel.setValueAt(strBasic, row, 3);
                this.tableModel.setValueAt(strSource, row, 4);
                try {
                    Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().update(Memory.getInstance(), new MemoryAccessNotice(1, address, value));
                }
                catch (Exception ex) {}
            }
        }
    }
    
    void resetModifiedSourceCode() {
        if (this.executeMods != null && !this.executeMods.isEmpty()) {
            final Enumeration<ModifiedCode> elements = this.executeMods.elements();
            while (elements.hasMoreElements()) {
                final ModifiedCode mc = elements.nextElement();
                this.tableModel.setValueAt(mc.getCode(), mc.getRow(), 2);
                this.tableModel.setValueAt(mc.getBasic(), mc.getRow(), 3);
                this.tableModel.setValueAt(mc.getSource(), mc.getRow(), 4);
            }
            this.executeMods.clear();
        }
    }
    
    int getIntCodeAddressAtRow(final int row) {
        return this.intAddresses[row];
    }
    
    public int getBreakpointCount() {
        int breakpointCount = 0;
        for (int i = 0; i < this.data.length; ++i) {
            if ((Boolean)this.data[i][0]) {
                ++breakpointCount;
            }
        }
        return breakpointCount;
    }
    
    public int[] getSortedBreakPointsArray() {
        int breakpointCount = this.getBreakpointCount();
        if (breakpointCount == 0 || !this.breakpointsEnabled) {
            return null;
        }
        final int[] breakpoints = new int[breakpointCount];
        breakpointCount = 0;
        for (int i = 0; i < this.data.length; ++i) {
            if ((Boolean)this.data[i][0]) {
                breakpoints[breakpointCount++] = this.intAddresses[i];
            }
        }
        Arrays.sort(breakpoints);
        return breakpoints;
    }
    
    public void clearAllBreakpoints() {
        for (int i = 0; i < this.tableModel.getRowCount(); ++i) {
            if ((Boolean)this.data[i][0]) {
                this.tableModel.setValueAt(Boolean.FALSE, i, 0);
            }
        }
        ((JCheckBox)((DefaultCellEditor)this.table.getCellEditor(0, 0)).getComponent()).setSelected(false);
    }
    
    public void highlightStepAtPC() {
        this.highlightStepAtAddress(RegisterFile.getProgramCounter(), false);
    }
    
    public void highlightStepAtPC(final boolean inDelaySlot) {
        this.highlightStepAtAddress(RegisterFile.getProgramCounter(), inDelaySlot);
    }
    
    public void highlightStepAtAddress(final int address) {
        this.highlightStepAtAddress(address, false);
    }
    
    public void highlightStepAtAddress(final int address, final boolean inDelaySlot) {
        this.highlightAddress = address;
        int row = 0;
        try {
            row = this.findRowForAddress(address);
        }
        catch (IllegalArgumentException e) {
            return;
        }
        this.table.scrollRectToVisible(this.table.getCellRect(row, 0, true));
        this.inDelaySlot = inDelaySlot;
        this.table.tableChanged(new TableModelEvent(this.tableModel));
    }
    
    public void setCodeHighlighting(final boolean highlightSetting) {
        this.codeHighlighting = highlightSetting;
    }
    
    public boolean getCodeHighlighting() {
        return this.codeHighlighting;
    }
    
    public void unhighlightAllSteps() {
        final boolean saved = this.getCodeHighlighting();
        this.setCodeHighlighting(false);
        this.table.tableChanged(new TableModelEvent(this.tableModel, 0, this.data.length - 1, 3));
        this.table.tableChanged(new TableModelEvent(this.tableModel, 0, this.data.length - 1, 4));
        this.setCodeHighlighting(saved);
    }
    
    void selectStepAtAddress(final int address) {
        int addressRow = 0;
        try {
            addressRow = this.findRowForAddress(address);
        }
        catch (IllegalArgumentException e) {
            return;
        }
        final int addressSourceColumn = this.table.convertColumnIndexToView(4);
        final Rectangle sourceCell = this.table.getCellRect(addressRow, addressSourceColumn, true);
        final double cellHeight = sourceCell.getHeight();
        final double viewHeight = this.tableScroller.getViewport().getExtentSize().getHeight();
        final int numberOfVisibleRows = (int)(viewHeight / cellHeight);
        final int newViewPositionY = Math.max((int)((addressRow - numberOfVisibleRows / 2) * cellHeight), 0);
        this.tableScroller.getViewport().setViewPosition(new Point(0, newViewPositionY));
        final MouseEvent fakeMouseEvent = new MouseEvent(this.table, 501, new Date().getTime(), 16, (int)sourceCell.getX() + 1, (int)sourceCell.getY() + 1, 1, false);
        final MouseListener[] mouseListeners = this.table.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; ++i) {
            mouseListeners[i].mousePressed(fakeMouseEvent);
        }
    }
    
    public void toggleBreakpoints() {
        final Rectangle rect = ((MyTippedJTable)this.table).getRectForColumnIndex(0);
        final MouseEvent fakeMouseEvent = new MouseEvent(this.table, 500, new Date().getTime(), 16, (int)rect.getX(), (int)rect.getY(), 1, false);
        final MouseListener[] mouseListeners = ((MyTippedJTable)this.table).tableHeader.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; ++i) {
            mouseListeners[i].mouseClicked(fakeMouseEvent);
        }
    }
    
    private void addAsTextSegmentObserver() {
        try {
            Memory.getInstance().addObserver(this, Memory.textBaseAddress, Memory.dataSegmentBaseAddress);
        }
        catch (AddressErrorException ex) {}
    }
    
    private void deleteAsTextSegmentObserver() {
        Memory.getInstance().deleteObserver(this);
    }
    
    private void reorderColumns() {
        final TableColumnModel oldtcm = this.table.getColumnModel();
        final TableColumnModel newtcm = new DefaultTableColumnModel();
        final int[] savedColumnOrder = Globals.getSettings().getTextColumnOrder();
        if (savedColumnOrder.length == this.table.getColumnCount()) {
            for (int i = 0; i < savedColumnOrder.length; ++i) {
                newtcm.addColumn(oldtcm.getColumn(savedColumnOrder[i]));
            }
            this.table.setColumnModel(newtcm);
        }
    }
    
    private int findRowForAddress(final int address) throws IllegalArgumentException {
        int addressRow = 0;
        try {
            addressRow = this.addressRows.get(new Integer(address));
        }
        catch (NullPointerException e) {
            throw new IllegalArgumentException();
        }
        return addressRow;
    }
    
    static {
        TextSegmentWindow.columnNames = new String[] { "Bkpt", "Address", "Code", "Basic", "Source" };
        monospacedPlain12Point = new Font("Monospaced", 0, 12);
    }
    
    class TextTableModel extends AbstractTableModel
    {
        Object[][] data;
        
        public TextTableModel(final Object[][] d) {
            this.data = d;
        }
        
        @Override
        public int getColumnCount() {
            return TextSegmentWindow.columnNames.length;
        }
        
        @Override
        public int getRowCount() {
            return this.data.length;
        }
        
        @Override
        public String getColumnName(final int col) {
            return TextSegmentWindow.columnNames[col];
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
            return col == 0 || (col == 2 && Globals.getSettings().getBooleanSetting(20));
        }
        
        @Override
        public void setValueAt(final Object value, final int row, final int col) {
            if (col != 2) {
                this.data[row][col] = value;
                this.fireTableCellUpdated(row, col);
                return;
            }
            int val = 0;
            int address = 0;
            if (value.equals(this.data[row][col])) {
                return;
            }
            try {
                val = Binary.stringToInt((String)value);
            }
            catch (NumberFormatException nfe) {
                this.data[row][col] = "INVALID";
                this.fireTableCellUpdated(row, col);
                return;
            }
            try {
                address = Binary.stringToInt((String)this.data[row][1]);
            }
            catch (NumberFormatException ex) {}
            synchronized (Globals.memoryAndRegistersLock) {
                try {
                    Globals.memory.setRawWord(address, val);
                }
                catch (AddressErrorException aee) {}
            }
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
    
    private class ModifiedCode
    {
        private Integer row;
        private Object code;
        private Object basic;
        private Object source;
        
        private ModifiedCode(final Integer row, final Object code, final Object basic, final Object source) {
            this.row = row;
            this.code = code;
            this.basic = basic;
            this.source = source;
        }
        
        private Integer getRow() {
            return this.row;
        }
        
        private Object getCode() {
            return this.code;
        }
        
        private Object getBasic() {
            return this.basic;
        }
        
        private Object getSource() {
            return this.source;
        }
    }
    
    class CodeCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            final TextSegmentWindow textSegment = Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow();
            final Settings settings = Globals.getSettings();
            final boolean highlighting = textSegment.getCodeHighlighting();
            if (highlighting && textSegment.getIntCodeAddressAtRow(row) == TextSegmentWindow.this.highlightAddress) {
                if (Simulator.inDelaySlot() || textSegment.inDelaySlot) {
                    cell.setBackground(settings.getColorSettingByPosition(6));
                    cell.setForeground(settings.getColorSettingByPosition(7));
                    cell.setFont(settings.getFontByPosition(4));
                }
                else {
                    cell.setBackground(settings.getColorSettingByPosition(4));
                    cell.setForeground(settings.getColorSettingByPosition(5));
                    cell.setFont(settings.getFontByPosition(3));
                }
            }
            else if (row % 2 == 0) {
                cell.setBackground(settings.getColorSettingByPosition(0));
                cell.setForeground(settings.getColorSettingByPosition(1));
                cell.setFont(settings.getFontByPosition(1));
            }
            else {
                cell.setBackground(settings.getColorSettingByPosition(2));
                cell.setForeground(settings.getColorSettingByPosition(3));
                cell.setFont(settings.getFontByPosition(2));
            }
            return cell;
        }
    }
    
    class MachineCodeCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setFont(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT);
            cell.setHorizontalAlignment(4);
            if (row % 2 == 0) {
                cell.setBackground(Globals.getSettings().getColorSettingByPosition(0));
                cell.setForeground(Globals.getSettings().getColorSettingByPosition(1));
            }
            else {
                cell.setBackground(Globals.getSettings().getColorSettingByPosition(2));
                cell.setForeground(Globals.getSettings().getColorSettingByPosition(3));
            }
            return cell;
        }
    }
    
    class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer
    {
        Border noFocusBorder;
        Border focusBorder;
        
        public CheckBoxTableCellRenderer() {
            this.setContentAreaFilled(true);
            this.setBorderPainted(true);
            this.setHorizontalAlignment(0);
            this.setVerticalAlignment(0);
        }
        
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            if (table != null) {
                if (isSelected) {
                    this.setForeground(table.getSelectionForeground());
                    this.setBackground(table.getSelectionBackground());
                }
                else {
                    this.setForeground(table.getForeground());
                    this.setBackground(table.getBackground());
                }
                this.setEnabled(table.isEnabled() && TextSegmentWindow.this.breakpointsEnabled);
                this.setComponentOrientation(table.getComponentOrientation());
                if (hasFocus) {
                    if (this.focusBorder == null) {
                        this.focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
                    }
                    this.setBorder(this.focusBorder);
                }
                else {
                    if (this.noFocusBorder == null) {
                        if (this.focusBorder == null) {
                            this.focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
                        }
                        if (this.focusBorder != null) {
                            final Insets n = this.focusBorder.getBorderInsets(this);
                            this.noFocusBorder = new EmptyBorder(n);
                        }
                    }
                    this.setBorder(this.noFocusBorder);
                }
                this.setSelected(Boolean.TRUE.equals(value));
            }
            return this;
        }
    }
    
    private class MyTippedJTable extends JTable
    {
        private JTableHeader tableHeader;
        private String[] columnToolTips;
        
        MyTippedJTable(final TextTableModel m) {
            super(m);
            this.columnToolTips = new String[] { "If checked, will set an execution breakpoint. Click header to disable/enable breakpoints", "Text segment address of binary instruction code", "32-bit binary MIPS instruction", "Basic assembler instruction", "Source code line" };
        }
        
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return this.tableHeader = new TextTableHeader(this.columnModel);
        }
        
        public Rectangle getRectForColumnIndex(final int realIndex) {
            for (int i = 0; i < this.columnModel.getColumnCount(); ++i) {
                if (this.columnModel.getColumn(i).getModelIndex() == realIndex) {
                    return this.tableHeader.getHeaderRect(i);
                }
            }
            return this.tableHeader.getHeaderRect(realIndex);
        }
        
        private class TextTableHeader extends JTableHeader
        {
            public TextTableHeader(final TableColumnModel cm) {
                super(cm);
                this.addMouseListener(new TextTableHeaderMouseListener());
            }
            
            @Override
            public String getToolTipText(final MouseEvent e) {
                final Point p = e.getPoint();
                final int index = this.columnModel.getColumnIndexAtX(p.x);
                final int realIndex = this.columnModel.getColumn(index).getModelIndex();
                return MyTippedJTable.this.columnToolTips[realIndex];
            }
            
            private class TextTableHeaderMouseListener implements MouseListener
            {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    final Point p = e.getPoint();
                    final int index = TextTableHeader.this.columnModel.getColumnIndexAtX(p.x);
                    final int realIndex = TextTableHeader.this.columnModel.getColumn(index).getModelIndex();
                    if (realIndex == 0) {
                        final JCheckBox check = (JCheckBox)((DefaultCellEditor)TextTableHeader.this.table.getCellEditor(0, index)).getComponent();
                        TextSegmentWindow.this.breakpointsEnabled = !TextSegmentWindow.this.breakpointsEnabled;
                        check.setEnabled(TextSegmentWindow.this.breakpointsEnabled);
                        TextTableHeader.this.table.tableChanged(new TableModelEvent(TextSegmentWindow.this.tableModel, 0, TextSegmentWindow.this.data.length - 1, 0));
                    }
                }
                
                @Override
                public void mouseEntered(final MouseEvent e) {
                }
                
                @Override
                public void mouseExited(final MouseEvent e) {
                }
                
                @Override
                public void mousePressed(final MouseEvent e) {
                }
                
                @Override
                public void mouseReleased(final MouseEvent e) {
                }
            }
        }
    }
    
    private class MyTableColumnMovingListener implements TableColumnModelListener
    {
        @Override
        public void columnAdded(final TableColumnModelEvent e) {
        }
        
        @Override
        public void columnRemoved(final TableColumnModelEvent e) {
        }
        
        @Override
        public void columnMarginChanged(final ChangeEvent e) {
        }
        
        @Override
        public void columnSelectionChanged(final ListSelectionEvent e) {
        }
        
        @Override
        public void columnMoved(final TableColumnModelEvent e) {
            final int[] columnOrder = new int[TextSegmentWindow.this.table.getColumnCount()];
            for (int i = 0; i < columnOrder.length; ++i) {
                columnOrder[i] = TextSegmentWindow.this.table.getColumnModel().getColumn(i).getModelIndex();
            }
            final int[] oldOrder = Globals.getSettings().getTextColumnOrder();
            for (int j = 0; j < columnOrder.length; ++j) {
                if (oldOrder[j] != columnOrder[j]) {
                    Globals.getSettings().setTextColumnOrder(columnOrder);
                    break;
                }
            }
        }
    }
}
