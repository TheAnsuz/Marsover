

package mars.venus;

import java.awt.Point;
import javax.swing.table.TableColumnModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import mars.assembler.Symbol;
import java.util.List;
import java.util.Collections;
import java.io.File;
import mars.assembler.SymbolTable;
import mars.mips.hardware.Memory;
import mars.util.Binary;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ItemEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import java.awt.Dimension;
import javax.swing.JComponent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.Box;
import mars.MIPSprogram;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.event.ItemListener;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import mars.Globals;
import java.util.Comparator;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Container;
import javax.swing.JInternalFrame;

public class LabelsWindow extends JInternalFrame
{
    private Container contentPane;
    private JPanel labelPanel;
    private JCheckBox dataLabels;
    private JCheckBox textLabels;
    private ArrayList<LabelsForSymbolTable> listOfLabelsForSymbolTable;
    private LabelsWindow labelsWindow;
    private static final int MAX_DISPLAYED_CHARS = 24;
    private static final int PREFERRED_NAME_COLUMN_WIDTH = 60;
    private static final int PREFERRED_ADDRESS_COLUMN_WIDTH = 60;
    private static final int LABEL_COLUMN = 0;
    private static final int ADDRESS_COLUMN = 1;
    private static final String[] columnToolTips;
    private static String[] columnNames;
    private Comparator tableSortComparator;
    private final Comparator[] tableSortingComparators;
    private static final int[][] sortStateTransitions;
    private static final char ASCENDING_SYMBOL = '\u25b2';
    private static final char DESCENDING_SYMBOL = '\u25bc';
    private static final String[][] sortColumnHeadings;
    private int sortState;
    
    public LabelsWindow() {
        super("Labels", true, false, true, true);
        this.tableSortingComparators = new Comparator[] { new LabelAddressAscendingComparator(), new DescendingComparator((Comparator)new LabelAddressAscendingComparator()), new LabelAddressAscendingComparator(), new DescendingComparator((Comparator)new LabelAddressAscendingComparator()), new LabelNameAscendingComparator(), new LabelNameAscendingComparator(), new DescendingComparator((Comparator)new LabelNameAscendingComparator()), new DescendingComparator((Comparator)new LabelNameAscendingComparator()) };
        this.sortState = 0;
        try {
            this.sortState = Integer.parseInt(Globals.getSettings().getLabelSortState());
        }
        catch (NumberFormatException nfe) {
            this.sortState = 0;
        }
        LabelsWindow.columnNames = LabelsWindow.sortColumnHeadings[this.sortState];
        this.tableSortComparator = this.tableSortingComparators[this.sortState];
        this.labelsWindow = this;
        this.contentPane = this.getContentPane();
        this.labelPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        final JPanel features = new JPanel();
        this.dataLabels = new JCheckBox("Data", true);
        this.textLabels = new JCheckBox("Text", true);
        this.dataLabels.addItemListener(new LabelItemListener());
        this.textLabels.addItemListener(new LabelItemListener());
        this.dataLabels.setToolTipText("If checked, will display labels defined in data segment");
        this.textLabels.setToolTipText("If checked, will display labels defined in text segment");
        features.add(this.dataLabels);
        features.add(this.textLabels);
        this.contentPane.add(features, "South");
        this.contentPane.add(this.labelPanel);
    }
    
    public void setupTable() {
        this.labelPanel.removeAll();
        this.labelPanel.add(this.generateLabelScrollPane());
    }
    
    public void clearWindow() {
        this.labelPanel.removeAll();
    }
    
    private JScrollPane generateLabelScrollPane() {
        (this.listOfLabelsForSymbolTable = new ArrayList()).add(new LabelsForSymbolTable(null));
        final ArrayList<MIPSprogram> MIPSprogramsAssembled = RunAssembleAction.getMIPSprogramsToAssemble();
        final Box allSymtabTables = Box.createVerticalBox();
        for (int i = 0; i < MIPSprogramsAssembled.size(); ++i) {
            this.listOfLabelsForSymbolTable.add(new LabelsForSymbolTable(MIPSprogramsAssembled.get(i)));
        }
        final ArrayList<JComponent> tableNames = new ArrayList();
        JTableHeader tableHeader = null;
        for (int j = 0; j < this.listOfLabelsForSymbolTable.size(); ++j) {
            final LabelsForSymbolTable symtab = this.listOfLabelsForSymbolTable.get(j);
            if (symtab.hasSymbols()) {
                String name = symtab.getSymbolTableName();
                if (name.length() > 24) {
                    name = name.substring(0, 21) + "...";
                }
                final JLabel nameLab = new JLabel(name, 2);
                final Box nameLabel = Box.createHorizontalBox();
                nameLabel.add(nameLab);
                nameLabel.add(Box.createHorizontalGlue());
                nameLabel.add(Box.createHorizontalStrut(1));
                tableNames.add(nameLabel);
                allSymtabTables.add(nameLabel);
                final JTable table = symtab.generateLabelTable();
                tableHeader = table.getTableHeader();
                tableHeader.setReorderingAllowed(false);
                table.setSelectionBackground(table.getBackground());
                table.addMouseListener(new LabelDisplayMouseListener());
                allSymtabTables.add(table);
            }
        }
        final JScrollPane labelScrollPane = new JScrollPane(allSymtabTables, 22, 30);
        for (int k = 0; k < tableNames.size(); ++k) {
            final JComponent nameLabel2 = tableNames.get(k);
            nameLabel2.setMaximumSize(new Dimension(labelScrollPane.getViewport().getViewSize().width, (int)(1.5 * nameLabel2.getFontMetrics(nameLabel2.getFont()).getHeight())));
        }
        labelScrollPane.setColumnHeaderView(tableHeader);
        return labelScrollPane;
    }
    
