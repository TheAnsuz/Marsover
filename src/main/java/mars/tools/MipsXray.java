

package mars.tools;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mars.Globals;
import mars.ProgramStatement;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.venus.RunAssembleAction;
import mars.venus.RunBackstepAction;
import mars.venus.RunStepAction;
import mars.venus.VenusUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MipsXray extends AbstractMarsToolAndApplication
{
    private static final long serialVersionUID = -1L;
    private static String heading;
    private static String version;
    protected Graphics g;
    protected int lastAddress;
    protected JLabel label;
    private Container painel;
    private DatapathAnimation datapathAnimation;
    private GraphicsConfiguration gc;
    private BufferedImage datapath;
    private String instructionBinary;
    private JButton Assemble;
    private JButton Step;
    private JButton runBackStep;
    private Action runAssembleAction;
    private Action runStepAction;
    private Action runBackstepAction;
    private VenusUI mainUI;
    private JToolBar toolbar;
    private Timer time;
    
    public MipsXray(final String title, final String heading) {
        super(title, heading);
        this.lastAddress = -1;
        this.painel = this.getContentPane();
    }
    
    public MipsXray() {
        super(MipsXray.heading + ", " + MipsXray.version, MipsXray.heading);
        this.lastAddress = -1;
        this.painel = this.getContentPane();
    }
    
    @Override
    public String getName() {
        return "MIPS X-Ray";
    }
    
    @Override
    protected JComponent getHelpComponent() {
        final String helpContent = "This plugin is used to visualizate the behavior of mips processor using the default datapath. \nIt reads the source code instruction and generates an animation representing the inputs and \noutputs of functional blocks and the interconnection between them.  The basic signals \nrepresented are, control signals, opcode bits and data of functional blocks.\n\nBesides the datapath representation, information for each instruction is displayed below\nthe datapath. That display includes opcode value, with the correspondent colors used to\nrepresent the signals in datapath, mnemonic of the instruction processed at the moment, registers\nused in the instruction and a label that indicates the color code used to represent control signals\n\nTo see the datapath of register bank and control units click inside the functional unit.\n\nVersion 2.0\nDeveloped by M\u00e1rcio Roberto, Guilherme Sales, Fabr\u00edcio Vivas, Fl\u00e1vio Cardeal and F\u00e1bio L\u00facio\nContact Marcio Roberto at marcio.rdaraujo@gmail.com with questions or comments.\n";
        final JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JOptionPane.showMessageDialog(MipsXray.this.theWindow, "This plugin is used to visualizate the behavior of mips processor using the default datapath. \nIt reads the source code instruction and generates an animation representing the inputs and \noutputs of functional blocks and the interconnection between them.  The basic signals \nrepresented are, control signals, opcode bits and data of functional blocks.\n\nBesides the datapath representation, information for each instruction is displayed below\nthe datapath. That display includes opcode value, with the correspondent colors used to\nrepresent the signals in datapath, mnemonic of the instruction processed at the moment, registers\nused in the instruction and a label that indicates the color code used to represent control signals\n\nTo see the datapath of register bank and control units click inside the functional unit.\n\nVersion 2.0\nDeveloped by M\u00e1rcio Roberto, Guilherme Sales, Fabr\u00edcio Vivas, Fl\u00e1vio Cardeal and F\u00e1bio L\u00facio\nContact Marcio Roberto at marcio.rdaraujo@gmail.com with questions or comments.\n");
            }
        });
        return help;
    }
    
    protected JComponent buildAnimationSequence() {
        final JPanel image = new JPanel(new GridBagLayout());
        return image;
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        this.mainUI = Globals.getGui();
        this.createActionObjects();
        this.toolbar = this.setUpToolBar();
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        try {
            final BufferedImage im = ImageIO.read(this.getClass().getResource("/images/datapath.png"));
            final int transparency = im.getColorModel().getTransparency();
            this.datapath = this.gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
            final Graphics2D g2d = this.datapath.createGraphics();
            g2d.drawImage(im, 0, 0, null);
            g2d.dispose();
        }
        catch (IOException e) {
            System.out.println("Load Image error for " + this.getClass().getResource("/images/datapath.png") + ":\n" + e);
            e.printStackTrace();
        }
        System.setProperty("sun.java2d.translaccel", "true");
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/datapath.png"));
        final Image im2 = icon.getImage();
        icon = new ImageIcon(im2);
        final JLabel label = new JLabel(icon);
        this.painel.add(label, "West");
        this.painel.add(this.toolbar, "North");
        this.setResizable(false);
        return (JComponent)this.painel;
    }
    
    protected JComponent buildMainDisplayArea(final String figure) {
        this.mainUI = Globals.getGui();
        this.createActionObjects();
        this.toolbar = this.setUpToolBar();
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        try {
            final BufferedImage im = ImageIO.read(this.getClass().getResource("/images/" + figure));
            final int transparency = im.getColorModel().getTransparency();
            this.datapath = this.gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
            final Graphics2D g2d = this.datapath.createGraphics();
            g2d.drawImage(im, 0, 0, null);
            g2d.dispose();
        }
        catch (IOException e) {
            System.out.println("Load Image error for " + this.getClass().getResource("/images/" + figure) + ":\n" + e);
            e.printStackTrace();
        }
        System.setProperty("sun.java2d.translaccel", "true");
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/" + figure));
        final Image im2 = icon.getImage();
        icon = new ImageIcon(im2);
        final JLabel label = new JLabel(icon);
        this.painel.add(label, "West");
        this.painel.add(this.toolbar, "North");
        this.setResizable(false);
        return (JComponent)this.painel;
    }
    
    @Override
    protected void addAsObserver() {
        this.addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }
    
    @Override
    protected void processMIPSUpdate(final Observable resource, final AccessNotice notice) {
        if (!notice.accessIsFromMIPS()) {
            return;
        }
        if (notice.getAccessType() != 0) {
            return;
        }
        final MemoryAccessNotice man = (MemoryAccessNotice)notice;
        final int currentAdress = man.getAddress();
        if (currentAdress == this.lastAddress) {
            return;
        }
        this.lastAddress = currentAdress;
        try {
            BasicInstruction instr = null;
            final ProgramStatement stmt = Memory.getInstance().getStatement(currentAdress);
            if (stmt == null) {
                return;
            }
            instr = (BasicInstruction)stmt.getInstruction();
            this.instructionBinary = stmt.getMachineStatement();
            final BasicInstructionFormat format = instr.getInstructionFormat();
            this.painel.removeAll();
            this.datapathAnimation = new DatapathAnimation(this.instructionBinary);
            this.createActionObjects();
            this.toolbar = this.setUpToolBar();
            this.painel.add(this.toolbar, "North");
            this.painel.add(this.datapathAnimation, "West");
            this.datapathAnimation.startAnimation(this.instructionBinary);
        }
        catch (AddressErrorException e) {
            e.printStackTrace();
        }
    }
    
    public void updateDisplay() {
        this.repaint();
    }
    
    private JToolBar setUpToolBar() {
        final JToolBar toolBar = new JToolBar();
        (this.Assemble = new JButton(this.runAssembleAction)).setText("");
        (this.runBackStep = new JButton(this.runBackstepAction)).setText("");
        (this.Step = new JButton(this.runStepAction)).setText("");
        toolBar.add(this.Assemble);
        toolBar.add(this.Step);
        return toolBar;
    }
    
    private void createActionObjects() {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Class cs = this.getClass();
        try {
            this.runAssembleAction = new RunAssembleAction("Assemble", new ImageIcon(tk.getImage(cs.getResource("/images/Assemble22.png"))), "Assemble the current file and clear breakpoints", 65, KeyStroke.getKeyStroke(114, 0), this.mainUI);
            this.runStepAction = new RunStepAction("Step", new ImageIcon(tk.getImage(cs.getResource("/images/StepForward22.png"))), "Run one step at a time", 84, KeyStroke.getKeyStroke(118, 0), this.mainUI);
            this.runBackstepAction = new RunBackstepAction("Backstep", new ImageIcon(tk.getImage(cs.getResource("/images/StepBack22.png"))), "Undo the last step", 66, KeyStroke.getKeyStroke(119, 0), this.mainUI);
        }
        catch (Exception e) {
            System.out.println("Internal Error: images folder not found, or other null pointer exception while creating Action objects");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    static {
        MipsXray.heading = "MIPS X-Ray - Animation of MIPS Datapath";
        MipsXray.version = " Version 2.0";
    }
    
    class Vertex
    {
        private int numIndex;
        private int init;
        private int end;
        private int current;
        private String name;
        public static final int movingUpside = 1;
        public static final int movingDownside = 2;
        public static final int movingLeft = 3;
        public static final int movingRight = 4;
        public int direction;
        public int oppositeAxis;
        private boolean isMovingXaxis;
        private Color color;
        private boolean first_interaction;
        private boolean active;
        private final boolean isText;
        private final ArrayList<Integer> targetVertex;
        
        public Vertex(final int index, final int init, final int end, final String name, final int oppositeAxis, final boolean isMovingXaxis, final String listOfColors, final String listTargetVertex, final boolean isText) {
            this.numIndex = index;
            this.init = init;
            this.current = this.init;
            this.end = end;
            this.name = name;
            this.oppositeAxis = oppositeAxis;
            this.isMovingXaxis = isMovingXaxis;
            this.first_interaction = true;
            this.active = false;
            this.isText = isText;
            this.color = new Color(0, 153, 0);
            if (isMovingXaxis) {
                if (init < end) {
                    this.direction = 3;
                }
                else {
                    this.direction = 4;
                }
            }
            else if (init < end) {
                this.direction = 1;
            }
            else {
                this.direction = 2;
            }
            final String[] list = listTargetVertex.split("#");
            this.targetVertex = new ArrayList<>();
            for (int i = 0; i < list.length; ++i) {
                this.targetVertex.add(Integer.parseInt(list[i]));
            }
            final String[] listColor = listOfColors.split("#");
            this.color = new Color(Integer.parseInt(listColor[0]), Integer.parseInt(listColor[1]), Integer.parseInt(listColor[2]));
        }
        
        public int getDirection() {
            return this.direction;
        }
        
        public boolean isText() {
            return this.isText;
        }
        
        public ArrayList<Integer> getTargetVertex() {
            return this.targetVertex;
        }
        
        public int getNumIndex() {
            return this.numIndex;
        }
        
        public void setNumIndex(final int numIndex) {
            this.numIndex = numIndex;
        }
        
        public int getInit() {
            return this.init;
        }
        
        public void setInit(final int init) {
            this.init = init;
        }
        
        public int getEnd() {
            return this.end;
        }
        
        public void setEnd(final int end) {
            this.end = end;
        }
        
        public int getCurrent() {
            return this.current;
        }
        
        public void setCurrent(final int current) {
            this.current = current;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void setName(final String name) {
            this.name = name;
        }
        
        public int getOppositeAxis() {
            return this.oppositeAxis;
        }
        
        public void setOppositeAxis(final int oppositeAxis) {
            this.oppositeAxis = oppositeAxis;
        }
        
        public boolean isMovingXaxis() {
            return this.isMovingXaxis;
        }
        
        public void setMovingXaxis(final boolean isMovingXaxis) {
            this.isMovingXaxis = isMovingXaxis;
        }
        
        public Color getColor() {
            return this.color;
        }
        
        public void setColor(final Color color) {
            this.color = color;
        }
        
        public boolean isFirst_interaction() {
            return this.first_interaction;
        }
        
        public void setFirst_interaction(final boolean first_interaction) {
            this.first_interaction = first_interaction;
        }
        
        public boolean isActive() {
            return this.active;
        }
        
        public void setActive(final boolean active) {
            this.active = active;
        }
    }
    
    class DatapathAnimation extends JPanel implements ActionListener, MouseListener
    {
        private static final long serialVersionUID = -2681757800180958534L;
        private final int PERIOD;
        private static final int PWIDTH = 1000;
        private static final int PHEIGHT = 574;
        private final GraphicsConfiguration gc;
        private final GraphicsDevice gd;
        private final int accelMemory;
        private final DecimalFormat df;
        private int counter;
        private boolean justStarted;
        private int indexX;
        private int indexY;
        private boolean xIsMoving;
        private boolean yIsMoving;
        private Vector<Vector<Vertex>> outputGraph;
        private final ArrayList<Vertex> vertexList;
        private ArrayList<Vertex> vertexTraversed;
        private final HashMap<String, String> opcodeEquivalenceTable;
        private final HashMap<String, String> functionEquivalenceTable;
        private final HashMap<String, String> registerEquivalenceTable;
        private String instructionCode;
        private final int countRegLabel;
        private final int countALULabel;
        private final int countPCLabel;
        private final Color green1;
        private final Color green2;
        private final Color yellow2;
        private final Color orange1;
        private final Color orange;
        private final Color blue2;
        private final int register;
        private final int control;
        private final int aluControl;
        private final int alu;
        private int currentUnit;
        private Graphics2D g2d;
        private BufferedImage datapath;
        
        @Override
        public void mousePressed(final MouseEvent e) {
            final PointerInfo a = MouseInfo.getPointerInfo();
        }
        
        public DatapathAnimation(final String instructionBinary) {
            this.PERIOD = 5;
            this.green1 = new Color(0, 153, 0);
            this.green2 = new Color(0, 77, 0);
            this.yellow2 = new Color(185, 182, 42);
            this.orange1 = new Color(255, 102, 0);
            this.orange = new Color(119, 34, 34);
            this.blue2 = new Color(0, 153, 255);
            this.register = 1;
            this.control = 2;
            this.aluControl = 3;
            this.alu = 4;
            this.df = new DecimalFormat("0.0");
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            this.gd = ge.getDefaultScreenDevice();
            this.gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
            this.accelMemory = this.gd.getAvailableAcceleratedMemory();
            this.setBackground(Color.white);
            this.setPreferredSize(new Dimension(1000, 574));
            this.initImages();
            this.vertexList = new ArrayList<>();
            this.counter = 0;
            this.justStarted = true;
            this.instructionCode = instructionBinary;
            this.opcodeEquivalenceTable = new HashMap<>();
            this.functionEquivalenceTable = new HashMap<>();
            this.registerEquivalenceTable = new HashMap<>();
            this.countRegLabel = 400;
            this.countALULabel = 380;
            this.countPCLabel = 380;
            this.loadHashMapValues();
            this.addMouseListener(this);
        }
        
        public void loadHashMapValues() {
            this.importXmlStringData("/MipsXRayOpcode.xml", this.opcodeEquivalenceTable, "equivalence", "bits", "mnemonic");
            this.importXmlStringData("/MipsXRayOpcode.xml", this.functionEquivalenceTable, "function_equivalence", "bits", "mnemonic");
            this.importXmlStringData("/MipsXRayOpcode.xml", this.registerEquivalenceTable, "register_equivalence", "bits", "mnemonic");
            this.importXmlDatapathMap("/MipsXRayOpcode.xml", "datapath_map");
        }
        
        public void importXmlStringData(final String xmlName, final HashMap table, final String elementTree, final String tagId, final String tagData) {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            try {
                final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                final Document doc = docBuilder.parse(this.getClass().getResource(xmlName).toString());
                final Element root = doc.getDocumentElement();
                final NodeList equivalenceList = root.getElementsByTagName(elementTree);
                for (int i = 0; i < equivalenceList.getLength(); ++i) {
                    final Element equivalenceItem = (Element)equivalenceList.item(i);
                    final NodeList bitsList = equivalenceItem.getElementsByTagName(tagId);
                    final NodeList mnemonic = equivalenceItem.getElementsByTagName(tagData);
                    for (int j = 0; j < bitsList.getLength(); ++j) {
                        table.put(bitsList.item(j).getTextContent(), mnemonic.item(j).getTextContent());
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void importXmlDatapathMap(final String xmlName, final String elementTree) {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            try {
                final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                final Document doc = docBuilder.parse(this.getClass().getResource(xmlName).toString());
                final Element root = doc.getDocumentElement();
                final NodeList datapath_mapList = root.getElementsByTagName(elementTree);
                for (int i = 0; i < datapath_mapList.getLength(); ++i) {
                    final Element datapath_mapItem = (Element)datapath_mapList.item(i);
                    final NodeList index_vertex = datapath_mapItem.getElementsByTagName("num_vertex");
                    final NodeList name = datapath_mapItem.getElementsByTagName("name");
                    final NodeList init = datapath_mapItem.getElementsByTagName("init");
                    final NodeList end = datapath_mapItem.getElementsByTagName("end");
                    NodeList color;
                    if (this.instructionCode.substring(0, 6).equals("000000")) {
                        color = datapath_mapItem.getElementsByTagName("color_Rtype");
                    }
                    else if (this.instructionCode.substring(0, 6).matches("00001[0-1]")) {
                        color = datapath_mapItem.getElementsByTagName("color_Jtype");
                    }
                    else if (this.instructionCode.substring(0, 6).matches("100[0-1][0-1][0-1]")) {
                        color = datapath_mapItem.getElementsByTagName("color_LOADtype");
                    }
                    else if (this.instructionCode.substring(0, 6).matches("101[0-1][0-1][0-1]")) {
                        color = datapath_mapItem.getElementsByTagName("color_STOREtype");
                    }
                    else if (this.instructionCode.substring(0, 6).matches("0001[0-1][0-1]")) {
                        color = datapath_mapItem.getElementsByTagName("color_BRANCHtype");
                    }
                    else {
                        color = datapath_mapItem.getElementsByTagName("color_Itype");
                    }
                    final NodeList other_axis = datapath_mapItem.getElementsByTagName("other_axis");
                    final NodeList isMovingXaxis = datapath_mapItem.getElementsByTagName("isMovingXaxis");
                    final NodeList targetVertex = datapath_mapItem.getElementsByTagName("target_vertex");
                    final NodeList isText = datapath_mapItem.getElementsByTagName("is_text");
                    for (int j = 0; j < index_vertex.getLength(); ++j) {
                        final Vertex vert = new Vertex(Integer.parseInt(index_vertex.item(j).getTextContent()), Integer.parseInt(init.item(j).getTextContent()), Integer.parseInt(end.item(j).getTextContent()), name.item(j).getTextContent(), Integer.parseInt(other_axis.item(j).getTextContent()), Boolean.parseBoolean(isMovingXaxis.item(j).getTextContent()), color.item(j).getTextContent(), targetVertex.item(j).getTextContent(), Boolean.parseBoolean(isText.item(j).getTextContent()));
                        this.vertexList.add(vert);
                    }
                }
                this.outputGraph = new Vector<>();
                this.vertexTraversed = new ArrayList<>();
                final int size = this.vertexList.size();
                for (int k = 0; k < this.vertexList.size(); ++k) {
                    final Vertex vertex = this.vertexList.get(k);
                    final ArrayList<Integer> targetList = vertex.getTargetVertex();
                    final Vector<Vertex> vertexOfTargets = new Vector<>();
                    for (int l = 0; l < targetList.size(); ++l) {
                        vertexOfTargets.add(this.vertexList.get(targetList.get(l)));
                    }
                    this.outputGraph.add(vertexOfTargets);
                }
                for (int k = 0; k < this.outputGraph.size(); ++k) {
                    final Vector<Vertex> vector = this.outputGraph.get(k);
                }
                this.vertexList.get(0).setActive(true);
                this.vertexTraversed.add(this.vertexList.get(0));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void setUpInstructionInfo(final Graphics2D g2d) {
            final FontRenderContext frc = g2d.getFontRenderContext();
            final Font font = new Font("Digital-7", 0, 15);
            final Font fontTitle = new Font("Verdana", 0, 10);
            if (this.instructionCode.substring(0, 6).equals("000000")) {
                TextLayout textVariable = new TextLayout("REGISTER TYPE INSTRUCTION", new Font("Arial", 1, 25), frc);
                g2d.setColor(Color.black);
                textVariable.draw(g2d, 280.0f, 30.0f);
                textVariable = new TextLayout("opcode", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(0, 6), font, frc);
                g2d.setColor(Color.magenta);
                textVariable.draw(g2d, 25.0f, 550.0f);
                textVariable = new TextLayout("rs", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 90.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(6, 11), font, frc);
                g2d.setColor(Color.green);
                textVariable.draw(g2d, 90.0f, 550.0f);
                textVariable = new TextLayout("rt", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 150.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(11, 16), font, frc);
                g2d.setColor(Color.blue);
                textVariable.draw(g2d, 150.0f, 550.0f);
                textVariable = new TextLayout("rd", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 210.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(16, 21), font, frc);
                g2d.setColor(Color.cyan);
                textVariable.draw(g2d, 210.0f, 550.0f);
                textVariable = new TextLayout("shamt", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 270.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(21, 26), font, frc);
                g2d.setColor(Color.black);
                textVariable.draw(g2d, 270.0f, 550.0f);
                textVariable = new TextLayout("function", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 330.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(26, 32), font, frc);
                g2d.setColor(this.orange1);
                textVariable.draw(g2d, 330.0f, 550.0f);
                textVariable = new TextLayout("Instruction", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 480.0f);
                textVariable = new TextLayout(this.functionEquivalenceTable.get(this.instructionCode.substring(26, 32)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 25.0f, 500.0f);
                textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(6, 11)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 65.0f, 500.0f);
                textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(16, 21)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 105.0f, 500.0f);
                textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(11, 16)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 145.0f, 500.0f);
            }
            else if (this.instructionCode.substring(0, 6).matches("00001[0-1]")) {
                TextLayout textVariable = new TextLayout("JUMP TYPE INSTRUCTION", new Font("Verdana", 1, 25), frc);
                g2d.setColor(Color.black);
                textVariable.draw(g2d, 280.0f, 30.0f);
                textVariable = new TextLayout("opcode", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(0, 6), font, frc);
                g2d.setColor(Color.magenta);
                textVariable.draw(g2d, 25.0f, 550.0f);
                textVariable = new TextLayout("address", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 95.0f, 530.0f);
                textVariable = new TextLayout("Instruction", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 480.0f);
                textVariable = new TextLayout(this.instructionCode.substring(6, 32), font, frc);
                g2d.setColor(Color.orange);
                textVariable.draw(g2d, 95.0f, 550.0f);
                textVariable = new TextLayout(this.opcodeEquivalenceTable.get(this.instructionCode.substring(0, 6)), font, frc);
                g2d.setColor(Color.cyan);
                textVariable.draw(g2d, 65.0f, 500.0f);
                textVariable = new TextLayout("LABEL", font, frc);
                g2d.setColor(Color.cyan);
                textVariable.draw(g2d, 105.0f, 500.0f);
            }
            else if (this.instructionCode.substring(0, 6).matches("100[0-1][0-1][0-1]")) {
                TextLayout textVariable = new TextLayout("LOAD TYPE INSTRUCTION", new Font("Verdana", 1, 25), frc);
                g2d.setColor(Color.black);
                textVariable.draw(g2d, 280.0f, 30.0f);
                textVariable = new TextLayout("opcode", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(0, 6), font, frc);
                g2d.setColor(Color.magenta);
                textVariable.draw(g2d, 25.0f, 550.0f);
                textVariable = new TextLayout("rs", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 90.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(6, 11), font, frc);
                g2d.setColor(Color.green);
                textVariable.draw(g2d, 90.0f, 550.0f);
                textVariable = new TextLayout("rt", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 145.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(11, 16), font, frc);
                g2d.setColor(Color.blue);
                textVariable.draw(g2d, 145.0f, 550.0f);
                textVariable = new TextLayout("Immediate", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 200.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(16, 32), font, frc);
                g2d.setColor(this.orange1);
                textVariable.draw(g2d, 200.0f, 550.0f);
                textVariable = new TextLayout("Instruction", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 480.0f);
                textVariable = new TextLayout(this.opcodeEquivalenceTable.get(this.instructionCode.substring(0, 6)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 25.0f, 500.0f);
                textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(6, 11)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 65.0f, 500.0f);
                textVariable = new TextLayout("M[ " + this.registerEquivalenceTable.get(this.instructionCode.substring(16, 21)) + " + " + this.parseBinToInt(this.instructionCode.substring(6, 32)) + " ]", font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 105.0f, 500.0f);
            }
            else if (this.instructionCode.substring(0, 6).matches("101[0-1][0-1][0-1]")) {
                TextLayout textVariable = new TextLayout("STORE TYPE INSTRUCTION", new Font("Verdana", 1, 25), frc);
                g2d.setColor(Color.black);
                textVariable.draw(g2d, 280.0f, 30.0f);
                textVariable = new TextLayout("opcode", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(0, 6), font, frc);
                g2d.setColor(Color.magenta);
                textVariable.draw(g2d, 25.0f, 550.0f);
                textVariable = new TextLayout("rs", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 90.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(6, 11), font, frc);
                g2d.setColor(Color.green);
                textVariable.draw(g2d, 90.0f, 550.0f);
                textVariable = new TextLayout("rt", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 145.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(11, 16), font, frc);
                g2d.setColor(Color.blue);
                textVariable.draw(g2d, 145.0f, 550.0f);
                textVariable = new TextLayout("Immediate", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 200.0f, 530.0f);
                textVariable = new TextLayout(this.instructionCode.substring(16, 32), font, frc);
                g2d.setColor(this.orange1);
                textVariable.draw(g2d, 200.0f, 550.0f);
                textVariable = new TextLayout("Instruction", fontTitle, frc);
                g2d.setColor(Color.red);
                textVariable.draw(g2d, 25.0f, 480.0f);
                textVariable = new TextLayout(this.opcodeEquivalenceTable.get(this.instructionCode.substring(0, 6)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 25.0f, 500.0f);
                textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(6, 11)), font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 65.0f, 500.0f);
                textVariable = new TextLayout("M[ " + this.registerEquivalenceTable.get(this.instructionCode.substring(16, 21)) + " + " + this.parseBinToInt(this.instructionCode.substring(6, 32)) + " ]", font, frc);
                g2d.setColor(Color.BLACK);
                textVariable.draw(g2d, 105.0f, 500.0f);
            }
            else if (!this.instructionCode.substring(0, 6).matches("0100[0-1][0-1]")) {
                if (this.instructionCode.substring(0, 6).matches("0001[0-1][0-1]")) {
                    TextLayout textVariable = new TextLayout("BRANCH TYPE INSTRUCTION", new Font("Verdana", 1, 25), frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 250.0f, 30.0f);
                    textVariable = new TextLayout("opcode", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 25.0f, 440.0f);
                    textVariable = new TextLayout("opcode", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 25.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(0, 6), font, frc);
                    g2d.setColor(Color.magenta);
                    textVariable.draw(g2d, 25.0f, 550.0f);
                    textVariable = new TextLayout("rs", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 90.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(6, 11), font, frc);
                    g2d.setColor(Color.green);
                    textVariable.draw(g2d, 90.0f, 550.0f);
                    textVariable = new TextLayout("rt", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 145.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(11, 16), font, frc);
                    g2d.setColor(Color.blue);
                    textVariable.draw(g2d, 145.0f, 550.0f);
                    textVariable = new TextLayout("Immediate", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 200.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(16, 32), font, frc);
                    g2d.setColor(Color.cyan);
                    textVariable.draw(g2d, 200.0f, 550.0f);
                    textVariable = new TextLayout("Instruction", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 25.0f, 480.0f);
                    textVariable = new TextLayout(this.opcodeEquivalenceTable.get(this.instructionCode.substring(0, 6)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 25.0f, 500.0f);
                    textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(6, 11)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 105.0f, 500.0f);
                    textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(11, 16)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 65.0f, 500.0f);
                    textVariable = new TextLayout(this.parseBinToInt(this.instructionCode.substring(16, 32)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 155.0f, 500.0f);
                }
                else {
                    TextLayout textVariable = new TextLayout("IMMEDIATE TYPE INSTRUCTION", new Font("Verdana", 1, 25), frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 250.0f, 30.0f);
                    textVariable = new TextLayout("opcode", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 25.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(0, 6), font, frc);
                    g2d.setColor(Color.magenta);
                    textVariable.draw(g2d, 25.0f, 550.0f);
                    textVariable = new TextLayout("rs", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 90.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(6, 11), font, frc);
                    g2d.setColor(Color.green);
                    textVariable.draw(g2d, 90.0f, 550.0f);
                    textVariable = new TextLayout("rt", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 145.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(11, 16), font, frc);
                    g2d.setColor(Color.blue);
                    textVariable.draw(g2d, 145.0f, 550.0f);
                    textVariable = new TextLayout("Immediate", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 200.0f, 530.0f);
                    textVariable = new TextLayout(this.instructionCode.substring(16, 32), font, frc);
                    g2d.setColor(Color.cyan);
                    textVariable.draw(g2d, 200.0f, 550.0f);
                    textVariable = new TextLayout("Instruction", fontTitle, frc);
                    g2d.setColor(Color.red);
                    textVariable.draw(g2d, 25.0f, 480.0f);
                    textVariable = new TextLayout(this.opcodeEquivalenceTable.get(this.instructionCode.substring(0, 6)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 25.0f, 500.0f);
                    textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(6, 11)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 105.0f, 500.0f);
                    textVariable = new TextLayout(this.registerEquivalenceTable.get(this.instructionCode.substring(11, 16)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 65.0f, 500.0f);
                    textVariable = new TextLayout(this.parseBinToInt(this.instructionCode.substring(16, 32)), font, frc);
                    g2d.setColor(Color.black);
                    textVariable.draw(g2d, 155.0f, 500.0f);
                }
            }
            TextLayout textVariable = new TextLayout("Control Signals", fontTitle, frc);
            g2d.setColor(Color.red);
            textVariable.draw(g2d, 25.0f, 440.0f);
            textVariable = new TextLayout("Active", font, frc);
            g2d.setColor(Color.red);
            textVariable.draw(g2d, 25.0f, 455.0f);
            textVariable = new TextLayout("Inactive", font, frc);
            g2d.setColor(Color.gray);
            textVariable.draw(g2d, 75.0f, 455.0f);
            textVariable = new TextLayout("To see details of control units and register bank click inside the functional block", font, frc);
            g2d.setColor(Color.black);
            textVariable.draw(g2d, 400.0f, 550.0f);
        }
        
        public void startAnimation(final String codeInstruction) {
            this.instructionCode = codeInstruction;
            MipsXray.this.time = new Timer(this.PERIOD, this);
            MipsXray.this.time.start();
        }
        
        private void initImages() {
            try {
                final BufferedImage im = ImageIO.read(this.getClass().getResource("/images/datapath.png"));
                final int transparency = im.getColorModel().getTransparency();
                this.datapath = this.gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
                (this.g2d = this.datapath.createGraphics()).drawImage(im, 0, 0, null);
                this.g2d.dispose();
            }
            catch (IOException e) {
                System.out.println("Load Image error for " + this.getClass().getResource("/images/datapath.png") + ":\n" + e);
            }
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            if (this.justStarted) {
                this.justStarted = false;
            }
            if (this.xIsMoving) {
                ++this.indexX;
            }
            if (this.yIsMoving) {
                --this.indexY;
            }
            this.repaint();
        }
        
        public void paintComponent(final Graphics g) {
            super.paintComponent(g);
            (this.g2d = (Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            this.drawImage(this.g2d = (Graphics2D)g, this.datapath, 0, 0, null);
            this.executeAnimation(g);
            this.counter = (this.counter + 1) % 100;
            this.g2d.dispose();
        }
        
        private void drawImage(final Graphics2D g2d, final BufferedImage im, final int x, final int y, final Color c) {
            if (im == null) {
                g2d.setColor(c);
                g2d.fillOval(x, y, 20, 20);
                g2d.setColor(Color.black);
                g2d.drawString("   ", x, y);
            }
            else {
                g2d.drawImage(im, x, y, this);
            }
        }
        
        public void printTrackLtoR(final Vertex v) {
            final int size = v.getEnd() - v.getInit();
            final int[] track = new int[size];
            for (int i = 0; i < size; ++i) {
                track[i] = v.getInit() + i;
            }
            if (v.isActive()) {
                v.setFirst_interaction(false);
                for (int i = 0; i < size; ++i) {
                    if (track[i] <= v.getCurrent()) {
                        this.g2d.setColor(v.getColor());
                        this.g2d.fillRect(track[i], v.getOppositeAxis(), 3, 3);
                    }
                }
                if (v.getCurrent() == track[size - 1]) {
                    v.setActive(false);
                }
                v.setCurrent(v.getCurrent() + 1);
            }
            else if (!v.isFirst_interaction()) {
                for (int i = 0; i < size; ++i) {
                    this.g2d.setColor(v.getColor());
                    this.g2d.fillRect(track[i], v.getOppositeAxis(), 3, 3);
                }
            }
        }
        
        public void printTrackRtoL(final Vertex v) {
            final int size = v.getInit() - v.getEnd();
            final int[] track = new int[size];
            for (int i = 0; i < size; ++i) {
                track[i] = v.getInit() - i;
            }
            if (v.isActive()) {
                v.setFirst_interaction(false);
                for (int i = 0; i < size; ++i) {
                    if (track[i] >= v.getCurrent()) {
                        this.g2d.setColor(v.getColor());
                        this.g2d.fillRect(track[i], v.getOppositeAxis(), 3, 3);
                    }
                }
                if (v.getCurrent() == track[size - 1]) {
                    v.setActive(false);
                }
                v.setCurrent(v.getCurrent() - 1);
            }
            else if (!v.isFirst_interaction()) {
                for (int i = 0; i < size; ++i) {
                    this.g2d.setColor(v.getColor());
                    this.g2d.fillRect(track[i], v.getOppositeAxis(), 3, 3);
                }
            }
        }
        
        public void printTrackDtoU(final Vertex v) {
            int size;
            int[] track;
            if (v.getInit() > v.getEnd()) {
                size = v.getInit() - v.getEnd();
                track = new int[size];
                for (int i = 0; i < size; ++i) {
                    track[i] = v.getInit() - i;
                }
            }
            else {
                size = v.getEnd() - v.getInit();
                track = new int[size];
                for (int i = 0; i < size; ++i) {
                    track[i] = v.getInit() + i;
                }
            }
            if (v.isActive()) {
                v.setFirst_interaction(false);
                for (int i = 0; i < size; ++i) {
                    if (track[i] >= v.getCurrent()) {
                        this.g2d.setColor(v.getColor());
                        this.g2d.fillRect(v.getOppositeAxis(), track[i], 3, 3);
                    }
                }
                if (v.getCurrent() == track[size - 1]) {
                    v.setActive(false);
                }
                v.setCurrent(v.getCurrent() - 1);
            }
            else if (!v.isFirst_interaction()) {
                for (int i = 0; i < size; ++i) {
                    this.g2d.setColor(v.getColor());
                    this.g2d.fillRect(v.getOppositeAxis(), track[i], 3, 3);
                }
            }
        }
        
        public void printTrackUtoD(final Vertex v) {
            final int size = v.getEnd() - v.getInit();
            final int[] track = new int[size];
            for (int i = 0; i < size; ++i) {
                track[i] = v.getInit() + i;
            }
            if (v.isActive()) {
                v.setFirst_interaction(false);
                for (int i = 0; i < size; ++i) {
                    if (track[i] <= v.getCurrent()) {
                        this.g2d.setColor(v.getColor());
                        this.g2d.fillRect(v.getOppositeAxis(), track[i], 3, 3);
                    }
                }
                if (v.getCurrent() == track[size - 1]) {
                    v.setActive(false);
                }
                v.setCurrent(v.getCurrent() + 1);
            }
            else if (!v.isFirst_interaction()) {
                for (int i = 0; i < size; ++i) {
                    this.g2d.setColor(v.getColor());
                    this.g2d.fillRect(v.getOppositeAxis(), track[i], 3, 3);
                }
            }
        }
        
        public void printTextDtoU(final Vertex v) {
            final FontRenderContext frc = this.g2d.getFontRenderContext();
            TextLayout actionInFunctionalBlock = new TextLayout(v.getName(), new Font("Verdana", 1, 13), frc);
            this.g2d.setColor(Color.RED);
            if (this.instructionCode.substring(0, 6).matches("101[0-1][0-1][0-1]") && !this.instructionCode.substring(0, 6).matches("0001[0-1][0-1]") && !this.instructionCode.substring(0, 6).matches("00001[0-1]")) {
                actionInFunctionalBlock = new TextLayout(" ", new Font("Verdana", 1, 13), frc);
            }
            if (v.getName().equals("ALUVALUE")) {
                if (this.instructionCode.substring(0, 6).equals("000000")) {
                    actionInFunctionalBlock = new TextLayout(this.functionEquivalenceTable.get(this.instructionCode.substring(26, 32)), new Font("Verdana", 1, 13), frc);
                }
                else {
                    actionInFunctionalBlock = new TextLayout(this.opcodeEquivalenceTable.get(this.instructionCode.substring(0, 6)), new Font("Verdana", 1, 13), frc);
                }
            }
            if (this.instructionCode.substring(0, 6).matches("0001[0-1][0-1]") && v.getName().equals("CP+4")) {
                actionInFunctionalBlock = new TextLayout("PC+OFFSET", new Font("Verdana", 1, 13), frc);
            }
            if (v.getName().equals("WRITING") && !this.instructionCode.substring(0, 6).matches("100[0-1][0-1][0-1]")) {
                actionInFunctionalBlock = new TextLayout(" ", new Font("Verdana", 1, 13), frc);
            }
            if (v.isActive()) {
                v.setFirst_interaction(false);
                actionInFunctionalBlock.draw(this.g2d, v.getOppositeAxis(), v.getCurrent());
                if (v.getCurrent() == v.getEnd()) {
                    v.setActive(false);
                }
                v.setCurrent(v.getCurrent() - 1);
            }
        }
        
        public String parseBinToInt(final String code) {
            int value = 0;
            for (int i = code.length() - 1; i >= 0; --i) {
                if ("1".equals(code.substring(i, i + 1))) {
                    value += (int)Math.pow(2.0, code.length() - i - 1);
                }
            }
            return Integer.toString(value);
        }
        
        private void executeAnimation(final Graphics g) {
            this.setUpInstructionInfo(this.g2d = (Graphics2D)g);
            for (int i = 0; i < this.vertexTraversed.size(); ++i) {
                final Vertex vert = this.vertexTraversed.get(i);
                if (vert.isMovingXaxis) {
                    if (vert.getDirection() == 3) {
                        this.printTrackLtoR(vert);
                        if (!vert.isActive()) {
                            for (int j = vert.getTargetVertex().size(), k = 0; k < j; ++k) {
                                final Vertex tempVertex = this.outputGraph.get(vert.getNumIndex()).get(k);
                                Boolean hasThisVertex = false;
                                for (int m = 0; m < this.vertexTraversed.size(); ++m) {
                                    if (tempVertex.getNumIndex() == this.vertexTraversed.get(m).getNumIndex()) {
                                        hasThisVertex = true;
                                    }
                                }
                                if (!hasThisVertex) {
                                    this.outputGraph.get(vert.getNumIndex()).get(k).setActive(true);
                                    this.vertexTraversed.add(this.outputGraph.get(vert.getNumIndex()).get(k));
                                }
                            }
                        }
                    }
                    else {
                        this.printTrackRtoL(vert);
                        if (!vert.isActive()) {
                            for (int j = vert.getTargetVertex().size(), k = 0; k < j; ++k) {
                                final Vertex tempVertex = this.outputGraph.get(vert.getNumIndex()).get(k);
                                Boolean hasThisVertex = false;
                                for (int m = 0; m < this.vertexTraversed.size(); ++m) {
                                    if (tempVertex.getNumIndex() == this.vertexTraversed.get(m).getNumIndex()) {
                                        hasThisVertex = true;
                                    }
                                }
                                if (!hasThisVertex) {
                                    this.outputGraph.get(vert.getNumIndex()).get(k).setActive(true);
                                    this.vertexTraversed.add(this.outputGraph.get(vert.getNumIndex()).get(k));
                                }
                            }
                        }
                    }
                }
                else if (vert.getDirection() == 2) {
                    if (vert.isText) {
                        this.printTextDtoU(vert);
                    }
                    else {
                        this.printTrackDtoU(vert);
                    }
                    if (!vert.isActive()) {
                        for (int j = vert.getTargetVertex().size(), k = 0; k < j; ++k) {
                            final Vertex tempVertex = this.outputGraph.get(vert.getNumIndex()).get(k);
                            Boolean hasThisVertex = false;
                            for (int m = 0; m < this.vertexTraversed.size(); ++m) {
                                if (tempVertex.getNumIndex() == this.vertexTraversed.get(m).getNumIndex()) {
                                    hasThisVertex = true;
                                }
                            }
                            if (!hasThisVertex) {
                                this.outputGraph.get(vert.getNumIndex()).get(k).setActive(true);
                                this.vertexTraversed.add(this.outputGraph.get(vert.getNumIndex()).get(k));
                            }
                        }
                    }
                }
                else {
                    this.printTrackUtoD(vert);
                    if (!vert.isActive()) {
                        for (int j = vert.getTargetVertex().size(), k = 0; k < j; ++k) {
                            final Vertex tempVertex = this.outputGraph.get(vert.getNumIndex()).get(k);
                            Boolean hasThisVertex = false;
                            for (int m = 0; m < this.vertexTraversed.size(); ++m) {
                                if (tempVertex.getNumIndex() == this.vertexTraversed.get(m).getNumIndex()) {
                                    hasThisVertex = true;
                                }
                            }
                            if (!hasThisVertex) {
                                this.outputGraph.get(vert.getNumIndex()).get(k).setActive(true);
                                this.vertexTraversed.add(this.outputGraph.get(vert.getNumIndex()).get(k));
                            }
                        }
                    }
                }
            }
        }
        
        @Override
        public void mouseClicked(final MouseEvent e) {
            final PointerInfo a = MouseInfo.getPointerInfo();
            if (e.getPoint().getX() > 425.0 && e.getPoint().getX() < 520.0 && e.getPoint().getY() > 300.0 && e.getPoint().getY() < 425.0) {
                MipsXray.this.buildMainDisplayArea("register.png");
                final FunctionUnitVisualization fu = new FunctionUnitVisualization(MipsXray.this.instructionBinary, this.register);
                fu.run();
            }
            if (e.getPoint().getX() > 355.0 && e.getPoint().getX() < 415.0 && e.getPoint().getY() > 180.0 && e.getPoint().getY() < 280.0) {
                MipsXray.this.buildMainDisplayArea("control.png");
                final FunctionUnitVisualization fu = new FunctionUnitVisualization(MipsXray.this.instructionBinary, this.control);
                fu.run();
            }
            if (e.getPoint().getX() > 560.0 && e.getPoint().getX() < 620.0 && e.getPoint().getY() > 450.0 && e.getPoint().getY() < 520.0) {
                MipsXray.this.buildMainDisplayArea("ALUcontrol.png");
                final FunctionUnitVisualization fu = new FunctionUnitVisualization(MipsXray.this.instructionBinary, this.aluControl);
                fu.run();
            }
        }
        
        @Override
        public void mouseEntered(final MouseEvent e) {
        }
        
        @Override
        public void mouseExited(final MouseEvent e) {
        }
        
        @Override
        public void mouseReleased(final MouseEvent e) {
        }
    }
}
