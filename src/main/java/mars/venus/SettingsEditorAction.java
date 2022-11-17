

package mars.venus;

import javax.swing.JColorChooser;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import mars.venus.editors.jeditsyntax.tokenmarker.MIPSTokenMarker;
import mars.venus.editors.jeditsyntax.SyntaxUtilities;
import javax.swing.AbstractButton;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import javax.swing.Box;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.text.Caret;
import javax.swing.JSpinner;
import javax.swing.JPanel;
import java.awt.Font;
import mars.venus.editors.jeditsyntax.SyntaxStyle;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.Frame;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import javax.swing.border.Border;
import javax.swing.JTextField;
import javax.swing.JSlider;
import javax.swing.JComboBox;
import javax.swing.JDialog;

public class SettingsEditorAction extends GuiAction
{
    JDialog editorDialog;
    JComboBox fontFamilySelector;
    JComboBox fontStyleSelector;
    JSlider tabSizeSelector;
    JTextField fontSizeDisplay;
    String initialFontFamily;
    String initialFontStyle;
    String initialFontSize;
    private static final int gridVGap = 2;
    private static final int gridHGap = 2;
    private static final Border ColorSelectButtonEnabledBorder;
    private static final Border ColorSelectButtonDisabledBorder;
    private static final String GENERIC_TOOL_TIP_TEXT = "Use generic editor (original MARS editor, similar to Notepad) instead of language-aware styled editor";
    private static final String SAMPLE_TOOL_TIP_TEXT = "Current setting; modify using buttons to the right";
    private static final String FOREGROUND_TOOL_TIP_TEXT = "Click, to select text color";
    private static final String BOLD_TOOL_TIP_TEXT = "Toggle text bold style";
    private static final String ITALIC_TOOL_TIP_TEXT = "Toggle text italic style";
    private static final String DEFAULT_TOOL_TIP_TEXT = "Check, to select defaults (disables buttons)";
    private static final String BOLD_BUTTON_TOOL_TIP_TEXT = "B";
    private static final String ITALIC_BUTTON_TOOL_TIP_TEXT = "I";
    private static final String TAB_SIZE_TOOL_TIP_TEXT = "Current tab size in characters";
    private static final String BLINK_SPINNER_TOOL_TIP_TEXT = "Current blinking rate in milliseconds";
    private static final String BLINK_SAMPLE_TOOL_TIP_TEXT = "Displays current blinking rate";
    private static final String CURRENT_LINE_HIGHLIGHT_TOOL_TIP_TEXT = "Check, to highlight line currently being edited";
    private static final String AUTO_INDENT_TOOL_TIP_TEXT = "Check, to enable auto-indent to previous line when Enter key is pressed";
    private static final String[] POPUP_GUIDANCE_TOOL_TIP_TEXT;
    