    public void updateLabelAddresses() {
        if (this.listOfLabelsForSymbolTable != null) {
            for (int i = 0; i < this.listOfLabelsForSymbolTable.size(); ++i) {
                this.listOfLabelsForSymbolTable.get(i).updateLabelAddresses();
            }
        }
    }
    
    static {
        columnToolTips = new String[] { "Programmer-defined label (identifier).", "Text or data segment address at which label is defined." };
        sortStateTransitions = new int[][] { { 4, 1 }, { 5, 0 }, { 6, 3 }, { 7, 2 }, { 6, 0 }, { 7, 1 }, { 4, 2 }, { 5, 3 } };
        sortColumnHeadings = new String[][] { { "Label", "Address  \u25b2" }, { "Label", "Address  \u25bc" }, { "Label", "Address  \u25b2" }, { "Label", "Address  \u25bc" }, { "Label  \u25b2", "Address" }, { "Label  \u25b2", "Address" }, { "Label  \u25bc", "Address" }, { "Label  \u25bc", "Address" } };
    }
    
    private class LabelItemListener implements ItemListener
    {
        @Override
        public void itemStateChanged(final ItemEvent ie) {
            for (int i = 0; i < LabelsWindow.this.listOfLabelsForSymbolTable.size(); ++i) {
                LabelsWindow.this.listOfLabelsForSymbolTable.get(i).generateLabelTable();
            }
        }
    }
    
