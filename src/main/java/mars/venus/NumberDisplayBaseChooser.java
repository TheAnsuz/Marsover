

package mars.venus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import mars.Globals;
import mars.util.Binary;

public class NumberDisplayBaseChooser extends JCheckBox
{
    public static final int DECIMAL = 10;
    public static final int HEXADECIMAL = 16;
    public static final int ASCII = 0;
    private int base;
    private JCheckBoxMenuItem settingMenuItem;
    
    public NumberDisplayBaseChooser(final String text, final boolean displayInHex) {
        super(text, displayInHex);
        this.base = getBase(displayInHex);
        this.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent ie) {
                final NumberDisplayBaseChooser choose = (NumberDisplayBaseChooser)ie.getItem();
                if (ie.getStateChange() == 1) {
                    choose.setBase(16);
                }
                else {
                    choose.setBase(10);
                }
                if (NumberDisplayBaseChooser.this.settingMenuItem != null) {
                    NumberDisplayBaseChooser.this.settingMenuItem.setSelected(choose.isSelected());
                    final ActionListener[] listeners = NumberDisplayBaseChooser.this.settingMenuItem.getActionListeners();
                    final ActionEvent event = new ActionEvent(NumberDisplayBaseChooser.this.settingMenuItem, 0, "chooser");
                    for (int i = 0; i < listeners.length; ++i) {
                        listeners[i].actionPerformed(event);
                    }
                }
                Globals.getGui().getMainPane().getExecutePane().numberDisplayBaseChanged(choose);
            }
        });
    }
    
    public int getBase() {
        return this.base;
    }
    
    public void setBase(final int newBase) {
        if (newBase == 10 || newBase == 16) {
            this.base = newBase;
        }
    }
    
    public static String formatUnsignedInteger(final int value, final int base) {
        if (base == 16) {
            return Binary.intToHexString(value);
        }
        return Binary.unsignedIntToIntString(value);
    }
    
    public static String formatNumber(final int value, final int base) {
        String result = null;
        switch (base) {
            case 16: {
                result = Binary.intToHexString(value);
                break;
            }
            case 10: {
                result = Integer.toString(value);
                break;
            }
            case 0: {
                result = Binary.intToAscii(value);
                break;
            }
            default: {
                result = Integer.toString(value);
                break;
            }
        }
        return result;
    }
    
    public static String formatNumber(final float value, final int base) {
        if (base == 16) {
            return Binary.intToHexString(Float.floatToIntBits(value));
        }
        return Float.toString(value);
    }
    
    public static String formatNumber(final double value, final int base) {
        if (base == 16) {
            final long lguy = Double.doubleToLongBits(value);
            return Binary.intToHexString(Binary.highOrderLongToInt(lguy)) + Binary.intToHexString(Binary.lowOrderLongToInt(lguy)).substring(2);
        }
        return Double.toString(value);
    }
    
    public String formatNumber(final int value) {
        if (this.base == 16) {
            return Binary.intToHexString(value);
        }
        return Integer.toString(value);
    }
    
    public String formatUnsignedInteger(final int value) {
        return formatUnsignedInteger(value, this.base);
    }
    
    public static String formatFloatNumber(final int value, final int base) {
        if (base == 16) {
            return Binary.intToHexString(value);
        }
        return Float.toString(Float.intBitsToFloat(value));
    }
    
    public static String formatDoubleNumber(final long value, final int base) {
        if (base == 16) {
            return Binary.longToHexString(value);
        }
        return Double.toString(Double.longBitsToDouble(value));
    }
    
    public void setSettingsMenuItem(final JCheckBoxMenuItem setter) {
        this.settingMenuItem = setter;
    }
    
    public static int getBase(final boolean setting) {
        return setting ? 16 : 10;
    }
}
