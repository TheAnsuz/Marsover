

package mars.venus.editors.jeditsyntax;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import mars.venus.editors.jeditsyntax.tokenmarker.Token;
import mars.venus.editors.jeditsyntax.tokenmarker.TokenMarker;

public class TextAreaPainter extends JComponent implements TabExpander
{
    int currentLineIndex;
    Token currentLineTokens;
    Segment currentLine;
    protected JEditTextArea textArea;
    protected SyntaxStyle[] styles;
    protected Color caretColor;
    protected Color selectionColor;
    protected Color lineHighlightColor;
    protected Color bracketHighlightColor;
    protected Color eolMarkerColor;
    protected boolean blockCaret;
    protected boolean lineHighlight;
    protected boolean bracketHighlight;
    protected boolean paintInvalid;
    protected boolean eolMarkers;
    protected int cols;
    protected int rows;
    protected int tabSize;
    protected int tabSizeChars;
    protected FontMetrics fm;
    protected Highlight highlights;
    
    public TextAreaPainter(final JEditTextArea textArea, final TextAreaDefaults defaults) {
        this.textArea = textArea;
        this.setAutoscrolls(true);
        this.setDoubleBuffered(true);
        this.setOpaque(true);
        ToolTipManager.sharedInstance().registerComponent(this);
        this.currentLine = new Segment();
        this.currentLineIndex = -1;
        this.setCursor(Cursor.getPredefinedCursor(2));
        this.setFont(new Font("Courier New", 0, 14));
        this.setForeground(Color.black);
        this.setBackground(Color.white);
        this.tabSizeChars = defaults.tabSize;
        this.blockCaret = defaults.blockCaret;
        this.styles = defaults.styles;
        this.cols = defaults.cols;
        this.rows = defaults.rows;
        this.caretColor = defaults.caretColor;
        this.selectionColor = defaults.selectionColor;
        this.lineHighlightColor = defaults.lineHighlightColor;
        this.lineHighlight = defaults.lineHighlight;
        this.bracketHighlightColor = defaults.bracketHighlightColor;
        this.bracketHighlight = defaults.bracketHighlight;
        this.paintInvalid = defaults.paintInvalid;
        this.eolMarkerColor = defaults.eolMarkerColor;
        this.eolMarkers = defaults.eolMarkers;
    }
    
    @Override
    public final boolean isManagingFocus() {
        return false;
    }
    
    public int getTabSize() {
        return this.tabSizeChars;
    }
    
    public void setTabSize(final int size) {
        this.tabSizeChars = size;
    }
    
    public final SyntaxStyle[] getStyles() {
        return this.styles;
    }
    
    public final void setStyles(final SyntaxStyle[] styles) {
        this.styles = styles;
        this.repaint();
    }
    
    public final Color getCaretColor() {
        return this.caretColor;
    }
    
    public final void setCaretColor(final Color caretColor) {
        this.caretColor = caretColor;
        this.invalidateSelectedLines();
    }
    
    public final Color getSelectionColor() {
        return this.selectionColor;
    }
    
    public final void setSelectionColor(final Color selectionColor) {
        this.selectionColor = selectionColor;
        this.invalidateSelectedLines();
    }
    
    public final Color getLineHighlightColor() {
        return this.lineHighlightColor;
    }
    
    public final void setLineHighlightColor(final Color lineHighlightColor) {
        this.lineHighlightColor = lineHighlightColor;
        this.invalidateSelectedLines();
    }
    
    public final boolean isLineHighlightEnabled() {
        return this.lineHighlight;
    }
    
    public final void setLineHighlightEnabled(final boolean lineHighlight) {
        this.lineHighlight = lineHighlight;
        this.invalidateSelectedLines();
    }
    
    public final Color getBracketHighlightColor() {
        return this.bracketHighlightColor;
    }
    
    public final void setBracketHighlightColor(final Color bracketHighlightColor) {
        this.bracketHighlightColor = bracketHighlightColor;
        this.invalidateLine(this.textArea.getBracketLine());
    }
    
    public final boolean isBracketHighlightEnabled() {
        return this.bracketHighlight;
    }
    
    public final void setBracketHighlightEnabled(final boolean bracketHighlight) {
        this.bracketHighlight = bracketHighlight;
        this.invalidateLine(this.textArea.getBracketLine());
    }
    
    public final boolean isBlockCaretEnabled() {
        return this.blockCaret;
    }
    
    public final void setBlockCaretEnabled(final boolean blockCaret) {
        this.blockCaret = blockCaret;
        this.invalidateSelectedLines();
    }
    
    public final Color getEOLMarkerColor() {
        return this.eolMarkerColor;
    }
    
    public final void setEOLMarkerColor(final Color eolMarkerColor) {
        this.eolMarkerColor = eolMarkerColor;
        this.repaint();
    }
    
    public final boolean getEOLMarkersPainted() {
        return this.eolMarkers;
    }
    
