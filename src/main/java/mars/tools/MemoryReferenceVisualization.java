

package mars.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.util.Binary;

public class MemoryReferenceVisualization extends AbstractMarsToolAndApplication
{
    private static String version;
    private static String heading;
    private JComboBox wordsPerUnitSelector;
    private JComboBox visualizationUnitPixelWidthSelector;
    private JComboBox visualizationUnitPixelHeightSelector;
    private JComboBox visualizationPixelWidthSelector;
    private JComboBox visualizationPixelHeightSelector;
    private JComboBox displayBaseAddressSelector;
    private JCheckBox drawHashMarksSelector;
    private Graphics drawingArea;
    private JPanel canvas;
    private JPanel results;
    private EmptyBorder emptyBorder;
    private Font countFonts;
    private Color backgroundColor;
    private final String[] wordsPerUnitChoices;
    private final int defaultWordsPerUnitIndex = 0;
    private final String[] visualizationUnitPixelWidthChoices;
    private final int defaultVisualizationUnitPixelWidthIndex = 4;
    private final String[] visualizationUnitPixelHeightChoices;
    private final int defaultVisualizationUnitPixelHeightIndex = 4;
    private final String[] displayAreaPixelWidthChoices;
    private final int defaultDisplayWidthIndex = 2;
    private final String[] displayAreaPixelHeightChoices;
    private final int defaultDisplayHeightIndex = 2;
    private final boolean defaultDrawHashMarks = true;
    private int unitPixelWidth;
    private int unitPixelHeight;
    private int wordsPerUnit;
    private int visualizationAreaWidthInPixels;
    private int visualizationAreaHeightInPixels;
    private CounterColor[] defaultCounterColors;
    private int[] countTable;
    private final int COUNT_INDEX_INIT = 10;
    private String[] displayBaseAddressChoices;
    private int[] displayBaseAddresses;
    private int defaultBaseAddressIndex;
    private int baseAddress;
    private Grid theGrid;
    private CounterColorScale counterColorScale;
    
    public MemoryReferenceVisualization(final String title, final String heading) {
        super(title, heading);
        this.emptyBorder = new EmptyBorder(4, 4, 4, 4);
        this.countFonts = new Font("Times", 1, 12);
        this.backgroundColor = Color.WHITE;
        this.wordsPerUnitChoices = new String[] { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" };
        this.visualizationUnitPixelWidthChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.visualizationUnitPixelHeightChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.displayAreaPixelWidthChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.displayAreaPixelHeightChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.unitPixelWidth = Integer.parseInt(this.visualizationUnitPixelWidthChoices[4]);
        this.unitPixelHeight = Integer.parseInt(this.visualizationUnitPixelHeightChoices[4]);
        this.wordsPerUnit = Integer.parseInt(this.wordsPerUnitChoices[0]);
        this.visualizationAreaWidthInPixels = Integer.parseInt(this.displayAreaPixelWidthChoices[2]);
        this.visualizationAreaHeightInPixels = Integer.parseInt(this.displayAreaPixelHeightChoices[2]);
        this.defaultCounterColors = new CounterColor[] { new CounterColor(0, Color.black), new CounterColor(1, Color.blue), new CounterColor(2, Color.green), new CounterColor(3, Color.yellow), new CounterColor(5, Color.orange), new CounterColor(10, Color.red) };
        this.countTable = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000, 10000, 50000, 100000, 500000, 1000000 };
    }
    
    public MemoryReferenceVisualization() {
        super("Memory Reference Visualization, " + MemoryReferenceVisualization.version, MemoryReferenceVisualization.heading);
        this.emptyBorder = new EmptyBorder(4, 4, 4, 4);
        this.countFonts = new Font("Times", 1, 12);
        this.backgroundColor = Color.WHITE;
        this.wordsPerUnitChoices = new String[] { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" };
        this.visualizationUnitPixelWidthChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.visualizationUnitPixelHeightChoices = new String[] { "1", "2", "4", "8", "16", "32" };
        this.displayAreaPixelWidthChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.displayAreaPixelHeightChoices = new String[] { "64", "128", "256", "512", "1024" };
        this.unitPixelWidth = Integer.parseInt(this.visualizationUnitPixelWidthChoices[4]);
        this.unitPixelHeight = Integer.parseInt(this.visualizationUnitPixelHeightChoices[4]);
        this.wordsPerUnit = Integer.parseInt(this.wordsPerUnitChoices[0]);
        this.visualizationAreaWidthInPixels = Integer.parseInt(this.displayAreaPixelWidthChoices[2]);
        this.visualizationAreaHeightInPixels = Integer.parseInt(this.displayAreaPixelHeightChoices[2]);
        this.defaultCounterColors = new CounterColor[] { new CounterColor(0, Color.black), new CounterColor(1, Color.blue), new CounterColor(2, Color.green), new CounterColor(3, Color.yellow), new CounterColor(5, Color.orange), new CounterColor(10, Color.red) };
        this.countTable = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000, 10000, 50000, 100000, 500000, 1000000 };
    }
    
