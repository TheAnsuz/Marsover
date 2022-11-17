

package mars.tools;

import mars.util.Binary;
import java.awt.BorderLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.hardware.AccessNotice;
import java.util.Observable;
import javax.swing.BorderFactory;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JComponent;
import java.util.Random;
import java.awt.Color;
import java.awt.Font;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JComboBox;

public class CacheSimulator extends AbstractMarsToolAndApplication
{
    private static boolean debug;
    private static String version;
    private static String heading;
    private JComboBox cacheBlockSizeSelector;
    private JComboBox cacheBlockCountSelector;
    private JComboBox cachePlacementSelector;
    private JComboBox cacheReplacementSelector;
    private JComboBox cacheSetSizeSelector;
    private JTextField memoryAccessCountDisplay;
    private JTextField cacheHitCountDisplay;
    private JTextField cacheMissCountDisplay;
    private JTextField replacementPolicyDisplay;
    private JTextField cachableAddressesDisplay;
    private JTextField cacheSizeDisplay;
    private JProgressBar cacheHitRateDisplay;
    private Animation animations;
    private JPanel logPanel;
    private JScrollPane logScroll;
    private JTextArea logText;
    private JCheckBox logShow;
    private EmptyBorder emptyBorder;
    private Font countFonts;
    private Color backgroundColor;
    private int[] cacheBlockSizeChoicesInt;
    private int[] cacheBlockCountChoicesInt;
    private String[] cacheBlockSizeChoices;
    private String[] cacheBlockCountChoices;
    private String[] placementPolicyChoices;
    private final int DIRECT = 0;
    private final int FULL = 1;
    private final int SET = 2;
    private String[] replacementPolicyChoices;
    private final int LRU = 0;
    private final int RANDOM = 1;
    private String[] cacheSetSizeChoices;
    private int defaultCacheBlockSizeIndex;
    private int defaultCacheBlockCountIndex;
    private int defaultPlacementPolicyIndex;
    private int defaultReplacementPolicyIndex;
    private int defaultCacheSetSizeIndex;
    private AbstractCache theCache;
    private int memoryAccessCount;
    private int cacheHitCount;
    private int cacheMissCount;
    private double cacheHitRate;
    private Random randu;
    