    public final void setEOLMarkersPainted(final boolean eolMarkers) {
        this.eolMarkers = eolMarkers;
        this.repaint();
    }
    
    public boolean getInvalidLinesPainted() {
        return this.paintInvalid;
    }
    
    public void setInvalidLinesPainted(final boolean paintInvalid) {
        this.paintInvalid = paintInvalid;
    }
    
    public void addCustomHighlight(final Highlight highlight) {
        highlight.init(this.textArea, this.highlights);
        this.highlights = highlight;
    }
    
    @Override
    public String getToolTipText(final MouseEvent evt) {
        if (this.highlights != null) {
            return this.highlights.getToolTipText(evt);
        }
        if (this.textArea.getTokenMarker() == null) {
            return null;
        }
        return this.textArea.getSyntaxSensitiveToolTipText(evt.getX(), evt.getY());
    }
    
    public FontMetrics getFontMetrics() {
        return this.fm;
    }
    
    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        this.fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        this.textArea.recalculateVisibleLines();
    }
    
    @Override
    public void paint(final Graphics gfx) {
        ((Graphics2D)gfx).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D)gfx).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        this.tabSize = this.fm.charWidth(' ') * this.tabSizeChars;
        final Rectangle clipRect = gfx.getClipBounds();
        gfx.setColor(this.getBackground());
        gfx.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
        final int height = this.fm.getHeight();
        final int firstLine = this.textArea.getFirstLine();
        final int firstInvalid = firstLine + clipRect.y / height;
        final int lastInvalid = firstLine + (clipRect.y + clipRect.height - 1) / height;
        try {
            final TokenMarker tokenMarker = ((SyntaxDocument)this.textArea.getDocument()).getTokenMarker();
            final int x = this.textArea.getHorizontalOffset();
            for (int line = firstInvalid; line <= lastInvalid; ++line) {
                this.paintLine(gfx, tokenMarker, line, x);
            }
            if (tokenMarker != null && tokenMarker.isNextLineRequested()) {
                final int h = clipRect.y + clipRect.height;
                this.repaint(0, h, this.getWidth(), this.getHeight() - h);
            }
        }
        catch (Exception e) {
            System.err.println("Error repainting line range {" + firstInvalid + "," + lastInvalid + "}:");
            e.printStackTrace();
        }
    }
    
    public final void invalidateLine(final int line) {
        this.repaint(0, this.textArea.lineToY(line) + this.fm.getMaxDescent() + this.fm.getLeading(), this.getWidth(), this.fm.getHeight());
    }
    
    public final void invalidateLineRange(final int firstLine, final int lastLine) {
        this.repaint(0, this.textArea.lineToY(firstLine) + this.fm.getMaxDescent() + this.fm.getLeading(), this.getWidth(), (lastLine - firstLine + 1) * this.fm.getHeight());
    }
    
    public final void invalidateSelectedLines() {
        this.invalidateLineRange(this.textArea.getSelectionStartLine(), this.textArea.getSelectionEndLine());
    }
    
    @Override
    public float nextTabStop(final float x, final int tabOffset) {
        final int offset = this.textArea.getHorizontalOffset();
        final int ntabs = ((int)x - offset) / this.tabSize;
        return ((ntabs + 1) * this.tabSize + offset);
    }
    
    @Override
    public Dimension getPreferredSize() {
        final Dimension dim = new Dimension();
        dim.width = this.fm.charWidth('w') * this.cols;
        dim.height = this.fm.getHeight() * this.rows;
        return dim;
    }
    
    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }
    
    protected void paintLine(final Graphics gfx, final TokenMarker tokenMarker, final int line, final int x) {
        final Font defaultFont = this.getFont();
        final Color defaultColor = this.getForeground();
        this.currentLineIndex = line;
        final int y = this.textArea.lineToY(line);
        if (line < 0 || line >= this.textArea.getLineCount()) {
            if (this.paintInvalid) {
                this.paintHighlight(gfx, line, y);
                this.styles[10].setGraphicsFlags(gfx, defaultFont);
                gfx.drawString("~", 0, y + this.fm.getHeight());
            }
        }
        else if (tokenMarker == null) {
            this.paintPlainLine(gfx, line, defaultFont, defaultColor, x, y);
        }
        else {
            this.paintSyntaxLine(gfx, tokenMarker, line, defaultFont, defaultColor, x, y);
        }
    }
    
    protected void paintPlainLine(final Graphics gfx, final int line, final Font defaultFont, final Color defaultColor, int x, int y) {
        this.paintHighlight(gfx, line, y);
        this.textArea.getLineText(line, this.currentLine);
        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);
        y += this.fm.getHeight();
        x = Utilities.drawTabbedText(this.currentLine, x, y, gfx, this, 0);
        if (this.eolMarkers) {
            gfx.setColor(this.eolMarkerColor);
            gfx.drawString(".", x, y);
        }
    }
    
    protected void paintSyntaxLine(final Graphics gfx, final TokenMarker tokenMarker, final int line, final Font defaultFont, final Color defaultColor, int x, int y) {
        this.textArea.getLineText(this.currentLineIndex, this.currentLine);
        this.currentLineTokens = tokenMarker.markTokens(this.currentLine, this.currentLineIndex);
        this.paintHighlight(gfx, line, y);
        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);
        y += this.fm.getHeight();
        x = SyntaxUtilities.paintSyntaxLine(this.currentLine, this.currentLineTokens, this.styles, this, gfx, x, y);
        if (this.eolMarkers) {
            gfx.setColor(this.eolMarkerColor);
            gfx.drawString(".", x, y);
        }
    }
    
    protected void paintHighlight(final Graphics gfx, final int line, final int y) {
        if (line >= this.textArea.getSelectionStartLine() && line <= this.textArea.getSelectionEndLine()) {
            this.paintLineHighlight(gfx, line, y);
        }
        if (this.highlights != null) {
            this.highlights.paintHighlight(gfx, line, y);
        }
        if (this.bracketHighlight && line == this.textArea.getBracketLine()) {
            this.paintBracketHighlight(gfx, line, y);
        }
        if (line == this.textArea.getCaretLine()) {
            this.paintCaret(gfx, line, y);
        }
    }
    
    protected void paintLineHighlight(final Graphics gfx, final int line, int y) {
        final int height = this.fm.getHeight();
        y += this.fm.getLeading() + this.fm.getMaxDescent();
        final int selectionStart = this.textArea.getSelectionStart();
        final int selectionEnd = this.textArea.getSelectionEnd();
        if (selectionStart == selectionEnd) {
            if (this.lineHighlight) {
                gfx.setColor(this.lineHighlightColor);
                gfx.fillRect(0, y, this.getWidth(), height);
            }
        }
        else {
            gfx.setColor(this.selectionColor);
            final int selectionStartLine = this.textArea.getSelectionStartLine();
            final int selectionEndLine = this.textArea.getSelectionEndLine();
            final int lineStart = this.textArea.getLineStartOffset(line);
            int x1;
            int x2;
            if (this.textArea.isSelectionRectangular()) {
                final int lineLen = this.textArea.getLineLength(line);
                x1 = this.textArea._offsetToX(line, Math.min(lineLen, selectionStart - this.textArea.getLineStartOffset(selectionStartLine)));
                x2 = this.textArea._offsetToX(line, Math.min(lineLen, selectionEnd - this.textArea.getLineStartOffset(selectionEndLine)));
                if (x1 == x2) {
                    ++x2;
                }
            }
            else if (selectionStartLine == selectionEndLine) {
                x1 = this.textArea._offsetToX(line, selectionStart - lineStart);
                x2 = this.textArea._offsetToX(line, selectionEnd - lineStart);
            }
            else if (line == selectionStartLine) {
                x1 = this.textArea._offsetToX(line, selectionStart - lineStart);
                x2 = this.getWidth();
            }
            else if (line == selectionEndLine) {
                x1 = 0;
                x2 = this.textArea._offsetToX(line, selectionEnd - lineStart);
            }
            else {
                x1 = 0;
                x2 = this.getWidth();
            }
            gfx.fillRect((x1 > x2) ? x2 : x1, y, (x1 > x2) ? (x1 - x2) : (x2 - x1), height);
        }
    }
    
    protected void paintBracketHighlight(final Graphics gfx, final int line, int y) {
        final int position = this.textArea.getBracketPosition();
        if (position == -1) {
            return;
        }
        y += this.fm.getLeading() + this.fm.getMaxDescent();
        final int x = this.textArea._offsetToX(line, position);
        gfx.setColor(this.bracketHighlightColor);
        gfx.drawRect(x, y, this.fm.charWidth('(') - 1, this.fm.getHeight() - 1);
    }
    
    protected void paintCaret(final Graphics gfx, final int line, int y) {
        if (this.textArea.isCaretVisible()) {
            final int offset = this.textArea.getCaretPosition() - this.textArea.getLineStartOffset(line);
            final int caretX = this.textArea._offsetToX(line, offset);
            final int caretWidth = (this.blockCaret || this.textArea.isOverwriteEnabled()) ? this.fm.charWidth('w') : 1;
            y += this.fm.getLeading() + this.fm.getMaxDescent();
            final int height = this.fm.getHeight();
            gfx.setColor(this.caretColor);
            if (this.textArea.isOverwriteEnabled()) {
                gfx.fillRect(caretX, y + height - 1, caretWidth, 1);
            }
            else {
                gfx.drawRect(caretX, y, caretWidth, height - 1);
            }
        }
    }
    
    public interface Highlight
    {
        void init(final JEditTextArea p0, final Highlight p1);
        
        void paintHighlight(final Graphics p0, final int p1, final int p2);
        
        String getToolTipText(final MouseEvent p0);
    }
}
