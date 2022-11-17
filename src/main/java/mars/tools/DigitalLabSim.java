

package mars.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import mars.Globals;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.simulator.Simulator;

public class DigitalLabSim extends AbstractMarsToolAndApplication
{
    private static String heading;
    private static String version;
    private static final int IN_ADRESS_DISPLAY_1;
    private static final int IN_ADRESS_DISPLAY_2;
    private static final int IN_ADRESS_HEXA_KEYBOARD;
    private static final int IN_ADRESS_COUNTER;
    private static final int OUT_ADRESS_HEXA_KEYBOARD;
    public static final int EXTERNAL_INTERRUPT_TIMER = 256;
    public static final int EXTERNAL_INTERRUPT_HEXA_KEYBOARD = 512;
    private static JPanel panelTools;
    private SevenSegmentPanel sevenSegPanel;
    private static int KeyBoardValueButtonClick;
    private HexaKeyboard hexaKeyPanel;
    private static boolean KeyboardInterruptOnOff;
    private static int CounterValueMax;
    private static int CounterValue;
    private static boolean CounterInterruptOnOff;
    private static OneSecondCounter SecondCounter;
    
    public DigitalLabSim(final String title, final String heading) {
        super(title, heading);
    }
    
    public DigitalLabSim() {
        super(DigitalLabSim.heading + ", " + DigitalLabSim.version, DigitalLabSim.heading);
    }
    
    public static void main(final String[] args) {
        new DigitalLabSim(DigitalLabSim.heading + ", " + DigitalLabSim.version, DigitalLabSim.heading).go();
    }
    
    @Override
    public String getName() {
        return "Digital Lab Sim";
    }
    
    @Override
    protected void addAsObserver() {
        this.addAsObserver(DigitalLabSim.IN_ADRESS_DISPLAY_1, DigitalLabSim.IN_ADRESS_DISPLAY_1);
        this.addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }
    
    @Override
    public void update(final Observable ressource, final Object accessNotice) {
        final MemoryAccessNotice notice = (MemoryAccessNotice)accessNotice;
        final int address = notice.getAddress();
        final char value = (char)notice.getValue();
        if (address == DigitalLabSim.IN_ADRESS_DISPLAY_1) {
            this.updateSevenSegment(1, value);
        }
        else if (address == DigitalLabSim.IN_ADRESS_DISPLAY_2) {
            this.updateSevenSegment(0, value);
        }
        else if (address == DigitalLabSim.IN_ADRESS_HEXA_KEYBOARD) {
            this.updateHexaKeyboard(value);
        }
        else if (address == DigitalLabSim.IN_ADRESS_COUNTER) {
            this.updateOneSecondCounter(value);
        }
        if (DigitalLabSim.CounterInterruptOnOff) {
            if (DigitalLabSim.CounterValue > 0) {
                --DigitalLabSim.CounterValue;
            }
            else {
                DigitalLabSim.CounterValue = DigitalLabSim.CounterValueMax;
                if ((Coprocessor0.getValue(12) & 0x2) == 0x0) {
                    Simulator.externalInterruptingDevice = 256;
                }
            }
        }
    }
    
    @Override
    protected void reset() {
        this.sevenSegPanel.resetSevenSegment();
        this.hexaKeyPanel.resetHexaKeyboard();
        DigitalLabSim.SecondCounter.resetOneSecondCounter();
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        DigitalLabSim.panelTools = new JPanel(new GridLayout(1, 2));
        this.sevenSegPanel = new SevenSegmentPanel();
        DigitalLabSim.panelTools.add(this.sevenSegPanel);
        this.hexaKeyPanel = new HexaKeyboard();
        DigitalLabSim.panelTools.add(this.hexaKeyPanel);
        DigitalLabSim.SecondCounter = new OneSecondCounter();
        return DigitalLabSim.panelTools;
    }
    
    private synchronized void updateMMIOControlAndData(final int dataAddr, final int dataValue) {
        if (!this.isBeingUsedAsAMarsTool || (this.isBeingUsedAsAMarsTool && this.connectButton.isConnected())) {
            synchronized (Globals.memoryAndRegistersLock) {
                try {
                    Globals.memory.setByte(dataAddr, dataValue);
                }
                catch (AddressErrorException aee) {
                    System.out.println("Tool author specified incorrect MMIO address!" + aee);
                    System.exit(0);
                }
            }
            if (Globals.getGui() != null && Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().getCodeHighlighting()) {
                Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().updateValues();
            }
        }
    }
    
