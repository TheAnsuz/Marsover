

package mars.venus;

import java.awt.FlowLayout;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.JTextField;
import java.util.ArrayList;
import javax.swing.ListCellRenderer;
import mars.mips.instructions.Instruction;
import mars.Globals;
import java.util.Iterator;
import java.awt.Font;
import javax.swing.JList;
import java.util.List;
import java.util.Collections;
import mars.assembler.Directives;
import java.util.Vector;
import javax.swing.JSplitPane;
import java.io.InputStream;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkListener;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import java.awt.LayoutManager;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Frame;
import javax.swing.JDialog;
import java.awt.Component;
import javax.swing.JTabbedPane;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import java.awt.Color;

public class HelpHelpAction extends GuiAction
{
    static Color altBackgroundColor;
    public static final String descriptionDetailSeparator = ":";
    
    public HelpHelpAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    private Dimension getSize() {
        return new Dimension(800, 600);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("MIPS", this.createMipsHelpInfoPanel());
        tabbedPane.addTab("MARS", this.createMarsHelpInfoPanel());
        tabbedPane.addTab("License", this.createCopyrightInfoPanel());
        tabbedPane.addTab("Bugs/Comments", this.createHTMLHelpPanel("BugReportingHelp.html"));
        tabbedPane.addTab("Acknowledgements", this.createHTMLHelpPanel("Acknowledgements.html"));
        tabbedPane.addTab("Instruction Set Song", this.createHTMLHelpPanel("MIPSInstructionSetSong.html"));
        final JDialog dialog = new JDialog(this.mainUI, "MARS 4.5 Help");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        final JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        final JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, 2));
        closePanel.add(Box.createHorizontalGlue());
        closePanel.add(closeButton);
        closePanel.add(Box.createHorizontalGlue());
        closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, 3));
        contentPane.add(tabbedPane);
        contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPane.add(closePanel);
        contentPane.setOpaque(true);
        dialog.setContentPane(contentPane);
        dialog.setSize(this.getSize());
        dialog.setLocationRelativeTo(this.mainUI);
        dialog.setVisible(true);
    }
    
    private JPanel createHTMLHelpPanel(final String filename) {
        final JPanel helpPanel = new JPanel(new BorderLayout());
        JScrollPane helpScrollPane;
        try {
            final InputStream is = this.getClass().getResourceAsStream("/help/" + filename);
            final BufferedReader in = new BufferedReader(new InputStreamReader(is));
            final StringBuffer text = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                text.append(line + "\n");
            }
            in.close();
            final JEditorPane helpDisplay = new JEditorPane("text/html", text.toString());
            helpDisplay.setEditable(false);
            helpDisplay.setCaretPosition(0);
            helpScrollPane = new JScrollPane(helpDisplay, 22, 30);
            helpDisplay.addHyperlinkListener(new HelpHyperlinkListener());
        }
        catch (Exception ie) {
            helpScrollPane = new JScrollPane(new JLabel("Error (" + ie + "): " + filename + " contents could not be loaded."));
        }
        helpPanel.add(helpScrollPane);
        return helpPanel;
    }
    
    private JPanel createCopyrightInfoPanel() {
        final JPanel marsCopyrightInfo = new JPanel(new BorderLayout());
        JScrollPane marsCopyrightScrollPane;
        try {
            final InputStream is = this.getClass().getResourceAsStream("/MARSlicense.txt");
            final BufferedReader in = new BufferedReader(new InputStreamReader(is));
            final StringBuffer text = new StringBuffer("<pre>");
            String line;
            while ((line = in.readLine()) != null) {
                text.append(line + "\n");
            }
            in.close();
            text.append("</pre>");
            final JEditorPane marsCopyrightDisplay = new JEditorPane("text/html", text.toString());
            marsCopyrightDisplay.setEditable(false);
            marsCopyrightDisplay.setCaretPosition(0);
            marsCopyrightScrollPane = new JScrollPane(marsCopyrightDisplay, 22, 30);
        }
        catch (Exception ioe) {
            marsCopyrightScrollPane = new JScrollPane(new JLabel("Error: license contents could not be loaded."));
        }
        marsCopyrightInfo.add(marsCopyrightScrollPane);
        return marsCopyrightInfo;
    }
    
    private JPanel createMarsHelpInfoPanel() {
        final JPanel marsHelpInfo = new JPanel(new BorderLayout());
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Intro", this.createHTMLHelpPanel("MarsHelpIntro.html"));
        tabbedPane.addTab("IDE", this.createHTMLHelpPanel("MarsHelpIDE.html"));
        tabbedPane.addTab("Debugging", this.createHTMLHelpPanel("MarsHelpDebugging.html"));
        tabbedPane.addTab("Settings", this.createHTMLHelpPanel("MarsHelpSettings.html"));
        tabbedPane.addTab("Tools", this.createHTMLHelpPanel("MarsHelpTools.html"));
        tabbedPane.addTab("Command", this.createHTMLHelpPanel("MarsHelpCommand.html"));
        tabbedPane.addTab("Limits", this.createHTMLHelpPanel("MarsHelpLimits.html"));
        tabbedPane.addTab("History", this.createHTMLHelpPanel("MarsHelpHistory.html"));
        marsHelpInfo.add(tabbedPane);
        return marsHelpInfo;
    }
    
    private JPanel createMipsHelpInfoPanel() {
        final JPanel mipsHelpInfo = new JPanel(new BorderLayout());
        final String helpRemarksColor = "CCFF99";
        final String helpRemarks = "<html><center><table bgcolor=\"#" + helpRemarksColor + "\" border=0 cellpadding=0><tr><th colspan=2><b><i><font size=+1>&nbsp;&nbsp;Operand Key for Example Instructions&nbsp;&nbsp;</font></i></b></th></tr><tr><td><tt>label, target</tt></td><td>any textual label</td></tr><tr><td><tt>$t1, $t2, $t3</tt></td><td>any integer register</td></tr><tr><td><tt>$f2, $f4, $f6</tt></td><td><i>even-numbered</i> floating point register</td></tr><tr><td><tt>$f0, $f1, $f3</tt></td><td><i>any</i> floating point register</td></tr><tr><td><tt>$8</tt></td><td>any Coprocessor 0 register</td></tr><tr><td><tt>1</tt></td><td>condition flag (0 to 7)</td></tr><tr><td><tt>10</tt></td><td>unsigned 5-bit integer (0 to 31)</td></tr><tr><td><tt>-100</tt></td><td>signed 16-bit integer (-32768 to 32767)</td></tr><tr><td><tt>100</tt></td><td>unsigned 16-bit integer (0 to 65535)</td></tr><tr><td><tt>100000</tt></td><td>signed 32-bit integer (-2147483648 to 2147483647)</td></tr><tr></tr><tr><td colspan=2><b><i><font size=+1>Load & Store addressing mode, basic instructions</font></i></b></td></tr><tr><td><tt>-100($t2)</tt></td><td>sign-extended 16-bit integer added to contents of $t2</td></tr><tr></tr><tr><td colspan=2><b><i><font size=+1>Load & Store addressing modes, pseudo instructions</font></i></b></td></tr><tr><td><tt>($t2)</tt></td><td>contents of $t2</td></tr><tr><td><tt>-100</tt></td><td>signed 16-bit integer</td></tr><tr><td><tt>100</tt></td><td>unsigned 16-bit integer</td></tr><tr><td><tt>100000</tt></td><td>signed 32-bit integer</td></tr><tr><td><tt>100($t2)</tt></td><td>zero-extended unsigned 16-bit integer added to contents of $t2</td></tr><tr><td><tt>100000($t2)</tt></td><td>signed 32-bit integer added to contents of $t2</td></tr><tr><td><tt>label</tt></td><td>32-bit address of label</td></tr><tr><td><tt>label($t2)</tt></td><td>32-bit address of label added to contents of $t2</td></tr><tr><td><tt>label+100000</tt></td><td>32-bit integer added to label's address</td></tr><tr><td><tt>label+100000($t2)&nbsp;&nbsp;&nbsp;</tt></td><td>sum of 32-bit integer, label's address, and contents of $t2</td></tr></table></center></html>";
        final JLabel helpRemarksLabel = new JLabel(helpRemarks, 0);
        helpRemarksLabel.setOpaque(true);
        helpRemarksLabel.setBackground(Color.decode("0x" + helpRemarksColor));
        final JScrollPane operandsScrollPane = new JScrollPane(helpRemarksLabel, 22, 32);
        mipsHelpInfo.add(operandsScrollPane, "North");
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Basic Instructions", this.createMipsInstructionHelpPane("mars.mips.instructions.BasicInstruction"));
        tabbedPane.addTab("Extended (pseudo) Instructions", this.createMipsInstructionHelpPane("mars.mips.instructions.ExtendedInstruction"));
        tabbedPane.addTab("Directives", this.createMipsDirectivesHelpPane());
        tabbedPane.addTab("Syscalls", this.createHTMLHelpPanel("SyscallHelp.html"));
        tabbedPane.addTab("Exceptions", this.createHTMLHelpPanel("ExceptionsHelp.html"));
        tabbedPane.addTab("Macros", this.createHTMLHelpPanel("MacrosHelp.html"));
        operandsScrollPane.setPreferredSize(new Dimension((int)this.getSize().getWidth(), (int)(this.getSize().getHeight() * 0.2)));
        operandsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        tabbedPane.setPreferredSize(new Dimension((int)this.getSize().getWidth(), (int)(this.getSize().getHeight() * 0.6)));
        final JSplitPane splitsville = new JSplitPane(0, operandsScrollPane, tabbedPane);
        splitsville.setOneTouchExpandable(true);
        splitsville.resetToPreferredSizes();
        mipsHelpInfo.add(splitsville);
        return mipsHelpInfo;
    }
    
    private JScrollPane createMipsDirectivesHelpPane() {
        final Vector exampleList = new Vector();
        final String blanks = "            ";
        for (final Directives direct : Directives.getDirectiveList()) {
            exampleList.add(direct.toString() + blanks.substring(0, Math.max(0, blanks.length() - direct.toString().length())) + direct.getDescription());
        }
        Collections.sort((List<Comparable>)exampleList);
        final JList examples = new JList(exampleList);
        final JScrollPane mipsScrollPane = new JScrollPane(examples, 22, 30);
        examples.setFont(new Font("Monospaced", 0, 12));
        return mipsScrollPane;
    }
    
    private JScrollPane createMipsInstructionHelpPane(final String instructionClassName) {
        final ArrayList instructionList = Globals.instructionSet.getInstructionList();
        final Vector exampleList = new Vector(instructionList.size());
        final Iterator<Instruction> it = instructionList.iterator();
        final String blanks = "                        ";
        while (it.hasNext()) {
            final Instruction instr = it.next();
            try {
                if (!Class.forName(instructionClassName).isInstance(instr)) {
                    continue;
                }
                exampleList.add(instr.getExampleFormat() + blanks.substring(0, Math.max(0, blanks.length() - instr.getExampleFormat().length())) + instr.getDescription());
            }
            catch (ClassNotFoundException cnfe) {
                System.out.println(cnfe + " " + instructionClassName);
            }
        }
        Collections.sort((List<Comparable>)exampleList);
        final JList examples = new JList(exampleList);
        final JScrollPane mipsScrollPane = new JScrollPane(examples, 22, 30);
        examples.setFont(new Font("Monospaced", 0, 12));
        examples.setCellRenderer(new MyCellRenderer());
        return mipsScrollPane;
    }
    
    static {
        HelpHelpAction.altBackgroundColor = new Color(238, 238, 238);
    }
    
    private class MyCellRenderer extends JLabel implements ListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            final String s = value.toString();
            this.setText(s);
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            }
            else {
                this.setBackground((index % 2 == 0) ? HelpHelpAction.altBackgroundColor : list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);
            return this;
        }
    }
    
    private class HelpHyperlinkListener implements HyperlinkListener
    {
        JDialog webpageDisplay;
        JTextField webpageURL;
        private static final String cannotDisplayMessage = "<html><title></title><body><strong>Unable to display requested document.</strong></body></html>";
        
        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                final JEditorPane pane = (JEditorPane)e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
                    final HTMLDocument doc = (HTMLDocument)pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                }
                else {
                    (this.webpageDisplay = new JDialog(HelpHelpAction.this.mainUI, "Primitive HTML Viewer")).setLayout(new BorderLayout());
                    this.webpageDisplay.setLocation(HelpHelpAction.this.mainUI.getSize().width / 6, HelpHelpAction.this.mainUI.getSize().height / 6);
                    JEditorPane webpagePane;
                    try {
                        webpagePane = new JEditorPane(e.getURL());
                    }
                    catch (Throwable t) {
                        webpagePane = new JEditorPane("text/html", "<html><title></title><body><strong>Unable to display requested document.</strong></body></html>");
                    }
                    webpagePane.addHyperlinkListener(new HyperlinkListener() {
                        @Override
                        public void hyperlinkUpdate(final HyperlinkEvent e) {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                final JEditorPane pane = (JEditorPane)e.getSource();
                                if (e instanceof HTMLFrameHyperlinkEvent) {
                                    final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
                                    final HTMLDocument doc = (HTMLDocument)pane.getDocument();
                                    doc.processHTMLFrameHyperlinkEvent(evt);
                                }
                                else {
                                    try {
                                        pane.setPage(e.getURL());
                                    }
                                    catch (Throwable t) {
                                        pane.setText("<html><title></title><body><strong>Unable to display requested document.</strong></body></html>");
                                    }
                                    HelpHyperlinkListener.this.webpageURL.setText(e.getURL().toString());
                                }
                            }
                        }
                    });
                    webpagePane.setPreferredSize(new Dimension(HelpHelpAction.this.mainUI.getSize().width * 2 / 3, HelpHelpAction.this.mainUI.getSize().height * 2 / 3));
                    webpagePane.setEditable(false);
                    webpagePane.setCaretPosition(0);
                    final JScrollPane webpageScrollPane = new JScrollPane(webpagePane, 20, 30);
                    (this.webpageURL = new JTextField(e.getURL().toString(), 50)).setEditable(false);
                    this.webpageURL.setBackground(Color.WHITE);
                    final JPanel URLPanel = new JPanel(new FlowLayout(0, 4, 4));
                    URLPanel.add(new JLabel("URL: "));
                    URLPanel.add(this.webpageURL);
                    this.webpageDisplay.add(URLPanel, "North");
                    this.webpageDisplay.add(webpageScrollPane);
                    final JButton closeButton = new JButton("Close");
                    closeButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            HelpHyperlinkListener.this.webpageDisplay.setVisible(false);
                            HelpHyperlinkListener.this.webpageDisplay.dispose();
                        }
                    });
                    final JPanel closePanel = new JPanel();
                    closePanel.setLayout(new BoxLayout(closePanel, 2));
                    closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
                    closePanel.add(Box.createHorizontalGlue());
                    closePanel.add(closeButton);
                    closePanel.add(Box.createHorizontalGlue());
                    this.webpageDisplay.add(closePanel, "South");
                    this.webpageDisplay.pack();
                    this.webpageDisplay.setVisible(true);
                }
            }
        }
    }
}
