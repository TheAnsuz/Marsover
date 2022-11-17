

package mars.venus;

import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import javax.swing.ListCellRenderer;
import java.util.Vector;
import mars.util.EditorFont;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Container;
import java.awt.Component;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JSlider;
import javax.swing.JComboBox;
import javax.swing.JDialog;

public abstract class AbstractFontSettingDialog extends JDialog
{
    JDialog editorDialog;
    JComboBox fontFamilySelector;
    JComboBox fontStyleSelector;
    JSlider fontSizeSelector;
    JSpinner fontSizeSpinSelector;
    JLabel fontSample;
    protected Font currentFont;
    String initialFontFamily;
    String initialFontStyle;
    String initialFontSize;
    private static String SEPARATOR;
    
    public AbstractFontSettingDialog(final Frame owner, final String title, final boolean modality, final Font currentFont) {
        super(owner, title, modality);
        this.currentFont = currentFont;
        final JPanel overallPanel = new JPanel(new BorderLayout());
        overallPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        overallPanel.add(this.buildDialogPanel(), "Center");
        overallPanel.add(this.buildControlPanel(), "South");
        this.setContentPane(overallPanel);
        this.setDefaultCloseOperation(0);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we) {
                AbstractFontSettingDialog.this.closeDialog();
            }
        });
        this.pack();
        this.setLocationRelativeTo(owner);
    }
    
    protected JPanel buildDialogPanel() {
        final JPanel contents = new JPanel(new BorderLayout(20, 20));
        contents.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.initialFontFamily = this.currentFont.getFamily();
        this.initialFontStyle = EditorFont.styleIntToStyleString(this.currentFont.getStyle());
        this.initialFontSize = EditorFont.sizeIntToSizeString(this.currentFont.getSize());
        final String[] commonFontFamilies = EditorFont.getCommonFamilies();
        final String[] allFontFamilies = EditorFont.getAllFamilies();
        final String[][] fullList = { commonFontFamilies, allFontFamilies };
        (this.fontFamilySelector = new JComboBox(this.makeVectorData(fullList))).setRenderer(new ComboBoxRenderer());
        this.fontFamilySelector.addActionListener(new BlockComboListener(this.fontFamilySelector));
        this.fontFamilySelector.setSelectedItem(this.currentFont.getFamily());
        this.fontFamilySelector.setEditable(false);
        this.fontFamilySelector.setMaximumRowCount(commonFontFamilies.length);
        this.fontFamilySelector.setToolTipText("Short list of common font families followed by complete list.");
        final String[] fontStyles = EditorFont.getFontStyleStrings();
        (this.fontStyleSelector = new JComboBox((E[])fontStyles)).setSelectedItem(EditorFont.styleIntToStyleString(this.currentFont.getStyle()));
        this.fontStyleSelector.setEditable(false);
        this.fontStyleSelector.setToolTipText("List of available font styles.");
        (this.fontSizeSelector = new JSlider(6, 72, this.currentFont.getSize())).setToolTipText("Use slider to select font size from 6 to 72.");
        this.fontSizeSelector.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                final Integer value = new Integer(((JSlider)e.getSource()).getValue());
                AbstractFontSettingDialog.this.fontSizeSpinSelector.setValue(value);
                AbstractFontSettingDialog.this.fontSample.setFont(AbstractFontSettingDialog.this.getFont());
            }
        });
        final SpinnerNumberModel fontSizeSpinnerModel = new SpinnerNumberModel(this.currentFont.getSize(), 6, 72, 1);
        (this.fontSizeSpinSelector = new JSpinner(fontSizeSpinnerModel)).setToolTipText("Current font size in points.");
        this.fontSizeSpinSelector.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                final Object value = ((JSpinner)e.getSource()).getValue();
                AbstractFontSettingDialog.this.fontSizeSelector.setValue((int)value);
                AbstractFontSettingDialog.this.fontSample.setFont(AbstractFontSettingDialog.this.getFont());
            }
        });
        final ActionListener updateSample = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                AbstractFontSettingDialog.this.fontSample.setFont(AbstractFontSettingDialog.this.getFont());
            }
        };
        this.fontFamilySelector.addActionListener(updateSample);
        this.fontStyleSelector.addActionListener(updateSample);
        final JPanel familyStyleComponents = new JPanel(new GridLayout(2, 2, 4, 4));
        familyStyleComponents.add(new JLabel("Font Family"));
        familyStyleComponents.add(new JLabel("Font Style"));
        familyStyleComponents.add(this.fontFamilySelector);
        familyStyleComponents.add(this.fontStyleSelector);
        (this.fontSample = new JLabel("Sample of this font", 0)).setBorder(new LineBorder(Color.BLACK));
        this.fontSample.setFont(this.getFont());
        this.fontSample.setToolTipText("Dynamically updated font sample based on current settings");
        final JPanel sizeComponents = new JPanel();
        sizeComponents.add(new JLabel("Font Size "));
        sizeComponents.add(this.fontSizeSelector);
        sizeComponents.add(this.fontSizeSpinSelector);
        final JPanel sizeAndSample = new JPanel(new GridLayout(2, 1, 4, 8));
        sizeAndSample.add(sizeComponents);
        sizeAndSample.add(this.fontSample);
        contents.add(familyStyleComponents, "North");
        contents.add(sizeAndSample, "Center");
        return contents;
    }
    
    protected abstract Component buildControlPanel();
    
    @Override
    public Font getFont() {
        return EditorFont.createFontFromStringValues((String)this.fontFamilySelector.getSelectedItem(), (String)this.fontStyleSelector.getSelectedItem(), this.fontSizeSpinSelector.getValue().toString());
    }
    
    protected void performApply() {
        this.apply(this.getFont());
    }
    
    protected void closeDialog() {
        this.setVisible(false);
        this.dispose();
    }
    
    protected void reset() {
        this.fontFamilySelector.setSelectedItem(this.initialFontFamily);
        this.fontStyleSelector.setSelectedItem(this.initialFontStyle);
        this.fontSizeSelector.setValue(EditorFont.sizeStringToSizeInt(this.initialFontSize));
        this.fontSizeSpinSelector.setValue(new Integer(EditorFont.sizeStringToSizeInt(this.initialFontSize)));
    }
    
    protected abstract void apply(final Font p0);
    
    private Vector makeVectorData(final String[][] str) {
        boolean needSeparator = false;
        final Vector data = new Vector();
        for (int i = 0; i < str.length; ++i) {
            if (needSeparator) {
                data.addElement(AbstractFontSettingDialog.SEPARATOR);
            }
            for (int j = 0; j < str[i].length; ++j) {
                data.addElement(str[i][j]);
                needSeparator = true;
            }
        }
        return data;
    }
    
    static {
        AbstractFontSettingDialog.SEPARATOR = "___SEPARATOR____";
    }
    
    private class ComboBoxRenderer extends JLabel implements ListCellRenderer
    {
        JSeparator separator;
        
        public ComboBoxRenderer() {
            this.setOpaque(true);
            this.setBorder(new EmptyBorder(1, 1, 1, 1));
            this.separator = new JSeparator(0);
        }
        
        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            final String str = (value == null) ? "" : value.toString();
            if (AbstractFontSettingDialog.SEPARATOR.equals(str)) {
                return this.separator;
            }
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            }
            else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setFont(list.getFont());
            this.setText(str);
            return this;
        }
    }
    
    private class BlockComboListener implements ActionListener
    {
        JComboBox combo;
        Object currentItem;
        
        BlockComboListener(final JComboBox combo) {
            (this.combo = combo).setSelectedIndex(0);
            this.currentItem = combo.getSelectedItem();
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            final String tempItem = (String)this.combo.getSelectedItem();
            if (AbstractFontSettingDialog.SEPARATOR.equals(tempItem)) {
                this.combo.setSelectedItem(this.currentItem);
            }
            else {
                this.currentItem = tempItem;
            }
        }
    }
}
