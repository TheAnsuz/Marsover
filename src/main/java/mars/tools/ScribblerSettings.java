

package mars.tools;

import java.awt.Color;

class ScribblerSettings
{
    private int width;
    private Color color;
    
    public ScribblerSettings(final int width, final Color color) {
        this.width = width;
        this.color = color;
    }
    
    public int getLineWidth() {
        return this.width;
    }
    
    public Color getLineColor() {
        return this.color;
    }
    
    public void setLineWidth(final int newWidth) {
        this.width = newWidth;
    }
    
    public void setLineColor(final Color newColor) {
        this.color = newColor;
    }
}
