

package mars.tools;

import javax.swing.JScrollBar;

class CaptureDisplayCentered implements CaptureDisplayAlignmentStrategy
{
    @Override
    public void setScrollBarValue(final JScrollBar scrollBar) {
        scrollBar.setValue((scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum() - scrollBar.getModel().getExtent()) / 2);
    }
}
