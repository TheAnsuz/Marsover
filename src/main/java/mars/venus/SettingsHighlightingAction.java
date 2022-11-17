

package mars.venus;

import java.awt.event.ItemEvent;
import javax.swing.JColorChooser;
import mars.Settings;
import java.awt.Insets;
import javax.swing.border.LineBorder;
import javax.swing.Box;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Container;
import java.awt.Frame;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JDialog;

public class SettingsHighlightingAction extends GuiAction
{
    JDialog highlightDialog;
    private static final int[] backgroundSettingPositions;
    private static final int[] foregroundSettingPositions;
    private static final int[] fontSettingPositions;
    JButton[] backgroundButtons;
    JButton[] foregroundButtons;
    JButton[] fontButtons;
    JCheckBox[] defaultCheckBoxes;
    JLabel[] samples;
    Color[] currentNondefaultBackground;
    Color[] currentNondefaultForeground;
    Color[] initialSettingsBackground;
    Color[] initialSettingsForeground;
    Font[] initialFont;
    Font[] currentFont;
    Font[] currentNondefaultFont;
    JButton dataHighlightButton;
    JButton registerHighlightButton;
    boolean currentDataHighlightSetting;
    boolean initialDataHighlightSetting;
    boolean currentRegisterHighlightSetting;
    boolean initialRegisterHighlightSetting;
    private static final int gridVGap = 2;
    private static final int gridHGap = 2;
    private static final String SAMPLE_TOOL_TIP_TEXT = "Preview based on background and text color settings";
    private static final String BACKGROUND_TOOL_TIP_TEXT = "Click, to select background color";
    private static final String FOREGROUND_TOOL_TIP_TEXT = "Click, to select text color";
    private static final String FONT_TOOL_TIP_TEXT = "Click, to select text font";
    private static final String DEFAULT_TOOL_TIP_TEXT = "Check, to select default color (disables color select buttons)";
    public static final String CLOSE_TOOL_TIP_TEXT = "Apply current settings and close dialog";
    public static final String APPLY_TOOL_TIP_TEXT = "Apply current settings now and leave dialog open";
    public static final String RESET_TOOL_TIP_TEXT = "Reset to initial settings without applying";
    public static final String CANCEL_TOOL_TIP_TEXT = "Close dialog without applying current settings";
    private static final String DATA_HIGHLIGHT_ENABLE_TOOL_TIP_TEXT = "Click, to enable or disable highlighting in Data Segment window";
    private static final String REGISTER_HIGHLIGHT_ENABLE_TOOL_TIP_TEXT = "Click, to enable or disable highlighting in Register windows";
    private static final String fontButtonText = "font";
    
