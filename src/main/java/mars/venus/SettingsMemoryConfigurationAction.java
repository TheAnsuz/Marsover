

package mars.venus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import mars.Globals;
import mars.mips.hardware.MemoryConfiguration;
import mars.mips.hardware.MemoryConfigurations;
import mars.simulator.Simulator;
import mars.util.Binary;

public class SettingsMemoryConfigurationAction extends GuiAction
{
    JDialog configDialog;
    JComboBox fontFamilySelector;
    JComboBox fontStyleSelector;
    JSlider fontSizeSelector;
    JTextField fontSizeDisplay;
    SettingsMemoryConfigurationAction thisAction;
    String initialFontFamily;
    String initialFontStyle;
    String initialFontSize;
    
    public SettingsMemoryConfigurationAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
        this.thisAction = this;
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        (this.configDialog = new MemoryConfigurationDialog(Globals.getGui(), "MIPS Memory Configuration", true)).setVisible(true);
    }
    
    private class MemoryConfigurationDialog extends JDialog implements ActionListener
    {
        JTextField[] addressDisplay;
        JLabel[] nameDisplay;
        ConfigurationButton selectedConfigurationButton;
        ConfigurationButton initialConfigurationButton;
        
        public MemoryConfigurationDialog(final Frame owner, final String title, final boolean modality) {
            super(owner, title, modality);
            this.setContentPane(this.buildDialogPanel());
            this.setDefaultCloseOperation(0);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent we) {
                    MemoryConfigurationDialog.this.performClose();
                }
            });
            this.pack();
            this.setLocationRelativeTo(owner);
        }
        
        private JPanel buildDialogPanel() {
            final JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            final JPanel configInfo = new JPanel(new FlowLayout());
            MemoryConfigurations.buildConfigurationCollection();
            configInfo.add(this.buildConfigChooser());
            configInfo.add(this.buildConfigDisplay());
            dialogPanel.add(configInfo);
            dialogPanel.add(this.buildControlPanel(), "South");
            return dialogPanel;
        }
        
        private Component buildConfigChooser() {
            final JPanel chooserPanel = new JPanel(new GridLayout(4, 1));
            final ButtonGroup choices = new ButtonGroup();
            final Iterator<MemoryConfiguration> configurationsIterator = MemoryConfigurations.getConfigurationsIterator();
            while (configurationsIterator.hasNext()) {
                final MemoryConfiguration config = configurationsIterator.next();
                final ConfigurationButton button = new ConfigurationButton(config);
                button.addActionListener(this);
                if (button.isSelected()) {
                    this.selectedConfigurationButton = button;
                    this.initialConfigurationButton = button;
                }
                choices.add(button);
                chooserPanel.add(button);
            }
            chooserPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Configuration"));
            return chooserPanel;
        }
        
        private Component buildConfigDisplay() {
            final JPanel displayPanel = new JPanel();
            final MemoryConfiguration config = MemoryConfigurations.getCurrentConfiguration();
            final String[] configurationItemNames = config.getConfigurationItemNames();
            final int numItems = configurationItemNames.length;
            final JPanel namesPanel = new JPanel(new GridLayout(numItems, 1));
            final JPanel valuesPanel = new JPanel(new GridLayout(numItems, 1));
            final Font monospaced = new Font("Monospaced", 0, 12);
            this.nameDisplay = new JLabel[numItems];
            this.addressDisplay = new JTextField[numItems];
            for (int i = 0; i < numItems; ++i) {
                this.nameDisplay[i] = new JLabel();
                (this.addressDisplay[i] = new JTextField()).setEditable(false);
                this.addressDisplay[i].setFont(monospaced);
            }
            for (int i = this.addressDisplay.length - 1; i >= 0; --i) {
                namesPanel.add(this.nameDisplay[i]);
                valuesPanel.add(this.addressDisplay[i]);
            }
            this.setConfigDisplay(config);
            final Box columns = Box.createHorizontalBox();
            columns.add(valuesPanel);
            columns.add(Box.createHorizontalStrut(6));
            columns.add(namesPanel);
            displayPanel.add(columns);
            return displayPanel;
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            final MemoryConfiguration config = ((ConfigurationButton)e.getSource()).getConfiguration();
            this.setConfigDisplay(config);
            this.selectedConfigurationButton = (ConfigurationButton)e.getSource();
        }
        
        private Component buildControlPanel() {
            final Box controlPanel = Box.createHorizontalBox();
            final JButton okButton = new JButton("Apply and Close");
            okButton.setToolTipText("Apply current settings and close dialog");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    MemoryConfigurationDialog.this.performApply();
                    MemoryConfigurationDialog.this.performClose();
                }
            });
            final JButton applyButton = new JButton("Apply");
            applyButton.setToolTipText("Apply current settings now and leave dialog open");
            applyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    MemoryConfigurationDialog.this.performApply();
                }
            });
            final JButton cancelButton = new JButton("Cancel");
            cancelButton.setToolTipText("Close dialog without applying current settings");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    MemoryConfigurationDialog.this.performClose();
                }
            });
            final JButton resetButton = new JButton("Reset");
            resetButton.setToolTipText("Reset to initial settings without applying");
            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    MemoryConfigurationDialog.this.performReset();
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
            return controlPanel;
        }
        
        private void performApply() {
            if (MemoryConfigurations.setCurrentConfiguration(this.selectedConfigurationButton.getConfiguration())) {
                Globals.getSettings().setMemoryConfiguration(this.selectedConfigurationButton.getConfiguration().getConfigurationIdentifier());
                Globals.getGui().getRegistersPane().getRegistersWindow().clearHighlighting();
                Globals.getGui().getRegistersPane().getRegistersWindow().updateRegisters();
                Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().updateBaseAddressComboBox();
                if (FileStatus.get() == 5 || FileStatus.get() == 6 || FileStatus.get() == 7) {
                    if (FileStatus.get() == 6) {
                        Simulator.getInstance().stopExecution(SettingsMemoryConfigurationAction.this.thisAction);
                    }
                    Globals.getGui().getRunAssembleAction().actionPerformed(null);
                }
            }
        }
        
        private void performClose() {
            this.setVisible(false);
            this.dispose();
        }
        
        private void performReset() {
            (this.selectedConfigurationButton = this.initialConfigurationButton).setSelected(true);
            this.setConfigDisplay(this.selectedConfigurationButton.getConfiguration());
        }
        
        private void setConfigDisplay(final MemoryConfiguration config) {
            final String[] configurationItemNames = config.getConfigurationItemNames();
            final int[] configurationItemValues = config.getConfigurationItemValues();
            final TreeMap treeSortedByAddress = new TreeMap();
            for (int i = 0; i < configurationItemValues.length; ++i) {
                treeSortedByAddress.put(Binary.intToHexString(configurationItemValues[i]) + configurationItemNames[i], configurationItemNames[i]);
            }
            final Iterator<Map.Entry<String,String>> setSortedByAddress = treeSortedByAddress.entrySet().iterator();
            final int addressStringLength = Binary.intToHexString(configurationItemValues[0]).length();
            for (int j = 0; j < configurationItemValues.length; ++j) {
                final Map.Entry<String,String> pair = setSortedByAddress.next();
                this.nameDisplay[j].setText(pair.getValue());
                this.addressDisplay[j].setText(pair.getKey().substring(0, addressStringLength));
            }
        }
    }
    
    private class ConfigurationButton extends JRadioButton
    {
        private final MemoryConfiguration configuration;
        
        public ConfigurationButton(final MemoryConfiguration config) {
            super(config.getConfigurationName(), config == MemoryConfigurations.getCurrentConfiguration());
            this.configuration = config;
        }
        
        public MemoryConfiguration getConfiguration() {
            return this.configuration;
        }
    }
}
