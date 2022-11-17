

package mars.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import mars.util.Binary;
import mars.mips.hardware.Memory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.hardware.AccessNotice;
import java.util.Observable;
import java.awt.Component;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Font;
import javax.swing.border.EmptyBorder;
import javax.swing.JPanel;
import java.awt.Graphics;
import javax.swing.JComboBox;

public class BitmapDisplay extends AbstractMarsToolAndApplication
{
    private static String version;
    private static String heading;
    private JComboBox visualizationUnitPixelWidthSelector;
    private JComboBox visualizationUnitPixelHeightSelector;
    private JComboBox visualizationPixelWidthSelector;
    private JComboBox visualizationPixelHeightSelector;
    private JComboBox displayBaseAddressSelector;
    private Graphics drawingArea;
    private JPanel canvas;
    private JPanel results;
    private EmptyBorder emptyBorder;
    private Font countFonts;
    private Color backgroundColor;
    private final String[] visualizationUnitPixelWidthChoices;
    private final int defaultVisualizationUnitPixelWidthIndex = 0;
    private final String[] visualizationUnitPixelHeightChoices;
    private final int defaultVisualizationUnitPixelHeightIndex = 0;
    private final String[] displayAreaPixelWidthChoices;
    private final int defaultDisplayWidthIndex = 3;
    private final String[] displayAreaPixelHeightChoices;
    private final int defaultDisplayHeightIndex = 2;
    private int unitPixelWidth;
    private int unitPixelHeight;
    private int displayAreaWidthInPixels;
    private int displayAreaHeightInPixels;
    private String[] displayBaseAddressChoices;
    private int[] displayBaseAddresses;
    private int defaultBaseAddressIndex;
    private int baseAddress;
    private Grid theGrid;
    
    public BitmapDisplay(final String title, final String heading) {
        super(title, heading);
        this.emptyBorder = new EmptyBorder(4, 4, 4, 4);
        this.countFonts = new Font("Times", 1, 12);
        this.backgroundColor = Color.WHITE;
        this.visualizationUnitPixelWidthChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.visualizationUnitPixelHeightChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.displayAreaPixelWidthChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.displayAreaPixelHeightChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.unitPixelWidth = Integer.parseInt(this.visualizationUnitPixelWidthChoices[0]);
        this.unitPixelHeight = Integer.parseInt(this.visualizationUnitPixelHeightChoices[0]);
        this.displayAreaWidthInPixels = Integer.parseInt(this.displayAreaPixelWidthChoices[3]);
        this.displayAreaHeightInPixels = Integer.parseInt(this.displayAreaPixelHeightChoices[2]);
    }
    
    public BitmapDisplay() {
        super("Bitmap Display, " + BitmapDisplay.version, BitmapDisplay.heading);
        this.emptyBorder = new EmptyBorder(4, 4, 4, 4);
        this.countFonts = new Font("Times", 1, 12);
        this.backgroundColor = Color.WHITE;
        this.visualizationUnitPixelWidthChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.visualizationUnitPixelHeightChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.displayAreaPixelWidthChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.displayAreaPixelHeightChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.unitPixelWidth = Integer.parseInt(this.visualizationUnitPixelWidthChoices[0]);
        this.unitPixelHeight = Integer.parseInt(this.visualizationUnitPixelHeightChoices[0]);
        this.displayAreaWidthInPixels = Integer.parseInt(this.displayAreaPixelWidthChoices[3]);
        this.displayAreaHeightInPixels = Integer.parseInt(this.displayAreaPixelHeightChoices[2]);
    }
    
    public static void main(final String[] args) {
        new BitmapDisplay("Bitmap Display stand-alone, " + BitmapDisplay.version, BitmapDisplay.heading).go();
    }
    
    @Override
    public String getName() {
        return "Bitmap Display";
    }
    
    @Override
    protected void addAsObserver() {
        int highAddress = this.baseAddress + this.theGrid.getRows() * this.theGrid.getColumns() * 4;
        if (this.baseAddress < 0 && highAddress > -4) {
            highAddress = -4;
        }
        this.addAsObserver(this.baseAddress, highAddress);
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        (this.results = new JPanel()).add(this.buildOrganizationArea());
        this.results.add(this.buildVisualizationArea());
        return this.results;
    }
    
    @Override
    protected void processMIPSUpdate(final Observable memory, final AccessNotice accessNotice) {
        if (accessNotice.getAccessType() == 1) {
            this.updateColorForAddress((MemoryAccessNotice)accessNotice);
        }
    }
    
