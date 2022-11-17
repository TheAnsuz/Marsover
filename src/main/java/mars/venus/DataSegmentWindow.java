

package mars.venus;

import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultComboBoxModel;
import mars.mips.hardware.MemoryAccessNotice;
import mars.simulator.SimulatorNotice;
import java.util.Observable;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellRenderer;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import javax.swing.event.TableModelEvent;
import mars.util.Binary;
import java.awt.event.MouseListener;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import mars.Globals;
import mars.simulator.Simulator;
import mars.mips.hardware.Memory;
import javax.swing.JComboBox;
import mars.Settings;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Container;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.util.Observer;
import javax.swing.JInternalFrame;

public class DataSegmentWindow extends JInternalFrame implements Observer
{
    private static final String[] dataSegmentNames;
    private static Object[][] dataData;
    private static JTable dataTable;
    private JScrollPane dataTableScroller;
    private Container contentPane;
    private JPanel tablePanel;
    private JButton dataButton;
    private JButton nextButton;
    private JButton prevButton;
    private JButton stakButton;
    private JButton globButton;
    private JButton heapButton;
    private JButton kernButton;
    private JButton extnButton;
    private JButton mmioButton;
    private JButton textButton;
    private JCheckBox asciiDisplayCheckBox;
    static final int VALUES_PER_ROW = 8;
    static final int NUMBER_OF_ROWS = 16;
    static final int NUMBER_OF_COLUMNS = 9;
    static final int BYTES_PER_VALUE = 4;
    static final int BYTES_PER_ROW = 32;
    static final int MEMORY_CHUNK_SIZE = 512;
    static final int PREV_NEXT_CHUNK_SIZE = 256;
    static final int ADDRESS_COLUMN = 0;
    static final boolean USER_MODE = false;
    static final boolean KERNEL_MODE = true;
    private boolean addressHighlighting;
    private boolean asciiDisplay;
    private int addressRow;
    private int addressColumn;
    private int addressRowFirstAddress;
    private Settings settings;
    int firstAddress;
    int homeAddress;
    boolean userOrKernelMode;
    JComboBox baseAddressSelector;
    private String[] displayBaseAddressChoices;
    private int[] displayBaseAddresses;
    private int defaultBaseAddressIndex;
    JButton[] baseAddressButtons;
    private static final int EXTERN_BASE_ADDRESS_INDEX = 0;
    private static final int GLOBAL_POINTER_ADDRESS_INDEX = 3;
    private static final int TEXT_BASE_ADDRESS_INDEX = 5;
    private static final int DATA_BASE_ADDRESS_INDEX = 1;
    private static final int HEAP_BASE_ADDRESS_INDEX = 2;
    private static final int STACK_POINTER_BASE_ADDRESS_INDEX = 4;
    private static final int KERNEL_DATA_BASE_ADDRESS_INDEX = 6;
    private static final int MMIO_BASE_ADDRESS_INDEX = 7;
    private int[] displayBaseAddressArray;
    String[] descriptions;
    
