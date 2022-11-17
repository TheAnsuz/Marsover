

package mars.venus;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.Box;
import java.awt.event.ActionListener;
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
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

public class SettingsExceptionHandlerAction extends GuiAction
{
    JDialog exceptionHandlerDialog;
    JCheckBox exceptionHandlerSetting;
    JButton exceptionHandlerSelectionButton;
    JTextField exceptionHandlerDisplay;
    boolean initialSelected;
    String initialPathname;
    
    public SettingsExceptionHandlerAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.initialSelected = Globals.getSettings().getExceptionHandlerEnabled();
        this.initialPathname = Globals.getSettings().getExceptionHandler();
        (this.exceptionHandlerDialog = new JDialog(Globals.getGui(), "Exception Handler", true)).setContentPane(this.buildDialogPanel());
        this.exceptionHandlerDialog.setDefaultCloseOperation(0);
        this.exceptionHandlerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we) {
                SettingsExceptionHandlerAction.this.closeDialog();
            }
        });
        this.exceptionHandlerDialog.pack();
        this.exceptionHandlerDialog.setLocationRelativeTo(Globals.getGui());
        this.exceptionHandlerDialog.setVisible(true);
    }
    
    private JPanel buildDialogPanel() {
        final JPanel contents = new JPanel(new BorderLayout(20, 20));
        contents.setBorder(new EmptyBorder(10, 10, 10, 10));
        (this.exceptionHandlerSetting = new JCheckBox("Include this exception handler file in all assemble operations")).setSelected(Globals.getSettings().getExceptionHandlerEnabled());
        this.exceptionHandlerSetting.addActionListener(new ExceptionHandlerSettingAction());
        contents.add(this.exceptionHandlerSetting, "North");
        final JPanel specifyHandlerFile = new JPanel();
        (this.exceptionHandlerSelectionButton = new JButton("Browse")).setEnabled(this.exceptionHandlerSetting.isSelected());
        this.exceptionHandlerSelectionButton.addActionListener(new ExceptionHandlerSelectionAction());
        (this.exceptionHandlerDisplay = new JTextField(Globals.getSettings().getExceptionHandler(), 30)).setEditable(false);
        this.exceptionHandlerDisplay.setEnabled(this.exceptionHandlerSetting.isSelected());
        specifyHandlerFile.add(this.exceptionHandlerSelectionButton);
        specifyHandlerFile.add(this.exceptionHandlerDisplay);
        contents.add(specifyHandlerFile, "Center");
        final Box controlPanel = Box.createHorizontalBox();
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsExceptionHandlerAction.this.performOK();
                SettingsExceptionHandlerAction.this.closeDialog();
            }
        });
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SettingsExceptionHandlerAction.this.closeDialog();
            }
        });
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(okButton);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(cancelButton);
        controlPanel.add(Box.createHorizontalGlue());
        contents.add(controlPanel, "South");
        return contents;
    }
    
    private void performOK() {
        final boolean finalSelected = this.exceptionHandlerSetting.isSelected();
        final String finalPathname = this.exceptionHandlerDisplay.getText();
        if (this.initialSelected != finalSelected || (this.initialPathname == null && finalPathname != null) || (this.initialPathname != null && !this.initialPathname.equals(finalPathname))) {
            Globals.getSettings().setExceptionHandlerEnabled(finalSelected);
            if (finalSelected) {
                Globals.getSettings().setExceptionHandler(finalPathname);
            }
        }
    }
    
    private void closeDialog() {
        this.exceptionHandlerDialog.setVisible(false);
        this.exceptionHandlerDialog.dispose();
    }
    
    private class ExceptionHandlerSettingAction implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final boolean selected = ((JCheckBox)e.getSource()).isSelected();
            SettingsExceptionHandlerAction.this.exceptionHandlerSelectionButton.setEnabled(selected);
            SettingsExceptionHandlerAction.this.exceptionHandlerDisplay.setEnabled(selected);
        }
    }
    
    private class ExceptionHandlerSelectionAction implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent e) {
            final JFileChooser chooser = new JFileChooser();
            String pathname = Globals.getSettings().getExceptionHandler();
            if (pathname != null) {
                final File file = new File(pathname);
                if (file.exists()) {
                    chooser.setSelectedFile(file);
                }
            }
            final int result = chooser.showOpenDialog(Globals.getGui());
            if (result == 0) {
                pathname = chooser.getSelectedFile().getPath();
                SettingsExceptionHandlerAction.this.exceptionHandlerDisplay.setText(pathname);
            }
        }
    }
}