    public SettingsEditorAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        (this.editorDialog = new EditorFontDialog(Globals.getGui(), "Text Editor Settings", true, Globals.getSettings().getEditorFont())).setVisible(true);
    }
    
    static {
        ColorSelectButtonEnabledBorder = new BevelBorder(0, Color.WHITE, Color.GRAY);
        ColorSelectButtonDisabledBorder = new LineBorder(Color.GRAY, 2);
        POPUP_GUIDANCE_TOOL_TIP_TEXT = new String[] { "Turns off instruction and directive guide popup while typing", "Generates instruction guide popup after first letter of potential instruction is typed", "Generates instruction guide popup after second letter of potential instruction is typed" };
    }
    
    private class EditorFontDialog extends AbstractFontSettingDialog
    {
        private JButton[] foregroundButtons;
        private JLabel[] samples;
        private JToggleButton[] bold;
        private JToggleButton[] italic;
        private JCheckBox[] useDefault;
        private int[] syntaxStyleIndex;
        private SyntaxStyle[] defaultStyles;
        private SyntaxStyle[] initialStyles;
        private SyntaxStyle[] currentStyles;
        private Font previewFont;
        private JPanel dialogPanel;
        private JPanel syntaxStylePanel;
        private JPanel otherSettingsPanel;
        private JSlider tabSizeSelector;
        private JSpinner tabSizeSpinSelector;
        private JSpinner blinkRateSpinSelector;
        private JSpinner popupPrefixLengthSpinSelector;
        private JCheckBox lineHighlightCheck;
        private JCheckBox genericEditorCheck;
        private JCheckBox autoIndentCheck;
        private Caret blinkCaret;
        private JTextField blinkSample;
        private ButtonGroup popupGuidanceButtons;
        private JRadioButton[] popupGuidanceOptions;
        private boolean syntaxStylesAction;
        private int initialEditorTabSize;
        private int initialCaretBlinkRate;
        private int initialPopupGuidance;
        private boolean initialLineHighlighting;
        private boolean initialGenericTextEditor;
        private boolean initialAutoIndent;
        
        public EditorFontDialog(final Frame owner, final String title, final boolean modality, final Font font) {
            super(owner, title, modality, font);
            this.syntaxStylesAction = false;
            if (Globals.getSettings().getBooleanSetting(18)) {
                this.syntaxStylePanel.setVisible(false);
                this.otherSettingsPanel.setVisible(false);
            }
        }
        
        @Override
        protected JPanel buildDialogPanel() {
            final JPanel dialog = new JPanel(new BorderLayout());
            final JPanel fontDialogPanel = super.buildDialogPanel();
            final JPanel syntaxStylePanel = this.buildSyntaxStylePanel();
            final JPanel otherSettingsPanel = this.buildOtherSettingsPanel();
            fontDialogPanel.setBorder(BorderFactory.createTitledBorder("Editor Font"));
            syntaxStylePanel.setBorder(BorderFactory.createTitledBorder("Syntax Styling"));
            otherSettingsPanel.setBorder(BorderFactory.createTitledBorder("Other Editor Settings"));
            dialog.add(fontDialogPanel, "West");
            dialog.add(syntaxStylePanel, "Center");
            dialog.add(otherSettingsPanel, "South");
            this.dialogPanel = dialog;
            this.syntaxStylePanel = syntaxStylePanel;
            this.otherSettingsPanel = otherSettingsPanel;
            return dialog;
        }
        
        @Override
        protected Component buildControlPanel() {
            final Box controlPanel = Box.createHorizontalBox();
            final JButton okButton = new JButton("Apply and Close");
            okButton.setToolTipText("Apply current settings and close dialog");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    EditorFontDialog.this.performApply();
                    EditorFontDialog.this.closeDialog();
                }
            });
            final JButton applyButton = new JButton("Apply");
            applyButton.setToolTipText("Apply current settings now and leave dialog open");
            applyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    EditorFontDialog.this.performApply();
                }
            });
            final JButton cancelButton = new JButton("Cancel");
            cancelButton.setToolTipText("Close dialog without applying current settings");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    EditorFontDialog.this.closeDialog();
                }
            });
            final JButton resetButton = new JButton("Reset");
            resetButton.setToolTipText("Reset to initial settings without applying");
            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    EditorFontDialog.this.reset();
                }
            });
            this.initialGenericTextEditor = Globals.getSettings().getBooleanSetting(18);
            (this.genericEditorCheck = new JCheckBox("Use Generic Editor", this.initialGenericTextEditor)).setToolTipText("Use generic editor (original MARS editor, similar to Notepad) instead of language-aware styled editor");
            this.genericEditorCheck.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(final ItemEvent e) {
                    if (e.getStateChange() == 1) {
                        EditorFontDialog.this.syntaxStylePanel.setVisible(false);
                        EditorFontDialog.this.otherSettingsPanel.setVisible(false);
                    }
                    else {
                        EditorFontDialog.this.syntaxStylePanel.setVisible(true);
                        EditorFontDialog.this.otherSettingsPanel.setVisible(true);
                    }
                }
            });
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(okButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(applyButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(cancelButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(resetButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(this.genericEditorCheck);
            controlPanel.add(Box.createHorizontalGlue());
            return controlPanel;
        }
        
        @Override
        protected void apply(final Font font) {
            Globals.getSettings().setBooleanSetting(18, this.genericEditorCheck.isSelected());
            Globals.getSettings().setBooleanSetting(15, this.lineHighlightCheck.isSelected());
            Globals.getSettings().setBooleanSetting(19, this.autoIndentCheck.isSelected());
            Globals.getSettings().setCaretBlinkRate((int)this.blinkRateSpinSelector.getValue());
            Globals.getSettings().setEditorTabSize(this.tabSizeSelector.getValue());
            if (this.syntaxStylesAction) {
                for (int i = 0; i < this.syntaxStyleIndex.length; ++i) {
                    Globals.getSettings().setEditorSyntaxStyleByPosition(this.syntaxStyleIndex[i], new SyntaxStyle(this.samples[i].getForeground(), this.italic[i].isSelected(), this.bold[i].isSelected()));
                }
                this.syntaxStylesAction = false;
            }
            Globals.getSettings().setEditorFont(font);
            int i = 0;
            while (i < this.popupGuidanceOptions.length) {
                if (this.popupGuidanceOptions[i].isSelected()) {
                    if (i == 0) {
                        Globals.getSettings().setBooleanSetting(16, false);
                        break;
                    }
                    Globals.getSettings().setBooleanSetting(16, true);
                    Globals.getSettings().setEditorPopupPrefixLength(i);
                    break;
                }
                else {
                    ++i;
                }
            }
        }
        
        @Override
        protected void reset() {
            super.reset();
            this.initializeSyntaxStyleChangeables();
            this.resetOtherSettings();
            this.syntaxStylesAction = true;
            this.genericEditorCheck.setSelected(this.initialGenericTextEditor);
        }
        
        private void resetOtherSettings() {
            this.tabSizeSelector.setValue(this.initialEditorTabSize);
            this.tabSizeSpinSelector.setValue(new Integer(this.initialEditorTabSize));
            this.lineHighlightCheck.setSelected(this.initialLineHighlighting);
            this.autoIndentCheck.setSelected(this.initialAutoIndent);
            this.blinkRateSpinSelector.setValue(new Integer(this.initialCaretBlinkRate));
            this.blinkCaret.setBlinkRate(this.initialCaretBlinkRate);
            this.popupGuidanceOptions[this.initialPopupGuidance].setSelected(true);
        }
        
        private JPanel buildOtherSettingsPanel() {
            final JPanel otherSettingsPanel = new JPanel();
            this.initialEditorTabSize = Globals.getSettings().getEditorTabSize();
            (this.tabSizeSelector = new JSlider(1, 32, this.initialEditorTabSize)).setToolTipText("Use slider to select tab size from 1 to 32.");
            this.tabSizeSelector.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    final Integer value = new Integer(((JSlider)e.getSource()).getValue());
                    EditorFontDialog.this.tabSizeSpinSelector.setValue(value);
                }
            });
            final SpinnerNumberModel tabSizeSpinnerModel = new SpinnerNumberModel(this.initialEditorTabSize, 1, 32, 1);
            (this.tabSizeSpinSelector = new JSpinner(tabSizeSpinnerModel)).setToolTipText("Current tab size in characters");
            this.tabSizeSpinSelector.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    final Object value = ((JSpinner)e.getSource()).getValue();
                    EditorFontDialog.this.tabSizeSelector.setValue((int)value);
                }
            });
            this.initialLineHighlighting = Globals.getSettings().getBooleanSetting(15);
            (this.lineHighlightCheck = new JCheckBox("Highlight the line currently being edited")).setSelected(this.initialLineHighlighting);
            this.lineHighlightCheck.setToolTipText("Check, to highlight line currently being edited");
            this.initialAutoIndent = Globals.getSettings().getBooleanSetting(19);
            (this.autoIndentCheck = new JCheckBox("Auto-Indent")).setSelected(this.initialAutoIndent);
            this.autoIndentCheck.setToolTipText("Check, to enable auto-indent to previous line when Enter key is pressed");
            this.initialCaretBlinkRate = Globals.getSettings().getCaretBlinkRate();
            (this.blinkSample = new JTextField("     ")).setCaretPosition(2);
            this.blinkSample.setToolTipText("Displays current blinking rate");
            this.blinkSample.setEnabled(false);
            (this.blinkCaret = this.blinkSample.getCaret()).setBlinkRate(this.initialCaretBlinkRate);
            this.blinkCaret.setVisible(true);
            final SpinnerNumberModel blinkRateSpinnerModel = new SpinnerNumberModel(this.initialCaretBlinkRate, 0, 1000, 100);
            (this.blinkRateSpinSelector = new JSpinner(blinkRateSpinnerModel)).setToolTipText("Current blinking rate in milliseconds");
            this.blinkRateSpinSelector.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    final Object value = ((JSpinner)e.getSource()).getValue();
                    EditorFontDialog.this.blinkCaret.setBlinkRate((int)value);
                    EditorFontDialog.this.blinkSample.requestFocus();
                    EditorFontDialog.this.blinkCaret.setVisible(true);
                }
            });
            final JPanel tabPanel = new JPanel(new FlowLayout(0));
            tabPanel.add(new JLabel("Tab Size"));
            tabPanel.add(this.tabSizeSelector);
            tabPanel.add(this.tabSizeSpinSelector);
            final JPanel blinkPanel = new JPanel(new FlowLayout(0));
            blinkPanel.add(new JLabel("Cursor Blinking Rate in ms (0=no blink)"));
            blinkPanel.add(this.blinkRateSpinSelector);
            blinkPanel.add(this.blinkSample);
            otherSettingsPanel.setLayout(new GridLayout(1, 2));
            final JPanel leftColumnSettingsPanel = new JPanel(new GridLayout(4, 1));
            leftColumnSettingsPanel.add(tabPanel);
            leftColumnSettingsPanel.add(blinkPanel);
            leftColumnSettingsPanel.add(this.lineHighlightCheck);
            leftColumnSettingsPanel.add(this.autoIndentCheck);
            final JPanel rightColumnSettingsPanel = new JPanel(new GridLayout(4, 1));
            this.popupGuidanceButtons = new ButtonGroup();
            (this.popupGuidanceOptions = new JRadioButton[3])[0] = new JRadioButton("No popup instruction or directive guide");
            this.popupGuidanceOptions[1] = new JRadioButton("Display instruction guide after 1 letter typed");
            this.popupGuidanceOptions[2] = new JRadioButton("Display instruction guide after 2 letters typed");
            for (int i = 0; i < this.popupGuidanceOptions.length; ++i) {
                this.popupGuidanceOptions[i].setSelected(false);
                this.popupGuidanceOptions[i].setToolTipText(SettingsEditorAction.POPUP_GUIDANCE_TOOL_TIP_TEXT[i]);
                this.popupGuidanceButtons.add(this.popupGuidanceOptions[i]);
            }
            this.initialPopupGuidance = (Globals.getSettings().getBooleanSetting(16) ? Globals.getSettings().getEditorPopupPrefixLength() : 0);
            this.popupGuidanceOptions[this.initialPopupGuidance].setSelected(true);
            final JPanel popupPanel = new JPanel(new GridLayout(3, 1));
            rightColumnSettingsPanel.setBorder(BorderFactory.createTitledBorder("Popup Instruction Guide"));
            rightColumnSettingsPanel.add(this.popupGuidanceOptions[0]);
            rightColumnSettingsPanel.add(this.popupGuidanceOptions[1]);
            rightColumnSettingsPanel.add(this.popupGuidanceOptions[2]);
            otherSettingsPanel.add(leftColumnSettingsPanel);
            otherSettingsPanel.add(rightColumnSettingsPanel);
            return otherSettingsPanel;
        }
        
        private JPanel buildSyntaxStylePanel() {
            final JPanel syntaxStylePanel = new JPanel();
            this.defaultStyles = SyntaxUtilities.getDefaultSyntaxStyles();
            this.initialStyles = SyntaxUtilities.getCurrentSyntaxStyles();
            final String[] labels = MIPSTokenMarker.getMIPSTokenLabels();
            final String[] sampleText = MIPSTokenMarker.getMIPSTokenExamples();
            this.syntaxStylesAction = false;
            int count = 0;
            for (int i = 0; i < labels.length; ++i) {
                if (labels[i] != null) {
                    ++count;
                }
            }
            this.syntaxStyleIndex = new int[count];
            this.currentStyles = new SyntaxStyle[count];
            final String[] label = new String[count];
            this.samples = new JLabel[count];
            this.foregroundButtons = new JButton[count];
            this.bold = new JToggleButton[count];
            this.italic = new JToggleButton[count];
            this.useDefault = new JCheckBox[count];
            final Font genericFont = new JLabel().getFont();
            this.previewFont = new Font("Monospaced", 0, genericFont.getSize());
            final Font boldFont = new Font("Serif", 1, genericFont.getSize());
            final Font italicFont = new Font("Serif", 2, genericFont.getSize());
            count = 0;
            for (int j = 0; j < labels.length; ++j) {
                if (labels[j] != null) {
                    this.syntaxStyleIndex[count] = j;
                    (this.samples[count] = new JLabel()).setOpaque(true);
                    this.samples[count].setHorizontalAlignment(0);
                    this.samples[count].setBorder(BorderFactory.createLineBorder(Color.black));
                    this.samples[count].setText(sampleText[j]);
                    this.samples[count].setBackground(Color.WHITE);
                    this.samples[count].setToolTipText("Current setting; modify using buttons to the right");
                    (this.foregroundButtons[count] = new ColorSelectButton()).addActionListener(new ForegroundChanger(count));
                    this.foregroundButtons[count].setToolTipText("Click, to select text color");
                    final BoldItalicChanger boldItalicChanger = new BoldItalicChanger(count);
                    (this.bold[count] = new JToggleButton("B", false)).setFont(boldFont);
                    this.bold[count].addActionListener(boldItalicChanger);
                    this.bold[count].setToolTipText("Toggle text bold style");
                    (this.italic[count] = new JToggleButton("I", false)).setFont(italicFont);
                    this.italic[count].addActionListener(boldItalicChanger);
                    this.italic[count].setToolTipText("Toggle text italic style");
                    label[count] = labels[j];
                    (this.useDefault[count] = new JCheckBox()).addItemListener(new DefaultChanger(count));
                    this.useDefault[count].setToolTipText("Check, to select defaults (disables buttons)");
                    ++count;
                }
            }
            this.initializeSyntaxStyleChangeables();
            syntaxStylePanel.setLayout(new BorderLayout());
            final JPanel labelPreviewPanel = new JPanel(new GridLayout(this.syntaxStyleIndex.length, 2, 2, 2));
            final JPanel buttonsPanel = new JPanel(new GridLayout(this.syntaxStyleIndex.length, 4, 2, 2));
            for (int k = 0; k < this.syntaxStyleIndex.length; ++k) {
                labelPreviewPanel.add(new JLabel(label[k], 4));
                labelPreviewPanel.add(this.samples[k]);
                buttonsPanel.add(this.foregroundButtons[k]);
                buttonsPanel.add(this.bold[k]);
                buttonsPanel.add(this.italic[k]);
                buttonsPanel.add(this.useDefault[k]);
            }
            final JPanel instructions = new JPanel(new FlowLayout(1));
            final JCheckBox illustrate = new JCheckBox() {
                @Override
                protected void processMouseEvent(final MouseEvent e) {
                }
                
                @Override
                protected void processKeyEvent(final KeyEvent e) {
                }
            };
            illustrate.setSelected(true);
            instructions.add(illustrate);
            instructions.add(new JLabel("= use defaults (disables buttons)"));
            labelPreviewPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            syntaxStylePanel.add(instructions, "North");
            syntaxStylePanel.add(labelPreviewPanel, "West");
            syntaxStylePanel.add(buttonsPanel, "Center");
            return syntaxStylePanel;
        }
        
        private void initializeSyntaxStyleChangeables() {
            for (int count = 0; count < this.samples.length; ++count) {
                final int i = this.syntaxStyleIndex[count];
                this.samples[count].setFont(this.previewFont);
                this.samples[count].setForeground(this.initialStyles[i].getColor());
                this.foregroundButtons[count].setBackground(this.initialStyles[i].getColor());
                this.foregroundButtons[count].setEnabled(true);
                this.currentStyles[count] = this.initialStyles[i];
                this.bold[count].setSelected(this.initialStyles[i].isBold());
                if (this.bold[count].isSelected()) {
                    final Font f = this.samples[count].getFont();
                    this.samples[count].setFont(f.deriveFont(f.getStyle() ^ 0x1));
                }
                this.italic[count].setSelected(this.initialStyles[i].isItalic());
                if (this.italic[count].isSelected()) {
                    final Font f = this.samples[count].getFont();
                    this.samples[count].setFont(f.deriveFont(f.getStyle() ^ 0x2));
                }
                this.useDefault[count].setSelected(this.initialStyles[i].toString().equals(this.defaultStyles[i].toString()));
                if (this.useDefault[count].isSelected()) {
                    this.foregroundButtons[count].setEnabled(false);
                    this.bold[count].setEnabled(false);
                    this.italic[count].setEnabled(false);
                }
            }
        }
        
        private void setSampleStyles(final JLabel sample, final SyntaxStyle style) {
            Font f = this.previewFont;
            if (style.isBold()) {
                f = f.deriveFont(f.getStyle() ^ 0x1);
            }
            if (style.isItalic()) {
                f = f.deriveFont(f.getStyle() ^ 0x2);
            }
            sample.setFont(f);
            sample.setForeground(style.getColor());
        }
        
        private class BoldItalicChanger implements ActionListener
        {
            private int row;
            
            public BoldItalicChanger(final int row) {
                this.row = row;
            }
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Font f = EditorFontDialog.this.samples[this.row].getFont();
                if (e.getActionCommand() == "B") {
                    if (EditorFontDialog.this.bold[this.row].isSelected()) {
                        EditorFontDialog.this.samples[this.row].setFont(f.deriveFont(f.getStyle() | 0x1));
                    }
                    else {
                        EditorFontDialog.this.samples[this.row].setFont(f.deriveFont(f.getStyle() ^ 0x1));
                    }
                }
                else if (EditorFontDialog.this.italic[this.row].isSelected()) {
                    EditorFontDialog.this.samples[this.row].setFont(f.deriveFont(f.getStyle() | 0x2));
                }
                else {
                    EditorFontDialog.this.samples[this.row].setFont(f.deriveFont(f.getStyle() ^ 0x2));
                }
                EditorFontDialog.this.currentStyles[this.row] = new SyntaxStyle(EditorFontDialog.this.foregroundButtons[this.row].getBackground(), EditorFontDialog.this.italic[this.row].isSelected(), EditorFontDialog.this.bold[this.row].isSelected());
                EditorFontDialog.this.syntaxStylesAction = true;
            }
        }
        
        private class ForegroundChanger implements ActionListener
        {
            private int row;
            
            public ForegroundChanger(final int pos) {
                this.row = pos;
            }
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JButton button = (JButton)e.getSource();
                final Color newColor = JColorChooser.showDialog(null, "Set Text Color", button.getBackground());
                if (newColor != null) {
                    button.setBackground(newColor);
                    EditorFontDialog.this.samples[this.row].setForeground(newColor);
                }
                EditorFontDialog.this.currentStyles[this.row] = new SyntaxStyle(button.getBackground(), EditorFontDialog.this.italic[this.row].isSelected(), EditorFontDialog.this.bold[this.row].isSelected());
                EditorFontDialog.this.syntaxStylesAction = true;
            }
        }
        
        private class DefaultChanger implements ItemListener
        {
            private int row;
            
            public DefaultChanger(final int pos) {
                this.row = pos;
            }
            
            @Override
            public void itemStateChanged(final ItemEvent e) {
                final Color newBackground = null;
                final Font newFont = null;
                if (e.getStateChange() == 1) {
                    EditorFontDialog.this.foregroundButtons[this.row].setEnabled(false);
                    EditorFontDialog.this.bold[this.row].setEnabled(false);
                    EditorFontDialog.this.italic[this.row].setEnabled(false);
                    EditorFontDialog.this.currentStyles[this.row] = new SyntaxStyle(EditorFontDialog.this.foregroundButtons[this.row].getBackground(), EditorFontDialog.this.italic[this.row].isSelected(), EditorFontDialog.this.bold[this.row].isSelected());
                    final SyntaxStyle defaultStyle = EditorFontDialog.this.defaultStyles[EditorFontDialog.this.syntaxStyleIndex[this.row]];
                    EditorFontDialog.this.setSampleStyles(EditorFontDialog.this.samples[this.row], defaultStyle);
                    EditorFontDialog.this.foregroundButtons[this.row].setBackground(defaultStyle.getColor());
                    EditorFontDialog.this.bold[this.row].setSelected(defaultStyle.isBold());
                    EditorFontDialog.this.italic[this.row].setSelected(defaultStyle.isItalic());
                }
                else {
                    EditorFontDialog.this.setSampleStyles(EditorFontDialog.this.samples[this.row], EditorFontDialog.this.currentStyles[this.row]);
                    EditorFontDialog.this.foregroundButtons[this.row].setBackground(EditorFontDialog.this.currentStyles[this.row].getColor());
                    EditorFontDialog.this.bold[this.row].setSelected(EditorFontDialog.this.currentStyles[this.row].isBold());
                    EditorFontDialog.this.italic[this.row].setSelected(EditorFontDialog.this.currentStyles[this.row].isItalic());
                    EditorFontDialog.this.foregroundButtons[this.row].setEnabled(true);
                    EditorFontDialog.this.bold[this.row].setEnabled(true);
                    EditorFontDialog.this.italic[this.row].setEnabled(true);
                }
                EditorFontDialog.this.syntaxStylesAction = true;
            }
        }
    }
}
