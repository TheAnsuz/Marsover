

package mars.tools;

import java.awt.Rectangle;

class CaptureMagnifierRectangle implements CaptureRectangleStrategy
{
    @Override
    public Rectangle getCaptureRectangle(final Rectangle magnifierRectangle) {
        return magnifierRectangle;
    }
}