    public CacheSimulator(final String title, final String heading) {
        super(title, heading);
        this.emptyBorder = new EmptyBorder(4, 4, 4, 4);
        this.countFonts = new Font("Times", 1, 12);
        this.backgroundColor = Color.WHITE;
        this.cacheBlockSizeChoices = new String[] { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" };
        this.cacheBlockCountChoices = new String[] { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" };
        this.placementPolicyChoices = new String[] { "Direct Mapping", "Fully Associative", "N-way Set Associative" };
        this.replacementPolicyChoices = new String[] { "LRU", "Random" };
        this.defaultCacheBlockSizeIndex = 2;
        this.defaultCacheBlockCountIndex = 3;
        this.defaultPlacementPolicyIndex = 0;
        this.defaultReplacementPolicyIndex = 0;
        this.defaultCacheSetSizeIndex = 0;
        this.randu = new Random(0L);
    }
    
    public CacheSimulator() {
        super("Data Cache Simulation Tool, " + CacheSimulator.version, CacheSimulator.heading);
        this.emptyBorder = new EmptyBorder(4, 4, 4, 4);
        this.countFonts = new Font("Times", 1, 12);
        this.backgroundColor = Color.WHITE;
        this.cacheBlockSizeChoices = new String[] { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" };
        this.cacheBlockCountChoices = new String[] { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048" };
        this.placementPolicyChoices = new String[] { "Direct Mapping", "Fully Associative", "N-way Set Associative" };
        this.replacementPolicyChoices = new String[] { "LRU", "Random" };
        this.defaultCacheBlockSizeIndex = 2;
        this.defaultCacheBlockCountIndex = 3;
        this.defaultPlacementPolicyIndex = 0;
        this.defaultReplacementPolicyIndex = 0;
        this.defaultCacheSetSizeIndex = 0;
        this.randu = new Random(0L);
    }
    
    public static void main(final String[] args) {
        new CacheSimulator("Data Cache Simulator stand-alone, " + CacheSimulator.version, CacheSimulator.heading).go();
    }
    
    @Override
    public String getName() {
        return "Data Cache Simulator";
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        final Box results = Box.createVerticalBox();
        results.add(this.buildOrganizationArea());
        results.add(this.buildPerformanceArea());
        results.add(this.buildLogArea());
        return results;
    }
    
    private JComponent buildLogArea() {
        this.logPanel = new JPanel();
        final TitledBorder ltb = new TitledBorder("Runtime Log");
        ltb.setTitleJustification(2);
        this.logPanel.setBorder(ltb);
        (this.logShow = new JCheckBox("Enabled", CacheSimulator.debug)).addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                CacheSimulator.debug = (e.getStateChange() == 1);
                CacheSimulator.this.resetLogDisplay();
                CacheSimulator.this.logText.setEnabled(CacheSimulator.debug);
                CacheSimulator.this.logText.setBackground(CacheSimulator.debug ? Color.WHITE : CacheSimulator.this.logPanel.getBackground());
            }
        });
        this.logPanel.add(this.logShow);
        (this.logText = new JTextArea(5, 70)).setEnabled(CacheSimulator.debug);
        this.logText.setBackground(CacheSimulator.debug ? Color.WHITE : this.logPanel.getBackground());
        this.logText.setFont(new Font("Monospaced", 0, 12));
        this.logText.setToolTipText("Displays cache activity log if enabled");
        this.logScroll = new JScrollPane(this.logText, 20, 30);
        this.logPanel.add(this.logScroll);
        return this.logPanel;
    }
    
    private JComponent buildOrganizationArea() {
        final JPanel organization = new JPanel(new GridLayout(3, 2));
        final TitledBorder otb = new TitledBorder("Cache Organization");
        otb.setTitleJustification(2);
        organization.setBorder(otb);
        (this.cachePlacementSelector = new JComboBox((String[])this.placementPolicyChoices)).setEditable(false);
        this.cachePlacementSelector.setBackground(this.backgroundColor);
        this.cachePlacementSelector.setSelectedIndex(this.defaultPlacementPolicyIndex);
        this.cachePlacementSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                CacheSimulator.this.updateCacheSetSizeSelector();
                CacheSimulator.this.reset();
            }
        });
        (this.cacheReplacementSelector = new JComboBox((String[])this.replacementPolicyChoices)).setEditable(false);
        this.cacheReplacementSelector.setBackground(this.backgroundColor);
        this.cacheReplacementSelector.setSelectedIndex(this.defaultReplacementPolicyIndex);
        (this.cacheBlockSizeSelector = new JComboBox((String[])this.cacheBlockSizeChoices)).setEditable(false);
        this.cacheBlockSizeSelector.setBackground(this.backgroundColor);
        this.cacheBlockSizeSelector.setSelectedIndex(this.defaultCacheBlockSizeIndex);
        this.cacheBlockSizeSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                CacheSimulator.this.updateCacheSizeDisplay();
                CacheSimulator.this.reset();
            }
        });
        (this.cacheBlockCountSelector = new JComboBox((String[])this.cacheBlockCountChoices)).setEditable(false);
        this.cacheBlockCountSelector.setBackground(this.backgroundColor);
        this.cacheBlockCountSelector.setSelectedIndex(this.defaultCacheBlockCountIndex);
        this.cacheBlockCountSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                CacheSimulator.this.updateCacheSetSizeSelector();
                CacheSimulator.this.theCache = CacheSimulator.this.createNewCache();
                CacheSimulator.this.resetCounts();
                CacheSimulator.this.updateDisplay();
                CacheSimulator.this.updateCacheSizeDisplay();
                CacheSimulator.this.animations.fillAnimationBoxWithCacheBlocks();
            }
        });
        (this.cacheSetSizeSelector = new JComboBox((String[])this.cacheSetSizeChoices)).setEditable(false);
        this.cacheSetSizeSelector.setBackground(this.backgroundColor);
        this.cacheSetSizeSelector.setSelectedIndex(this.defaultCacheSetSizeIndex);
        this.cacheSetSizeSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                CacheSimulator.this.reset();
            }
        });
        final JPanel placementPolicyRow = this.getPanelWithBorderLayout();
        placementPolicyRow.setBorder(this.emptyBorder);
        placementPolicyRow.add(new JLabel("Placement Policy "), "West");
        placementPolicyRow.add(this.cachePlacementSelector, "East");
        final JPanel replacementPolicyRow = this.getPanelWithBorderLayout();
        replacementPolicyRow.setBorder(this.emptyBorder);
        replacementPolicyRow.add(new JLabel("Block Replacement Policy "), "West");
        replacementPolicyRow.add(this.cacheReplacementSelector, "East");
        final JPanel cacheSetSizeRow = this.getPanelWithBorderLayout();
        cacheSetSizeRow.setBorder(this.emptyBorder);
        cacheSetSizeRow.add(new JLabel("Set size (blocks) "), "West");
        cacheSetSizeRow.add(this.cacheSetSizeSelector, "East");
        final JPanel cacheNumberBlocksRow = this.getPanelWithBorderLayout();
        cacheNumberBlocksRow.setBorder(this.emptyBorder);
        cacheNumberBlocksRow.add(new JLabel("Number of blocks "), "West");
        cacheNumberBlocksRow.add(this.cacheBlockCountSelector, "East");
        final JPanel cacheBlockSizeRow = this.getPanelWithBorderLayout();
        cacheBlockSizeRow.setBorder(this.emptyBorder);
        cacheBlockSizeRow.add(new JLabel("Cache block size (words) "), "West");
        cacheBlockSizeRow.add(this.cacheBlockSizeSelector, "East");
        final JPanel cacheTotalSizeRow = this.getPanelWithBorderLayout();
        cacheTotalSizeRow.setBorder(this.emptyBorder);
        cacheTotalSizeRow.add(new JLabel("Cache size (bytes) "), "West");
        (this.cacheSizeDisplay = new JTextField(8)).setHorizontalAlignment(4);
        this.cacheSizeDisplay.setEditable(false);
        this.cacheSizeDisplay.setBackground(this.backgroundColor);
        this.cacheSizeDisplay.setFont(this.countFonts);
        cacheTotalSizeRow.add(this.cacheSizeDisplay, "East");
        this.updateCacheSizeDisplay();
        organization.add(placementPolicyRow);
        organization.add(cacheNumberBlocksRow);
        organization.add(replacementPolicyRow);
        organization.add(cacheBlockSizeRow);
        organization.add(cacheSetSizeRow);
        organization.add(cacheTotalSizeRow);
        return organization;
    }
    
    private JComponent buildPerformanceArea() {
        final JPanel performance = new JPanel(new GridLayout(1, 2));
        final TitledBorder ptb = new TitledBorder("Cache Performance");
        ptb.setTitleJustification(2);
        performance.setBorder(ptb);
        final JPanel memoryAccessCountRow = this.getPanelWithBorderLayout();
        memoryAccessCountRow.setBorder(this.emptyBorder);
        memoryAccessCountRow.add(new JLabel("Memory Access Count "), "West");
        (this.memoryAccessCountDisplay = new JTextField(10)).setHorizontalAlignment(4);
        this.memoryAccessCountDisplay.setEditable(false);
        this.memoryAccessCountDisplay.setBackground(this.backgroundColor);
        this.memoryAccessCountDisplay.setFont(this.countFonts);
        memoryAccessCountRow.add(this.memoryAccessCountDisplay, "East");
        final JPanel cacheHitCountRow = this.getPanelWithBorderLayout();
        cacheHitCountRow.setBorder(this.emptyBorder);
        cacheHitCountRow.add(new JLabel("Cache Hit Count "), "West");
        (this.cacheHitCountDisplay = new JTextField(10)).setHorizontalAlignment(4);
        this.cacheHitCountDisplay.setEditable(false);
        this.cacheHitCountDisplay.setBackground(this.backgroundColor);
        this.cacheHitCountDisplay.setFont(this.countFonts);
        cacheHitCountRow.add(this.cacheHitCountDisplay, "East");
        final JPanel cacheMissCountRow = this.getPanelWithBorderLayout();
        cacheMissCountRow.setBorder(this.emptyBorder);
        cacheMissCountRow.add(new JLabel("Cache Miss Count "), "West");
        (this.cacheMissCountDisplay = new JTextField(10)).setHorizontalAlignment(4);
        this.cacheMissCountDisplay.setEditable(false);
        this.cacheMissCountDisplay.setBackground(this.backgroundColor);
        this.cacheMissCountDisplay.setFont(this.countFonts);
        cacheMissCountRow.add(this.cacheMissCountDisplay, "East");
        final JPanel cacheHitRateRow = this.getPanelWithBorderLayout();
        cacheHitRateRow.setBorder(this.emptyBorder);
        cacheHitRateRow.add(new JLabel("Cache Hit Rate "), "West");
        (this.cacheHitRateDisplay = new JProgressBar(0, 0, 100)).setStringPainted(true);
        this.cacheHitRateDisplay.setForeground(Color.BLUE);
        this.cacheHitRateDisplay.setBackground(this.backgroundColor);
        this.cacheHitRateDisplay.setFont(this.countFonts);
        cacheHitRateRow.add(this.cacheHitRateDisplay, "East");
        this.resetCounts();
        this.updateDisplay();
        final JPanel performanceMeasures = new JPanel(new GridLayout(4, 1));
        performanceMeasures.add(memoryAccessCountRow);
        performanceMeasures.add(cacheHitCountRow);
        performanceMeasures.add(cacheMissCountRow);
        performanceMeasures.add(cacheHitRateRow);
        performance.add(performanceMeasures);
        (this.animations = new Animation()).fillAnimationBoxWithCacheBlocks();
        final JPanel animationsPanel = new JPanel(new GridLayout(1, 2));
        final Box animationsLabel = Box.createVerticalBox();
        final JPanel tableTitle1 = new JPanel(new FlowLayout(0));
        final JPanel tableTitle2 = new JPanel(new FlowLayout(0));
        tableTitle1.add(new JLabel("Cache Block Table"));
        tableTitle2.add(new JLabel("(block 0 at top)"));
        animationsLabel.add(tableTitle1);
        animationsLabel.add(tableTitle2);
        final Dimension colorKeyBoxSize = new Dimension(8, 8);
        final JPanel emptyKey = new JPanel(new FlowLayout(0));
        final JPanel emptyBox = new JPanel();
        emptyBox.setSize(colorKeyBoxSize);
        emptyBox.setBackground(this.animations.defaultColor);
        emptyBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        emptyKey.add(emptyBox);
        emptyKey.add(new JLabel(" = empty"));
        final JPanel missBox = new JPanel();
        final JPanel missKey = new JPanel(new FlowLayout(0));
        missBox.setSize(colorKeyBoxSize);
        missBox.setBackground(this.animations.missColor);
        missBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        missKey.add(missBox);
        missKey.add(new JLabel(" = miss"));
        final JPanel hitKey = new JPanel(new FlowLayout(0));
        final JPanel hitBox = new JPanel();
        hitBox.setSize(colorKeyBoxSize);
        hitBox.setBackground(this.animations.hitColor);
        hitBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        hitKey.add(hitBox);
        hitKey.add(new JLabel(" = hit"));
        animationsLabel.add(emptyKey);
        animationsLabel.add(hitKey);
        animationsLabel.add(missKey);
        animationsLabel.add(Box.createVerticalGlue());
        animationsPanel.add(animationsLabel);
        animationsPanel.add(this.animations.getAnimationBox());
        performance.add(animationsPanel);
        return performance;
    }
    
    @Override
    protected void processMIPSUpdate(final Observable memory, final AccessNotice accessNotice) {
        final MemoryAccessNotice notice = (MemoryAccessNotice)accessNotice;
        ++this.memoryAccessCount;
        final CacheAccessResult cacheAccessResult = this.theCache.isItAHitThenReadOnMiss(notice.getAddress());
        if (cacheAccessResult.isHit()) {
            ++this.cacheHitCount;
            this.animations.showHit(cacheAccessResult.getBlock());
        }
        else {
            ++this.cacheMissCount;
            this.animations.showMiss(cacheAccessResult.getBlock());
        }
        this.cacheHitRate = this.cacheHitCount / (double)this.memoryAccessCount;
    }
    
    @Override
    protected void initializePreGUI() {
        this.cacheBlockSizeChoicesInt = new int[this.cacheBlockSizeChoices.length];
        for (int i = 0; i < this.cacheBlockSizeChoices.length; ++i) {
            try {
                this.cacheBlockSizeChoicesInt[i] = Integer.parseInt(this.cacheBlockSizeChoices[i]);
            }
            catch (NumberFormatException nfe) {
                this.cacheBlockSizeChoicesInt[i] = 1;
            }
        }
        this.cacheBlockCountChoicesInt = new int[this.cacheBlockCountChoices.length];
        for (int i = 0; i < this.cacheBlockCountChoices.length; ++i) {
            try {
                this.cacheBlockCountChoicesInt[i] = Integer.parseInt(this.cacheBlockCountChoices[i]);
            }
            catch (NumberFormatException nfe) {
                this.cacheBlockCountChoicesInt[i] = 1;
            }
        }
        this.cacheSetSizeChoices = this.determineSetSizeChoices(this.defaultCacheBlockCountIndex, this.defaultPlacementPolicyIndex);
    }
    
    @Override
    protected void initializePostGUI() {
        this.theCache = this.createNewCache();
    }
    
    @Override
    protected void reset() {
        this.theCache = this.createNewCache();
        this.resetCounts();
        this.updateDisplay();
        this.animations.reset();
        this.resetLogDisplay();
    }
    
    @Override
    protected void updateDisplay() {
        this.updateMemoryAccessCountDisplay();
        this.updateCacheHitCountDisplay();
        this.updateCacheMissCountDisplay();
        this.updateCacheHitRateDisplay();
    }
    
    private String[] determineSetSizeChoices(final int cacheBlockCountIndex, final int placementPolicyIndex) {
        final int firstBlockCountIndex = 0;
        final int lastBlockCountIndex = cacheBlockCountIndex;
        String[] choices = null;
        switch (placementPolicyIndex) {
            case 0: {
                choices = new String[] { this.cacheBlockCountChoices[firstBlockCountIndex] };
                break;
            }
            case 2: {
                choices = new String[lastBlockCountIndex - firstBlockCountIndex + 1];
                for (int i = 0; i < choices.length; ++i) {
                    choices[i] = this.cacheBlockCountChoices[firstBlockCountIndex + i];
                }
                break;
            }
            default: {
                choices = new String[] { this.cacheBlockCountChoices[lastBlockCountIndex] };
                break;
            }
        }
        return choices;
    }
    
    private void updateCacheSetSizeSelector() {
        this.cacheSetSizeSelector.setModel(new DefaultComboBoxModel<String>(this.determineSetSizeChoices(this.cacheBlockCountSelector.getSelectedIndex(), this.cachePlacementSelector.getSelectedIndex())));
    }
    
    private AbstractCache createNewCache() {
        AbstractCache theNewCache = null;
        int setSize = 1;
        try {
            setSize = Integer.parseInt((String)this.cacheSetSizeSelector.getSelectedItem());
        }
        catch (NumberFormatException ex) {}
        theNewCache = new AnyCache(this.cacheBlockCountChoicesInt[this.cacheBlockCountSelector.getSelectedIndex()], this.cacheBlockSizeChoicesInt[this.cacheBlockSizeSelector.getSelectedIndex()], setSize);
        return theNewCache;
    }
    
    private void resetCounts() {
        this.memoryAccessCount = 0;
        this.cacheHitCount = 0;
        this.cacheMissCount = 0;
        this.cacheHitRate = 0.0;
    }
    
    private void updateMemoryAccessCountDisplay() {
        this.memoryAccessCountDisplay.setText(new Integer(this.memoryAccessCount).toString());
    }
    
    private void updateCacheHitCountDisplay() {
        this.cacheHitCountDisplay.setText(new Integer(this.cacheHitCount).toString());
    }
    
    private void updateCacheMissCountDisplay() {
        this.cacheMissCountDisplay.setText(new Integer(this.cacheMissCount).toString());
    }
    
    private void updateCacheHitRateDisplay() {
        this.cacheHitRateDisplay.setValue((int)Math.round(this.cacheHitRate * 100.0));
    }
    
    private void updateCacheSizeDisplay() {
        final int cacheSize = this.cacheBlockSizeChoicesInt[this.cacheBlockSizeSelector.getSelectedIndex()] * this.cacheBlockCountChoicesInt[this.cacheBlockCountSelector.getSelectedIndex()] * 4;
        this.cacheSizeDisplay.setText(Integer.toString(cacheSize));
    }
    
    private JPanel getPanelWithBorderLayout() {
        return new JPanel(new BorderLayout(2, 2));
    }
    
    private void resetLogDisplay() {
        this.logText.setText("");
    }
    
    private void writeLog(final String text) {
        this.logText.append(text);
        this.logText.setCaretPosition(this.logText.getDocument().getLength());
    }
    
    static {
        CacheSimulator.debug = false;
        CacheSimulator.version = "Version 1.2";
        CacheSimulator.heading = "Simulate and illustrate data cache performance";
    }
    
    private class CacheBlock
    {
        private boolean valid;
        private int tag;
        private int sizeInWords;
        private int mostRecentAccessTime;
        
        public CacheBlock(final int sizeInWords) {
            this.valid = false;
            this.tag = 0;
            this.sizeInWords = sizeInWords;
            this.mostRecentAccessTime = -1;
        }
    }
    
    private class CacheAccessResult
    {
        private boolean hitOrMiss;
        private int blockNumber;
        
        public CacheAccessResult(final boolean hitOrMiss, final int blockNumber) {
            this.hitOrMiss = hitOrMiss;
            this.blockNumber = blockNumber;
        }
        
        public boolean isHit() {
            return this.hitOrMiss;
        }
        
        public int getBlock() {
            return this.blockNumber;
        }
    }
    
    private abstract class AbstractCache
    {
        private int numberOfBlocks;
        private int blockSizeInWords;
        private int setSizeInBlocks;
        private int numberOfSets;
        protected CacheBlock[] blocks;
        
        protected AbstractCache(final int numberOfBlocks, final int blockSizeInWords, final int setSizeInBlocks) {
            this.numberOfBlocks = numberOfBlocks;
            this.blockSizeInWords = blockSizeInWords;
            this.setSizeInBlocks = setSizeInBlocks;
            this.numberOfSets = numberOfBlocks / setSizeInBlocks;
            this.blocks = new CacheBlock[numberOfBlocks];
            this.reset();
        }
        
        public int getNumberOfBlocks() {
            return this.numberOfBlocks;
        }
        
        public int getNumberOfSets() {
            return this.numberOfSets;
        }
        
        public int getSetSizeInBlocks() {
            return this.setSizeInBlocks;
        }
        
        public int getBlockSizeInWords() {
            return this.blockSizeInWords;
        }
        
        public int getCacheSizeInWords() {
            return this.numberOfBlocks * this.blockSizeInWords;
        }
        
        public int getCacheSizeInBytes() {
            return this.numberOfBlocks * this.blockSizeInWords * 4;
        }
        
        public int getSetNumber(final int address) {
            return address / 4 / this.blockSizeInWords % this.numberOfSets;
        }
        
        public int getTag(final int address) {
            return address / 4 / this.blockSizeInWords / this.numberOfSets;
        }
        
        public int getFirstBlockToSearch(final int address) {
            return this.getSetNumber(address) * this.setSizeInBlocks;
        }
        
        public int getLastBlockToSearch(final int address) {
            return this.getFirstBlockToSearch(address) + this.setSizeInBlocks - 1;
        }
        
        public void reset() {
            for (int i = 0; i < this.numberOfBlocks; ++i) {
                this.blocks[i] = new CacheBlock(this.blockSizeInWords);
            }
            System.gc();
        }
        
        public abstract CacheAccessResult isItAHitThenReadOnMiss(final int p0);
    }
    
    private class AnyCache extends AbstractCache
    {
        private final int SET_FULL = 0;
        private final int HIT = 1;
        private final int MISS = 2;
        
        public AnyCache(final int numberOfBlocks, final int blockSizeInWords, final int setSizeInBlocks) {
            super(numberOfBlocks, blockSizeInWords, setSizeInBlocks);
        }
        
        @Override
        public CacheAccessResult isItAHitThenReadOnMiss(final int address) {
            int result = 0;
            final int firstBlock = this.getFirstBlockToSearch(address);
            final int lastBlock = this.getLastBlockToSearch(address);
            if (CacheSimulator.debug) {
                CacheSimulator.this.writeLog("(" + CacheSimulator.this.memoryAccessCount + ") address: " + Binary.intToHexString(address) + " (tag " + Binary.intToHexString(this.getTag(address)) + ")  block range: " + firstBlock + "-" + lastBlock + "\n");
            }
            int blockNumber;
            CacheBlock block;
            for (blockNumber = 0, blockNumber = firstBlock; blockNumber <= lastBlock; ++blockNumber) {
                block = this.blocks[blockNumber];
                if (CacheSimulator.debug) {
                    CacheSimulator.this.writeLog("   trying block " + blockNumber + (block.valid ? (" tag " + Binary.intToHexString(block.tag)) : " empty"));
                }
                if (block.valid && block.tag == this.getTag(address)) {
                    if (CacheSimulator.debug) {
                        CacheSimulator.this.writeLog(" -- HIT\n");
                    }
                    result = 1;
                    block.mostRecentAccessTime = CacheSimulator.this.memoryAccessCount;
                    break;
                }
                if (!block.valid) {
                    if (CacheSimulator.debug) {
                        CacheSimulator.this.writeLog(" -- MISS\n");
                    }
                    result = 2;
                    block.valid = true;
                    block.tag = this.getTag(address);
                    block.mostRecentAccessTime = CacheSimulator.this.memoryAccessCount;
                    break;
                }
                if (CacheSimulator.debug) {
                    CacheSimulator.this.writeLog(" -- OCCUPIED\n");
                }
            }
            if (result == 0) {
                if (CacheSimulator.debug) {
                    CacheSimulator.this.writeLog("   MISS due to FULL SET");
                }
                final int blockToReplace = this.selectBlockToReplace(firstBlock, lastBlock);
                block = this.blocks[blockToReplace];
                block.tag = this.getTag(address);
                block.mostRecentAccessTime = CacheSimulator.this.memoryAccessCount;
                blockNumber = blockToReplace;
            }
            return new CacheAccessResult(result == 1, blockNumber);
        }
        
        private int selectBlockToReplace(final int first, final int last) {
            int replaceBlock = first;
            if (first != last) {
                switch (CacheSimulator.this.cacheReplacementSelector.getSelectedIndex()) {
                    case 1: {
                        replaceBlock = first + CacheSimulator.this.randu.nextInt(last - first + 1);
                        if (CacheSimulator.debug) {
                            CacheSimulator.this.writeLog(" -- Random replace block " + replaceBlock + "\n");
                            break;
                        }
                        break;
                    }
                    default: {
                        int leastRecentAccessTime = CacheSimulator.this.memoryAccessCount;
                        for (int block = first; block <= last; ++block) {
                            if (this.blocks[block].mostRecentAccessTime < leastRecentAccessTime) {
                                leastRecentAccessTime = this.blocks[block].mostRecentAccessTime;
                                replaceBlock = block;
                            }
                        }
                        if (CacheSimulator.debug) {
                            CacheSimulator.this.writeLog(" -- LRU replace block " + replaceBlock + "; unused since (" + leastRecentAccessTime + ")\n");
                            break;
                        }
                        break;
                    }
                }
            }
            return replaceBlock;
        }
    }
    
    private class Animation
    {
        private Box animation;
        private JTextField[] blocks;
        public final Color hitColor;
        public final Color missColor;
        public final Color defaultColor;
        
        public Animation() {
            this.hitColor = Color.GREEN;
            this.missColor = Color.RED;
            this.defaultColor = Color.WHITE;
            this.animation = Box.createVerticalBox();
        }
        
        private Box getAnimationBox() {
            return this.animation;
        }
        
        public int getNumberOfBlocks() {
            return (this.blocks == null) ? 0 : this.blocks.length;
        }
        
        public void showHit(final int blockNum) {
            this.blocks[blockNum].setBackground(this.hitColor);
        }
        
        public void showMiss(final int blockNum) {
            this.blocks[blockNum].setBackground(this.missColor);
        }
        
        public void reset() {
            for (int i = 0; i < this.blocks.length; ++i) {
                this.blocks[i].setBackground(this.defaultColor);
            }
        }
        
        private void fillAnimationBoxWithCacheBlocks() {
            this.animation.setVisible(false);
            this.animation.removeAll();
            final int numberOfBlocks = CacheSimulator.this.cacheBlockCountChoicesInt[CacheSimulator.this.cacheBlockCountSelector.getSelectedIndex()];
            final int totalVerticalPixels = 128;
            final int blockPixelHeight = (numberOfBlocks > totalVerticalPixels) ? 1 : (totalVerticalPixels / numberOfBlocks);
            final int blockPixelWidth = 40;
            final Dimension blockDimension = new Dimension(blockPixelWidth, blockPixelHeight);
            this.blocks = new JTextField[numberOfBlocks];
            for (int i = 0; i < numberOfBlocks; ++i) {
                (this.blocks[i] = new JTextField()).setEditable(false);
                this.blocks[i].setBackground(this.defaultColor);
                this.blocks[i].setSize(blockDimension);
                this.blocks[i].setPreferredSize(blockDimension);
                this.animation.add(this.blocks[i]);
            }
            this.animation.repaint();
            this.animation.setVisible(true);
        }
    }
}
