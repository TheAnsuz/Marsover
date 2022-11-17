

package mars.venus.editors.jeditsyntax;

import java.awt.Graphics;
import java.awt.Toolkit;
import mars.util.Binary;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Color;

public class SyntaxStyle
{
    private Color color;
    private boolean italic;
    private boolean bold;
    private Font lastFont;
    private Font lastStyledFont;
    private FontMetrics fontMetrics;
    
    public SyntaxStyle(final Color color, final boolean italic, final boolean bold) {
        this.color = color;
        this.italic = italic;
        this.bold = bold;
    }
    
    public Color getColor() {
        return this.color;
    }
    
    public String getColorAsHexString() {
        return Binary.intToHexString(this.color.getRed() << 16 | this.color.getGreen() << 8 | this.color.getBlue());
    }
    
    public boolean isPlain() {
        return !this.bold && !this.italic;
    }
    
    public boolean isItalic() {
        return this.italic;
    }
    
    public boolean isBold() {
        return this.bold;
    }
    
    public Font getStyledFont(final Font font) {
        if (font == null) {
            throw new NullPointerException("font param must not be null");
        }
        if (font.equals(this.lastFont)) {
            return this.lastStyledFont;
        }
        this.lastFont = font;
        return this.lastStyledFont = new Font(font.getFamily(), (this.bold ? 1 : 0) | (this.italic ? 2 : 0), font.getSize());
    }
    
    public FontMetrics getFontMetrics(final Font font) {
        if (font == null) {
            throw new NullPointerException("font param must not be null");
        }
        if (font.equals(this.lastFont) && this.fontMetrics != null) {
            return this.fontMetrics;
        }
        this.lastFont = font;
        this.lastStyledFont = new Font(font.getFamily(), (this.bold ? 1 : 0) | (this.italic ? 2 : 0), font.getSize());
        return this.fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(this.lastStyledFont);
    }
    
    public void setGraphicsFlags(final Graphics gfx, final Font font) {
        final Font _font = this.getStyledFont(font);
        gfx.setFont(_font);
        gfx.setColor(this.color);
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + "[color=" + this.color + (this.italic ? ",italic" : "") + (this.bold ? ",bold" : "") + "]";
    }
}
