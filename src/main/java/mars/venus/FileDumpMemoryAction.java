

package mars.venus;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import mars.mips.dump.DumpFormat;
import javax.swing.Box;
import javax.swing.ListCellRenderer;
import mars.mips.dump.DumpFormatLoader;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.awt.Label;
import mars.util.Binary;
import mars.mips.hardware.AddressErrorException;
import mars.util.MemoryDump;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Component;
import mars.Globals;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;

public class FileDumpMemoryAction extends GuiAction
{
    private JDialog dumpDialog;
    private static final String title = "Dump Memory To File";
    private String[] segmentArray;
    private int[] baseAddressArray;
    private int[] limitAddressArray;
    private int[] highAddressArray;
    private String[] segmentListArray;
    private int[] segmentListBaseArray;
    private int[] segmentListHighArray;
    private JComboBox segmentListSelector;
    private JComboBox formatListSelector;
    
    public FileDumpMemoryAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.dumpMemory();
    }
    
    private boolean dumpMemory() {
        (this.dumpDialog = this.createDumpDialog()).pack();
        this.dumpDialog.setLocationRelativeTo(Globals.getGui());
        this.dumpDialog.setVisible(true);
        return true;
    }
    
    private JDialog createDumpDialog() {
        final JDialog dumpDialog = new JDialog(Globals.getGui(), "Dump Memory To File", true);
        dumpDialog.setContentPane(this.buildDialogPanel());
        dumpDialog.setDefaultCloseOperation(0);
        dumpDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we) {
                FileDumpMemoryAction.this.closeDialog();
            }
        });
        return dumpDialog;
    }
    
    private JPanel buildDialogPanel() {
        final JPanel contents = new JPanel(new BorderLayout(20, 20));
        contents.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.segmentArray = MemoryDump.getSegmentNames();
        this.baseAddressArray = MemoryDump.getBaseAddresses(this.segmentArray);
        this.limitAddressArray = MemoryDump.getLimitAddresses(this.segmentArray);
        this.highAddressArray = new int[this.segmentArray.length];
        this.segmentListArray = new String[this.segmentArray.length];
        this.segmentListBaseArray = new int[this.segmentArray.length];
        this.segmentListHighArray = new int[this.segmentArray.length];
        int segmentCount = 0;
        for (int i = 0; i < this.segmentArray.length; ++i) {
            try {
                this.highAddressArray[i] = Globals.memory.getAddressOfFirstNull(this.baseAddressArray[i], this.limitAddressArray[i]) - 4;
            }
            catch (AddressErrorException aee) {
                this.highAddressArray[i] = this.baseAddressArray[i] - 4;
            }
            if (this.highAddressArray[i] >= this.baseAddressArray[i]) {
                this.segmentListBaseArray[segmentCount] = this.baseAddressArray[i];
                this.segmentListHighArray[segmentCount] = this.highAddressArray[i];
                this.segmentListArray[segmentCount] = this.segmentArray[i] + " (" + Binary.intToHexString(this.baseAddressArray[i]) + " - " + Binary.intToHexString(this.highAddressArray[i]) + ")";
                ++segmentCount;
            }
        }
        if (segmentCount == 0) {
            contents.add(new Label("There is nothing to dump!"), "North");
            final JButton OKButton = new JButton("OK");
            OKButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    FileDumpMemoryAction.this.closeDialog();
                }
            });
            contents.add(OKButton, "South");
            return contents;
        }
        if (segmentCount < this.segmentListArray.length) {
            final String[] tempArray = new String[segmentCount];
            System.arraycopy(this.segmentListArray, 0, tempArray, 0, segmentCount);
            this.segmentListArray = tempArray;
        }
        (this.segmentListSelector = new JComboBox((E[])this.segmentListArray)).setSelectedIndex(0);
        final JPanel segmentPanel = new JPanel(new BorderLayout());
        segmentPanel.add(new Label("Memory Segment"), "North");
        segmentPanel.add(this.segmentListSelector);
        contents.add(segmentPanel, "West");
        final ArrayList dumpFormats = new DumpFormatLoader().loadDumpFormats();
        (this.formatListSelector = new JComboBox((E[])dumpFormats.toArray())).setRenderer(new DumpFormatComboBoxRenderer(this.formatListSelector));
        this.formatListSelector.setSelectedIndex(0);
        final JPanel formatPanel = new JPanel(new BorderLayout());
        formatPanel.add(new Label("Dump Format"), "North");
        formatPanel.add(this.formatListSelector);
        contents.add(formatPanel, "East");
        final Box controlPanel = Box.createHorizontalBox();
        final JButton dumpButton = new JButton("Dump To File...");
        dumpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (FileDumpMemoryAction.this.performDump(FileDumpMemoryAction.this.segmentListBaseArray[FileDumpMemoryAction.this.segmentListSelector.getSelectedIndex()], FileDumpMemoryAction.this.segmentListHighArray[FileDumpMemoryAction.this.segmentListSelector.getSelectedIndex()], (DumpFormat)FileDumpMemoryAction.this.formatListSelector.getSelectedItem())) {
                    FileDumpMemoryAction.this.closeDialog();
                }
            }
        });
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                FileDumpMemoryAction.this.closeDialog();
            }
        });
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(dumpButton);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(cancelButton);
        controlPanel.add(Box.createHorizontalGlue());
        contents.add(controlPanel, "South");
        return contents;
    }
    
    private boolean performDump(final int firstAddress, final int lastAddress, final DumpFormat format) {
        File theFile = null;
        JFileChooser saveDialog = null;
        boolean operationOK = false;
        saveDialog = new JFileChooser(this.mainUI.getEditor().getCurrentSaveDirectory());
        saveDialog.setDialogTitle("Dump Memory To File");
        while (!operationOK) {
            final int decision = saveDialog.showSaveDialog(this.mainUI);
            if (decision != 0) {
                return false;
            }
            theFile = saveDialog.getSelectedFile();
            operationOK = true;
            if (theFile.exists()) {
                final int overwrite = JOptionPane.showConfirmDialog(this.mainUI, "File " + theFile.getName() + " already exists.  Do you wish to overwrite it?", "Overwrite existing file?", 1, 2);
                switch (overwrite) {
                    case 0: {
                        operationOK = true;
                        break;
                    }
                    case 1: {
                        operationOK = false;
                        break;
                    }
                    case 2: {
                        return false;
                    }
                    default: {
                        return false;
                    }
                }
            }
            if (!operationOK) {
                continue;
            }
            try {
                format.dumpMemoryRange(theFile, firstAddress, lastAddress);
            }
            catch (AddressErrorException ex) {}
            catch (IOException ex2) {}
        }
        return true;
    }
    
    private void closeDialog() {
        this.dumpDialog.setVisible(false);
        this.dumpDialog.dispose();
    }
    
    private class DumpFormatComboBoxRenderer extends BasicComboBoxRenderer
    {
        private JComboBox myMaster;
        
        public DumpFormatComboBoxRenderer(final JComboBox myMaster) {
            this.myMaster = myMaster;
        }
        
        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            this.setToolTipText(value.toString());
            if (index >= 0 && this.myMaster.getItemAt(index).getDescription() != null) {
                this.setToolTipText(this.myMaster.getItemAt(index).getDescription());
            }
            return this;
        }
    }
}