    @Override
    protected JComponent getHelpComponent() {
        final String helpContent = " This tool is composed of 3 parts : two seven-segment displays, an hexadecimal keyboard and counter \nSeven segment display\n Byte value at address 0xFFFF0010 : command right seven segment display \n  Byte value at address 0xFFFF0011 : command left seven segment display \n  Each bit of these two bytes are connected to segments (bit 0 for a segment, 1 for b segment and 7 for point \n \nHexadecimal keyboard\n Byte value at address 0xFFFF0012 : command row number of hexadecimal keyboard (bit 0 to 3) and enable keyboard interrupt (bit 7) \n Byte value at address 0xFFFF0014 : receive row and column of the key pressed, 0 if not key pressed \n The mips program have to scan, one by one, each row (send 1,2,4,8...) and then observe if a key is pressed (that mean byte value at adresse 0xFFFF0014 is different from zero).  This byte value is composed of row number (4 left bits) and column number (4 right bits) Here you'll find the code for each key : 0x11,0x21,0x41,0x81,0x12,0x22,0x42,0x82,0x14,0x24,0x44,0x84,0x18,0x28,0x48,0x88. \n For exemple key number 2 return 0x41, that mean the key is on column 3 and row 1. \n If keyboard interruption is enable, an exception is started, with cause register bit number 11 set.\n \nCounter\n Byte value at address 0xFFFF0013 : If one bit of this byte is set, the counter interruption is enable.\n If counter interruption is enable, every 30 instructions, an exception is started with cause register bit number 10.\n   (contributed by Didier Teifreto, dteifreto@lifc.univ-fcomte.fr)";
        final JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JTextArea ja = new JTextArea(" This tool is composed of 3 parts : two seven-segment displays, an hexadecimal keyboard and counter \nSeven segment display\n Byte value at address 0xFFFF0010 : command right seven segment display \n  Byte value at address 0xFFFF0011 : command left seven segment display \n  Each bit of these two bytes are connected to segments (bit 0 for a segment, 1 for b segment and 7 for point \n \nHexadecimal keyboard\n Byte value at address 0xFFFF0012 : command row number of hexadecimal keyboard (bit 0 to 3) and enable keyboard interrupt (bit 7) \n Byte value at address 0xFFFF0014 : receive row and column of the key pressed, 0 if not key pressed \n The mips program have to scan, one by one, each row (send 1,2,4,8...) and then observe if a key is pressed (that mean byte value at adresse 0xFFFF0014 is different from zero).  This byte value is composed of row number (4 left bits) and column number (4 right bits) Here you'll find the code for each key : 0x11,0x21,0x41,0x81,0x12,0x22,0x42,0x82,0x14,0x24,0x44,0x84,0x18,0x28,0x48,0x88. \n For exemple key number 2 return 0x41, that mean the key is on column 3 and row 1. \n If keyboard interruption is enable, an exception is started, with cause register bit number 11 set.\n \nCounter\n Byte value at address 0xFFFF0013 : If one bit of this byte is set, the counter interruption is enable.\n If counter interruption is enable, every 30 instructions, an exception is started with cause register bit number 10.\n   (contributed by Didier Teifreto, dteifreto@lifc.univ-fcomte.fr)");
                ja.setRows(20);
                ja.setColumns(60);
                ja.setLineWrap(true);
                ja.setWrapStyleWord(true);
                JOptionPane.showMessageDialog(DigitalLabSim.this.theWindow, new JScrollPane(ja), "Simulating the Hexa Keyboard and Seven segment display", 1);
            }
        });
        return help;
    }
    
    public void updateSevenSegment(final int number, final char value) {
        this.sevenSegPanel.display[number].modifyDisplay(value);
    }
    
    public void updateHexaKeyboard(final char row) {
        final int key = DigitalLabSim.KeyBoardValueButtonClick;
        if (key != -1 && 1 << key / 4 == (row & '\u000f')) {
            this.updateMMIOControlAndData(DigitalLabSim.OUT_ADRESS_HEXA_KEYBOARD, (char)(1 << key / 4) | 1 << 4 + key % 4);
        }
        else {
            this.updateMMIOControlAndData(DigitalLabSim.OUT_ADRESS_HEXA_KEYBOARD, 0);
        }
        DigitalLabSim.KeyboardInterruptOnOff = (row & '\u00f0') != 0x0;
    }
    
    public void updateOneSecondCounter(final char value) {
        if (value != '\0') {
            DigitalLabSim.CounterInterruptOnOff = true;
            DigitalLabSim.CounterValue = DigitalLabSim.CounterValueMax;
        }
        else {
            DigitalLabSim.CounterInterruptOnOff = false;
        }
    }
    
    static {
        DigitalLabSim.heading = "Digital Lab Sim";
        DigitalLabSim.version = " Version 1.0 (Didier Teifreto)";
        IN_ADRESS_DISPLAY_1 = Memory.memoryMapBaseAddress + 16;
        IN_ADRESS_DISPLAY_2 = Memory.memoryMapBaseAddress + 17;
        IN_ADRESS_HEXA_KEYBOARD = Memory.memoryMapBaseAddress + 18;
        IN_ADRESS_COUNTER = Memory.memoryMapBaseAddress + 19;
        OUT_ADRESS_HEXA_KEYBOARD = Memory.memoryMapBaseAddress + 20;
        DigitalLabSim.KeyBoardValueButtonClick = -1;
        DigitalLabSim.KeyboardInterruptOnOff = false;
        DigitalLabSim.CounterValueMax = 30;
        DigitalLabSim.CounterValue = DigitalLabSim.CounterValueMax;
        DigitalLabSim.CounterInterruptOnOff = false;
    }
    
    public class SevenSegmentDisplay extends JComponent
    {
        public char aff;
        
        public SevenSegmentDisplay(final char aff) {
            this.aff = aff;
            this.setPreferredSize(new Dimension(60, 80));
        }
        
        public void modifyDisplay(final char val) {
            this.aff = val;
            this.repaint();
        }
        
        public void SwitchSegment(final Graphics g, final char segment) {
            switch (segment) {
                case 'a': {
                    final int[] pxa1 = { 12, 9, 12 };
                    final int[] pxa2 = { 36, 39, 36 };
                    final int[] pya = { 5, 8, 11 };
                    g.fillPolygon(pxa1, pya, 3);
                    g.fillPolygon(pxa2, pya, 3);
                    g.fillRect(12, 5, 24, 6);
                    break;
                }
                case 'b': {
                    final int[] pxb = { 37, 40, 43 };
                    final int[] pyb1 = { 12, 9, 12 };
                    final int[] pyb2 = { 36, 39, 36 };
                    g.fillPolygon(pxb, pyb1, 3);
                    g.fillPolygon(pxb, pyb2, 3);
                    g.fillRect(37, 12, 6, 24);
                    break;
                }
                case 'c': {
                    final int[] pxc = { 37, 40, 43 };
                    final int[] pyc1 = { 44, 41, 44 };
                    final int[] pyc2 = { 68, 71, 68 };
                    g.fillPolygon(pxc, pyc1, 3);
                    g.fillPolygon(pxc, pyc2, 3);
                    g.fillRect(37, 44, 6, 24);
                    break;
                }
                case 'd': {
                    final int[] pxd1 = { 12, 9, 12 };
                    final int[] pxd2 = { 36, 39, 36 };
                    final int[] pyd = { 69, 72, 75 };
                    g.fillPolygon(pxd1, pyd, 3);
                    g.fillPolygon(pxd2, pyd, 3);
                    g.fillRect(12, 69, 24, 6);
                    break;
                }
                case 'e': {
                    final int[] pxe = { 5, 8, 11 };
                    final int[] pye1 = { 44, 41, 44 };
                    final int[] pye2 = { 68, 71, 68 };
                    g.fillPolygon(pxe, pye1, 3);
                    g.fillPolygon(pxe, pye2, 3);
                    g.fillRect(5, 44, 6, 24);
                    break;
                }
                case 'f': {
                    final int[] pxf = { 5, 8, 11 };
                    final int[] pyf1 = { 12, 9, 12 };
                    final int[] pyf2 = { 36, 39, 36 };
                    g.fillPolygon(pxf, pyf1, 3);
                    g.fillPolygon(pxf, pyf2, 3);
                    g.fillRect(5, 12, 6, 24);
                    break;
                }
                case 'g': {
                    final int[] pxg1 = { 12, 9, 12 };
                    final int[] pxg2 = { 36, 39, 36 };
                    final int[] pyg = { 37, 40, 43 };
                    g.fillPolygon(pxg1, pyg, 3);
                    g.fillPolygon(pxg2, pyg, 3);
                    g.fillRect(12, 37, 24, 6);
                    break;
                }
                case 'h': {
                    g.fillOval(49, 68, 8, 8);
                    break;
                }
            }
        }
        
        @Override
        public void paint(final Graphics g) {
            for (char c = 'a'; c <= 'h'; ++c) {
                if ((this.aff & '\u0001') == 0x1) {
                    g.setColor(Color.RED);
                }
                else {
                    g.setColor(Color.LIGHT_GRAY);
                }
                this.SwitchSegment(g, c);
                this.aff >>>= 1;
            }
        }
    }
    
    public class SevenSegmentPanel extends JPanel
    {
        public SevenSegmentDisplay[] display;
        
        public SevenSegmentPanel() {
            final FlowLayout fl = new FlowLayout();
            this.setLayout(fl);
            this.display = new SevenSegmentDisplay[2];
            for (int i = 0; i < 2; ++i) {
                this.add(this.display[i] = new SevenSegmentDisplay('\0'));
            }
        }
        
        public void modifyDisplay(final int num, final char val) {
            this.display[num].modifyDisplay(val);
            this.display[num].repaint();
        }
        
        public void resetSevenSegment() {
            for (int i = 0; i < 2; ++i) {
                this.modifyDisplay(i, '\0');
            }
        }
    }
    
    public class HexaKeyboard extends JPanel
    {
        public JButton[] button;
        
        public HexaKeyboard() {
            final GridLayout layout = new GridLayout(4, 4);
            this.setLayout(layout);
            this.button = new JButton[16];
            for (int i = 0; i < 16; ++i) {
                (this.button[i] = new JButton(Integer.toHexString(i))).setBackground(Color.WHITE);
                this.button[i].setMargin(new Insets(10, 10, 10, 10));
                this.button[i].addMouseListener(new EcouteurClick(i));
                this.add(this.button[i]);
            }
        }
        
        public void resetHexaKeyboard() {
            DigitalLabSim.KeyBoardValueButtonClick = -1;
            for (int i = 0; i < 16; ++i) {
                this.button[i].setBackground(Color.WHITE);
            }
        }
        
        public class EcouteurClick implements MouseListener
        {
            private final int buttonValue;
            
            public EcouteurClick(final int val) {
                this.buttonValue = val;
            }
            
            @Override
            public void mouseEntered(final MouseEvent arg0) {
            }
            
            @Override
            public void mouseExited(final MouseEvent arg0) {
            }
            
            @Override
            public void mousePressed(final MouseEvent arg0) {
            }
            
            @Override
            public void mouseReleased(final MouseEvent arg0) {
            }
            
            @Override
            public void mouseClicked(final MouseEvent arg0) {
                if (DigitalLabSim.KeyBoardValueButtonClick != -1) {
                    DigitalLabSim.KeyBoardValueButtonClick = -1;
                    DigitalLabSim.this.updateMMIOControlAndData(DigitalLabSim.OUT_ADRESS_HEXA_KEYBOARD, 0);
                    for (int i = 0; i < 16; ++i) {
                        HexaKeyboard.this.button[i].setBackground(Color.WHITE);
                    }
                }
                else {
                    DigitalLabSim.KeyBoardValueButtonClick = this.buttonValue;
                    HexaKeyboard.this.button[DigitalLabSim.KeyBoardValueButtonClick].setBackground(Color.GREEN);
                    if (DigitalLabSim.KeyboardInterruptOnOff && (Coprocessor0.getValue(12) & 0x2) == 0x0) {
                        Simulator.externalInterruptingDevice = 512;
                    }
                }
            }
        }
    }
    
    public class OneSecondCounter
    {
        public OneSecondCounter() {
            DigitalLabSim.CounterInterruptOnOff = false;
        }
        
        public void resetOneSecondCounter() {
            DigitalLabSim.CounterInterruptOnOff = false;
            DigitalLabSim.CounterValue = DigitalLabSim.CounterValueMax;
        }
    }
}
