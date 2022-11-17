

package mars.venus.editors.jeditsyntax;

import javax.swing.event.DocumentEvent;
import javax.swing.undo.UndoableEdit;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import mars.venus.editors.jeditsyntax.tokenmarker.TokenMarker;
import javax.swing.text.PlainDocument;

public class SyntaxDocument extends PlainDocument
{
    protected TokenMarker tokenMarker;
    
    public TokenMarker getTokenMarker() {
        return this.tokenMarker;
    }
    
    public void setTokenMarker(final TokenMarker tm) {
        this.tokenMarker = tm;
        if (tm == null) {
            return;
        }
        this.tokenMarker.insertLines(0, this.getDefaultRootElement().getElementCount());
        this.tokenizeLines();
    }
    
    public void tokenizeLines() {
        this.tokenizeLines(0, this.getDefaultRootElement().getElementCount());
    }
    
    public void tokenizeLines(final int start, int len) {
        if (this.tokenMarker == null || !this.tokenMarker.supportsMultilineTokens()) {
            return;
        }
        final Segment lineSegment = new Segment();
        final Element map = this.getDefaultRootElement();
        len += start;
        try {
            for (int i = start; i < len; ++i) {
                final Element lineElement = map.getElement(i);
                final int lineStart = lineElement.getStartOffset();
                this.getText(lineStart, lineElement.getEndOffset() - lineStart - 1, lineSegment);
                this.tokenMarker.markTokens(lineSegment, i);
            }
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
        }
    }
    
    public void beginCompoundEdit() {
    }
    
    public void endCompoundEdit() {
    }
    
    public void addUndoableEdit(final UndoableEdit edit) {
    }
    
    @Override
    protected void fireInsertUpdate(final DocumentEvent evt) {
        if (this.tokenMarker != null) {
            final DocumentEvent.ElementChange ch = evt.getChange(this.getDefaultRootElement());
            if (ch != null) {
                this.tokenMarker.insertLines(ch.getIndex() + 1, ch.getChildrenAdded().length - ch.getChildrenRemoved().length);
            }
        }
        super.fireInsertUpdate(evt);
    }
    
    @Override
    protected void fireRemoveUpdate(final DocumentEvent evt) {
        if (this.tokenMarker != null) {
            final DocumentEvent.ElementChange ch = evt.getChange(this.getDefaultRootElement());
            if (ch != null) {
                this.tokenMarker.deleteLines(ch.getIndex() + 1, ch.getChildrenRemoved().length - ch.getChildrenAdded().length);
            }
        }
        super.fireRemoveUpdate(evt);
    }
}
