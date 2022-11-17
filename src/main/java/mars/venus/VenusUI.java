

package mars.venus;

import java.awt.event.MouseListener;
import javax.swing.JPopupMenu;
import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import mars.mips.dump.DumpFormatLoader;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import mars.Globals;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JFrame;

public class VenusUI extends JFrame
{
    VenusUI mainUI;
    public JMenuBar menu;
    JToolBar toolbar;
    MainPane mainPane;
    RegistersPane registersPane;
    RegistersWindow registersTab;
    Coprocessor1Window coprocessor1Tab;
    Coprocessor0Window coprocessor0Tab;
    MessagesPane messagesPane;
    JSplitPane splitter;
    JSplitPane horizonSplitter;
    JPanel north;
    private int frameState;
    private static int menuState;
    private static boolean reset;
    private static boolean started;
    Editor editor;
    private JMenu file;
    private JMenu run;
    private JMenu window;
    private JMenu help;
    private JMenu edit;
    private JMenu settings;
    private JMenuItem fileNew;
    private JMenuItem fileOpen;
    private JMenuItem fileClose;
    private JMenuItem fileCloseAll;
    private JMenuItem fileSave;
    private JMenuItem fileSaveAs;
    private JMenuItem fileSaveAll;
    private JMenuItem fileDumpMemory;
    private JMenuItem filePrint;
    private JMenuItem fileExit;
    private JMenuItem editUndo;
    private JMenuItem editRedo;
    private JMenuItem editCut;
    private JMenuItem editCopy;
    private JMenuItem editPaste;
    private JMenuItem editFindReplace;
    private JMenuItem editSelectAll;
    private JMenuItem runGo;
    private JMenuItem runStep;
    private JMenuItem runBackstep;
    private JMenuItem runReset;
    private JMenuItem runAssemble;
    private JMenuItem runStop;
    private JMenuItem runPause;
    private JMenuItem runClearBreakpoints;
    private JMenuItem runToggleBreakpoints;
    private JCheckBoxMenuItem settingsLabel;
    private JCheckBoxMenuItem settingsPopupInput;
    private JCheckBoxMenuItem settingsValueDisplayBase;
    private JCheckBoxMenuItem settingsAddressDisplayBase;
    private JCheckBoxMenuItem settingsExtended;
    private JCheckBoxMenuItem settingsAssembleOnOpen;
    private JCheckBoxMenuItem settingsAssembleAll;
    private JCheckBoxMenuItem settingsWarningsAreErrors;
    private JCheckBoxMenuItem settingsStartAtMain;
    private JCheckBoxMenuItem settingsDelayedBranching;
    private JCheckBoxMenuItem settingsProgramArguments;
    private JCheckBoxMenuItem settingsSelfModifyingCode;
    private JMenuItem settingsExceptionHandler;
    private JMenuItem settingsEditor;
    private JMenuItem settingsHighlighting;
    private JMenuItem settingsMemoryConfiguration;
    private JMenuItem helpHelp;
    private JMenuItem helpAbout;
    private JButton Undo;
    private JButton Redo;
    private JButton Cut;
    private JButton Copy;
    private JButton Paste;
    private JButton FindReplace;
    private JButton SelectAll;
    private JButton New;
    private JButton Open;
    private JButton Save;
    private JButton SaveAs;
    private JButton SaveAll;
    private JButton DumpMemory;
    private JButton Print;
    private JButton Run;
    private JButton Assemble;
    private JButton Reset;
    private JButton Step;
    private JButton Backstep;
    private JButton Stop;
    private JButton Pause;
    private JButton Help;
    private Action fileNewAction;
    private Action fileOpenAction;
    private Action fileCloseAction;
    private Action fileCloseAllAction;
    private Action fileSaveAction;
    private Action fileSaveAsAction;
    private Action fileSaveAllAction;
    private Action fileDumpMemoryAction;
    private Action filePrintAction;
    private Action fileExitAction;
    EditUndoAction editUndoAction;
    EditRedoAction editRedoAction;
    private Action editCutAction;
    private Action editCopyAction;
    private Action editPasteAction;
    private Action editFindReplaceAction;
    private Action editSelectAllAction;
    private Action runAssembleAction;
    private Action runGoAction;
    private Action runStepAction;
    private Action runBackstepAction;
    private Action runResetAction;
    private Action runStopAction;
    private Action runPauseAction;
    private Action runClearBreakpointsAction;
    private Action runToggleBreakpointsAction;
    private Action settingsLabelAction;
    private Action settingsPopupInputAction;
    private Action settingsValueDisplayBaseAction;
    private Action settingsAddressDisplayBaseAction;
    private Action settingsExtendedAction;
    private Action settingsAssembleOnOpenAction;
    private Action settingsAssembleAllAction;
    private Action settingsWarningsAreErrorsAction;
    private Action settingsStartAtMainAction;
    private Action settingsProgramArgumentsAction;
    private Action settingsDelayedBranchingAction;
    private Action settingsExceptionHandlerAction;
    private Action settingsEditorAction;
    private Action settingsHighlightingAction;
    private Action settingsMemoryConfigurationAction;
    private Action settingsSelfModifyingCodeAction;
    private Action helpHelpAction;
    private Action helpAboutAction;
    