    public static void main(final String[] args) {
        new MemoryReferenceVisualization("Memory Reference Visualization stand-alone, " + MemoryReferenceVisualization.version, MemoryReferenceVisualization.heading).go();
    }
    
    @Override
    public String getName() {
        return "Memory Reference Visualization";
    }
    
    @Override
    protected void addAsObserver() {
        int highAddress = this.baseAddress + this.theGrid.getRows() * this.theGrid.getColumns() * 4 * this.wordsPerUnit;
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
        this.incrementReferenceCountForAddress(((MemoryAccessNotice)accessNotice).getAddress());
        this.updateDisplay();
    }
    
    @Override
    protected void initializePreGUI() {
        this.initializeDisplayBaseChoices();
        this.counterColorScale = new CounterColorScale(this.defaultCounterColors);
        this.theGrid = new Grid(this.visualizationAreaHeightInPixels / this.unitPixelHeight, this.visualizationAreaWidthInPixels / this.unitPixelWidth);
    }
    
    @Override
    protected void initializePostGUI() {
        this.wordsPerUnit = this.getIntComboBoxSelection(this.wordsPerUnitSelector);
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
        final String helpContent = "Use this program to visualize dynamic memory reference\npatterns in MIPS assembly programs.  It may be run either\nfrom MARS' Tools menu or as a stand-alone application.  For\nthe latter, simply write a small driver to instantiate a\nMemoryReferenceVisualization object and invoke its go() method.\n\nYou can easily learn to use this small program by playing with\nit!  For the best animation, set the MIPS program to run in\ntimed mode using the Run Speed slider.  Each rectangular unit\non the display represents one or more memory words (default 1)\nand each time a memory word is accessed by the MIPS program,\nits reference count is incremented then rendered in the color\nassigned to the count value.  You can change the count-color\nassignments using the count slider and color patch.  Select a\ncounter value then click on the color patch to change the color.\nThis color will apply beginning at the selected count and\nextending up to the next slider-provided count.\n\nContact Pete Sanderson at psanderson@otterbein.edu with\nquestions or comments.\n";
        final JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JOptionPane.showMessageDialog(MemoryReferenceVisualization.this.theWindow, "Use this program to visualize dynamic memory reference\npatterns in MIPS assembly programs.  It may be run either\nfrom MARS' Tools menu or as a stand-alone application.  For\nthe latter, simply write a small driver to instantiate a\nMemoryReferenceVisualization object and invoke its go() method.\n\nYou can easily learn to use this small program by playing with\nit!  For the best animation, set the MIPS program to run in\ntimed mode using the Run Speed slider.  Each rectangular unit\non the display represents one or more memory words (default 1)\nand each time a memory word is accessed by the MIPS program,\nits reference count is incremented then rendered in the color\nassigned to the count value.  You can change the count-color\nassignments using the count slider and color patch.  Select a\ncounter value then click on the color patch to change the color.\nThis color will apply beginning at the selected count and\nextending up to the next slider-provided count.\n\nContact Pete Sanderson at psanderson@otterbein.edu with\nquestions or comments.\n");
            }
        });
        return help;
    }
    
    private JComponent buildOrganizationArea() {
        final JPanel organization = new JPanel(new GridLayout(9, 1));
        (this.drawHashMarksSelector = new JCheckBox()).setSelected(true);
        this.drawHashMarksSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.updateDisplay();
            }
        });
        (this.wordsPerUnitSelector = new JComboBox(this.wordsPerUnitChoices)).setEditable(false);
        this.wordsPerUnitSelector.setBackground(this.backgroundColor);
        this.wordsPerUnitSelector.setSelectedIndex(0);
        this.wordsPerUnitSelector.setToolTipText("Number of memory words represented by one visualization element (rectangle)");
        this.wordsPerUnitSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.wordsPerUnit = MemoryReferenceVisualization.this.getIntComboBoxSelection(MemoryReferenceVisualization.this.wordsPerUnitSelector);
                MemoryReferenceVisualization.this.reset();
            }
        });
        (this.visualizationUnitPixelWidthSelector = new JComboBox(this.visualizationUnitPixelWidthChoices)).setEditable(false);
        this.visualizationUnitPixelWidthSelector.setBackground(this.backgroundColor);
        this.visualizationUnitPixelWidthSelector.setSelectedIndex(4);
        this.visualizationUnitPixelWidthSelector.setToolTipText("Width in pixels of rectangle representing memory access");
        this.visualizationUnitPixelWidthSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.unitPixelWidth = MemoryReferenceVisualization.this.getIntComboBoxSelection(MemoryReferenceVisualization.this.visualizationUnitPixelWidthSelector);
                MemoryReferenceVisualization.this.theGrid = MemoryReferenceVisualization.this.createNewGrid();
                MemoryReferenceVisualization.this.updateDisplay();
            }
        });
        (this.visualizationUnitPixelHeightSelector = new JComboBox(this.visualizationUnitPixelHeightChoices)).setEditable(false);
        this.visualizationUnitPixelHeightSelector.setBackground(this.backgroundColor);
        this.visualizationUnitPixelHeightSelector.setSelectedIndex(4);
        this.visualizationUnitPixelHeightSelector.setToolTipText("Height in pixels of rectangle representing memory access");
        this.visualizationUnitPixelHeightSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.unitPixelHeight = MemoryReferenceVisualization.this.getIntComboBoxSelection(MemoryReferenceVisualization.this.visualizationUnitPixelHeightSelector);
                MemoryReferenceVisualization.this.theGrid = MemoryReferenceVisualization.this.createNewGrid();
                MemoryReferenceVisualization.this.updateDisplay();
            }
        });
        (this.visualizationPixelWidthSelector = new JComboBox(this.displayAreaPixelWidthChoices)).setEditable(false);
        this.visualizationPixelWidthSelector.setBackground(this.backgroundColor);
        this.visualizationPixelWidthSelector.setSelectedIndex(2);
        this.visualizationPixelWidthSelector.setToolTipText("Total width in pixels of visualization area");
        this.visualizationPixelWidthSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.visualizationAreaWidthInPixels = MemoryReferenceVisualization.this.getIntComboBoxSelection(MemoryReferenceVisualization.this.visualizationPixelWidthSelector);
                MemoryReferenceVisualization.this.canvas.setPreferredSize(MemoryReferenceVisualization.this.getDisplayAreaDimension());
                MemoryReferenceVisualization.this.canvas.setSize(MemoryReferenceVisualization.this.getDisplayAreaDimension());
                MemoryReferenceVisualization.this.theGrid = MemoryReferenceVisualization.this.createNewGrid();
                MemoryReferenceVisualization.this.canvas.repaint();
                MemoryReferenceVisualization.this.updateDisplay();
            }
        });
        (this.visualizationPixelHeightSelector = new JComboBox(this.displayAreaPixelHeightChoices)).setEditable(false);
        this.visualizationPixelHeightSelector.setBackground(this.backgroundColor);
        this.visualizationPixelHeightSelector.setSelectedIndex(2);
        this.visualizationPixelHeightSelector.setToolTipText("Total height in pixels of visualization area");
        this.visualizationPixelHeightSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.visualizationAreaHeightInPixels = MemoryReferenceVisualization.this.getIntComboBoxSelection(MemoryReferenceVisualization.this.visualizationPixelHeightSelector);
                MemoryReferenceVisualization.this.canvas.setPreferredSize(MemoryReferenceVisualization.this.getDisplayAreaDimension());
                MemoryReferenceVisualization.this.canvas.setSize(MemoryReferenceVisualization.this.getDisplayAreaDimension());
                MemoryReferenceVisualization.this.theGrid = MemoryReferenceVisualization.this.createNewGrid();
                MemoryReferenceVisualization.this.canvas.repaint();
                MemoryReferenceVisualization.this.updateDisplay();
            }
        });
        (this.displayBaseAddressSelector = new JComboBox(this.displayBaseAddressChoices)).setEditable(false);
        this.displayBaseAddressSelector.setBackground(this.backgroundColor);
        this.displayBaseAddressSelector.setSelectedIndex(this.defaultBaseAddressIndex);
        this.displayBaseAddressSelector.setToolTipText("Base address for visualization area (upper left corner)");
        this.displayBaseAddressSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MemoryReferenceVisualization.this.updateBaseAddress();
                if (MemoryReferenceVisualization.this.connectButton != null && MemoryReferenceVisualization.this.connectButton.isConnected()) {
                    MemoryReferenceVisualization.this.deleteAsObserver();
                    MemoryReferenceVisualization.this.addAsObserver();
                }
                MemoryReferenceVisualization.this.theGrid = MemoryReferenceVisualization.this.createNewGrid();
                MemoryReferenceVisualization.this.updateDisplay();
            }
        });
        final JPanel hashMarksRow = this.getPanelWithBorderLayout();
        hashMarksRow.setBorder(this.emptyBorder);
        hashMarksRow.add(new JLabel("Show unit boundaries (grid marks)"), "West");
        hashMarksRow.add(this.drawHashMarksSelector, "East");
        final JPanel wordsPerUnitRow = this.getPanelWithBorderLayout();
        wordsPerUnitRow.setBorder(this.emptyBorder);
        wordsPerUnitRow.add(new JLabel("Memory Words per Unit "), "West");
        wordsPerUnitRow.add(this.wordsPerUnitSelector, "East");
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
        final ColorChooserControls colorChooserControls = new ColorChooserControls();
        organization.add(hashMarksRow);
        organization.add(wordsPerUnitRow);
        organization.add(unitWidthInPixelsRow);
        organization.add(unitHeightInPixelsRow);
        organization.add(widthInPixelsRow);
        organization.add(heightInPixelsRow);
        organization.add(baseAddressRow);
        organization.add(colorChooserControls.colorChooserRow);
        organization.add(colorChooserControls.countDisplayRow);
        return organization;
    }
    
    private JComponent buildVisualizationArea() {
        (this.canvas = new GraphicsPanel()).setPreferredSize(this.getDisplayAreaDimension());
        this.canvas.setToolTipText("Memory reference count visualization area");
        return this.canvas;
    }
    
    private void initializeDisplayBaseChoices() {
        final int[] displayBaseAddressArray = { Memory.textBaseAddress, Memory.dataSegmentBaseAddress, Memory.globalPointer, Memory.dataBaseAddress, Memory.heapBaseAddress, Memory.memoryMapBaseAddress };
        final String[] descriptions = { " (text)", " (global data)", " ($gp)", " (static data)", " (heap)", " (memory map)" };
        this.displayBaseAddresses = displayBaseAddressArray;
        this.displayBaseAddressChoices = new String[displayBaseAddressArray.length];
        for (int i = 0; i < this.displayBaseAddressChoices.length; ++i) {
            this.displayBaseAddressChoices[i] = Binary.intToHexString(displayBaseAddressArray[i]) + descriptions[i];
        }
        this.defaultBaseAddressIndex = 3;
        this.baseAddress = displayBaseAddressArray[this.defaultBaseAddressIndex];
    }
    
    private void updateBaseAddress() {
        this.baseAddress = this.displayBaseAddresses[this.displayBaseAddressSelector.getSelectedIndex()];
    }
    
    private Dimension getDisplayAreaDimension() {
        return new Dimension(this.visualizationAreaWidthInPixels, this.visualizationAreaHeightInPixels);
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
        final int rows = this.visualizationAreaHeightInPixels / this.unitPixelHeight;
        final int columns = this.visualizationAreaWidthInPixels / this.unitPixelWidth;
        return new Grid(rows, columns);
    }
    
    private void incrementReferenceCountForAddress(final int address) {
        final int offset = (address - this.baseAddress) / 4 / this.wordsPerUnit;
        this.theGrid.incrementElement(offset / this.theGrid.getColumns(), offset % this.theGrid.getColumns());
    }
    
    static {
        MemoryReferenceVisualization.version = "Version 1.0";
        MemoryReferenceVisualization.heading = "Visualizing memory reference patterns";
    }
    
    private class GraphicsPanel extends JPanel
    {
        @Override
        public void paint(final Graphics g) {
            this.paintGrid(g, MemoryReferenceVisualization.this.theGrid);
            if (MemoryReferenceVisualization.this.drawHashMarksSelector.isSelected()) {
                this.paintHashMarks(g, MemoryReferenceVisualization.this.theGrid);
            }
        }
        
        private void paintHashMarks(final Graphics g, final Grid grid) {
            g.setColor(this.getContrastingColor(MemoryReferenceVisualization.this.counterColorScale.getColor(0)));
            int leftX = 0;
            final int rightX = MemoryReferenceVisualization.this.visualizationAreaWidthInPixels;
            int upperY = 0;
            final int lowerY = MemoryReferenceVisualization.this.visualizationAreaHeightInPixels;
            for (int j = 0; j < grid.getColumns(); ++j) {
                g.drawLine(leftX, upperY, leftX, lowerY);
                leftX += MemoryReferenceVisualization.this.unitPixelWidth;
            }
            leftX = 0;
            for (int i = 0; i < grid.getRows(); ++i) {
                g.drawLine(leftX, upperY, rightX, upperY);
                upperY += MemoryReferenceVisualization.this.unitPixelHeight;
            }
        }
        
        private void paintGrid(final Graphics g, final Grid grid) {
            int upperLeftX = 0;
            int upperLeftY = 0;
            for (int i = 0; i < grid.getRows(); ++i) {
                for (int j = 0; j < grid.getColumns(); ++j) {
                    g.setColor(MemoryReferenceVisualization.this.counterColorScale.getColor(grid.getElementFast(i, j)));
                    g.fillRect(upperLeftX, upperLeftY, MemoryReferenceVisualization.this.unitPixelWidth, MemoryReferenceVisualization.this.unitPixelHeight);
                    upperLeftX += MemoryReferenceVisualization.this.unitPixelWidth;
                }
                upperLeftX = 0;
                upperLeftY += MemoryReferenceVisualization.this.unitPixelHeight;
            }
        }
        
        private Color getContrastingColor(final Color color) {
            return new Color(color.getRGB() ^ 0xFFFFFF);
        }
    }
    
    private class ColorChooserControls
    {
        private JLabel sliderLabel;
        private JSlider colorRangeSlider;
        private JButton currentColorButton;
        private JPanel colorChooserRow;
        private JPanel countDisplayRow;
        private volatile int counterIndex;
        
        private ColorChooserControls() {
            this.sliderLabel = null;
            this.colorRangeSlider = null;
            (this.colorRangeSlider = new JSlider(0, 0, MemoryReferenceVisualization.this.countTable.length - 1, 10)).setToolTipText("View or change color associated with each reference count value");
            this.colorRangeSlider.setPaintTicks(false);
            this.colorRangeSlider.addChangeListener(new ColorChooserListener());
            this.counterIndex = 10;
            (this.sliderLabel = new JLabel(this.setLabel(MemoryReferenceVisualization.this.countTable[this.counterIndex]))).setToolTipText("Reference count values listed on non-linear scale of " + MemoryReferenceVisualization.this.countTable[0] + " to " + MemoryReferenceVisualization.this.countTable[MemoryReferenceVisualization.this.countTable.length - 1]);
            this.sliderLabel.setHorizontalAlignment(0);
            this.sliderLabel.setAlignmentX(0.5f);
            (this.currentColorButton = new JButton("   ")).setToolTipText("Click here to change color for the reference count subrange based at current value");
            this.currentColorButton.setBackground(MemoryReferenceVisualization.this.counterColorScale.getColor(MemoryReferenceVisualization.this.countTable[this.counterIndex]));
            this.currentColorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final int counterValue = MemoryReferenceVisualization.this.countTable[ColorChooserControls.this.counterIndex];
                    final int highEnd = MemoryReferenceVisualization.this.counterColorScale.getHighEndOfRange(counterValue);
                    final String dialogLabel = "Select color for reference count " + ((counterValue == highEnd) ? ("value " + counterValue) : ("range " + counterValue + "-" + highEnd));
                    final Color newColor = JColorChooser.showDialog(MemoryReferenceVisualization.this.theWindow, dialogLabel, MemoryReferenceVisualization.this.counterColorScale.getColor(counterValue));
                    if (newColor != null && !newColor.equals(MemoryReferenceVisualization.this.counterColorScale.getColor(counterValue))) {
                        MemoryReferenceVisualization.this.counterColorScale.insertOrReplace(new CounterColor(counterValue, newColor));
                        ColorChooserControls.this.currentColorButton.setBackground(newColor);
                        MemoryReferenceVisualization.this.updateDisplay();
                    }
                }
            });
            this.colorChooserRow = new JPanel();
            this.countDisplayRow = new JPanel();
            this.colorChooserRow.add(this.colorRangeSlider);
            this.colorChooserRow.add(this.currentColorButton);
            this.countDisplayRow.add(this.sliderLabel);
        }
        
        private String setLabel(final int value) {
            String spaces = "  ";
            if (value >= 10) {
                spaces = " ";
            }
            else if (value >= 100) {
                spaces = "";
            }
            return "Counter value " + spaces + value;
        }
        
        private class ColorChooserListener implements ChangeListener
        {
            @Override
            public void stateChanged(final ChangeEvent e) {
                final JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    ColorChooserControls.this.counterIndex = source.getValue();
                }
                else {
                    final int count = MemoryReferenceVisualization.this.countTable[source.getValue()];
                    ColorChooserControls.this.sliderLabel.setText(ColorChooserControls.this.setLabel(count));
                    ColorChooserControls.this.currentColorButton.setBackground(MemoryReferenceVisualization.this.counterColorScale.getColor(count));
                }
            }
        }
    }
    
    private class CounterColorScale
    {
        CounterColor[] counterColors;
        
        CounterColorScale(final CounterColor[] colors) {
            this.counterColors = colors;
        }
        
        private Color getColor(final int count) {
            Color result = this.counterColors[0].associatedColor;
            for (int index = 0; index < this.counterColors.length && count >= this.counterColors[index].colorRangeStart; ++index) {
                result = this.counterColors[index].associatedColor;
            }
            return result;
        }
        
        private int getHighEndOfRange(final int count) {
            int highEnd = Integer.MAX_VALUE;
            if (count < this.counterColors[this.counterColors.length - 1].colorRangeStart) {
                for (int index = 0; index < this.counterColors.length - 1 && count >= this.counterColors[index].colorRangeStart; ++index) {
                    highEnd = this.counterColors[index + 1].colorRangeStart - 1;
                }
            }
            return highEnd;
        }
        
        private void insertOrReplace(final CounterColor newColor) {
            final int index = Arrays.binarySearch(this.counterColors, newColor);
            if (index >= 0) {
                this.counterColors[index] = newColor;
            }
            else {
                final int insertIndex = -index - 1;
                final CounterColor[] newSortedArray = new CounterColor[this.counterColors.length + 1];
                System.arraycopy(this.counterColors, 0, newSortedArray, 0, insertIndex);
                System.arraycopy(this.counterColors, insertIndex, newSortedArray, insertIndex + 1, this.counterColors.length - insertIndex);
                newSortedArray[insertIndex] = newColor;
                this.counterColors = newSortedArray;
            }
        }
    }
    
    private class CounterColor implements Comparable
    {
        private int colorRangeStart;
        private Color associatedColor;
        
        public CounterColor(final int start, final Color color) {
            this.colorRangeStart = start;
            this.associatedColor = color;
        }
        
        @Override
        public int compareTo(final Object other) {
            if (other instanceof CounterColor) {
                return this.colorRangeStart - ((CounterColor)other).colorRangeStart;
            }
            throw new ClassCastException();
        }
    }
    
    private class Grid
    {
        int[][] grid;
        int rows;
        int columns;
        
        private Grid(final int rows, final int columns) {
            this.grid = new int[rows][columns];
            this.rows = rows;
            this.columns = columns;
        }
        
        private int getRows() {
            return this.rows;
        }
        
        private int getColumns() {
            return this.columns;
        }
        
        private int getElement(final int row, final int column) {
            return (row >= 0 && row <= this.rows && column >= 0 && column <= this.columns) ? this.grid[row][column] : -1;
        }
        
        private int getElementFast(final int row, final int column) {
            return this.grid[row][column];
        }
        
        private int incrementElement(final int row, final int column) {
            int n;
            if (row >= 0 && row <= this.rows && column >= 0 && column <= this.columns) {
                final int[] array = this.grid[row];
                n = ++array[column];
            }
            else {
                n = -1;
            }
            return n;
        }
        
        private void reset() {
            for (int i = 0; i < this.rows; ++i) {
                for (int j = 0; j < this.columns; ++j) {
                    this.grid[i][j] = 0;
                }
            }
        }
    }
}