    public DataSegmentWindow(final NumberDisplayBaseChooser[] choosers) {
        super("Data Segment", true, false, true, true);
        this.addressHighlighting = false;
        this.asciiDisplay = false;
        this.displayBaseAddressArray = new int[] { Memory.externBaseAddress, Memory.dataBaseAddress, Memory.heapBaseAddress, -1, -1, Memory.textBaseAddress, Memory.kernelDataBaseAddress, Memory.memoryMapBaseAddress };
        this.descriptions = new String[] { " (.extern)", " (.data)", " (heap)", "current $gp", "current $sp", " (.text)", " (.kdata)", " (MMIO)" };
        Simulator.getInstance().addObserver(this);
        (this.settings = Globals.getSettings()).addObserver(this);
        final Memory memory = Globals.memory;
        this.homeAddress = Memory.dataBaseAddress;
        this.firstAddress = this.homeAddress;
        this.userOrKernelMode = false;
        this.addressHighlighting = false;
        this.contentPane = this.getContentPane();
        this.tablePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        final JPanel features = new JPanel();
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Class cs = this.getClass();
        try {
            this.prevButton = new PrevButton(new ImageIcon(tk.getImage(cs.getResource("/images/Previous22.png"))));
            this.nextButton = new NextButton(new ImageIcon(tk.getImage(cs.getResource("/images/Next22.png"))));
            this.dataButton = new JButton();
            this.stakButton = new JButton();
            this.globButton = new JButton();
            this.heapButton = new JButton();
            this.extnButton = new JButton();
            this.mmioButton = new JButton();
            this.textButton = new JButton();
            this.kernButton = new JButton();
        }
        catch (NullPointerException e) {
            System.out.println("Internal Error: images folder not found");
            System.exit(0);
        }
        this.initializeBaseAddressChoices();
        (this.baseAddressSelector = new JComboBox()).setModel(new CustomComboBoxModel(this.displayBaseAddressChoices));
        this.baseAddressSelector.setEditable(false);
        this.baseAddressSelector.setSelectedIndex(this.defaultBaseAddressIndex);
        this.baseAddressSelector.setToolTipText("Base address for data segment display");
        this.baseAddressSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DataSegmentWindow.this.baseAddressButtons[DataSegmentWindow.this.baseAddressSelector.getSelectedIndex()].getActionListeners()[0].actionPerformed(null);
            }
        });
        this.addButtonActionListenersAndInitialize();
        final JPanel navButtons = new JPanel(new GridLayout(1, 4));
        navButtons.add(this.prevButton);
        navButtons.add(this.nextButton);
        features.add(navButtons);
        features.add(this.baseAddressSelector);
        for (int i = 0; i < choosers.length; ++i) {
            features.add(choosers[i]);
        }
        (this.asciiDisplayCheckBox = new JCheckBox("ASCII", this.asciiDisplay)).setToolTipText("Display data segment values in ASCII (overrides Hexadecimal Values setting)");
        this.asciiDisplayCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                DataSegmentWindow.this.asciiDisplay = (e.getStateChange() == 1);
                DataSegmentWindow.this.updateValues();
            }
        });
        features.add(this.asciiDisplayCheckBox);
        this.contentPane.add(features, "South");
    }
    
    public void updateBaseAddressComboBox() {
        this.displayBaseAddressArray[0] = Memory.externBaseAddress;
        this.displayBaseAddressArray[3] = -1;
        this.displayBaseAddressArray[1] = Memory.dataBaseAddress;
        this.displayBaseAddressArray[2] = Memory.heapBaseAddress;
        this.displayBaseAddressArray[4] = -1;
        this.displayBaseAddressArray[6] = Memory.kernelDataBaseAddress;
        this.displayBaseAddressArray[7] = Memory.memoryMapBaseAddress;
        this.displayBaseAddressArray[5] = Memory.textBaseAddress;
        this.displayBaseAddressChoices = this.createBaseAddressLabelsArray(this.displayBaseAddressArray, this.descriptions);
        this.baseAddressSelector.setModel(new CustomComboBoxModel(this.displayBaseAddressChoices));
        this.displayBaseAddresses = this.displayBaseAddressArray;
        this.baseAddressSelector.setSelectedIndex(this.defaultBaseAddressIndex);
    }
    
    void selectCellForAddress(final int address) {
        final Point rowColumn = this.displayCellForAddress(address);
        if (rowColumn == null) {
            return;
        }
        final Rectangle addressCell = DataSegmentWindow.dataTable.getCellRect(rowColumn.x, rowColumn.y, true);
        final MouseEvent fakeMouseEvent = new MouseEvent(DataSegmentWindow.dataTable, 501, new Date().getTime(), 16, (int)addressCell.getX() + 1, (int)addressCell.getY() + 1, 1, false);
        final MouseListener[] mouseListeners = DataSegmentWindow.dataTable.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; ++i) {
            mouseListeners[i].mousePressed(fakeMouseEvent);
        }
    }
    
    void highlightCellForAddress(final int address) {
        final Point rowColumn = this.displayCellForAddress(address);
        if (rowColumn == null || rowColumn.x < 0 || rowColumn.y < 0) {
            return;
        }
        this.addressRow = rowColumn.x;
        this.addressColumn = rowColumn.y;
        this.addressRowFirstAddress = Binary.stringToInt(DataSegmentWindow.dataTable.getValueAt(this.addressRow, 0).toString());
        DataSegmentWindow.dataTable.tableChanged(new TableModelEvent(DataSegmentWindow.dataTable.getModel(), 0, DataSegmentWindow.dataData.length - 1));
    }
    
    private Point displayCellForAddress(final int address) {
        final int desiredComboBoxIndex = this.getBaseAddressIndexForAddress(address);
        if (desiredComboBoxIndex < 0) {
            return null;
        }
        this.baseAddressSelector.setSelectedIndex(desiredComboBoxIndex);
        ((CustomComboBoxModel)this.baseAddressSelector.getModel()).forceComboBoxUpdate(desiredComboBoxIndex);
        this.baseAddressButtons[desiredComboBoxIndex].getActionListeners()[0].actionPerformed(null);
        int baseAddress = this.displayBaseAddressArray[desiredComboBoxIndex];
        if (baseAddress == -1) {
            if (desiredComboBoxIndex == 3) {
                baseAddress = RegisterFile.getValue(28) - RegisterFile.getValue(28) % 32;
            }
            else {
                if (desiredComboBoxIndex != 4) {
                    return null;
                }
                baseAddress = RegisterFile.getValue(29) - RegisterFile.getValue(29) % 32;
            }
        }
        final int byteOffset = address - baseAddress;
        final int chunkOffset = byteOffset / 512;
        final int byteOffsetIntoChunk = byteOffset % 512;
        this.firstAddress = this.firstAddress + chunkOffset * 512 - 256;
        this.nextButton.getActionListeners()[0].actionPerformed(null);
        final int addrRow = byteOffsetIntoChunk / 32;
        int addrColumn = byteOffsetIntoChunk % 32 / 4 + 1;
        addrColumn = DataSegmentWindow.dataTable.convertColumnIndexToView(addrColumn);
        final Rectangle addressCell = DataSegmentWindow.dataTable.getCellRect(addrRow, addrColumn, true);
        final double cellHeight = addressCell.getHeight();
        final double viewHeight = this.dataTableScroller.getViewport().getExtentSize().getHeight();
        final int numberOfVisibleRows = (int)(viewHeight / cellHeight);
        final int newViewPositionY = Math.max((int)((addrRow - numberOfVisibleRows / 2) * cellHeight), 0);
        this.dataTableScroller.getViewport().setViewPosition(new Point(0, newViewPositionY));
        return new Point(addrRow, addrColumn);
    }
    
    private void initializeBaseAddressChoices() {
        (this.baseAddressButtons = new JButton[this.descriptions.length])[0] = this.extnButton;
        this.baseAddressButtons[3] = this.globButton;
        this.baseAddressButtons[1] = this.dataButton;
        this.baseAddressButtons[2] = this.heapButton;
        this.baseAddressButtons[4] = this.stakButton;
        this.baseAddressButtons[6] = this.kernButton;
        this.baseAddressButtons[7] = this.mmioButton;
        this.baseAddressButtons[5] = this.textButton;
        this.displayBaseAddresses = this.displayBaseAddressArray;
        this.displayBaseAddressChoices = this.createBaseAddressLabelsArray(this.displayBaseAddressArray, this.descriptions);
        this.defaultBaseAddressIndex = 1;
    }
    
    private String[] createBaseAddressLabelsArray(final int[] baseAddressArray, final String[] descriptions) {
        final String[] baseAddressChoices = new String[baseAddressArray.length];
        for (int i = 0; i < baseAddressChoices.length; ++i) {
            baseAddressChoices[i] = ((baseAddressArray[i] != -1) ? Binary.intToHexString(baseAddressArray[i]) : "") + descriptions[i];
        }
        return baseAddressChoices;
    }
    
    private int getBaseAddressIndexForAddress(final int address) {
        int desiredComboBoxIndex = -1;
        if (Memory.inKernelDataSegment(address)) {
            return 6;
        }
        if (Memory.inMemoryMapSegment(address)) {
            return 7;
        }
        if (Memory.inTextSegment(address)) {
            return 5;
        }
        int shortDistance = Integer.MAX_VALUE;
        int thisDistance = address - Memory.externBaseAddress;
        if (thisDistance >= 0 && thisDistance < shortDistance) {
            shortDistance = thisDistance;
            desiredComboBoxIndex = 0;
        }
        thisDistance = Math.abs(address - RegisterFile.getValue(28));
        if (thisDistance < shortDistance) {
            shortDistance = thisDistance;
            desiredComboBoxIndex = 3;
        }
        thisDistance = address - Memory.dataBaseAddress;
        if (thisDistance >= 0 && thisDistance < shortDistance) {
            shortDistance = thisDistance;
            desiredComboBoxIndex = 1;
        }
        thisDistance = address - Memory.heapBaseAddress;
        if (thisDistance >= 0 && thisDistance < shortDistance) {
            shortDistance = thisDistance;
            desiredComboBoxIndex = 2;
        }
        thisDistance = Math.abs(address - RegisterFile.getValue(29));
        if (thisDistance < shortDistance) {
            shortDistance = thisDistance;
            desiredComboBoxIndex = 4;
        }
        return desiredComboBoxIndex;
    }
    
    private JScrollPane generateDataPanel() {
        DataSegmentWindow.dataData = new Object[16][9];
        final int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
        final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
        int address = this.homeAddress;
        for (int row = 0; row < 16; ++row) {
            DataSegmentWindow.dataData[row][0] = NumberDisplayBaseChooser.formatUnsignedInteger(address, addressBase);
            for (int column = 1; column < 9; ++column) {
                try {
                    DataSegmentWindow.dataData[row][column] = NumberDisplayBaseChooser.formatNumber(Globals.memory.getRawWord(address), valueBase);
                }
                catch (AddressErrorException aee) {
                    DataSegmentWindow.dataData[row][column] = NumberDisplayBaseChooser.formatNumber(0, valueBase);
                }
                address += 4;
            }
        }
        final String[] names = new String[9];
        for (int i = 0; i < 9; ++i) {
            names[i] = this.getHeaderStringForColumn(i, addressBase);
        }
        DataSegmentWindow.dataTable = new MyTippedJTable(new DataTableModel(DataSegmentWindow.dataData, names));
        DataSegmentWindow.dataTable.getTableHeader().setReorderingAllowed(false);
        DataSegmentWindow.dataTable.setRowSelectionAllowed(false);
        final MonoRightCellRenderer monoRightCellRenderer = new MonoRightCellRenderer();
        DataSegmentWindow.dataTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        DataSegmentWindow.dataTable.getColumnModel().getColumn(0).setCellRenderer(monoRightCellRenderer);
        final AddressCellRenderer addressCellRenderer = new AddressCellRenderer();
        for (int j = 1; j < 9; ++j) {
            DataSegmentWindow.dataTable.getColumnModel().getColumn(j).setPreferredWidth(60);
            DataSegmentWindow.dataTable.getColumnModel().getColumn(j).setCellRenderer(addressCellRenderer);
        }
        return this.dataTableScroller = new JScrollPane(DataSegmentWindow.dataTable, 22, 32);
    }
    
    private String getHeaderStringForColumn(final int i, final int base) {
        return (i == 0) ? "Address" : ("Value (+" + Integer.toString((i - 1) * 4, base) + ")");
    }
    
    public void setupTable() {
        this.tablePanel.removeAll();
        this.tablePanel.add(this.generateDataPanel());
        this.contentPane.add(this.tablePanel);
        this.enableAllButtons();
    }
    
    public void clearWindow() {
        this.tablePanel.removeAll();
        this.disableAllButtons();
    }
    
    public void clearHighlighting() {
        this.addressHighlighting = false;
        DataSegmentWindow.dataTable.tableChanged(new TableModelEvent(DataSegmentWindow.dataTable.getModel(), 0, DataSegmentWindow.dataData.length - 1));
        this.addressColumn = -1;
    }
    
    private int getValueDisplayFormat() {
        return this.asciiDisplay ? 0 : Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
    }
    
    public void updateModelForMemoryRange(final int firstAddr) {
        if (this.tablePanel.getComponentCount() == 0) {
            return;
        }
        final int valueBase = this.getValueDisplayFormat();
        final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
        int address = firstAddr;
        final TableModel dataModel = DataSegmentWindow.dataTable.getModel();
        for (int row = 0; row < 16; ++row) {
            ((DataTableModel)dataModel).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatUnsignedInteger(address, addressBase), row, 0);
            for (int column = 1; column < 9; ++column) {
                try {
                    ((DataTableModel)dataModel).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(Globals.memory.getWordNoNotify(address), valueBase), row, column);
                }
                catch (AddressErrorException aee) {
                    if (Memory.inTextSegment(address)) {
                        int displayValue = 0;
                        if (!Globals.getSettings().getBooleanSetting(20)) {
                            Globals.getSettings().setBooleanSettingNonPersistent(20, true);
                            try {
                                displayValue = Globals.memory.getWordNoNotify(address);
                            }
                            catch (AddressErrorException ex) {}
                            Globals.getSettings().setBooleanSettingNonPersistent(20, false);
                        }
                        ((DataTableModel)dataModel).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(displayValue, valueBase), row, column);
                    }
                    else {
                        ((DataTableModel)dataModel).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(0, valueBase), row, column);
                    }
                }
                address += 4;
            }
        }
    }
    
    public void updateCell(final int address, final int value) {
        final int offset = address - this.firstAddress;
        if (offset < 0 || offset >= 512) {
            return;
        }
        final int row = offset / 32;
        final int column = offset % 32 / 4 + 1;
        final int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
        ((DataTableModel)DataSegmentWindow.dataTable.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(value, valueBase), row, column);
    }
    
    public void updateDataAddresses() {
        if (this.tablePanel.getComponentCount() == 0) {
            return;
        }
        final int addressBase = Globals.getGui().getMainPane().getExecutePane().getAddressDisplayBase();
        int address = this.firstAddress;
        for (int i = 0; i < 16; ++i) {
            final String formattedAddress = NumberDisplayBaseChooser.formatUnsignedInteger(address, addressBase);
            ((DataTableModel)DataSegmentWindow.dataTable.getModel()).setDisplayAndModelValueAt(formattedAddress, i, 0);
            address += 32;
        }
        for (int i = 1; i < 9; ++i) {
            DataSegmentWindow.dataTable.getColumnModel().getColumn(i).setHeaderValue(this.getHeaderStringForColumn(i, addressBase));
        }
        DataSegmentWindow.dataTable.getTableHeader().repaint();
    }
    
    public void updateValues() {
        this.updateModelForMemoryRange(this.firstAddress);
    }
    
    public void resetMemoryRange() {
        this.baseAddressSelector.getActionListeners()[0].actionPerformed(null);
    }
    
    public void resetValues() {
        final int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
        final TableModel dataModel = DataSegmentWindow.dataTable.getModel();
        for (int row = 0; row < 16; ++row) {
            for (int column = 1; column < 9; ++column) {
                ((DataTableModel)dataModel).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(0, valueBase), row, column);
            }
        }
        this.disableAllButtons();
    }
    
    private void disableAllButtons() {
        this.baseAddressSelector.setEnabled(false);
        this.globButton.setEnabled(false);
        this.stakButton.setEnabled(false);
        this.heapButton.setEnabled(false);
        this.extnButton.setEnabled(false);
        this.mmioButton.setEnabled(false);
        this.textButton.setEnabled(false);
        this.kernButton.setEnabled(false);
        this.prevButton.setEnabled(false);
        this.nextButton.setEnabled(false);
        this.dataButton.setEnabled(false);
    }
    
    private void enableAllButtons() {
        this.baseAddressSelector.setEnabled(true);
        this.globButton.setEnabled(true);
        this.stakButton.setEnabled(true);
        this.heapButton.setEnabled(true);
        this.extnButton.setEnabled(true);
        this.mmioButton.setEnabled(true);
        this.textButton.setEnabled(this.settings.getBooleanSetting(20));
        this.kernButton.setEnabled(true);
        this.prevButton.setEnabled(true);
        this.nextButton.setEnabled(true);
        this.dataButton.setEnabled(true);
    }
    
    private void addButtonActionListenersAndInitialize() {
        this.disableAllButtons();
        this.globButton.setToolTipText("View range around global pointer");
        this.stakButton.setToolTipText("View range around stack pointer");
        final JButton heapButton = this.heapButton;
        final StringBuilder append = new StringBuilder().append("View range around heap base address ");
        final Memory memory = Globals.memory;
        heapButton.setToolTipText(append.append(Binary.intToHexString(Memory.heapBaseAddress)).toString());
        final JButton kernButton = this.kernButton;
        final StringBuilder append2 = new StringBuilder().append("View range around kernel data base address ");
        final Memory memory2 = Globals.memory;
        kernButton.setToolTipText(append2.append(Binary.intToHexString(Memory.kernelDataBaseAddress)).toString());
        final JButton extnButton = this.extnButton;
        final StringBuilder append3 = new StringBuilder().append("View range around static global base address ");
        final Memory memory3 = Globals.memory;
        extnButton.setToolTipText(append3.append(Binary.intToHexString(Memory.externBaseAddress)).toString());
        final JButton mmioButton = this.mmioButton;
        final StringBuilder append4 = new StringBuilder().append("View range around MMIO base address ");
        final Memory memory4 = Globals.memory;
        mmioButton.setToolTipText(append4.append(Binary.intToHexString(Memory.memoryMapBaseAddress)).toString());
        final JButton textButton = this.textButton;
        final StringBuilder append5 = new StringBuilder().append("View range around program code ");
        final Memory memory5 = Globals.memory;
        textButton.setToolTipText(append5.append(Binary.intToHexString(Memory.textBaseAddress)).toString());
        this.prevButton.setToolTipText("View next lower address range; hold down for rapid fire");
        this.nextButton.setToolTipText("View next higher address range; hold down for rapid fire");
        final JButton dataButton = this.dataButton;
        final StringBuilder append6 = new StringBuilder().append("View range around static data segment base address ");
        final Memory memory6 = Globals.memory;
        dataButton.setToolTipText(append6.append(Binary.intToHexString(Memory.dataBaseAddress)).toString());
        this.globButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = false;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.firstAddress = Math.max(Memory.dataSegmentBaseAddress, RegisterFile.getValue(28));
                DataSegmentWindow.this.firstAddress -= DataSegmentWindow.this.firstAddress % 32;
                DataSegmentWindow.this.homeAddress = DataSegmentWindow.this.firstAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.stakButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = false;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.firstAddress = Math.max(Memory.dataSegmentBaseAddress, RegisterFile.getValue(29));
                DataSegmentWindow.this.firstAddress -= DataSegmentWindow.this.firstAddress % 32;
                final DataSegmentWindow this$2 = DataSegmentWindow.this;
                final Memory memory2 = Globals.memory;
                this$2.homeAddress = Memory.stackBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.heapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = false;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.homeAddress = Memory.heapBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.homeAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.extnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = false;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.homeAddress = Memory.externBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.homeAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.kernButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = true;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.homeAddress = Memory.kernelDataBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.homeAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.mmioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = true;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.homeAddress = Memory.memoryMapBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.homeAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.textButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = false;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.homeAddress = Memory.textBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.homeAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
        this.dataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {
                DataSegmentWindow.this.userOrKernelMode = false;
                final DataSegmentWindow this$0 = DataSegmentWindow.this;
                final Memory memory = Globals.memory;
                this$0.homeAddress = Memory.dataBaseAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.homeAddress;
                DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
                DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
            }
        });
    }
    
    private int setFirstAddressAndPrevNextButtonEnableStatus(int lowAddress) {
        int n;
        if (!this.userOrKernelMode) {
            final Memory memory = Globals.memory;
            final int textBaseAddress = Memory.textBaseAddress;
            final Memory memory2 = Globals.memory;
            final int min = Math.min(textBaseAddress, Memory.dataSegmentBaseAddress);
            final Memory memory3 = Globals.memory;
            n = Math.min(min, Memory.dataBaseAddress);
        }
        else {
            final Memory memory4 = Globals.memory;
            n = Memory.kernelDataBaseAddress;
        }
        final int lowLimit = n;
        int n2;
        if (!this.userOrKernelMode) {
            final Memory memory5 = Globals.memory;
            n2 = Memory.userHighAddress;
        }
        else {
            final Memory memory6 = Globals.memory;
            n2 = Memory.kernelHighAddress;
        }
        final int highLimit = n2;
        if (lowAddress <= lowLimit) {
            lowAddress = lowLimit;
            this.prevButton.setEnabled(false);
        }
        else {
            this.prevButton.setEnabled(true);
        }
        if (lowAddress >= highLimit - 512) {
            lowAddress = highLimit - 512 + 1;
            this.nextButton.setEnabled(false);
        }
        else {
            this.nextButton.setEnabled(true);
        }
        return lowAddress;
    }
    
    @Override
    public void update(final Observable observable, final Object obj) {
        if (observable == Simulator.getInstance()) {
            final SimulatorNotice notice = (SimulatorNotice)obj;
            if (notice.getAction() == 0) {
                if (notice.getRunSpeed() != 40.0 || notice.getMaxSteps() == 1) {
                    Memory.getInstance().addObserver(this);
                    this.addressHighlighting = true;
                }
            }
            else {
                Memory.getInstance().deleteObserver(this);
            }
        }
        else if (observable != this.settings) {
            if (obj instanceof MemoryAccessNotice) {
                final MemoryAccessNotice access = (MemoryAccessNotice)obj;
                if (access.getAccessType() == 1) {
                    final int address = access.getAddress();
                    this.highlightCellForAddress(address);
                }
            }
        }
    }
    
    static {
        dataSegmentNames = new String[] { "Data", "Stack", "Kernel" };
    }
    
    private class CustomComboBoxModel extends DefaultComboBoxModel
    {
        public CustomComboBoxModel(final Object[] list) {
            super(list);
        }
        
        private void forceComboBoxUpdate(final int index) {
            super.fireContentsChanged(this, index, index);
        }
    }
    
    class DataTableModel extends AbstractTableModel
    {
        String[] columnNames;
        Object[][] data;
        
        public DataTableModel(final Object[][] d, final String[] n) {
            this.data = d;
            this.columnNames = n;
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
        public boolean isCellEditable(final int row, final int col) {
            return col != 0 && !DataSegmentWindow.this.asciiDisplay;
        }
        
        @Override
        public Class getColumnClass(final int c) {
            return this.getValueAt(0, c).getClass();
        }
        
        @Override
        public void setValueAt(final Object value, final int row, final int col) {
            int val = 0;
            int address = 0;
            try {
                val = Binary.stringToInt((String)value);
            }
            catch (NumberFormatException nfe) {
                this.data[row][col] = "INVALID";
                this.fireTableCellUpdated(row, col);
                return;
            }
            try {
                address = Binary.stringToInt((String)this.data[row][0]) + (col - 1) * 4;
            }
            catch (NumberFormatException ex) {}
            synchronized (Globals.memoryAndRegistersLock) {
                try {
                    Globals.memory.setRawWord(address, val);
                }
                catch (AddressErrorException aee) {
                    return;
                }
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
    
    class AddressCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setHorizontalAlignment(4);
            final int rowFirstAddress = Binary.stringToInt(table.getValueAt(row, 0).toString());
            if (DataSegmentWindow.this.settings.getDataSegmentHighlighting() && DataSegmentWindow.this.addressHighlighting && rowFirstAddress == DataSegmentWindow.this.addressRowFirstAddress && column == DataSegmentWindow.this.addressColumn) {
                cell.setBackground(DataSegmentWindow.this.settings.getColorSettingByPosition(8));
                cell.setForeground(DataSegmentWindow.this.settings.getColorSettingByPosition(9));
                cell.setFont(DataSegmentWindow.this.settings.getFontByPosition(5));
            }
            else if (row % 2 == 0) {
                cell.setBackground(DataSegmentWindow.this.settings.getColorSettingByPosition(0));
                cell.setForeground(DataSegmentWindow.this.settings.getColorSettingByPosition(1));
                cell.setFont(DataSegmentWindow.this.settings.getFontByPosition(1));
            }
            else {
                cell.setBackground(DataSegmentWindow.this.settings.getColorSettingByPosition(2));
                cell.setForeground(DataSegmentWindow.this.settings.getColorSettingByPosition(3));
                cell.setFont(DataSegmentWindow.this.settings.getFontByPosition(2));
            }
            return cell;
        }
    }
    
    private class MyTippedJTable extends JTable
    {
        private String[] columnToolTips;
        
        MyTippedJTable(final DataTableModel m) {
            super(m);
            this.columnToolTips = new String[] { "Base MIPS memory address for this row of the table.", "32-bit value stored at base address for its row.", "32-bit value stored ", " bytes beyond base address for its row." };
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
                    return (realIndex < 2) ? MyTippedJTable.this.columnToolTips[realIndex] : (MyTippedJTable.this.columnToolTips[2] + (realIndex - 1) * 4 + MyTippedJTable.this.columnToolTips[3]);
                }
            };
        }
    }
    
    private class PrevButton extends RepeatButton
    {
        public PrevButton(final Icon ico) {
            super(ico);
            this.setInitialDelay(500);
            this.setDelay(60);
            this.addActionListener(this);
        }
        
        @Override
        public void actionPerformed(final ActionEvent ae) {
            final DataSegmentWindow this$0 = DataSegmentWindow.this;
            this$0.firstAddress -= 256;
            DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
            DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
        }
    }
    
    private class NextButton extends RepeatButton
    {
        public NextButton(final Icon ico) {
            super(ico);
            this.setInitialDelay(500);
            this.setDelay(60);
            this.addActionListener(this);
        }
        
        @Override
        public void actionPerformed(final ActionEvent ae) {
            final DataSegmentWindow this$0 = DataSegmentWindow.this;
            this$0.firstAddress += 256;
            DataSegmentWindow.this.firstAddress = DataSegmentWindow.this.setFirstAddressAndPrevNextButtonEnableStatus(DataSegmentWindow.this.firstAddress);
            DataSegmentWindow.this.updateModelForMemoryRange(DataSegmentWindow.this.firstAddress);
        }
    }
}
