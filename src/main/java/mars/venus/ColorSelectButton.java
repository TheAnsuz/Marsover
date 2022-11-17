

package mars.venus;

import javax.swing.border.LineBorder;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.JButton;

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