    private class LabelDisplayMouseListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(final MouseEvent e) {
            final JTable table = (JTable)e.getSource();
            final int row = table.rowAtPoint(e.getPoint());
            final int column = table.columnAtPoint(e.getPoint());
            Object data = table.getValueAt(row, column);
            if (table.getColumnName(column).equals(LabelsWindow.columnNames[0])) {
                data = table.getModel().getValueAt(row, 1);
            }
            int address = 0;
            try {
                address = Binary.stringToInt((String)data);
            }
            catch (NumberFormatException ex) {}
            catch (ClassCastException ex2) {}
            if (Memory.inTextSegment(address) || Memory.inKernelTextSegment(address)) {
                Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().selectStepAtAddress(address);
            }
            else {
                Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().selectCellForAddress(address);
            }
        }
    }
    
    private class LabelsForSymbolTable
    {
        private MIPSprogram myMIPSprogram;
        private Object[][] labelData;
        private JTable labelTable;
        private ArrayList<Symbol> symbols;
        private SymbolTable symbolTable;
        private String tableName;
        
        public LabelsForSymbolTable(final MIPSprogram myMIPSprogram) {
            this.myMIPSprogram = myMIPSprogram;
            this.symbolTable = ((myMIPSprogram == null) ? Globals.symbolTable : myMIPSprogram.getLocalSymbolTable());
            this.tableName = ((myMIPSprogram == null) ? "(global)" : new File(myMIPSprogram.getFilename()).getName());
        }
        
        public String getSymbolTableName() {
            return this.tableName;
        }
        
        public boolean hasSymbols() {
            return this.symbolTable.getSize() != 0;
        }
        
        private JTable generateLabelTable() {
            final SymbolTable symbolTable = (this.myMIPSprogram == null) ? Globals.symbolTable : this.myMIPSprogram.getLocalSymbolTable();
            final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
            if (LabelsWindow.this.textLabels.isSelected() && LabelsWindow.this.dataLabels.isSelected()) {
                this.symbols = symbolTable.getAllSymbols();
            }
            else if (LabelsWindow.this.textLabels.isSelected() && !LabelsWindow.this.dataLabels.isSelected()) {
                this.symbols = symbolTable.getTextSymbols();
            }
            else if (!LabelsWindow.this.textLabels.isSelected() && LabelsWindow.this.dataLabels.isSelected()) {
                this.symbols = symbolTable.getDataSymbols();
            }
            else {
                this.symbols = new ArrayList();
            }
            Collections.sort(this.symbols, LabelsWindow.this.tableSortComparator);
            this.labelData = new Object[this.symbols.size()][2];
            for (int i = 0; i < this.symbols.size(); ++i) {
                final Symbol s = this.symbols.get(i);
                this.labelData[i][0] = s.getName();
                this.labelData[i][1] = NumberDisplayBaseChooser.formatNumber(s.getAddress(), addressBase);
            }
            final LabelTableModel m = new LabelTableModel(this.labelData, LabelsWindow.columnNames);
            if (this.labelTable == null) {
                this.labelTable = new MyTippedJTable(m);
            }
            else {
                this.labelTable.setModel(m);
            }
            this.labelTable.getColumnModel().getColumn(1).setCellRenderer(new MonoRightCellRenderer());
            return this.labelTable;
        }
        
        public void updateLabelAddresses() {
            if (LabelsWindow.this.labelPanel.getComponentCount() == 0) {
                return;
            }
            final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
            for (int numSymbols = (this.labelData == null) ? 0 : this.labelData.length, i = 0; i < numSymbols; ++i) {
                final int address = this.symbols.get(i).getAddress();
                final String formattedAddress = NumberDisplayBaseChooser.formatNumber(address, addressBase);
                this.labelTable.getModel().setValueAt(formattedAddress, i, 1);
            }
        }
    }
    
    class LabelTableModel extends AbstractTableModel
    {
        String[] columns;
        Object[][] data;
        
        public LabelTableModel(final Object[][] d, final String[] n) {
            this.data = d;
            this.columns = n;
        }
        
        @Override
        public int getColumnCount() {
            return this.columns.length;
        }
        
        @Override
        public int getRowCount() {
            return this.data.length;
        }
        
        @Override
        public String getColumnName(final int col) {
            return this.columns[col];
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
        public void setValueAt(final Object value, final int row, final int col) {
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
        MyTippedJTable(final LabelTableModel m) {
            super(m);
        }
        
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new SymbolTableHeader(this.columnModel);
        }
        
        @Override
        public Component prepareRenderer(final TableCellRenderer renderer, final int rowIndex, final int vColIndex) {
            final Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent) {
                final JComponent jc = (JComponent)c;
                jc.setToolTipText("Click on label or address to view it in Text/Data Segment");
            }
            return c;
        }
        
        private class SymbolTableHeader extends JTableHeader
        {
            public SymbolTableHeader(final TableColumnModel cm) {
                super(cm);
                this.addMouseListener(new SymbolTableHeaderMouseListener());
            }
            
            @Override
            public String getToolTipText(final MouseEvent e) {
                final Point p = e.getPoint();
                final int index = this.columnModel.getColumnIndexAtX(p.x);
                final int realIndex = this.columnModel.getColumn(index).getModelIndex();
                return LabelsWindow.columnToolTips[realIndex];
            }
            
            private class SymbolTableHeaderMouseListener implements MouseListener
            {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    final Point p = e.getPoint();
                    final int index = SymbolTableHeader.this.columnModel.getColumnIndexAtX(p.x);
                    final int realIndex = SymbolTableHeader.this.columnModel.getColumn(index).getModelIndex();
                    LabelsWindow.this.sortState = LabelsWindow.sortStateTransitions[LabelsWindow.this.sortState][realIndex];
                    LabelsWindow.this.tableSortComparator = LabelsWindow.this.tableSortingComparators[LabelsWindow.this.sortState];
                    LabelsWindow.columnNames = LabelsWindow.sortColumnHeadings[LabelsWindow.this.sortState];
                    Globals.getSettings().setLabelSortState(new Integer(LabelsWindow.this.sortState).toString());
                    LabelsWindow.this.setupTable();
                    Globals.getGui().getMainPane().getExecutePane().setLabelWindowVisibility(false);
                    Globals.getGui().getMainPane().getExecutePane().setLabelWindowVisibility(true);
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
    
    private class LabelNameAscendingComparator implements Comparator
    {
        @Override
        public int compare(final Object a, final Object b) {
            return ((Symbol)a).getName().toLowerCase().compareTo(((Symbol)b).getName().toLowerCase());
        }
    }
    
    private class LabelAddressAscendingComparator implements Comparator
    {
        @Override
        public int compare(final Object a, final Object b) {
            final int addrA = ((Symbol)a).getAddress();
            final int addrB = ((Symbol)b).getAddress();
            return ((addrA >= 0 && addrB >= 0) || (addrA < 0 && addrB < 0)) ? (addrA - addrB) : addrB;
        }
    }
    
    private class DescendingComparator implements Comparator
    {
        private Comparator opposite;
        
        private DescendingComparator(final Comparator opposite) {
            this.opposite = opposite;
        }
        
        @Override
        public int compare(final Object a, final Object b) {
            return this.opposite.compare(b, a);
        }
    }
}
