

package mars.tools;

import javax.swing.JScrollBar;

class CaptureDisplayUpperleft implements CaptureDisplayAlignmentStrategy
{
    @Override
    public void setScrollBarValue(final JScrollBar scrollBar) {
        scrollBar.setValue(0);
    }
}