    public SettingsHighlightingAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        (this.highlightDialog = new JDialog(Globals.getGui(), "Runtime Table Highlighting Colors and Fonts", true)).setContentPane(this.buildDialogPanel());
        this.highlightDialog.setDefaultCloseOperation(0);
        this.highlightDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we) {
                SettingsHighlightingAction.this.closeDialog();
            }
        });
        this.highlightDialog.pack();
        this.highlightDialog.setLocationRelativeTo(Globals.getGui());
        this.highlightDialog.setVisible(true);
    }
    
    private JPanel buildDialogPanel() {
        final JPanel contents = new JPanel(new BorderLayout(20, 20));
        contents.setBorder(new EmptyBorder(10, 10, 10, 10));
        final JPanel patches = new JPanel(new GridLayout(SettingsHighlightingAction.backgroundSettingPositions.length, 4, 2, 2));
        this.currentNondefaultBackground = new Color[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.currentNondefaultForeground = new Color[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.initialSettingsBackground = new Color[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.initialSettingsForeground = new Color[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.initialFont = new Font[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.currentFont = new Font[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.currentNondefaultFont = new Font[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.backgroundButtons = new JButton[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.foregroundButtons = new JButton[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.fontButtons = new JButton[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.defaultCheckBoxes = new JCheckBox[SettingsHighlightingAction.backgroundSettingPositions.length];
        this.samples = new JLabel[SettingsHighlightingAction.backgroundSettingPositions.length];
        for (int i = 0; i < SettingsHighlightingAction.backgroundSettingPositions.length; ++i) {
            this.backgroundButtons[i] = new ColorSelectButton();
            this.foregroundButtons[i] = new ColorSelectButton();
            this.fontButtons[i] = new JButton("font");
            this.defaultCheckBoxes[i] = new JCheckBox();
            this.samples[i] = new JLabel(" preview ");
            this.backgroundButtons[i].addActionListener(new BackgroundChanger(i));
            this.foregroundButtons[i].addActionListener(new ForegroundChanger(i));
            this.fontButtons[i].addActionListener(new FontChanger(i));
            this.defaultCheckBoxes[i].addItemListener(new DefaultChanger(i));
            this.samples[i].setToolTipText("Preview based on background and text color settings");
            this.backgroundButtons[i].setToolTipText("Click, to select background color");
            this.foregroundButtons[i].setToolTipText("Click, to select text color");
            this.fontButtons[i].setToolTipText("Click, to select text font");
            this.defaultCheckBoxes[i].setToolTipText("Check, to select default color (disables color select buttons)");
        }
        this.initializeButtonColors();
        for (int i = 0; i < SettingsHighlightingAction.backgroundSettingPositions.length; ++i) {
            patches.add(this.backgroundButtons[i]);
            patches.add(this.foregroundButtons[i]);
            patches.add(this.fontButtons[i]);
            patches.add(this.defaultCheckBoxes[i]);
        }
        final JPanel descriptions = new JPanel(new GridLayout(SettingsHighlightingAction.backgroundSettingPositions.length, 1, 2, 2));
        descriptions.add(new JLabel("Text Segment highlighting", 4));
        descriptions.add(new JLabel("Text Segment Delay Slot highlighting", 4));
        descriptions.add(new JLabel("Data Segment highlighting *", 4));
        descriptions.add(new JLabel("Register highlighting *", 4));
        descriptions.add(new JLabel("Even row normal", 4));
        descriptions.add(new JLabel("Odd row normal", 4));
        final JPanel sample = new JPanel(new GridLayout(SettingsHighlightingAction.backgroundSettingPositions.length, 1, 2, 2));
        for (int j = 0; j < SettingsHighlightingAction.backgroundSettingPositions.length; ++j) {
            sample.add(this.samples[j]);
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
        instructions.add(new JLabel("= use default colors (disables color selection buttons)"));
        final int spacer = 10;
        final Box mainArea = Box.createHorizontalBox();
        mainArea.add(Box.createHorizontalGlue());
        mainArea.add(descriptions);
        mainArea.add(Box.createHorizontalStrut(spacer));
        mainArea.add(Box.createHorizontalGlue());
        mainArea.add(Box.createHorizontalStrut(spacer));
        mainArea.add(sample);
        mainArea.add(Box.createHorizontalStrut(spacer));
        mainArea.add(Box.createHorizontalGlue());
        mainArea.add(Box.createHorizontalStrut(spacer));
        mainArea.add(patches);
        contents.add(mainArea, "East");
        contents.add(instructions, "North");
        final JPanel dataRegisterHighlightControl = new JPanel(new GridLayout(2, 1));
        (this.dataHighlightButton = new JButton()).setText(this.getHighlightControlText(this.currentDataHighlightSetting));
        this.dataHighlightButton.setToolTipText("Click, to enable or disable highlighting in Data Segment window");
        this.dataHighlightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsHighlightingAction.this.currentDataHighlightSetting = !SettingsHighlightingAction.this.currentDataHighlightSetting;
                SettingsHighlightingAction.this.dataHighlightButton.setText(SettingsHighlightingAction.this.getHighlightControlText(SettingsHighlightingAction.this.currentDataHighlightSetting));
            }
        });
        (this.registerHighlightButton = new JButton()).setText(this.getHighlightControlText(this.currentRegisterHighlightSetting));
        this.registerHighlightButton.setToolTipText("Click, to enable or disable highlighting in Register windows");
        this.registerHighlightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsHighlightingAction.this.currentRegisterHighlightSetting = !SettingsHighlightingAction.this.currentRegisterHighlightSetting;
                SettingsHighlightingAction.this.registerHighlightButton.setText(SettingsHighlightingAction.this.getHighlightControlText(SettingsHighlightingAction.this.currentRegisterHighlightSetting));
            }
        });
        final JPanel dataHighlightPanel = new JPanel(new FlowLayout(0));
        final JPanel registerHighlightPanel = new JPanel(new FlowLayout(0));
        dataHighlightPanel.add(new JLabel("* Data Segment highlighting is"));
        dataHighlightPanel.add(this.dataHighlightButton);
        registerHighlightPanel.add(new JLabel("* Register highlighting is"));
        registerHighlightPanel.add(this.registerHighlightButton);
        dataRegisterHighlightControl.setBorder(new LineBorder(Color.BLACK));
        dataRegisterHighlightControl.add(dataHighlightPanel);
        dataRegisterHighlightControl.add(registerHighlightPanel);
        final Box controlPanel = Box.createHorizontalBox();
        final JButton okButton = new JButton("Apply and Close");
        okButton.setToolTipText("Apply current settings and close dialog");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsHighlightingAction.this.setHighlightingSettings();
                SettingsHighlightingAction.this.closeDialog();
            }
        });
        final JButton applyButton = new JButton("Apply");
        applyButton.setToolTipText("Apply current settings now and leave dialog open");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsHighlightingAction.this.setHighlightingSettings();
            }
        });
        final JButton resetButton = new JButton("Reset");
        resetButton.setToolTipText("Reset to initial settings without applying");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsHighlightingAction.this.resetButtonColors();
            }
        });
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close dialog without applying current settings");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsHighlightingAction.this.closeDialog();
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
        final JPanel allControls = new JPanel(new GridLayout(2, 1));
        allControls.add(dataRegisterHighlightControl);
        allControls.add(controlPanel);
        contents.add(allControls, "South");
        return contents;
    }
    
    private String getHighlightControlText(final boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
    
    private void initializeButtonColors() {
        final Settings settings = Globals.getSettings();
        final LineBorder lineBorder = new LineBorder(Color.BLACK);
        for (int i = 0; i < SettingsHighlightingAction.backgroundSettingPositions.length; ++i) {
            final Color backgroundSetting = settings.getColorSettingByPosition(SettingsHighlightingAction.backgroundSettingPositions[i]);
            final Color foregroundSetting = settings.getColorSettingByPosition(SettingsHighlightingAction.foregroundSettingPositions[i]);
            final Font fontSetting = settings.getFontByPosition(SettingsHighlightingAction.fontSettingPositions[i]);
            this.backgroundButtons[i].setBackground(backgroundSetting);
            this.foregroundButtons[i].setBackground(foregroundSetting);
            this.fontButtons[i].setFont(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT);
            this.fontButtons[i].setMargin(new Insets(4, 4, 4, 4));
            this.initialFont[i] = (this.currentFont[i] = fontSetting);
            this.currentNondefaultBackground[i] = backgroundSetting;
            this.currentNondefaultForeground[i] = foregroundSetting;
            this.currentNondefaultFont[i] = fontSetting;
            this.initialSettingsBackground[i] = backgroundSetting;
            this.initialSettingsForeground[i] = foregroundSetting;
            this.samples[i].setOpaque(true);
            this.samples[i].setBorder(lineBorder);
            this.samples[i].setBackground(backgroundSetting);
            this.samples[i].setForeground(foregroundSetting);
            this.samples[i].setFont(fontSetting);
            final boolean usingDefaults = backgroundSetting.equals(settings.getDefaultColorSettingByPosition(SettingsHighlightingAction.backgroundSettingPositions[i])) && foregroundSetting.equals(settings.getDefaultColorSettingByPosition(SettingsHighlightingAction.foregroundSettingPositions[i])) && fontSetting.equals(settings.getDefaultFontByPosition(SettingsHighlightingAction.fontSettingPositions[i]));
            this.defaultCheckBoxes[i].setSelected(usingDefaults);
            this.backgroundButtons[i].setEnabled(!usingDefaults);
            this.foregroundButtons[i].setEnabled(!usingDefaults);
            this.fontButtons[i].setEnabled(!usingDefaults);
        }
        final boolean dataSegmentHighlighting = settings.getDataSegmentHighlighting();
        this.initialDataHighlightSetting = dataSegmentHighlighting;
        this.currentDataHighlightSetting = dataSegmentHighlighting;
        final boolean registersHighlighting = settings.getRegistersHighlighting();
        this.initialRegisterHighlightSetting = registersHighlighting;
        this.currentRegisterHighlightSetting = registersHighlighting;
    }
    
    private void setHighlightingSettings() {
        final Settings settings = Globals.getSettings();
        for (int i = 0; i < SettingsHighlightingAction.backgroundSettingPositions.length; ++i) {
            settings.setColorSettingByPosition(SettingsHighlightingAction.backgroundSettingPositions[i], this.backgroundButtons[i].getBackground());
            settings.setColorSettingByPosition(SettingsHighlightingAction.foregroundSettingPositions[i], this.foregroundButtons[i].getBackground());
            settings.setFontByPosition(SettingsHighlightingAction.fontSettingPositions[i], this.samples[i].getFont());
        }
        settings.setDataSegmentHighlighting(this.currentDataHighlightSetting);
        settings.setRegistersHighlighting(this.currentRegisterHighlightSetting);
        final ExecutePane executePane = Globals.getGui().getMainPane().getExecutePane();
        executePane.getRegistersWindow().refresh();
        executePane.getCoprocessor0Window().refresh();
        executePane.getCoprocessor1Window().refresh();
        if (executePane.getTextSegmentWindow().getContentPane().getComponentCount() > 0) {
            executePane.getDataSegmentWindow().updateValues();
            executePane.getTextSegmentWindow().highlightStepAtPC();
        }
    }
    
    private void resetButtonColors() {
        final Settings settings = Globals.getSettings();
        this.dataHighlightButton.setText(this.getHighlightControlText(this.initialDataHighlightSetting));
        this.registerHighlightButton.setText(this.getHighlightControlText(this.initialRegisterHighlightSetting));
        for (int i = 0; i < SettingsHighlightingAction.backgroundSettingPositions.length; ++i) {
            final Color backgroundSetting = this.initialSettingsBackground[i];
            final Color foregroundSetting = this.initialSettingsForeground[i];
            final Font fontSetting = this.initialFont[i];
            this.backgroundButtons[i].setBackground(backgroundSetting);
            this.foregroundButtons[i].setBackground(foregroundSetting);
            this.samples[i].setBackground(backgroundSetting);
            this.samples[i].setForeground(foregroundSetting);
            this.samples[i].setFont(fontSetting);
            final boolean usingDefaults = backgroundSetting.equals(settings.getDefaultColorSettingByPosition(SettingsHighlightingAction.backgroundSettingPositions[i])) && foregroundSetting.equals(settings.getDefaultColorSettingByPosition(SettingsHighlightingAction.foregroundSettingPositions[i])) && fontSetting.equals(settings.getDefaultFontByPosition(SettingsHighlightingAction.fontSettingPositions[i]));
            this.defaultCheckBoxes[i].setSelected(usingDefaults);
            this.backgroundButtons[i].setEnabled(!usingDefaults);
            this.foregroundButtons[i].setEnabled(!usingDefaults);
            this.fontButtons[i].setEnabled(!usingDefaults);
        }
    }
    
    private void closeDialog() {
        this.highlightDialog.setVisible(false);
        this.highlightDialog.dispose();
    }
    
    static {
        backgroundSettingPositions = new int[] { 4, 6, 8, 10, 0, 2 };
        foregroundSettingPositions = new int[] { 5, 7, 9, 11, 1, 3 };
        fontSettingPositions = new int[] { 3, 4, 5, 6, 1, 2 };
    }
    
    private class BackgroundChanger implements ActionListener
    {
        private int position;
        
        public BackgroundChanger(final int pos) {
            this.position = pos;
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            final JButton button = (JButton)e.getSource();
            final Color newColor = JColorChooser.showDialog(null, "Set Background Color", button.getBackground());
            if (newColor != null) {
                button.setBackground(newColor);
                SettingsHighlightingAction.this.currentNondefaultBackground[this.position] = newColor;
                SettingsHighlightingAction.this.samples[this.position].setBackground(newColor);
            }
        }
    }
    
    private class ForegroundChanger implements ActionListener
    {
        private int position;
        
        public ForegroundChanger(final int pos) {
            this.position = pos;
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            final JButton button = (JButton)e.getSource();
            final Color newColor = JColorChooser.showDialog(null, "Set Text Color", button.getBackground());
            if (newColor != null) {
                button.setBackground(newColor);
                SettingsHighlightingAction.this.currentNondefaultForeground[this.position] = newColor;
                SettingsHighlightingAction.this.samples[this.position].setForeground(newColor);
            }
        }
    }
    
    private class FontChanger implements ActionListener
    {
        private int position;
        
        public FontChanger(final int pos) {
            this.position = pos;
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            final JButton button = (JButton)e.getSource();
            final FontSettingDialog fontDialog = new FontSettingDialog(null, "Select Text Font", SettingsHighlightingAction.this.samples[this.position].getFont());
            final Font newFont = fontDialog.showDialog();
            if (newFont != null) {
                SettingsHighlightingAction.this.samples[this.position].setFont(newFont);
            }
        }
    }
    
    private class DefaultChanger implements ItemListener
    {
        private int position;
        
        public DefaultChanger(final int pos) {
            this.position = pos;
        }
        
        @Override
        public void itemStateChanged(final ItemEvent e) {
            Color newBackground = null;
            Color newForeground = null;
            Font newFont = null;
            if (e.getStateChange() == 1) {
                SettingsHighlightingAction.this.backgroundButtons[this.position].setEnabled(false);
                SettingsHighlightingAction.this.foregroundButtons[this.position].setEnabled(false);
                SettingsHighlightingAction.this.fontButtons[this.position].setEnabled(false);
                newBackground = Globals.getSettings().getDefaultColorSettingByPosition(SettingsHighlightingAction.backgroundSettingPositions[this.position]);
                newForeground = Globals.getSettings().getDefaultColorSettingByPosition(SettingsHighlightingAction.foregroundSettingPositions[this.position]);
                newFont = Globals.getSettings().getDefaultFontByPosition(SettingsHighlightingAction.fontSettingPositions[this.position]);
                SettingsHighlightingAction.this.currentNondefaultBackground[this.position] = SettingsHighlightingAction.this.backgroundButtons[this.position].getBackground();
                SettingsHighlightingAction.this.currentNondefaultForeground[this.position] = SettingsHighlightingAction.this.foregroundButtons[this.position].getBackground();
                SettingsHighlightingAction.this.currentNondefaultFont[this.position] = SettingsHighlightingAction.this.samples[this.position].getFont();
            }
            else {
                SettingsHighlightingAction.this.backgroundButtons[this.position].setEnabled(true);
                SettingsHighlightingAction.this.foregroundButtons[this.position].setEnabled(true);
                SettingsHighlightingAction.this.fontButtons[this.position].setEnabled(true);
                newBackground = SettingsHighlightingAction.this.currentNondefaultBackground[this.position];
                newForeground = SettingsHighlightingAction.this.currentNondefaultForeground[this.position];
                newFont = SettingsHighlightingAction.this.currentNondefaultFont[this.position];
            }
            SettingsHighlightingAction.this.backgroundButtons[this.position].setBackground(newBackground);
            SettingsHighlightingAction.this.foregroundButtons[this.position].setBackground(newForeground);
            SettingsHighlightingAction.this.samples[this.position].setBackground(newBackground);
            SettingsHighlightingAction.this.samples[this.position].setForeground(newForeground);
            SettingsHighlightingAction.this.samples[this.position].setFont(newFont);
        }
    }
    
    private class FontSettingDialog extends AbstractFontSettingDialog
    {
        private boolean resultOK;
        
        public FontSettingDialog(final Frame owner, final String title, final Font currentFont) {
            super(owner, title, true, currentFont);
        }
        
        private Font showDialog() {
            this.setVisible(this.resultOK = true);
            return this.resultOK ? this.getFont() : null;
        }
        
        @Override
        protected void closeDialog() {
            this.setVisible(false);
        }
        
        private void performOK() {
            this.resultOK = true;
        }
        
        private void performCancel() {
            this.resultOK = false;
        }
        
        @Override
        protected Component buildControlPanel() {
            final Box controlPanel = Box.createHorizontalBox();
            final JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    FontSettingDialog.this.performOK();
                    FontSettingDialog.this.closeDialog();
                }
            });
            final JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    FontSettingDialog.this.performCancel();
                    FontSettingDialog.this.closeDialog();
                }
            });
            final JButton resetButton = new JButton("Reset");
            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    FontSettingDialog.this.reset();
                }
            });
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(okButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(cancelButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(resetButton);
            controlPanel.add(Box.createHorizontalGlue());
            return controlPanel;
        }
        
        @Override
        protected void apply(final Font font) {
        }
    }
}
