

package mars.tools;

import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.awt.image.ImageObserver;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import java.text.DecimalFormat;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

class UnitAnimation extends JPanel implements ActionListener
{
    private static final long serialVersionUID = -2681757800180958534L;
    private int PERIOD;
    private static final int PWIDTH = 1000;
    private static final int PHEIGHT = 574;
    private GraphicsConfiguration gc;
    private GraphicsDevice gd;
    private int accelMemory;
    private DecimalFormat df;
    private int counter;
    private boolean justStarted;
    private int indexX;
    private int indexY;
    private boolean xIsMoving;
    private boolean yIsMoving;
    private Vector<Vector<Vertex>> outputGraph;
    private ArrayList<Vertex> vertexList;
    private ArrayList<Vertex> vertexTraversed;
    private HashMap<String, String> registerEquivalenceTable;
    private String instructionCode;
    private int countRegLabel;
    private int countALULabel;
    private int countPCLabel;
    private int register;
    private int control;
    private int aluControl;
    private int alu;
    private int datapatTypeUsed;
    private Boolean cursorInIM;
    private Boolean cursorInALU;
    private Boolean cursorInDataMem;
    private Boolean cursorInReg;
    private Graphics2D g2d;
    private BufferedImage datapath;
    
    public UnitAnimation(final String instructionBinary, final int datapathType) {
        this.PERIOD = 8;
        this.register = 1;
        this.control = 2;
        this.aluControl = 3;
        this.alu = 4;
        this.datapatTypeUsed = datapathType;
        this.cursorInIM = false;
        this.cursorInALU = false;
        this.cursorInDataMem = false;
        this.df = new DecimalFormat("0.0");
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.gd = ge.getDefaultScreenDevice();
        this.gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        this.accelMemory = this.gd.getAvailableAcceleratedMemory();
        this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(1000, 574));
        this.initImages();
        this.vertexList = new ArrayList<Vertex>();
        this.counter = 0;
        this.justStarted = true;
        this.instructionCode = instructionBinary;
        this.registerEquivalenceTable = new HashMap<String, String>();
        this.countRegLabel = 400;
        this.countALULabel = 380;
        this.countPCLabel = 380;
        this.loadHashMapValues();
    }
    
    public void loadHashMapValues() {
        if (this.datapatTypeUsed == this.register) {
            this.importXmlStringData("/registerDatapath.xml", this.registerEquivalenceTable, "register_equivalence", "bits", "mnemonic");
            this.importXmlDatapathMap("/registerDatapath.xml", "datapath_map");
        }
        else if (this.datapatTypeUsed == this.control) {
            this.importXmlStringData("/controlDatapath.xml", this.registerEquivalenceTable, "register_equivalence", "bits", "mnemonic");
            this.importXmlDatapathMap("/controlDatapath.xml", "datapath_map");
        }
        else if (this.datapatTypeUsed == this.aluControl) {
            this.importXmlStringData("/ALUcontrolDatapath.xml", this.registerEquivalenceTable, "register_equivalence", "bits", "mnemonic");
            this.importXmlDatapathMapAluControl("/ALUcontrolDatapath.xml", "datapath_map");
        }
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
            this.outputGraph = new Vector<Vector<Vertex>>();
            this.vertexTraversed = new ArrayList<Vertex>();
            final int size = this.vertexList.size();
            for (int k = 0; k < this.vertexList.size(); ++k) {
                final Vertex vertex = this.vertexList.get(k);
                final ArrayList<Integer> targetList = vertex.getTargetVertex();
                final Vector<Vertex> vertexOfTargets = new Vector<Vertex>();
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
    
    public void importXmlDatapathMapAluControl(final String xmlName, final String elementTree) {
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
                    if (this.instructionCode.substring(28, 32).matches("0000")) {
                        color = datapath_mapItem.getElementsByTagName("ALU_out010");
                        System.out.println("ALU_out010 type " + this.instructionCode.substring(28, 32));
                    }
                    else if (this.instructionCode.substring(28, 32).matches("0010")) {
                        color = datapath_mapItem.getElementsByTagName("ALU_out110");
                        System.out.println("ALU_out110 type " + this.instructionCode.substring(28, 32));
                    }
                    else if (this.instructionCode.substring(28, 32).matches("0100")) {
                        color = datapath_mapItem.getElementsByTagName("ALU_out000");
                        System.out.println("ALU_out000 type " + this.instructionCode.substring(28, 32));
                    }
                    else if (this.instructionCode.substring(28, 32).matches("0101")) {
                        color = datapath_mapItem.getElementsByTagName("ALU_out001");
                        System.out.println("ALU_out001 type " + this.instructionCode.substring(28, 32));
                    }
                    else {
                        color = datapath_mapItem.getElementsByTagName("ALU_out111");
                        System.out.println("ALU_out111 type " + this.instructionCode.substring(28, 32));
                    }
                }
                else if (this.instructionCode.substring(0, 6).matches("00001[0-1]")) {
                    color = datapath_mapItem.getElementsByTagName("color_Jtype");
                    System.out.println("jtype");
                }
                else if (this.instructionCode.substring(0, 6).matches("100[0-1][0-1][0-1]")) {
                    color = datapath_mapItem.getElementsByTagName("color_LOADtype");
                    System.out.println("load type");
                }
                else if (this.instructionCode.substring(0, 6).matches("101[0-1][0-1][0-1]")) {
                    color = datapath_mapItem.getElementsByTagName("color_STOREtype");
                    System.out.println("store type");
                }
                else if (this.instructionCode.substring(0, 6).matches("0001[0-1][0-1]")) {
                    color = datapath_mapItem.getElementsByTagName("color_BRANCHtype");
                    System.out.println("branch type");
                }
                else {
                    color = datapath_mapItem.getElementsByTagName("color_Itype");
                    System.out.println("immediate type");
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
            this.outputGraph = new Vector<Vector<Vertex>>();
            this.vertexTraversed = new ArrayList<Vertex>();
            final int size = this.vertexList.size();
            for (int k = 0; k < this.vertexList.size(); ++k) {
                final Vertex vertex = this.vertexList.get(k);
                final ArrayList<Integer> targetList = vertex.getTargetVertex();
                final Vector<Vertex> vertexOfTargets = new Vector<Vertex>();
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
    
    public void startAnimation(final String codeInstruction) {
        this.instructionCode = codeInstruction;
        new Timer(this.PERIOD, this).start();
        this.repaint();
    }
    
    private void initImages() {
        try {
            BufferedImage im;
            if (this.datapatTypeUsed == this.register) {
                im = ImageIO.read(this.getClass().getResource("/images/register.png"));
            }
            else if (this.datapatTypeUsed == this.control) {
                im = ImageIO.read(this.getClass().getResource("/images/control.png"));
            }
            else if (this.datapatTypeUsed == this.aluControl) {
                im = ImageIO.read(this.getClass().getResource("/images/ALUcontrol.png"));
            }
            else {
                im = ImageIO.read(this.getClass().getResource("/images/alu.png"));
            }
            final int transparency = im.getColorModel().getTransparency();
            this.datapath = this.gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
            (this.g2d = this.datapath.createGraphics()).drawImage(im, 0, 0, null);
            this.g2d.dispose();
        }
        catch (IOException e) {
            System.out.println("Load Image error for " + this.getClass().getResource("/images/register.png") + ":\n" + e);
        }
    }
    
    public void updateDisplay() {
        this.repaint();
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
        this.g2d = (Graphics2D)g;
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
                if (!vert.isText) {
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
        private boolean isText;
        private ArrayList<Integer> targetVertex;
        
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
            this.targetVertex = new ArrayList<Integer>();
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
}