    @Override
    protected void initializePreGUI() {
        this.initializeDisplayBaseChoices();
        this.theGrid = new Grid(this.displayAreaHeightInPixels / this.unitPixelHeight, this.displayAreaWidthInPixels / this.unitPixelWidth);
    }
    
    @Override
    protected void initializePostGUI() {
        this.theGrid = this.createNewGrid();
        this.updateBaseAddress();
    }
    
    @Override
    protected void reset() {
        this.resetCounts();
        this.updateDisplay();
    }
    
    @Override
    protected void updateDisplay() {
        this.canvas.repaint();
    }
    
    @Override
    protected JComponent getHelpComponent() {
        final String helpContent = "Use this program to simulate a basic bitmap display where\neach memory word in a specified address space corresponds to\none display pixel in row-major order starting at the upper left\ncorner of the display.  This tool may be run either from the\nMARS Tools menu or as a stand-alone application.\n\nYou can easily learn to use this small program by playing with\nit!   Each rectangular unit on the display represents one memory\nword in a contiguous address space starting with the specified\nbase address.  The value stored in that word will be interpreted\nas a 24-bit RGB color value with the red component in bits 16-23,\nthe green component in bits 8-15, and the blue component in bits 0-7.\nEach time a memory word within the display address space is written\nby the MIPS program, its position in the display will be rendered\nin the color that its value represents.\n\nVersion 1.0 is very basic and was constructed from the Memory\nReference Visualization tool's code.  Feel free to improve it and\nsend me your code for consideration in the next MARS release.\n\nContact Pete Sanderson at psanderson@otterbein.edu with\nquestions or comments.\n";
        final JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JOptionPane.showMessageDialog(BitmapDisplay.this.theWindow, "Use this program to simulate a basic bitmap display where\neach memory word in a specified address space corresponds to\none display pixel in row-major order starting at the upper left\ncorner of the display.  This tool may be run either from the\nMARS Tools menu or as a stand-alone application.\n\nYou can easily learn to use this small program by playing with\nit!   Each rectangular unit on the display represents one memory\nword in a contiguous address space starting with the specified\nbase address.  The value stored in that word will be interpreted\nas a 24-bit RGB color value with the red component in bits 16-23,\nthe green component in bits 8-15, and the blue component in bits 0-7.\nEach time a memory word within the display address space is written\nby the MIPS program, its position in the display will be rendered\nin the color that its value represents.\n\nVersion 1.0 is very basic and was constructed from the Memory\nReference Visualization tool's code.  Feel free to improve it and\nsend me your code for consideration in the next MARS release.\n\nContact Pete Sanderson at psanderson@otterbein.edu with\nquestions or comments.\n");
            }
        });
        return help;
    }
    
    private JComponent buildOrganizationArea() {
        final JPanel organization = new JPanel(new GridLayout(8, 1));
        (this.visualizationUnitPixelWidthSelector = new JComboBox((E[])this.visualizationUnitPixelWidthChoices)).setEditable(false);
        this.visualizationUnitPixelWidthSelector.setBackground(this.backgroundColor);
        this.visualizationUnitPixelWidthSelector.setSelectedIndex(0);
        this.visualizationUnitPixelWidthSelector.setToolTipText("Width in pixels of rectangle representing memory word");
        this.visualizationUnitPixelWidthSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BitmapDisplay.this.unitPixelWidth = BitmapDisplay.this.getIntComboBoxSelection(BitmapDisplay.this.visualizationUnitPixelWidthSelector);
                BitmapDisplay.this.theGrid = BitmapDisplay.this.createNewGrid();
                BitmapDisplay.this.updateDisplay();
            }
        });
        (this.visualizationUnitPixelHeightSelector = new JComboBox((E[])this.visualizationUnitPixelHeightChoices)).setEditable(false);
        this.visualizationUnitPixelHeightSelector.setBackground(this.backgroundColor);
        this.visualizationUnitPixelHeightSelector.setSelectedIndex(0);
        this.visualizationUnitPixelHeightSelector.setToolTipText("Height in pixels of rectangle representing memory word");
        this.visualizationUnitPixelHeightSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BitmapDisplay.this.unitPixelHeight = BitmapDisplay.this.getIntComboBoxSelection(BitmapDisplay.this.visualizationUnitPixelHeightSelector);
                BitmapDisplay.this.theGrid = BitmapDisplay.this.createNewGrid();
                BitmapDisplay.this.updateDisplay();
            }
        });
        (this.visualizationPixelWidthSelector = new JComboBox((E[])this.displayAreaPixelWidthChoices)).setEditable(false);
        this.visualizationPixelWidthSelector.setBackground(this.backgroundColor);
        this.visualizationPixelWidthSelector.setSelectedIndex(3);
        this.visualizationPixelWidthSelector.setToolTipText("Total width in pixels of display area");
        this.visualizationPixelWidthSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BitmapDisplay.this.displayAreaWidthInPixels = BitmapDisplay.this.getIntComboBoxSelection(BitmapDisplay.this.visualizationPixelWidthSelector);
                BitmapDisplay.this.canvas.setPreferredSize(BitmapDisplay.this.getDisplayAreaDimension());
                BitmapDisplay.this.canvas.setSize(BitmapDisplay.this.getDisplayAreaDimension());
                BitmapDisplay.this.theGrid = BitmapDisplay.this.createNewGrid();
                BitmapDisplay.this.updateDisplay();
            }
        });
        (this.visualizationPixelHeightSelector = new JComboBox((E[])this.displayAreaPixelHeightChoices)).setEditable(false);
        this.visualizationPixelHeightSelector.setBackground(this.backgroundColor);
        this.visualizationPixelHeightSelector.setSelectedIndex(2);
        this.visualizationPixelHeightSelector.setToolTipText("Total height in pixels of display area");
        this.visualizationPixelHeightSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BitmapDisplay.this.displayAreaHeightInPixels = BitmapDisplay.this.getIntComboBoxSelection(BitmapDisplay.this.visualizationPixelHeightSelector);
                BitmapDisplay.this.canvas.setPreferredSize(BitmapDisplay.this.getDisplayAreaDimension());
                BitmapDisplay.this.canvas.setSize(BitmapDisplay.this.getDisplayAreaDimension());
                BitmapDisplay.this.theGrid = BitmapDisplay.this.createNewGrid();
                BitmapDisplay.this.updateDisplay();
            }
        });
        (this.displayBaseAddressSelector = new JComboBox((E[])this.displayBaseAddressChoices)).setEditable(false);
        this.displayBaseAddressSelector.setBackground(this.backgroundColor);
        this.displayBaseAddressSelector.setSelectedIndex(this.defaultBaseAddressIndex);
        this.displayBaseAddressSelector.setToolTipText("Base address for display area (upper left corner)");
        this.displayBaseAddressSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BitmapDisplay.this.updateBaseAddress();
                if (BitmapDisplay.this.connectButton != null && BitmapDisplay.this.connectButton.isConnected()) {
                    BitmapDisplay.this.deleteAsObserver();
                    BitmapDisplay.this.addAsObserver();
                }
                BitmapDisplay.this.theGrid = BitmapDisplay.this.createNewGrid();
                BitmapDisplay.this.updateDisplay();
            }
        });
        final JPanel unitWidthInPixelsRow = this.getPanelWithBorderLayout();
        unitWidthInPixelsRow.setBorder(this.emptyBorder);
        unitWidthInPixelsRow.add(new JLabel("Unit Width in Pixels "), "West");
        unitWidthInPixelsRow.add(this.visualizationUnitPixelWidthSelector, "East");
        final JPanel unitHeightInPixelsRow = this.getPanelWithBorderLayout();
        unitHeightInPixelsRow.setBorder(this.emptyBorder);
        unitHeightInPixelsRow.add(new JLabel("Unit Height in Pixels "), "West");
        unitHeightInPixelsRow.add(this.visualizationUnitPixelHeightSelector, "East");
        final JPanel widthInPixelsRow = this.getPanelWithBorderLayout();
        widthInPixelsRow.setBorder(this.emptyBorder);
        widthInPixelsRow.add(new JLabel("Display Width in Pixels "), "West");
        widthInPixelsRow.add(this.visualizationPixelWidthSelector, "East");
        final JPanel heightInPixelsRow = this.getPanelWithBorderLayout();
        heightInPixelsRow.setBorder(this.emptyBorder);
        heightInPixelsRow.add(new JLabel("Display Height in Pixels "), "West");
        heightInPixelsRow.add(this.visualizationPixelHeightSelector, "East");
        final JPanel baseAddressRow = this.getPanelWithBorderLayout();
        baseAddressRow.setBorder(this.emptyBorder);
        baseAddressRow.add(new JLabel("Base address for display "), "West");
        baseAddressRow.add(this.displayBaseAddressSelector, "East");
        organization.add(unitWidthInPixelsRow);
        organization.add(unitHeightInPixelsRow);
        organization.add(widthInPixelsRow);
        organization.add(heightInPixelsRow);
        organization.add(baseAddressRow);
        return organization;
    }
    
    private JComponent buildVisualizationArea() {
        (this.canvas = new GraphicsPanel()).setPreferredSize(this.getDisplayAreaDimension());
        this.canvas.setToolTipText("Bitmap display area");
        return this.canvas;
    }
    
    private void initializeDisplayBaseChoices() {
        final int[] displayBaseAddressArray = { Memory.dataSegmentBaseAddress, Memory.globalPointer, Memory.dataBaseAddress, Memory.heapBaseAddress, Memory.memoryMapBaseAddress };
        final String[] descriptions = { " (global data)", " ($gp)", " (static data)", " (heap)", " (memory map)" };
        this.displayBaseAddresses = displayBaseAddressArray;
        this.displayBaseAddressChoices = new String[displayBaseAddressArray.length];
        for (int i = 0; i < this.displayBaseAddressChoices.length; ++i) {
            this.displayBaseAddressChoices[i] = Binary.intToHexString(displayBaseAddressArray[i]) + descriptions[i];
        }
        this.defaultBaseAddressIndex = 2;
        this.baseAddress = displayBaseAddressArray[this.defaultBaseAddressIndex];
    }
    
    private void updateBaseAddress() {
        this.baseAddress = this.displayBaseAddresses[this.displayBaseAddressSelector.getSelectedIndex()];
    }
    
    private Dimension getDisplayAreaDimension() {
        return new Dimension(this.displayAreaWidthInPixels, this.displayAreaHeightInPixels);
    }
    
    private void resetCounts() {
        this.theGrid.reset();
    }
    
    private int getIntComboBoxSelection(final JComboBox comboBox) {
        try {
            return Integer.parseInt((String)comboBox.getSelectedItem());
        }
        catch (NumberFormatException nfe) {
            return 1;
        }
    }
    
    private JPanel getPanelWithBorderLayout() {
        return new JPanel(new BorderLayout(2, 2));
    }
    
    private Grid createNewGrid() {
        final int rows = this.displayAreaHeightInPixels / this.unitPixelHeight;
        final int columns = this.displayAreaWidthInPixels / this.unitPixelWidth;
        return new Grid(rows, columns);
    }
    
    private void updateColorForAddress(final MemoryAccessNotice notice) {
        final int address = notice.getAddress();
        final int value = notice.getValue();
        final int offset = (address - this.baseAddress) / 4;
        try {
            this.theGrid.setElement(offset / this.theGrid.getColumns(), offset % this.theGrid.getColumns(), value);
        }
        catch (IndexOutOfBoundsException ex) {}
    }
    
    static {
        BitmapDisplay.version = "Version 1.0";
        BitmapDisplay.heading = "Bitmap Display";
    }
    
    private class GraphicsPanel extends JPanel
    {
        @Override
        public void paint(final Graphics g) {
            this.paintGrid(g, BitmapDisplay.this.theGrid);
        }
        
        private void paintGrid(final Graphics g, final Grid grid) {
            int upperLeftX = 0;
            int upperLeftY = 0;
            for (int i = 0; i < grid.getRows(); ++i) {
                for (int j = 0; j < grid.getColumns(); ++j) {
                    g.setColor(grid.getElementFast(i, j));
                    g.fillRect(upperLeftX, upperLeftY, BitmapDisplay.this.unitPixelWidth, BitmapDisplay.this.unitPixelHeight);
                    upperLeftX += BitmapDisplay.this.unitPixelWidth;
                }
                upperLeftX = 0;
                upperLeftY += BitmapDisplay.this.unitPixelHeight;
            }
        }
    }
    
    private class Grid
    {
        Color[][] grid;
        int rows;
        int columns;
        
        private Grid(final int rows, final int columns) {
            this.grid = new Color[rows][columns];
            this.rows = rows;
            this.columns = columns;
            this.reset();
        }
        
        private int getRows() {
            return this.rows;
        }
        
        private int getColumns() {
            return this.columns;
        }
        
        private Color getElement(final int row, final int column) {
            return (row >= 0 && row <= this.rows && column >= 0 && column <= this.columns) ? this.grid[row][column] : null;
        }
        
        private Color getElementFast(final int row, final int column) {
            return this.grid[row][column];
        }
        
        private void setElement(final int row, final int column, final int color) {
            this.grid[row][column] = new Color(color);
        }
        
        private void setElement(final int row, final int column, final Color color) {
            this.grid[row][column] = color;
        }
        
        private void reset() {
            for (int i = 0; i < this.rows; ++i) {
                for (int j = 0; j < this.columns; ++j) {
                    this.grid[i][j] = Color.BLACK;
                }
            }
        }
    }
}