    public VenusUI(final String s) {
        super(s);
        Globals.setGui(this.mainUI = this);
        this.editor = new Editor(this);
        final double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        final double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        final double messageWidthPct = (screenWidth < 1000.0) ? 0.67 : 0.73;
        final double messageHeightPct = (screenWidth < 1000.0) ? 0.12 : 0.15;
        final double mainWidthPct = (screenWidth < 1000.0) ? 0.67 : 0.73;
        final double mainHeightPct = (screenWidth < 1000.0) ? 0.6 : 0.65;
        final double registersWidthPct = (screenWidth < 1000.0) ? 0.18 : 0.22;
        final double registersHeightPct = (screenWidth < 1000.0) ? 0.72 : 0.8;
        final Dimension messagesPanePreferredSize = new Dimension((int)(screenWidth * messageWidthPct), (int)(screenHeight * messageHeightPct));
        final Dimension mainPanePreferredSize = new Dimension((int)(screenWidth * mainWidthPct), (int)(screenHeight * mainHeightPct));
        final Dimension registersPanePreferredSize = new Dimension((int)(screenWidth * registersWidthPct), (int)(screenHeight * registersHeightPct));
        Globals.initialize(true);
        final URL im = this.getClass().getResource("/images/RedMars16.gif");
        if (im == null) {
            System.out.println("Internal Error: images folder or file not found");
            System.exit(0);
        }
        final Image mars = Toolkit.getDefaultToolkit().getImage(im);
        this.setIconImage(mars);
        this.registersTab = new RegistersWindow();
        this.coprocessor1Tab = new Coprocessor1Window();
        this.coprocessor0Tab = new Coprocessor0Window();
        (this.registersPane = new RegistersPane(this.mainUI, this.registersTab, this.coprocessor1Tab, this.coprocessor0Tab)).setPreferredSize(registersPanePreferredSize);
        (this.mainPane = new MainPane(this.mainUI, this.editor, this.registersTab, this.coprocessor1Tab, this.coprocessor0Tab)).setPreferredSize(mainPanePreferredSize);
        (this.messagesPane = new MessagesPane()).setPreferredSize(messagesPanePreferredSize);
        (this.splitter = new JSplitPane(0, this.mainPane, this.messagesPane)).setOneTouchExpandable(true);
        this.splitter.resetToPreferredSizes();
        (this.horizonSplitter = new JSplitPane(1, this.splitter, this.registersPane)).setOneTouchExpandable(true);
        this.horizonSplitter.resetToPreferredSizes();
        this.createActionObjects();
        this.setJMenuBar(this.menu = this.setUpMenuBar());
        this.toolbar = this.setUpToolBar();
        final JPanel jp = new JPanel(new FlowLayout(0));
        jp.add(this.toolbar);
        jp.add(RunSpeedPanel.getInstance());
        final JPanel center = new JPanel(new BorderLayout());
        center.add(jp, "North");
        center.add(this.horizonSplitter);
        this.getContentPane().add(center);
        FileStatus.reset();
        FileStatus.set(0);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(final WindowEvent e) {
                VenusUI.this.mainUI.setExtendedState(6);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (VenusUI.this.mainUI.editor.closeAll()) {
                    System.exit(0);
                }
            }
        });
        this.setDefaultCloseOperation(0);
        this.pack();
        this.setVisible(true);
    }
    
    private void createActionObjects() {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Class cs = this.getClass();
        try {
            this.fileNewAction = new FileNewAction("New", new ImageIcon(tk.getImage(cs.getResource("/images/New22.png"))), "Create a new file for editing", new Integer(78), KeyStroke.getKeyStroke(78, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.fileOpenAction = new FileOpenAction("Open ...", new ImageIcon(tk.getImage(cs.getResource("/images/Open22.png"))), "Open a file for editing", new Integer(79), KeyStroke.getKeyStroke(79, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.fileCloseAction = new FileCloseAction("Close", null, "Close the current file", new Integer(67), KeyStroke.getKeyStroke(87, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.fileCloseAllAction = new FileCloseAllAction("Close All", null, "Close all open files", new Integer(76), null, this.mainUI);
            this.fileSaveAction = new FileSaveAction("Save", new ImageIcon(tk.getImage(cs.getResource("/images/Save22.png"))), "Save the current file", new Integer(83), KeyStroke.getKeyStroke(83, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.fileSaveAsAction = new FileSaveAsAction("Save as ...", new ImageIcon(tk.getImage(cs.getResource("/images/SaveAs22.png"))), "Save current file with different name", new Integer(65), null, this.mainUI);
            this.fileSaveAllAction = new FileSaveAllAction("Save All", null, "Save all open files", new Integer(86), null, this.mainUI);
            this.fileDumpMemoryAction = new FileDumpMemoryAction("Dump Memory ...", new ImageIcon(tk.getImage(cs.getResource("/images/Dump22.png"))), "Dump machine code or data in an available format", new Integer(68), KeyStroke.getKeyStroke(68, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.filePrintAction = new FilePrintAction("Print ...", new ImageIcon(tk.getImage(cs.getResource("/images/Print22.gif"))), "Print current file", new Integer(80), null, this.mainUI);
            this.fileExitAction = new FileExitAction("Exit", null, "Exit Mars", new Integer(88), null, this.mainUI);
            this.editUndoAction = new EditUndoAction("Undo", new ImageIcon(tk.getImage(cs.getResource("/images/Undo22.png"))), "Undo last edit", new Integer(85), KeyStroke.getKeyStroke(90, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.editRedoAction = new EditRedoAction("Redo", new ImageIcon(tk.getImage(cs.getResource("/images/Redo22.png"))), "Redo last edit", new Integer(82), KeyStroke.getKeyStroke(89, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.editCutAction = new EditCutAction("Cut", new ImageIcon(tk.getImage(cs.getResource("/images/Cut22.gif"))), "Cut", new Integer(67), KeyStroke.getKeyStroke(88, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.editCopyAction = new EditCopyAction("Copy", new ImageIcon(tk.getImage(cs.getResource("/images/Copy22.png"))), "Copy", new Integer(79), KeyStroke.getKeyStroke(67, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.editPasteAction = new EditPasteAction("Paste", new ImageIcon(tk.getImage(cs.getResource("/images/Paste22.png"))), "Paste", new Integer(80), KeyStroke.getKeyStroke(86, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.editFindReplaceAction = new EditFindReplaceAction("Find/Replace", new ImageIcon(tk.getImage(cs.getResource("/images/Find22.png"))), "Find/Replace", new Integer(70), KeyStroke.getKeyStroke(70, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.editSelectAllAction = new EditSelectAllAction("Select All", null, "Select All", new Integer(65), KeyStroke.getKeyStroke(65, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.runAssembleAction = new RunAssembleAction("Assemble", new ImageIcon(tk.getImage(cs.getResource("/images/Assemble22.png"))), "Assemble the current file and clear breakpoints", new Integer(65), KeyStroke.getKeyStroke(114, 0), this.mainUI);
            this.runGoAction = new RunGoAction("Go", new ImageIcon(tk.getImage(cs.getResource("/images/Play22.png"))), "Run the current program", new Integer(71), KeyStroke.getKeyStroke(116, 0), this.mainUI);
            this.runStepAction = new RunStepAction("Step", new ImageIcon(tk.getImage(cs.getResource("/images/StepForward22.png"))), "Run one step at a time", new Integer(84), KeyStroke.getKeyStroke(118, 0), this.mainUI);
            this.runBackstepAction = new RunBackstepAction("Backstep", new ImageIcon(tk.getImage(cs.getResource("/images/StepBack22.png"))), "Undo the last step", new Integer(66), KeyStroke.getKeyStroke(119, 0), this.mainUI);
            this.runPauseAction = new RunPauseAction("Pause", new ImageIcon(tk.getImage(cs.getResource("/images/Pause22.png"))), "Pause the currently running program", new Integer(80), KeyStroke.getKeyStroke(120, 0), this.mainUI);
            this.runStopAction = new RunStopAction("Stop", new ImageIcon(tk.getImage(cs.getResource("/images/Stop22.png"))), "Stop the currently running program", new Integer(83), KeyStroke.getKeyStroke(122, 0), this.mainUI);
            this.runResetAction = new RunResetAction("Reset", new ImageIcon(tk.getImage(cs.getResource("/images/Reset22.png"))), "Reset MIPS memory and registers", new Integer(82), KeyStroke.getKeyStroke(123, 0), this.mainUI);
            this.runClearBreakpointsAction = new RunClearBreakpointsAction("Clear all breakpoints", null, "Clears all execution breakpoints set since the last assemble.", new Integer(75), KeyStroke.getKeyStroke(75, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.runToggleBreakpointsAction = new RunToggleBreakpointsAction("Toggle all breakpoints", null, "Disable/enable all breakpoints without clearing (can also click Bkpt column header)", new Integer(84), KeyStroke.getKeyStroke(84, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), this.mainUI);
            this.settingsLabelAction = new SettingsLabelAction("Show Labels Window (symbol table)", null, "Toggle visibility of Labels window (symbol table) in the Execute tab", null, null, this.mainUI);
            this.settingsPopupInputAction = new SettingsPopupInputAction("Popup dialog for input syscalls (5,6,7,8,12)", null, "If set, use popup dialog for input syscalls (5,6,7,8,12) instead of cursor in Run I/O window", null, null, this.mainUI);
            this.settingsValueDisplayBaseAction = new SettingsValueDisplayBaseAction("Values displayed in hexadecimal", null, "Toggle between hexadecimal and decimal display of memory/register values", null, null, this.mainUI);
            this.settingsAddressDisplayBaseAction = new SettingsAddressDisplayBaseAction("Addresses displayed in hexadecimal", null, "Toggle between hexadecimal and decimal display of memory addresses", null, null, this.mainUI);
            this.settingsExtendedAction = new SettingsExtendedAction("Permit extended (pseudo) instructions and formats", null, "If set, MIPS extended (pseudo) instructions are formats are permitted.", null, null, this.mainUI);
            this.settingsAssembleOnOpenAction = new SettingsAssembleOnOpenAction("Assemble file upon opening", null, "If set, a file will be automatically assembled as soon as it is opened.  File Open dialog will show most recently opened file.", null, null, this.mainUI);
            this.settingsAssembleAllAction = new SettingsAssembleAllAction("Assemble all files in directory", null, "If set, all files in current directory will be assembled when Assemble operation is selected.", null, null, this.mainUI);
            this.settingsWarningsAreErrorsAction = new SettingsWarningsAreErrorsAction("Assembler warnings are considered errors", null, "If set, assembler warnings will be interpreted as errors and prevent successful assembly.", null, null, this.mainUI);
            this.settingsStartAtMainAction = new SettingsStartAtMainAction("Initialize Program Counter to global 'main' if defined", null, "If set, assembler will initialize Program Counter to text address globally labeled 'main', if defined.", null, null, this.mainUI);
            this.settingsProgramArgumentsAction = new SettingsProgramArgumentsAction("Program arguments provided to MIPS program", null, "If set, program arguments for MIPS program can be entered in border of Text Segment window.", null, null, this.mainUI);
            this.settingsDelayedBranchingAction = new SettingsDelayedBranchingAction("Delayed branching", null, "If set, delayed branching will occur during MIPS execution.", null, null, this.mainUI);
            this.settingsSelfModifyingCodeAction = new SettingsSelfModifyingCodeAction("Self-modifying code", null, "If set, the MIPS program can write and branch to both text and data segments.", null, null, this.mainUI);
            this.settingsEditorAction = new SettingsEditorAction("Editor...", null, "View and modify text editor settings.", null, null, this.mainUI);
            this.settingsHighlightingAction = new SettingsHighlightingAction("Highlighting...", null, "View and modify Execute Tab highlighting colors", null, null, this.mainUI);
            this.settingsExceptionHandlerAction = new SettingsExceptionHandlerAction("Exception Handler...", null, "If set, the specified exception handler file will be included in all Assemble operations.", null, null, this.mainUI);
            this.settingsMemoryConfigurationAction = new SettingsMemoryConfigurationAction("Memory Configuration...", null, "View and modify memory segment base addresses for simulated MIPS.", null, null, this.mainUI);
            this.helpHelpAction = new HelpHelpAction("Help", new ImageIcon(tk.getImage(cs.getResource("/images/Help22.png"))), "Help", new Integer(72), KeyStroke.getKeyStroke(112, 0), this.mainUI);
            this.helpAboutAction = new HelpAboutAction("About ...", null, "Information about Mars", null, null, this.mainUI);
        }
        catch (NullPointerException e) {
            System.out.println("Internal Error: images folder not found, or other null pointer exception while creating Action objects");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    private JMenuBar setUpMenuBar() {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Class cs = this.getClass();
        final JMenuBar menuBar = new JMenuBar();
        (this.file = new JMenu("File")).setMnemonic(70);
        (this.edit = new JMenu("Edit")).setMnemonic(69);
        (this.run = new JMenu("Run")).setMnemonic(82);
        (this.settings = new JMenu("Settings")).setMnemonic(83);
        (this.help = new JMenu("Help")).setMnemonic(72);
        (this.fileNew = new JMenuItem(this.fileNewAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/New16.png"))));
        (this.fileOpen = new JMenuItem(this.fileOpenAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Open16.png"))));
        (this.fileClose = new JMenuItem(this.fileCloseAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        (this.fileCloseAll = new JMenuItem(this.fileCloseAllAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        (this.fileSave = new JMenuItem(this.fileSaveAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Save16.png"))));
        (this.fileSaveAs = new JMenuItem(this.fileSaveAsAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/SaveAs16.png"))));
        (this.fileSaveAll = new JMenuItem(this.fileSaveAllAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        (this.fileDumpMemory = new JMenuItem(this.fileDumpMemoryAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Dump16.png"))));
        (this.filePrint = new JMenuItem(this.filePrintAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Print16.gif"))));
        (this.fileExit = new JMenuItem(this.fileExitAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        this.file.add(this.fileNew);
        this.file.add(this.fileOpen);
        this.file.add(this.fileClose);
        this.file.add(this.fileCloseAll);
        this.file.addSeparator();
        this.file.add(this.fileSave);
        this.file.add(this.fileSaveAs);
        this.file.add(this.fileSaveAll);
        if (new DumpFormatLoader().loadDumpFormats().size() > 0) {
            this.file.add(this.fileDumpMemory);
        }
        this.file.addSeparator();
        this.file.add(this.filePrint);
        this.file.addSeparator();
        this.file.add(this.fileExit);
        (this.editUndo = new JMenuItem(this.editUndoAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Undo16.png"))));
        (this.editRedo = new JMenuItem(this.editRedoAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Redo16.png"))));
        (this.editCut = new JMenuItem(this.editCutAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Cut16.gif"))));
        (this.editCopy = new JMenuItem(this.editCopyAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Copy16.png"))));
        (this.editPaste = new JMenuItem(this.editPasteAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Paste16.png"))));
        (this.editFindReplace = new JMenuItem(this.editFindReplaceAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Find16.png"))));
        (this.editSelectAll = new JMenuItem(this.editSelectAllAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        this.edit.add(this.editUndo);
        this.edit.add(this.editRedo);
        this.edit.addSeparator();
        this.edit.add(this.editCut);
        this.edit.add(this.editCopy);
        this.edit.add(this.editPaste);
        this.edit.addSeparator();
        this.edit.add(this.editFindReplace);
        this.edit.add(this.editSelectAll);
        (this.runAssemble = new JMenuItem(this.runAssembleAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Assemble16.png"))));
        (this.runGo = new JMenuItem(this.runGoAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Play16.png"))));
        (this.runStep = new JMenuItem(this.runStepAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/StepForward16.png"))));
        (this.runBackstep = new JMenuItem(this.runBackstepAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/StepBack16.png"))));
        (this.runReset = new JMenuItem(this.runResetAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Reset16.png"))));
        (this.runStop = new JMenuItem(this.runStopAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Stop16.png"))));
        (this.runPause = new JMenuItem(this.runPauseAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Pause16.png"))));
        (this.runClearBreakpoints = new JMenuItem(this.runClearBreakpointsAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        (this.runToggleBreakpoints = new JMenuItem(this.runToggleBreakpointsAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        this.run.add(this.runAssemble);
        this.run.add(this.runGo);
        this.run.add(this.runStep);
        this.run.add(this.runBackstep);
        this.run.add(this.runPause);
        this.run.add(this.runStop);
        this.run.add(this.runReset);
        this.run.addSeparator();
        this.run.add(this.runClearBreakpoints);
        this.run.add(this.runToggleBreakpoints);
        (this.settingsLabel = new JCheckBoxMenuItem(this.settingsLabelAction)).setSelected(Globals.getSettings().getLabelWindowVisibility());
        (this.settingsPopupInput = new JCheckBoxMenuItem(this.settingsPopupInputAction)).setSelected(Globals.getSettings().getBooleanSetting(17));
        (this.settingsValueDisplayBase = new JCheckBoxMenuItem(this.settingsValueDisplayBaseAction)).setSelected(Globals.getSettings().getDisplayValuesInHex());
        this.mainPane.getExecutePane().getValueDisplayBaseChooser().setSettingsMenuItem(this.settingsValueDisplayBase);
        (this.settingsAddressDisplayBase = new JCheckBoxMenuItem(this.settingsAddressDisplayBaseAction)).setSelected(Globals.getSettings().getDisplayAddressesInHex());
        this.mainPane.getExecutePane().getAddressDisplayBaseChooser().setSettingsMenuItem(this.settingsAddressDisplayBase);
        (this.settingsExtended = new JCheckBoxMenuItem(this.settingsExtendedAction)).setSelected(Globals.getSettings().getExtendedAssemblerEnabled());
        (this.settingsDelayedBranching = new JCheckBoxMenuItem(this.settingsDelayedBranchingAction)).setSelected(Globals.getSettings().getDelayedBranchingEnabled());
        (this.settingsSelfModifyingCode = new JCheckBoxMenuItem(this.settingsSelfModifyingCodeAction)).setSelected(Globals.getSettings().getBooleanSetting(20));
        (this.settingsAssembleOnOpen = new JCheckBoxMenuItem(this.settingsAssembleOnOpenAction)).setSelected(Globals.getSettings().getAssembleOnOpenEnabled());
        (this.settingsAssembleAll = new JCheckBoxMenuItem(this.settingsAssembleAllAction)).setSelected(Globals.getSettings().getAssembleAllEnabled());
        (this.settingsWarningsAreErrors = new JCheckBoxMenuItem(this.settingsWarningsAreErrorsAction)).setSelected(Globals.getSettings().getWarningsAreErrors());
        (this.settingsStartAtMain = new JCheckBoxMenuItem(this.settingsStartAtMainAction)).setSelected(Globals.getSettings().getStartAtMain());
        (this.settingsProgramArguments = new JCheckBoxMenuItem(this.settingsProgramArgumentsAction)).setSelected(Globals.getSettings().getProgramArguments());
        this.settingsEditor = new JMenuItem(this.settingsEditorAction);
        this.settingsHighlighting = new JMenuItem(this.settingsHighlightingAction);
        this.settingsExceptionHandler = new JMenuItem(this.settingsExceptionHandlerAction);
        this.settingsMemoryConfiguration = new JMenuItem(this.settingsMemoryConfigurationAction);
        this.settings.add(this.settingsLabel);
        this.settings.add(this.settingsProgramArguments);
        this.settings.add(this.settingsPopupInput);
        this.settings.add(this.settingsAddressDisplayBase);
        this.settings.add(this.settingsValueDisplayBase);
        this.settings.addSeparator();
        this.settings.add(this.settingsAssembleOnOpen);
        this.settings.add(this.settingsAssembleAll);
        this.settings.add(this.settingsWarningsAreErrors);
        this.settings.add(this.settingsStartAtMain);
        this.settings.addSeparator();
        this.settings.add(this.settingsExtended);
        this.settings.add(this.settingsDelayedBranching);
        this.settings.add(this.settingsSelfModifyingCode);
        this.settings.addSeparator();
        this.settings.add(this.settingsEditor);
        this.settings.add(this.settingsHighlighting);
        this.settings.add(this.settingsExceptionHandler);
        this.settings.add(this.settingsMemoryConfiguration);
        (this.helpHelp = new JMenuItem(this.helpHelpAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/Help16.png"))));
        (this.helpAbout = new JMenuItem(this.helpAboutAction)).setIcon(new ImageIcon(tk.getImage(cs.getResource("/images/MyBlank16.gif"))));
        this.help.add(this.helpHelp);
        this.help.addSeparator();
        this.help.add(this.helpAbout);
        menuBar.add(this.file);
        menuBar.add(this.edit);
        menuBar.add(this.run);
        menuBar.add(this.settings);
        final JMenu toolMenu = new ToolLoader().buildToolsMenu();
        if (toolMenu != null) {
            menuBar.add(toolMenu);
        }
        menuBar.add(this.help);
        return menuBar;
    }
    
    JToolBar setUpToolBar() {
        final JToolBar toolBar = new JToolBar();
        (this.New = new JButton(this.fileNewAction)).setText("");
        (this.Open = new JButton(this.fileOpenAction)).setText("");
        (this.Save = new JButton(this.fileSaveAction)).setText("");
        (this.SaveAs = new JButton(this.fileSaveAsAction)).setText("");
        (this.DumpMemory = new JButton(this.fileDumpMemoryAction)).setText("");
        (this.Print = new JButton(this.filePrintAction)).setText("");
        (this.Undo = new JButton(this.editUndoAction)).setText("");
        (this.Redo = new JButton(this.editRedoAction)).setText("");
        (this.Cut = new JButton(this.editCutAction)).setText("");
        (this.Copy = new JButton(this.editCopyAction)).setText("");
        (this.Paste = new JButton(this.editPasteAction)).setText("");
        (this.FindReplace = new JButton(this.editFindReplaceAction)).setText("");
        (this.SelectAll = new JButton(this.editSelectAllAction)).setText("");
        (this.Run = new JButton(this.runGoAction)).setText("");
        (this.Assemble = new JButton(this.runAssembleAction)).setText("");
        (this.Step = new JButton(this.runStepAction)).setText("");
        (this.Backstep = new JButton(this.runBackstepAction)).setText("");
        (this.Reset = new JButton(this.runResetAction)).setText("");
        (this.Stop = new JButton(this.runStopAction)).setText("");
        (this.Pause = new JButton(this.runPauseAction)).setText("");
        (this.Help = new JButton(this.helpHelpAction)).setText("");
        toolBar.add(this.New);
        toolBar.add(this.Open);
        toolBar.add(this.Save);
        toolBar.add(this.SaveAs);
        if (new DumpFormatLoader().loadDumpFormats().size() > 0) {
            toolBar.add(this.DumpMemory);
        }
        toolBar.add(this.Print);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(this.Undo);
        toolBar.add(this.Redo);
        toolBar.add(this.Cut);
        toolBar.add(this.Copy);
        toolBar.add(this.Paste);
        toolBar.add(this.FindReplace);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(this.Assemble);
        toolBar.add(this.Run);
        toolBar.add(this.Step);
        toolBar.add(this.Backstep);
        toolBar.add(this.Pause);
        toolBar.add(this.Stop);
        toolBar.add(this.Reset);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(this.Help);
        toolBar.add(new JToolBar.Separator());
        return toolBar;
    }
    
    void setMenuState(final int status) {
        switch (VenusUI.menuState = status) {
            case 0: {
                this.setMenuStateInitial();
                break;
            }
            case 1: {
                this.setMenuStateEditingNew();
                break;
            }
            case 2: {
                this.setMenuStateEditingNew();
                break;
            }
            case 3: {
                this.setMenuStateNotEdited();
                break;
            }
            case 4: {
                this.setMenuStateEditing();
                break;
            }
            case 5: {
                this.setMenuStateRunnable();
                break;
            }
            case 6: {
                this.setMenuStateRunning();
                break;
            }
            case 7: {
                this.setMenuStateTerminated();
                break;
            }
            case 8: {
                break;
            }
            default: {
                System.out.println("Invalid File Status: " + status);
                break;
            }
        }
    }
    
    void setMenuStateInitial() {
        this.fileNewAction.setEnabled(true);
        this.fileOpenAction.setEnabled(true);
        this.fileCloseAction.setEnabled(false);
        this.fileCloseAllAction.setEnabled(false);
        this.fileSaveAction.setEnabled(false);
        this.fileSaveAsAction.setEnabled(false);
        this.fileSaveAllAction.setEnabled(false);
        this.fileDumpMemoryAction.setEnabled(false);
        this.filePrintAction.setEnabled(false);
        this.fileExitAction.setEnabled(true);
        this.editUndoAction.setEnabled(false);
        this.editRedoAction.setEnabled(false);
        this.editCutAction.setEnabled(false);
        this.editCopyAction.setEnabled(false);
        this.editPasteAction.setEnabled(false);
        this.editFindReplaceAction.setEnabled(false);
        this.editSelectAllAction.setEnabled(false);
        this.settingsDelayedBranchingAction.setEnabled(true);
        this.settingsMemoryConfigurationAction.setEnabled(true);
        this.runAssembleAction.setEnabled(false);
        this.runGoAction.setEnabled(false);
        this.runStepAction.setEnabled(false);
        this.runBackstepAction.setEnabled(false);
        this.runResetAction.setEnabled(false);
        this.runStopAction.setEnabled(false);
        this.runPauseAction.setEnabled(false);
        this.runClearBreakpointsAction.setEnabled(false);
        this.runToggleBreakpointsAction.setEnabled(false);
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.updateUndoState();
        this.editRedoAction.updateRedoState();
    }
    
    void setMenuStateNotEdited() {
        this.fileNewAction.setEnabled(true);
        this.fileOpenAction.setEnabled(true);
        this.fileCloseAction.setEnabled(true);
        this.fileCloseAllAction.setEnabled(true);
        this.fileSaveAction.setEnabled(true);
        this.fileSaveAsAction.setEnabled(true);
        this.fileSaveAllAction.setEnabled(true);
        this.fileDumpMemoryAction.setEnabled(false);
        this.filePrintAction.setEnabled(true);
        this.fileExitAction.setEnabled(true);
        this.editCutAction.setEnabled(true);
        this.editCopyAction.setEnabled(true);
        this.editPasteAction.setEnabled(true);
        this.editFindReplaceAction.setEnabled(true);
        this.editSelectAllAction.setEnabled(true);
        this.settingsDelayedBranchingAction.setEnabled(true);
        this.settingsMemoryConfigurationAction.setEnabled(true);
        this.runAssembleAction.setEnabled(true);
        if (!Globals.getSettings().getBooleanSetting(3)) {
            this.runGoAction.setEnabled(false);
            this.runStepAction.setEnabled(false);
            this.runBackstepAction.setEnabled(false);
            this.runResetAction.setEnabled(false);
            this.runStopAction.setEnabled(false);
            this.runPauseAction.setEnabled(false);
            this.runClearBreakpointsAction.setEnabled(false);
            this.runToggleBreakpointsAction.setEnabled(false);
        }
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.updateUndoState();
        this.editRedoAction.updateRedoState();
    }
    
    void setMenuStateEditing() {
        this.fileNewAction.setEnabled(true);
        this.fileOpenAction.setEnabled(true);
        this.fileCloseAction.setEnabled(true);
        this.fileCloseAllAction.setEnabled(true);
        this.fileSaveAction.setEnabled(true);
        this.fileSaveAsAction.setEnabled(true);
        this.fileSaveAllAction.setEnabled(true);
        this.fileDumpMemoryAction.setEnabled(false);
        this.filePrintAction.setEnabled(true);
        this.fileExitAction.setEnabled(true);
        this.editCutAction.setEnabled(true);
        this.editCopyAction.setEnabled(true);
        this.editPasteAction.setEnabled(true);
        this.editFindReplaceAction.setEnabled(true);
        this.editSelectAllAction.setEnabled(true);
        this.settingsDelayedBranchingAction.setEnabled(true);
        this.settingsMemoryConfigurationAction.setEnabled(true);
        this.runAssembleAction.setEnabled(true);
        this.runGoAction.setEnabled(false);
        this.runStepAction.setEnabled(false);
        this.runBackstepAction.setEnabled(false);
        this.runResetAction.setEnabled(false);
        this.runStopAction.setEnabled(false);
        this.runPauseAction.setEnabled(false);
        this.runClearBreakpointsAction.setEnabled(false);
        this.runToggleBreakpointsAction.setEnabled(false);
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.updateUndoState();
        this.editRedoAction.updateRedoState();
    }
    
    void setMenuStateEditingNew() {
        this.fileNewAction.setEnabled(true);
        this.fileOpenAction.setEnabled(true);
        this.fileCloseAction.setEnabled(true);
        this.fileCloseAllAction.setEnabled(true);
        this.fileSaveAction.setEnabled(true);
        this.fileSaveAsAction.setEnabled(true);
        this.fileSaveAllAction.setEnabled(true);
        this.fileDumpMemoryAction.setEnabled(false);
        this.filePrintAction.setEnabled(true);
        this.fileExitAction.setEnabled(true);
        this.editCutAction.setEnabled(true);
        this.editCopyAction.setEnabled(true);
        this.editPasteAction.setEnabled(true);
        this.editFindReplaceAction.setEnabled(true);
        this.editSelectAllAction.setEnabled(true);
        this.settingsDelayedBranchingAction.setEnabled(true);
        this.settingsMemoryConfigurationAction.setEnabled(true);
        this.runAssembleAction.setEnabled(false);
        this.runGoAction.setEnabled(false);
        this.runStepAction.setEnabled(false);
        this.runBackstepAction.setEnabled(false);
        this.runResetAction.setEnabled(false);
        this.runStopAction.setEnabled(false);
        this.runPauseAction.setEnabled(false);
        this.runClearBreakpointsAction.setEnabled(false);
        this.runToggleBreakpointsAction.setEnabled(false);
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.updateUndoState();
        this.editRedoAction.updateRedoState();
    }
    
    void setMenuStateRunnable() {
        this.fileNewAction.setEnabled(true);
        this.fileOpenAction.setEnabled(true);
        this.fileCloseAction.setEnabled(true);
        this.fileCloseAllAction.setEnabled(true);
        this.fileSaveAction.setEnabled(true);
        this.fileSaveAsAction.setEnabled(true);
        this.fileSaveAllAction.setEnabled(true);
        this.fileDumpMemoryAction.setEnabled(true);
        this.filePrintAction.setEnabled(true);
        this.fileExitAction.setEnabled(true);
        this.editCutAction.setEnabled(true);
        this.editCopyAction.setEnabled(true);
        this.editPasteAction.setEnabled(true);
        this.editFindReplaceAction.setEnabled(true);
        this.editSelectAllAction.setEnabled(true);
        this.settingsDelayedBranchingAction.setEnabled(true);
        this.settingsMemoryConfigurationAction.setEnabled(true);
        this.runAssembleAction.setEnabled(true);
        this.runGoAction.setEnabled(true);
        this.runStepAction.setEnabled(true);
        this.runBackstepAction.setEnabled(Globals.getSettings().getBackSteppingEnabled() && !Globals.program.getBackStepper().empty());
        this.runResetAction.setEnabled(true);
        this.runStopAction.setEnabled(false);
        this.runPauseAction.setEnabled(false);
        this.runToggleBreakpointsAction.setEnabled(true);
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.updateUndoState();
        this.editRedoAction.updateRedoState();
    }
    
    void setMenuStateRunning() {
        this.fileNewAction.setEnabled(false);
        this.fileOpenAction.setEnabled(false);
        this.fileCloseAction.setEnabled(false);
        this.fileCloseAllAction.setEnabled(false);
        this.fileSaveAction.setEnabled(false);
        this.fileSaveAsAction.setEnabled(false);
        this.fileSaveAllAction.setEnabled(false);
        this.fileDumpMemoryAction.setEnabled(false);
        this.filePrintAction.setEnabled(false);
        this.fileExitAction.setEnabled(false);
        this.editCutAction.setEnabled(false);
        this.editCopyAction.setEnabled(false);
        this.editPasteAction.setEnabled(false);
        this.editFindReplaceAction.setEnabled(false);
        this.editSelectAllAction.setEnabled(false);
        this.settingsDelayedBranchingAction.setEnabled(false);
        this.settingsMemoryConfigurationAction.setEnabled(false);
        this.runAssembleAction.setEnabled(false);
        this.runGoAction.setEnabled(false);
        this.runStepAction.setEnabled(false);
        this.runBackstepAction.setEnabled(false);
        this.runResetAction.setEnabled(false);
        this.runStopAction.setEnabled(true);
        this.runPauseAction.setEnabled(true);
        this.runToggleBreakpointsAction.setEnabled(false);
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.setEnabled(false);
        this.editRedoAction.setEnabled(false);
    }
    
    void setMenuStateTerminated() {
        this.fileNewAction.setEnabled(true);
        this.fileOpenAction.setEnabled(true);
        this.fileCloseAction.setEnabled(true);
        this.fileCloseAllAction.setEnabled(true);
        this.fileSaveAction.setEnabled(true);
        this.fileSaveAsAction.setEnabled(true);
        this.fileSaveAllAction.setEnabled(true);
        this.fileDumpMemoryAction.setEnabled(true);
        this.filePrintAction.setEnabled(true);
        this.fileExitAction.setEnabled(true);
        this.editCutAction.setEnabled(true);
        this.editCopyAction.setEnabled(true);
        this.editPasteAction.setEnabled(true);
        this.editFindReplaceAction.setEnabled(true);
        this.editSelectAllAction.setEnabled(true);
        this.settingsDelayedBranchingAction.setEnabled(true);
        this.settingsMemoryConfigurationAction.setEnabled(true);
        this.runAssembleAction.setEnabled(true);
        this.runGoAction.setEnabled(false);
        this.runStepAction.setEnabled(false);
        this.runBackstepAction.setEnabled(Globals.getSettings().getBackSteppingEnabled() && !Globals.program.getBackStepper().empty());
        this.runResetAction.setEnabled(true);
        this.runStopAction.setEnabled(false);
        this.runPauseAction.setEnabled(false);
        this.runToggleBreakpointsAction.setEnabled(true);
        this.helpHelpAction.setEnabled(true);
        this.helpAboutAction.setEnabled(true);
        this.editUndoAction.updateUndoState();
        this.editRedoAction.updateRedoState();
    }
    
    public static int getMenuState() {
        return VenusUI.menuState;
    }
    
    public static void setReset(final boolean b) {
        VenusUI.reset = b;
    }
    
    public static void setStarted(final boolean b) {
        VenusUI.started = b;
    }
    
    public static boolean getReset() {
        return VenusUI.reset;
    }
    
    public static boolean getStarted() {
        return VenusUI.started;
    }
    
    public Editor getEditor() {
        return this.editor;
    }
    
    public MainPane getMainPane() {
        return this.mainPane;
    }
    
    public MessagesPane getMessagesPane() {
        return this.messagesPane;
    }
    
    public RegistersPane getRegistersPane() {
        return this.registersPane;
    }
    
    public JCheckBoxMenuItem getValueDisplayBaseMenuItem() {
        return this.settingsValueDisplayBase;
    }
    
    public JCheckBoxMenuItem getAddressDisplayBaseMenuItem() {
        return this.settingsAddressDisplayBase;
    }
    
    public Action getRunAssembleAction() {
        return this.runAssembleAction;
    }
    
    public void haveMenuRequestFocus() {
        this.menu.requestFocus();
    }
    
    public void dispatchEventToMenu(final KeyEvent evt) {
        this.menu.dispatchEvent(evt);
    }
    
    private void setupPopupMenu() {
        final JPopupMenu popup = new JPopupMenu();
        popup.add(new JCheckBoxMenuItem(this.settingsLabelAction));
        final MouseListener popupListener = new PopupListener(popup);
        this.addMouseListener(popupListener);
    }
    
    static {
        VenusUI.menuState = 0;
        VenusUI.reset = true;
        VenusUI.started = false;
    }
}
