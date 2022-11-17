

package mars.venus;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

class ColorSelectButton extends JButton
{
    public static final Border ColorSelectButtonEnabledBorder;
    public static final Border ColorSelectButtonDisabledBorder;
    
    @Override
    public void setEnabled(final boolean status) {
        super.setEnabled(status);
        this.setBorder(status ? ColorSelectButton.ColorSelectButtonEnabledBorder : ColorSelectButton.ColorSelectButtonDisabledBorder);
    }
    
    static {
        ColorSelectButtonEnabledBorder = new BevelBorder(0, Color.WHITE, Color.GRAY);
        ColorSelectButtonDisabledBorder = new LineBorder(Color.GRAY, 2);
    }
}
