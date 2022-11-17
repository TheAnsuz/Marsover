

package mars.venus.editors.jeditsyntax;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import mars.Globals;
import mars.venus.editors.jeditsyntax.tokenmarker.Token;
import mars.venus.editors.jeditsyntax.tokenmarker.TokenMarker;

public class JEditTextArea extends JComponent
{
    public static String LEFT_OF_SCROLLBAR;
    public static Color POPUP_HELP_TEXT_COLOR;
    private static final int VERTICAL_SCROLLBAR_UNIT_INCREMENT_IN_LINES = 1;
    private static final int LINES_PER_MOUSE_WHEEL_NOTCH = 3;
    private JScrollBar lineNumbersVertical;
    JPopupMenu popupMenu;
    protected static String CENTER;
    protected static String RIGHT;
    protected static String BOTTOM;
    protected static JEditTextArea focusedComponent;
    protected static Timer caretTimer;
    protected TextAreaPainter painter;
    protected JPopupMenu popup;
    protected EventListenerList listenerList;
    protected MutableCaretEvent caretEvent;
    protected boolean caretBlinks;
    protected boolean caretVisible;
    protected boolean blink;
    protected boolean editable;
    protected int caretBlinkRate;
    protected int firstLine;
    protected int visibleLines;
    protected int electricScroll;
    protected int horizontalOffset;
    protected JScrollBar vertical;
    protected JScrollBar horizontal;
    protected boolean scrollBarsInitialized;
    protected InputHandler inputHandler;
    protected SyntaxDocument document;
    protected DocumentHandler documentHandler;
    protected Segment lineSegment;
    protected int selectionStart;
    protected int selectionStartLine;
    protected int selectionEnd;
    protected int selectionEndLine;
    protected boolean biasLeft;
    protected int bracketPosition;
    protected int bracketLine;
    protected int magicCaret;
    protected boolean overwrite;
    protected boolean rectSelect;
    protected boolean unredoing;
    
    public JEditTextArea(final JComponent lineNumbers) {
        this(TextAreaDefaults.getDefaults(), lineNumbers);
    }
    
    public JEditTextArea(final TextAreaDefaults defaults, final JComponent lineNumbers) {
        this.unredoing = false;
        this.enableEvents(8L);
        this.painter = new TextAreaPainter(this, defaults);
        this.documentHandler = new DocumentHandler();
        this.listenerList = new EventListenerList();
        this.caretEvent = new MutableCaretEvent();
        this.lineSegment = new Segment();
        final int n = -1;
        this.bracketPosition = n;
        this.bracketLine = n;
        this.blink = true;
        this.unredoing = false;
        final JScrollPane lineNumberScroller = new JScrollPane(lineNumbers, 21, 31);
        lineNumberScroller.setBorder(new EmptyBorder(1, 1, 1, 1));
        this.lineNumbersVertical = lineNumberScroller.getVerticalScrollBar();
        final JPanel lineNumbersPlusPainter = new JPanel(new BorderLayout());
        lineNumbersPlusPainter.add(this.painter, "Center");
        lineNumbersPlusPainter.add(lineNumberScroller, "West");
        this.setLayout(new ScrollLayout());
        this.add(JEditTextArea.CENTER, lineNumbersPlusPainter);
        this.add(JEditTextArea.RIGHT, this.vertical = new JScrollBar(1));
        this.add(JEditTextArea.BOTTOM, this.horizontal = new JScrollBar(0));
        this.vertical.addAdjustmentListener(new AdjustHandler());
        this.horizontal.addAdjustmentListener(new AdjustHandler());
        this.painter.addComponentListener(new ComponentHandler());
        this.painter.addMouseListener(new MouseHandler());
        this.painter.addMouseMotionListener(new DragHandler());
        this.painter.addMouseWheelListener(new MouseWheelHandler());
        this.addFocusListener(new FocusHandler());
        this.setInputHandler(defaults.inputHandler);
        this.setDocument(defaults.document);
        this.editable = defaults.editable;
        this.caretVisible = defaults.caretVisible;
        this.caretBlinks = defaults.caretBlinks;
        this.caretBlinkRate = defaults.caretBlinkRate;
        this.electricScroll = defaults.electricScroll;
        this.popup = defaults.popup;
        JEditTextArea.caretTimer.setDelay(this.caretBlinkRate);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                if (JEditTextArea.this.isFocusOwner() && e.getKeyCode() == 9 && e.getModifiers() == 0) {
                    JEditTextArea.this.processKeyEvent(e);
                    return true;
                }
                return false;
            }
        });
        JEditTextArea.focusedComponent = this;
    }
    
    public final TextAreaPainter getPainter() {
        return this.painter;
    }
    
    public final InputHandler getInputHandler() {
        return this.inputHandler;
    }
    
    public void setInputHandler(final InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
    
    public final boolean isCaretBlinkEnabled() {
        return this.caretBlinks;
    }
    
    public void setCaretBlinkEnabled(final boolean caretBlinks) {
        if (!(this.caretBlinks = caretBlinks)) {
            this.blink = false;
        }
        this.painter.invalidateSelectedLines();
    }
    
    public final boolean isCaretVisible() {
        return (!this.caretBlinks || this.blink) && this.caretVisible;
    }
    
    public void setCaretVisible(final boolean caretVisible) {
        this.caretVisible = caretVisible;
        this.blink = true;
        this.painter.invalidateSelectedLines();
    }
    
    public final void blinkCaret() {
        if (this.caretBlinks) {
            this.blink = !this.blink;
            this.painter.invalidateSelectedLines();
        }
        else {
            this.blink = true;
        }
    }
    
    public final int getElectricScroll() {
        return this.electricScroll;
    }
    
    public final void setElectricScroll(final int electricScroll) {
        this.electricScroll = electricScroll;
    }
    
    public void updateScrollBars() {
        if (this.vertical != null && this.visibleLines != 0) {
            this.vertical.setValues(this.firstLine, this.visibleLines, 0, this.getLineCount());
            this.vertical.setUnitIncrement(1);
            this.vertical.setBlockIncrement(this.visibleLines);
            final int height = this.painter.getFontMetrics(this.painter.getFont()).getHeight();
            this.lineNumbersVertical.setValues(this.firstLine * height, this.visibleLines * height, 0, this.getLineCount() * height);
            this.lineNumbersVertical.setUnitIncrement(1 * height);
            this.lineNumbersVertical.setBlockIncrement(this.visibleLines * height);
        }
        final int width = this.painter.getWidth();
        if (this.horizontal != null && width != 0) {
            this.horizontal.setValues(-this.horizontalOffset, width, 0, width * 5);
            this.horizontal.setUnitIncrement(this.painter.getFontMetrics().charWidth('w'));
            this.horizontal.setBlockIncrement(width / 2);
        }
    }
    
    public final int getFirstLine() {
        return this.firstLine;
    }
    
    public void setFirstLine(final int firstLine) {
        if (firstLine == this.firstLine) {
            return;
        }
        final int oldFirstLine = this.firstLine;
        this.firstLine = firstLine;
        this.updateScrollBars();
        this.painter.repaint();
    }
    
    public final int getVisibleLines() {
        return this.visibleLines;
    }
    
    public final void recalculateVisibleLines() {
        if (this.painter == null) {
            return;
        }
        final int height = this.painter.getHeight();
        final int lineHeight = this.painter.getFontMetrics().getHeight();
        final int oldVisibleLines = this.visibleLines;
        this.visibleLines = height / lineHeight;
        this.updateScrollBars();
    }
    
    public final int getHorizontalOffset() {
        return this.horizontalOffset;
    }
    
    public void setHorizontalOffset(final int horizontalOffset) {
        if (horizontalOffset == this.horizontalOffset) {
            return;
        }
        if ((this.horizontalOffset = horizontalOffset) != this.horizontal.getValue()) {
            this.updateScrollBars();
        }
        this.painter.repaint();
    }
    
    public boolean setOrigin(final int firstLine, final int horizontalOffset) {
        boolean changed = false;
        final int oldFirstLine = this.firstLine;
        if (horizontalOffset != this.horizontalOffset) {
            this.horizontalOffset = horizontalOffset;
            changed = true;
        }
        if (firstLine != this.firstLine) {
            this.firstLine = firstLine;
            changed = true;
        }
        if (changed) {
            this.updateScrollBars();
            this.painter.repaint();
        }
        return changed;
    }
    
    public boolean scrollToCaret() {
        final int line = this.getCaretLine();
        final int lineStart = this.getLineStartOffset(line);
        final int offset = Math.max(0, Math.min(this.getLineLength(line) - 1, this.getCaretPosition() - lineStart));
        return this.scrollTo(line, offset);
    }
    
    public boolean scrollTo(final int line, final int offset) {
        if (this.visibleLines == 0) {
            this.setFirstLine(Math.max(0, line - this.electricScroll));
            return true;
        }
        int newFirstLine = this.firstLine;
        int newHorizontalOffset = this.horizontalOffset;
        if (line < this.firstLine + this.electricScroll) {
            newFirstLine = Math.max(0, line - this.electricScroll);
        }
        else if (line + this.electricScroll >= this.firstLine + this.visibleLines) {
            newFirstLine = line - this.visibleLines + this.electricScroll + 1;
            if (newFirstLine + this.visibleLines >= this.getLineCount()) {
                newFirstLine = this.getLineCount() - this.visibleLines;
            }
            if (newFirstLine < 0) {
                newFirstLine = 0;
            }
        }
        final int x = this._offsetToX(line, offset);
        final int width = this.painter.getFontMetrics().charWidth('w');
        if (x < 0) {
            newHorizontalOffset = Math.min(0, this.horizontalOffset - x + width + 5);
        }
        else if (x + width >= this.painter.getWidth()) {
            newHorizontalOffset = this.horizontalOffset + (this.painter.getWidth() - x) - width - 5;
        }
        return this.setOrigin(newFirstLine, newHorizontalOffset);
    }
    
    public int lineToY(final int line) {
        final FontMetrics fm = this.painter.getFontMetrics();
        return (line - this.firstLine) * fm.getHeight() - (fm.getLeading() + fm.getMaxDescent());
    }
    
    public int yToLine(final int y) {
        final FontMetrics fm = this.painter.getFontMetrics();
        final int height = fm.getHeight();
        return Math.max(0, Math.min(this.getLineCount() - 1, y / height + this.firstLine));
    }
    
    public final int offsetToX(final int line, final int offset) {
        this.painter.currentLineTokens = null;
        return this._offsetToX(line, offset);
    }
    
    public int _offsetToX(final int line, final int offset) {
        final TokenMarker tokenMarker = this.getTokenMarker();
        FontMetrics fm = this.painter.getFontMetrics();
        this.getLineText(line, this.lineSegment);
        final int segmentOffset = this.lineSegment.offset;
        int x = this.horizontalOffset;
        if (tokenMarker == null) {
            this.lineSegment.count = offset;
            return x + Utilities.getTabbedTextWidth(this.lineSegment, fm, x, this.painter, 0);
        }
        Token tokens;
        if (this.painter.currentLineIndex == line && this.painter.currentLineTokens != null) {
            tokens = this.painter.currentLineTokens;
        }
        else {
            this.painter.currentLineIndex = line;
            final TextAreaPainter painter = this.painter;
            final Token markTokens = tokenMarker.markTokens(this.lineSegment, line);
            painter.currentLineTokens = markTokens;
            tokens = markTokens;
        }
        final Toolkit toolkit = this.painter.getToolkit();
        final Font defaultFont = this.painter.getFont();
        final SyntaxStyle[] styles = this.painter.getStyles();
        while (true) {
            final byte id = tokens.id;
            if (id == 127) {
                return x;
            }
            if (id == 0) {
                fm = this.painter.getFontMetrics();
            }
            else {
                fm = styles[id].getFontMetrics(defaultFont);
            }
            final int length = tokens.length;
            if (offset + segmentOffset < this.lineSegment.offset + length) {
                this.lineSegment.count = offset - (this.lineSegment.offset - segmentOffset);
                return x + Utilities.getTabbedTextWidth(this.lineSegment, fm, x, this.painter, 0);
            }
            this.lineSegment.count = length;
            x += Utilities.getTabbedTextWidth(this.lineSegment, fm, x, this.painter, 0);
            final Segment lineSegment = this.lineSegment;
            lineSegment.offset += length;
            tokens = tokens.next;
        }
    }
    
    public int xToOffset(final int line, final int x) {
        final TokenMarker tokenMarker = this.getTokenMarker();
        FontMetrics fm = this.painter.getFontMetrics();
        this.getLineText(line, this.lineSegment);
        final char[] segmentArray = this.lineSegment.array;
        final int segmentOffset = this.lineSegment.offset;
        final int segmentCount = this.lineSegment.count;
        int width = this.horizontalOffset;
        if (tokenMarker == null) {
            for (int i = 0; i < segmentCount; ++i) {
                final char c = segmentArray[i + segmentOffset];
                int charWidth;
                if (c == '\t') {
                    charWidth = (int)this.painter.nextTabStop(width, i) - width;
                }
                else {
                    charWidth = fm.charWidth(c);
                }
                if (this.painter.isBlockCaretEnabled()) {
                    if (x - charWidth <= width) {
                        return i;
                    }
                }
                else if (x - charWidth / 2 <= width) {
                    return i;
                }
                width += charWidth;
            }
            return segmentCount;
        }
        Token tokens;
        if (this.painter.currentLineIndex == line && this.painter.currentLineTokens != null) {
            tokens = this.painter.currentLineTokens;
        }
        else {
            this.painter.currentLineIndex = line;
            final TextAreaPainter painter = this.painter;
            final Token markTokens = tokenMarker.markTokens(this.lineSegment, line);
            painter.currentLineTokens = markTokens;
            tokens = markTokens;
        }
        int offset = 0;
        final Toolkit toolkit = this.painter.getToolkit();
        final Font defaultFont = this.painter.getFont();
        final SyntaxStyle[] styles = this.painter.getStyles();
        while (true) {
            final byte id = tokens.id;
            if (id == 127) {
                return offset;
            }
            if (id == 0) {
                fm = this.painter.getFontMetrics();
            }
            else {
                fm = styles[id].getFontMetrics(defaultFont);
            }
            final int length = tokens.length;
            for (int j = 0; j < length; ++j) {
                final char c2 = segmentArray[segmentOffset + offset + j];
                int charWidth2;
                if (c2 == '\t') {
                    charWidth2 = (int)this.painter.nextTabStop(width, offset + j) - width;
                }
                else {
                    charWidth2 = fm.charWidth(c2);
                }
                if (this.painter.isBlockCaretEnabled()) {
                    if (x - charWidth2 <= width) {
                        return offset + j;
                    }
                }
                else if (x - charWidth2 / 2 <= width) {
                    return offset + j;
                }
                width += charWidth2;
            }
            offset += length;
            tokens = tokens.next;
        }
    }
    
    public int xyToOffset(final int x, final int y) {
        final int line = this.yToLine(y);
        final int start = this.getLineStartOffset(line);
        return start + this.xToOffset(line, x);
    }
    
    public final Document getDocument() {
        return this.document;
    }
    
    public void setDocument(final SyntaxDocument document) {
        if (this.document == document) {
            return;
        }
        if (this.document != null) {
            this.document.removeDocumentListener(this.documentHandler);
        }
        (this.document = document).addDocumentListener(this.documentHandler);
        this.select(0, 0);
        this.updateScrollBars();
        this.painter.repaint();
    }
    
    public final TokenMarker getTokenMarker() {
        return this.document.getTokenMarker();
    }
    
    public final void setTokenMarker(final TokenMarker tokenMarker) {
        this.document.setTokenMarker(tokenMarker);
    }
    
    public final int getDocumentLength() {
        return this.document.getLength();
    }
    
    public final int getLineCount() {
        return this.document.getDefaultRootElement().getElementCount();
    }
    
    public final int getLineOfOffset(final int offset) {
        return this.document.getDefaultRootElement().getElementIndex(offset);
    }
    
    public int getLineStartOffset(final int line) {
        final Element lineElement = this.document.getDefaultRootElement().getElement(line);
        if (lineElement == null) {
            return -1;
        }
        return lineElement.getStartOffset();
    }
    
    public int getLineEndOffset(final int line) {
        final Element lineElement = this.document.getDefaultRootElement().getElement(line);
        if (lineElement == null) {
            return -1;
        }
        return lineElement.getEndOffset();
    }
    
    public int getLineLength(final int line) {
        final Element lineElement = this.document.getDefaultRootElement().getElement(line);
        if (lineElement == null) {
            return -1;
        }
        return lineElement.getEndOffset() - lineElement.getStartOffset() - 1;
    }
    
    public String getText() {
        try {
            return this.document.getText(0, this.document.getLength());
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
            return null;
        }
    }
    
    public void setText(final String text) {
        try {
            this.document.beginCompoundEdit();
            this.document.remove(0, this.document.getLength());
            this.document.insertString(0, text, null);
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
        }
        finally {
            this.document.endCompoundEdit();
        }
    }
    
    public final String getText(final int start, final int len) {
        try {
            return this.document.getText(start, len);
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
            return null;
        }
    }
    
    public final void getText(final int start, final int len, final Segment segment) {
        try {
            this.document.getText(start, len, segment);
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
            final int n = 0;
            segment.count = n;
            segment.offset = n;
        }
    }
    
    public final String getLineText(final int lineIndex) {
        final int start = this.getLineStartOffset(lineIndex);
        return this.getText(start, this.getLineEndOffset(lineIndex) - start - 1);
    }
    
    public final void getLineText(final int lineIndex, final Segment segment) {
        final int start = this.getLineStartOffset(lineIndex);
        this.getText(start, this.getLineEndOffset(lineIndex) - start - 1, segment);
    }
    
    public final int getSelectionStart() {
        return this.selectionStart;
    }
    
    public int getSelectionStart(final int line) {
        if (line == this.selectionStartLine) {
            return this.selectionStart;
        }
        if (this.rectSelect) {
            final Element map = this.document.getDefaultRootElement();
            final int start = this.selectionStart - map.getElement(this.selectionStartLine).getStartOffset();
            final Element lineElement = map.getElement(line);
            final int lineStart = lineElement.getStartOffset();
            final int lineEnd = lineElement.getEndOffset() - 1;
            return Math.min(lineEnd, lineStart + start);
        }
        return this.getLineStartOffset(line);
    }
    
    public final int getSelectionStartLine() {
        return this.selectionStartLine;
    }
    
    public final void setSelectionStart(final int selectionStart) {
        this.select(selectionStart, this.selectionEnd);
    }
    
    public final int getSelectionEnd() {
        return this.selectionEnd;
    }
    
    public int getSelectionEnd(final int line) {
        if (line == this.selectionEndLine) {
            return this.selectionEnd;
        }
        if (this.rectSelect) {
            final Element map = this.document.getDefaultRootElement();
            final int end = this.selectionEnd - map.getElement(this.selectionEndLine).getStartOffset();
            final Element lineElement = map.getElement(line);
            final int lineStart = lineElement.getStartOffset();
            final int lineEnd = lineElement.getEndOffset() - 1;
            return Math.min(lineEnd, lineStart + end);
        }
        return this.getLineEndOffset(line) - 1;
    }
    
    public final int getSelectionEndLine() {
        return this.selectionEndLine;
    }
    
    public final void setSelectionEnd(final int selectionEnd) {
        this.select(this.selectionStart, selectionEnd);
    }
    
    public final int getCaretPosition() {
        return this.biasLeft ? this.selectionStart : this.selectionEnd;
    }
    
    public final int getCaretLine() {
        return this.biasLeft ? this.selectionStartLine : this.selectionEndLine;
    }
    
    public final int getMarkPosition() {
        return this.biasLeft ? this.selectionEnd : this.selectionStart;
    }
    
    public final int getMarkLine() {
        return this.biasLeft ? this.selectionEndLine : this.selectionStartLine;
    }
    
    public final void setCaretPosition(final int caret) {
        this.select(caret, caret);
    }
    
    public final void selectAll() {
        this.select(0, this.getDocumentLength());
    }
    
    public final void selectNone() {
        this.select(this.getCaretPosition(), this.getCaretPosition());
    }
    
    public void select(final int start, final int end) {
        int newStart;
        int newEnd;
        boolean newBias;
        if (start <= end) {
            newStart = start;
            newEnd = end;
            newBias = false;
        }
        else {
            newStart = end;
            newEnd = start;
            newBias = true;
        }
        if (newStart < 0 || newEnd > this.getDocumentLength()) {
            throw new IllegalArgumentException("Bounds out of range: " + newStart + "," + newEnd);
        }
        if (newStart != this.selectionStart || newEnd != this.selectionEnd || newBias != this.biasLeft) {
            final int newStartLine = this.getLineOfOffset(newStart);
            final int newEndLine = this.getLineOfOffset(newEnd);
            if (this.painter.isBracketHighlightEnabled()) {
                if (this.bracketLine != -1) {
                    this.painter.invalidateLine(this.bracketLine);
                }
                this.updateBracketHighlight(end);
                if (this.bracketLine != -1) {
                    this.painter.invalidateLine(this.bracketLine);
                }
            }
            this.painter.invalidateLineRange(this.selectionStartLine, this.selectionEndLine);
            this.painter.invalidateLineRange(newStartLine, newEndLine);
            this.document.addUndoableEdit(new CaretUndo(this.selectionStart, this.selectionEnd));
            this.selectionStart = newStart;
            this.selectionEnd = newEnd;
            this.selectionStartLine = newStartLine;
            this.selectionEndLine = newEndLine;
            this.biasLeft = newBias;
            this.fireCaretEvent();
        }
        this.blink = true;
        JEditTextArea.caretTimer.restart();
        if (this.selectionStart == this.selectionEnd) {
            this.rectSelect = false;
        }
        this.magicCaret = -1;
        this.scrollToCaret();
    }
    
    public final String getSelectedText() {
        if (this.selectionStart == this.selectionEnd) {
            return null;
        }
        if (this.rectSelect) {
            final Element map = this.document.getDefaultRootElement();
            int start = this.selectionStart - map.getElement(this.selectionStartLine).getStartOffset();
            int end = this.selectionEnd - map.getElement(this.selectionEndLine).getStartOffset();
            if (end < start) {
                final int tmp = end;
                end = start;
                start = tmp;
            }
            final StringBuffer buf = new StringBuffer();
            final Segment seg = new Segment();
            for (int i = this.selectionStartLine; i <= this.selectionEndLine; ++i) {
                final Element lineElement = map.getElement(i);
                int lineStart = lineElement.getStartOffset();
                final int lineEnd = lineElement.getEndOffset() - 1;
                int lineLen = lineEnd - lineStart;
                lineStart = Math.min(lineStart + start, lineEnd);
                lineLen = Math.min(end - start, lineEnd - lineStart);
                this.getText(lineStart, lineLen, seg);
                buf.append(seg.array, seg.offset, seg.count);
                if (i != this.selectionEndLine) {
                    buf.append('\n');
                }
            }
            return buf.toString();
        }
        return this.getText(this.selectionStart, this.selectionEnd - this.selectionStart);
    }
    
    public void setSelectedText(final String selectedText) {
        if (!this.editable) {
            throw new InternalError("Text component read only");
        }
        this.document.beginCompoundEdit();
        try {
            if (this.rectSelect) {
                final Element map = this.document.getDefaultRootElement();
                int start = this.selectionStart - map.getElement(this.selectionStartLine).getStartOffset();
                int end = this.selectionEnd - map.getElement(this.selectionEndLine).getStartOffset();
                if (end < start) {
                    final int tmp = end;
                    end = start;
                    start = tmp;
                }
                int lastNewline = 0;
                int currNewline = 0;
                for (int i = this.selectionStartLine; i <= this.selectionEndLine; ++i) {
                    final Element lineElement = map.getElement(i);
                    final int lineStart = lineElement.getStartOffset();
                    final int lineEnd = lineElement.getEndOffset() - 1;
                    final int rectStart = Math.min(lineEnd, lineStart + start);
                    this.document.remove(rectStart, Math.min(lineEnd - rectStart, end - start));
                    if (selectedText != null) {
                        currNewline = selectedText.indexOf(10, lastNewline);
                        if (currNewline == -1) {
                            currNewline = selectedText.length();
                        }
                        this.document.insertString(rectStart, selectedText.substring(lastNewline, currNewline), null);
                        lastNewline = Math.min(selectedText.length(), currNewline + 1);
                    }
                }
                if (selectedText != null && currNewline != selectedText.length()) {
                    final int offset = map.getElement(this.selectionEndLine).getEndOffset() - 1;
                    this.document.insertString(offset, "\n", null);
                    this.document.insertString(offset + 1, selectedText.substring(currNewline + 1), null);
                }
            }
            else {
                this.document.remove(this.selectionStart, this.selectionEnd - this.selectionStart);
                if (selectedText != null) {
                    this.document.insertString(this.selectionStart, selectedText, null);
                }
            }
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
            throw new InternalError("Cannot replace selection");
        }
        finally {
            this.document.endCompoundEdit();
        }
        this.setCaretPosition(this.selectionEnd);
    }
    
    public final boolean isEditable() {
        return this.editable;
    }
    
    public final void setEditable(final boolean editable) {
        this.editable = editable;
    }
    
    public final JPopupMenu getRightClickPopup() {
        return this.popup;
    }
    
    public final void setRightClickPopup(final JPopupMenu popup) {
        this.popup = popup;
    }
    
    public final int getMagicCaretPosition() {
        return this.magicCaret;
    }
    
    public final void setMagicCaretPosition(final int magicCaret) {
        this.magicCaret = magicCaret;
    }
    
    public void overwriteSetSelectedText(final String str) {
        if (!this.overwrite || this.selectionStart != this.selectionEnd) {
            this.setSelectedText(str);
            this.applySyntaxSensitiveHelp();
            return;
        }
        final int caret = this.getCaretPosition();
        final int caretLineEnd = this.getLineEndOffset(this.getCaretLine());
        if (caretLineEnd - caret <= str.length()) {
            this.setSelectedText(str);
            this.applySyntaxSensitiveHelp();
            return;
        }
        this.document.beginCompoundEdit();
        try {
            this.document.remove(caret, str.length());
            this.document.insertString(caret, str, null);
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
        }
        finally {
            this.document.endCompoundEdit();
        }
        this.applySyntaxSensitiveHelp();
    }
    
    public final boolean isOverwriteEnabled() {
        return this.overwrite;
    }
    
    public final void setOverwriteEnabled(final boolean overwrite) {
        this.overwrite = overwrite;
        this.painter.invalidateSelectedLines();
    }
    
    public final boolean isSelectionRectangular() {
        return this.rectSelect;
    }
    
    public final void setSelectionRectangular(final boolean rectSelect) {
        this.rectSelect = rectSelect;
        this.painter.invalidateSelectedLines();
    }
    
    public final int getBracketPosition() {
        return this.bracketPosition;
    }
    
    public final int getBracketLine() {
        return this.bracketLine;
    }
    
    public final void addCaretListener(final CaretListener listener) {
        this.listenerList.add(CaretListener.class, listener);
    }
    
    public final void removeCaretListener(final CaretListener listener) {
        this.listenerList.remove(CaretListener.class, listener);
    }
    
    public void cut() {
        if (this.editable) {
            this.copy();
            this.setSelectedText("");
        }
    }
    
    public void copy() {
        if (this.selectionStart != this.selectionEnd) {
            final Clipboard clipboard = this.getToolkit().getSystemClipboard();
            final String selection = this.getSelectedText();
            final int repeatCount = this.inputHandler.getRepeatCount();
            final StringBuffer buf = new StringBuffer();
            for (int i = 0; i < repeatCount; ++i) {
                buf.append(selection);
            }
            clipboard.setContents(new StringSelection(buf.toString()), null);
        }
    }
    
    public void paste() {
        if (this.editable) {
            final Clipboard clipboard = this.getToolkit().getSystemClipboard();
            try {
                String selection = ((String)clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor)).replace('\r', '\n');
                final int repeatCount = this.inputHandler.getRepeatCount();
                final StringBuffer buf = new StringBuffer();
                for (int i = 0; i < repeatCount; ++i) {
                    buf.append(selection);
                }
                selection = buf.toString();
                this.setSelectedText(selection);
            }
            catch (Exception e) {
                this.getToolkit().beep();
                System.err.println("Clipboard does not contain a string");
            }
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (JEditTextArea.focusedComponent == this) {
            JEditTextArea.focusedComponent = null;
        }
    }
    
    public void processKeyEvent(final KeyEvent evt) {
        if (this.inputHandler == null) {
            return;
        }
        switch (evt.getID()) {
            case 400: {
                this.inputHandler.keyTyped(evt);
                break;
            }
            case 401: {
                if (!this.checkPopupCompletion(evt)) {
                    this.inputHandler.keyPressed(evt);
                }
                this.checkPopupMenu(evt);
                break;
            }
            case 402: {
                this.inputHandler.keyReleased(evt);
                break;
            }
        }
    }
    
    protected void fireCaretEvent() {
        final Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; --i) {
            if (listeners[i] == CaretListener.class) {
                ((CaretListener)listeners[i + 1]).caretUpdate(this.caretEvent);
            }
        }
    }
    
    protected void updateBracketHighlight(final int newCaretPosition) {
        if (newCaretPosition == 0) {
            final int n = -1;
            this.bracketLine = n;
            this.bracketPosition = n;
            return;
        }
        try {
            final int offset = TextUtilities.findMatchingBracket(this.document, newCaretPosition - 1);
            if (offset != -1) {
                this.bracketLine = this.getLineOfOffset(offset);
                this.bracketPosition = offset - this.getLineStartOffset(this.bracketLine);
                return;
            }
        }
        catch (BadLocationException bl) {
            bl.printStackTrace();
        }
        final int n2 = -1;
        this.bracketPosition = n2;
        this.bracketLine = n2;
    }
    
    protected void documentChanged(final DocumentEvent evt) {
        final DocumentEvent.ElementChange ch = evt.getChange(this.document.getDefaultRootElement());
        int count;
        if (ch == null) {
            count = 0;
        }
        else {
            count = ch.getChildrenAdded().length - ch.getChildrenRemoved().length;
        }
        final int line = this.getLineOfOffset(evt.getOffset());
        if (count == 0) {
            this.painter.invalidateLine(line);
        }
        else if (line < this.firstLine) {
            this.setFirstLine(this.firstLine + count);
        }
        else {
            this.painter.invalidateLineRange(line, this.firstLine + this.visibleLines);
            this.updateScrollBars();
        }
    }
    
    public String getSyntaxSensitiveToolTipText(final int x, final int y) {
        String result = null;
        final int line = this.yToLine(y);
        final ArrayList<PopupHelpItem> matches = this.getSyntaxSensitiveHelpAtLineOffset(line, this.xToOffset(line, x), true);
        if (matches == null) {
            return null;
        }
        final int length = PopupHelpItem.maxExampleLength(matches) + 2;
        result = "<html>";
        for (int i = 0; i < matches.size(); ++i) {
            final PopupHelpItem match = matches.get(i);
            result = result + ((i == 0) ? "" : "<br>") + "<tt>" + match.getExamplePaddedToLength(length).replaceAll(" ", "&nbsp;") + "</tt>" + match.getDescription();
        }
        return result + "</html>";
    }
    
    public String getAutoIndent() {
        return Globals.getSettings().getBooleanSetting(19) ? this.getLeadingWhiteSpace() : "";
    }
    
    public String getLeadingWhiteSpace() {
        final int line = this.getCaretLine();
        final int lineLength = this.getLineLength(line);
        String indent = "";
        if (lineLength > 0) {
            final String text = this.getText(this.getLineStartOffset(line), lineLength);
            for (int position = 0; position < text.length(); ++position) {
                final char character = text.charAt(position);
                if (character != '\t' && character != ' ') {
                    break;
                }
                indent += character;
            }
        }
        return indent;
    }
    
    private ArrayList getSyntaxSensitiveHelpAtLineOffset(final int line, final int offset, final boolean exact) {
        ArrayList matches = null;
        final TokenMarker tokenMarker = this.getTokenMarker();
        if (tokenMarker != null) {
            final Segment lineSegment = new Segment();
            this.getLineText(line, lineSegment);
            final Token tokenList;
            Token tokens = tokenList = tokenMarker.markTokens(lineSegment, line);
            int tokenOffset = 0;
            Token tokenAtOffset = null;
            for (Token toke = tokens; toke.id != 127; toke = toke.next) {}
            while (true) {
                final byte id = tokens.id;
                if (id == 127) {
                    break;
                }
                final int length = tokens.length;
                if (offset > tokenOffset && offset <= tokenOffset + length) {
                    tokenAtOffset = tokens;
                    break;
                }
                tokenOffset += length;
                tokens = tokens.next;
            }
            if (tokenAtOffset != null) {
                final String tokenText = lineSegment.toString().substring(tokenOffset, tokenOffset + tokenAtOffset.length);
                if (exact) {
                    matches = tokenMarker.getTokenExactMatchHelp(tokenAtOffset, tokenText);
                }
                else {
                    matches = tokenMarker.getTokenPrefixMatchHelp(lineSegment.toString(), tokenList, tokenAtOffset, tokenText);
                }
            }
        }
        return matches;
    }
    
    private void applySyntaxSensitiveHelp() {
        if (!Globals.getSettings().getBooleanSetting(16)) {
            return;
        }
        final int line = this.getCaretLine();
        final int lineStart = this.getLineStartOffset(line);
        final int offset = Math.max(1, Math.min(this.getLineLength(line), this.getCaretPosition() - lineStart));
        final ArrayList<PopupHelpItem> helpItems = this.getSyntaxSensitiveHelpAtLineOffset(line, offset, false);
        if (helpItems == null && this.popupMenu != null) {
            this.popupMenu.setVisible(false);
            this.popupMenu = null;
        }
        if (helpItems != null) {
            this.popupMenu = new JPopupMenu();
            final int length = PopupHelpItem.maxExampleLength(helpItems) + 2;
            for (int i = 0; i < helpItems.size(); ++i) {
                final PopupHelpItem item = helpItems.get(i);
                final JMenuItem menuItem = new JMenuItem("<html><tt>" + item.getExamplePaddedToLength(length).replaceAll(" ", "&nbsp;") + "</tt>" + item.getDescription() + "</html>");
                if (item.getExact()) {
                    menuItem.setSelected(false);
                }
                else {
                    menuItem.addActionListener(new PopupHelpActionListener(item.getTokenText(), item.getExample()));
                }
                this.popupMenu.add(menuItem);
            }
            this.popupMenu.pack();
            final int y = this.lineToY(line);
            final int x = this.offsetToX(line, offset);
            final int height = this.painter.getFontMetrics(this.painter.getFont()).getHeight();
            final int width = this.painter.getFontMetrics(this.painter.getFont()).charWidth('w');
            final int menuXLoc = x + width + width + width;
            final int menuYLoc = y + height + height;
            this.popupMenu.show(this, menuXLoc, menuYLoc);
            this.requestFocusInWindow();
        }
    }
    
    private void checkAutoIndent(final KeyEvent evt) {
        if (evt.getKeyCode() == 10) {
            final int line = this.getCaretLine();
            if (line <= 0) {
                return;
            }
            final int previousLine = line - 1;
            final int previousLineLength = this.getLineLength(previousLine);
            if (previousLineLength <= 0) {
                return;
            }
            final String previous = this.getText(this.getLineStartOffset(previousLine), previousLineLength);
            String indent = "";
            for (int position = 0; position < previous.length(); ++position) {
                final char character = previous.charAt(position);
                if (character != '\t' && character != ' ') {
                    break;
                }
                indent += character;
            }
            this.overwriteSetSelectedText(indent);
        }
    }
    
    private void checkPopupMenu(final KeyEvent evt) {
        if (evt.getKeyCode() == 8 || evt.getKeyCode() == 127) {
            this.applySyntaxSensitiveHelp();
        }
        if ((evt.getKeyCode() == 10 || evt.getKeyCode() == 27) && this.popupMenu != null && this.popupMenu.isVisible()) {
            this.popupMenu.setVisible(false);
        }
    }
    
    private boolean checkPopupCompletion(final KeyEvent evt) {
        if ((evt.getKeyCode() != 38 && evt.getKeyCode() != 40) || this.popupMenu == null || !this.popupMenu.isVisible() || this.popupMenu.getComponentCount() <= 0) {
            if ((evt.getKeyCode() == 9 || evt.getKeyCode() == 10) && this.popupMenu != null && this.popupMenu.isVisible() && this.popupMenu.getComponentCount() > 0) {
                final MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
                if (path.length < 1 || !(path[path.length - 1] instanceof AbstractButton)) {
                    return false;
                }
                final AbstractButton item = (AbstractButton)path[path.length - 1].getComponent();
                if (item.isEnabled()) {
                    final ActionListener[] listeners = item.getActionListeners();
                    if (listeners.length > 0) {
                        listeners[0].actionPerformed(new ActionEvent(item, 1001, (evt.getKeyCode() == 9) ? "\t" : " "));
                        return true;
                    }
                }
            }
            return false;
        }
        final MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
        if (path.length < 1 || !(path[path.length - 1] instanceof AbstractButton)) {
            return false;
        }
        final AbstractButton item = (AbstractButton)path[path.length - 1].getComponent();
        if (!item.isEnabled()) {
            return false;
        }
        int index = this.popupMenu.getComponentIndex(item);
        if (index < 0) {
            return false;
        }
        if (evt.getKeyCode() == 38) {
            index = ((index == 0) ? (this.popupMenu.getComponentCount() - 1) : (index - 1));
        }
        else {
            index = ((index == this.popupMenu.getComponentCount() - 1) ? 0 : (index + 1));
        }
        final MenuElement[] newPath = { path[0], (MenuElement)this.popupMenu.getComponentAtIndex(index) };
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MenuSelectionManager.defaultManager().setSelectedPath(newPath);
            }
        });
        return true;
    }
    
    static {
        JEditTextArea.LEFT_OF_SCROLLBAR = "los";
        JEditTextArea.POPUP_HELP_TEXT_COLOR = Color.BLACK;
        JEditTextArea.CENTER = "center";
        JEditTextArea.RIGHT = "right";
        JEditTextArea.BOTTOM = "bottom";
        (JEditTextArea.caretTimer = new Timer(500, new CaretBlinker())).setInitialDelay(500);
        JEditTextArea.caretTimer.start();
    }
    
    class ScrollLayout implements LayoutManager
    {
        private Component center;
        private Component right;
        private Component bottom;
        private final Vector leftOfScrollBar;
        
        ScrollLayout() {
            this.leftOfScrollBar = new Vector();
        }
        
        @Override
        public void addLayoutComponent(final String name, final Component comp) {
            if (name.equals(JEditTextArea.CENTER)) {
                this.center = comp;
            }
            else if (name.equals(JEditTextArea.RIGHT)) {
                this.right = comp;
            }
            else if (name.equals(JEditTextArea.BOTTOM)) {
                this.bottom = comp;
            }
            else if (name.equals(JEditTextArea.LEFT_OF_SCROLLBAR)) {
                this.leftOfScrollBar.addElement(comp);
            }
        }
        
        @Override
        public void removeLayoutComponent(final Component comp) {
            if (this.center == comp) {
                this.center = null;
            }
            if (this.right == comp) {
                this.right = null;
            }
            if (this.bottom == comp) {
                this.bottom = null;
            }
            else {
                this.leftOfScrollBar.removeElement(comp);
            }
        }
        
        @Override
        public Dimension preferredLayoutSize(final Container parent) {
            final Dimension dim = new Dimension();
            final Insets insets = JEditTextArea.this.getInsets();
            dim.width = insets.left + insets.right;
            dim.height = insets.top + insets.bottom;
            final Dimension centerPref = this.center.getPreferredSize();
            final Dimension dimension = dim;
            dimension.width += centerPref.width;
            final Dimension dimension2 = dim;
            dimension2.height += centerPref.height;
            final Dimension rightPref = this.right.getPreferredSize();
            final Dimension dimension3 = dim;
            dimension3.width += rightPref.width;
            final Dimension bottomPref = this.bottom.getPreferredSize();
            final Dimension dimension4 = dim;
            dimension4.height += bottomPref.height;
            return dim;
        }
        
        @Override
        public Dimension minimumLayoutSize(final Container parent) {
            final Dimension dim = new Dimension();
            final Insets insets = JEditTextArea.this.getInsets();
            dim.width = insets.left + insets.right;
            dim.height = insets.top + insets.bottom;
            final Dimension centerPref = this.center.getMinimumSize();
            final Dimension dimension = dim;
            dimension.width += centerPref.width;
            final Dimension dimension2 = dim;
            dimension2.height += centerPref.height;
            final Dimension rightPref = this.right.getMinimumSize();
            final Dimension dimension3 = dim;
            dimension3.width += rightPref.width;
            final Dimension bottomPref = this.bottom.getMinimumSize();
            final Dimension dimension4 = dim;
            dimension4.height += bottomPref.height;
            return dim;
        }
        
        @Override
        public void layoutContainer(final Container parent) {
            final Dimension size = parent.getSize();
            final Insets insets = parent.getInsets();
            final int itop = insets.top;
            int ileft = insets.left;
            final int ibottom = insets.bottom;
            final int iright = insets.right;
            final int rightWidth = this.right.getPreferredSize().width;
            final int bottomHeight = this.bottom.getPreferredSize().height;
            final int centerWidth = size.width - rightWidth - ileft - iright;
            final int centerHeight = size.height - bottomHeight - itop - ibottom;
            this.center.setBounds(ileft, itop, centerWidth, centerHeight);
            this.right.setBounds(ileft + centerWidth, itop, rightWidth, centerHeight);
            final Enumeration<Component> status = this.leftOfScrollBar.elements();
            while (status.hasMoreElements()) {
                final Component comp = status.nextElement();
                final Dimension dim = comp.getPreferredSize();
                comp.setBounds(ileft, itop + centerHeight, dim.width, bottomHeight);
                ileft += dim.width;
            }
            this.bottom.setBounds(ileft, itop + centerHeight, size.width - rightWidth - ileft - iright, bottomHeight);
        }
    }
    
    static class CaretBlinker implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            if (JEditTextArea.focusedComponent != null && JEditTextArea.focusedComponent.hasFocus()) {
                JEditTextArea.focusedComponent.blinkCaret();
            }
        }
    }
    
    class MutableCaretEvent extends CaretEvent
    {
        MutableCaretEvent() {
            super(JEditTextArea.this);
        }
        
        @Override
        public int getDot() {
            return JEditTextArea.this.getCaretPosition();
        }
        
        @Override
        public int getMark() {
            return JEditTextArea.this.getMarkPosition();
        }
    }
    
    class AdjustHandler implements AdjustmentListener
    {
        @Override
        public void adjustmentValueChanged(final AdjustmentEvent evt) {
            if (!JEditTextArea.this.scrollBarsInitialized) {
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (evt.getAdjustable() == JEditTextArea.this.vertical) {
                        JEditTextArea.this.setFirstLine(JEditTextArea.this.vertical.getValue());
                    }
                    else {
                        JEditTextArea.this.setHorizontalOffset(-JEditTextArea.this.horizontal.getValue());
                    }
                }
            });
        }
    }
    
    class ComponentHandler extends ComponentAdapter
    {
        @Override
        public void componentResized(final ComponentEvent evt) {
            JEditTextArea.this.recalculateVisibleLines();
            JEditTextArea.this.scrollBarsInitialized = true;
        }
    }
    
    class DocumentHandler implements DocumentListener
    {
        @Override
        public void insertUpdate(final DocumentEvent evt) {
            JEditTextArea.this.documentChanged(evt);
            final int offset = evt.getOffset();
            final int length = evt.getLength();
            if (JEditTextArea.this.unredoing) {
                JEditTextArea.this.select(offset, offset + length);
                return;
            }
            int newStart;
            if (JEditTextArea.this.selectionStart > offset || (JEditTextArea.this.selectionStart == JEditTextArea.this.selectionEnd && JEditTextArea.this.selectionStart == offset)) {
                newStart = JEditTextArea.this.selectionStart + length;
            }
            else {
                newStart = JEditTextArea.this.selectionStart;
            }
            int newEnd;
            if (JEditTextArea.this.selectionEnd >= offset) {
                newEnd = JEditTextArea.this.selectionEnd + length;
            }
            else {
                newEnd = JEditTextArea.this.selectionEnd;
            }
            JEditTextArea.this.select(newStart, newEnd);
        }
        
        @Override
        public void removeUpdate(final DocumentEvent evt) {
            JEditTextArea.this.documentChanged(evt);
            final int offset = evt.getOffset();
            final int length = evt.getLength();
            if (JEditTextArea.this.unredoing) {
                JEditTextArea.this.select(offset, offset);
                JEditTextArea.this.setCaretPosition(offset);
                return;
            }
            int newStart;
            if (JEditTextArea.this.selectionStart > offset) {
                if (JEditTextArea.this.selectionStart > offset + length) {
                    newStart = JEditTextArea.this.selectionStart - length;
                }
                else {
                    newStart = offset;
                }
            }
            else {
                newStart = JEditTextArea.this.selectionStart;
            }
            int newEnd;
            if (JEditTextArea.this.selectionEnd > offset) {
                if (JEditTextArea.this.selectionEnd > offset + length) {
                    newEnd = JEditTextArea.this.selectionEnd - length;
                }
                else {
                    newEnd = offset;
                }
            }
            else {
                newEnd = JEditTextArea.this.selectionEnd;
            }
            JEditTextArea.this.select(newStart, newEnd);
        }
        
        @Override
        public void changedUpdate(final DocumentEvent evt) {
        }
    }
    
    class DragHandler implements MouseMotionListener
    {
        @Override
        public void mouseDragged(final MouseEvent evt) {
            if (JEditTextArea.this.popup != null && JEditTextArea.this.popup.isVisible()) {
                return;
            }
            JEditTextArea.this.setSelectionRectangular((evt.getModifiers() & 0x2) != 0x0);
            JEditTextArea.this.select(JEditTextArea.this.getMarkPosition(), JEditTextArea.this.xyToOffset(evt.getX(), evt.getY()));
        }
        
        @Override
        public void mouseMoved(final MouseEvent evt) {
        }
    }
    
    class FocusHandler implements FocusListener
    {
        @Override
        public void focusGained(final FocusEvent evt) {
            JEditTextArea.this.setCaretVisible(true);
            JEditTextArea.focusedComponent = JEditTextArea.this;
        }
        
        @Override
        public void focusLost(final FocusEvent evt) {
            JEditTextArea.this.setCaretVisible(false);
            JEditTextArea.focusedComponent = null;
        }
    }
    
    class MouseWheelHandler implements MouseWheelListener
    {
        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            final int maxMotion = Math.abs(e.getWheelRotation()) * 3;
            if (e.getWheelRotation() < 0) {
                JEditTextArea.this.setFirstLine(JEditTextArea.this.getFirstLine() - Math.min(maxMotion, JEditTextArea.this.getFirstLine()));
            }
            else {
                JEditTextArea.this.setFirstLine(JEditTextArea.this.getFirstLine() + Math.min(maxMotion, Math.max(0, JEditTextArea.this.getLineCount() - (JEditTextArea.this.getFirstLine() + JEditTextArea.this.visibleLines))));
            }
        }
    }
    
    class MouseHandler extends MouseAdapter
    {
        @Override
        public void mousePressed(final MouseEvent evt) {
            JEditTextArea.this.requestFocus();
            JEditTextArea.this.setCaretVisible(true);
            JEditTextArea.focusedComponent = JEditTextArea.this;
            if ((evt.getModifiers() & 0x4) != 0x0 && JEditTextArea.this.popup != null) {
                JEditTextArea.this.popup.show(JEditTextArea.this.painter, evt.getX(), evt.getY());
                return;
            }
            final int line = JEditTextArea.this.yToLine(evt.getY());
            final int offset = JEditTextArea.this.xToOffset(line, evt.getX());
            final int dot = JEditTextArea.this.getLineStartOffset(line) + offset;
            switch (evt.getClickCount()) {
                case 1: {
                    this.doSingleClick(evt, line, offset, dot);
                    break;
                }
                case 2: {
                    try {
                        this.doDoubleClick(evt, line, offset, dot);
                    }
                    catch (BadLocationException bl) {
                        bl.printStackTrace();
                    }
                    break;
                }
                case 3: {
                    this.doTripleClick(evt, line, offset, dot);
                    break;
                }
            }
        }
        
        private void doSingleClick(final MouseEvent evt, final int line, final int offset, final int dot) {
            if ((evt.getModifiers() & 0x1) != 0x0) {
                JEditTextArea.this.rectSelect = ((evt.getModifiers() & 0x2) != 0x0);
                JEditTextArea.this.select(JEditTextArea.this.getMarkPosition(), dot);
            }
            else {
                JEditTextArea.this.setCaretPosition(dot);
            }
        }
        
        private void doDoubleClick(final MouseEvent evt, final int line, final int offset, final int dot) throws BadLocationException {
            if (JEditTextArea.this.getLineLength(line) == 0) {
                return;
            }
            try {
                int bracket = TextUtilities.findMatchingBracket(JEditTextArea.this.document, Math.max(0, dot - 1));
                if (bracket != -1) {
                    int mark = JEditTextArea.this.getMarkPosition();
                    if (bracket > mark) {
                        ++bracket;
                        --mark;
                    }
                    JEditTextArea.this.select(mark, bracket);
                    return;
                }
            }
            catch (BadLocationException bl) {
                bl.printStackTrace();
            }
            final String lineText = JEditTextArea.this.getLineText(line);
            char ch = lineText.charAt(Math.max(0, offset - 1));
            String noWordSep = (String)JEditTextArea.this.document.getProperty("noWordSep");
            if (noWordSep == null) {
                noWordSep = "";
            }
            final boolean selectNoLetter = !Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1;
            int wordStart = 0;
            for (int i = offset - 1; i >= 0; --i) {
                ch = lineText.charAt(i);
                if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
                    wordStart = i + 1;
                    break;
                }
            }
            int wordEnd = lineText.length();
            for (int j = offset; j < lineText.length(); ++j) {
                ch = lineText.charAt(j);
                if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
                    wordEnd = j;
                    break;
                }
            }
            final int lineStart = JEditTextArea.this.getLineStartOffset(line);
            JEditTextArea.this.select(lineStart + wordStart, lineStart + wordEnd);
        }
        
        private void doTripleClick(final MouseEvent evt, final int line, final int offset, final int dot) {
            JEditTextArea.this.select(JEditTextArea.this.getLineStartOffset(line), JEditTextArea.this.getLineEndOffset(line) - 1);
        }
    }
    
    class CaretUndo extends AbstractUndoableEdit
    {
        private int start;
        private int end;
        
        CaretUndo(final int start, final int end) {
            this.start = start;
            this.end = end;
        }
        
        @Override
        public boolean isSignificant() {
            return false;
        }
        
        @Override
        public String getPresentationName() {
            return "caret move";
        }
        
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            JEditTextArea.this.select(this.start, this.end);
        }
        
        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            JEditTextArea.this.select(this.start, this.end);
        }
        
        @Override
        public boolean addEdit(final UndoableEdit edit) {
            if (edit instanceof CaretUndo) {
                final CaretUndo cedit = (CaretUndo)edit;
                this.start = cedit.start;
                this.end = cedit.end;
                cedit.die();
                return true;
            }
            return false;
        }
    }
    
    private class PopupHelpActionListener implements ActionListener
    {
        private final String tokenText;
        private final String text;
        
        public PopupHelpActionListener(final String tokenText, final String text) {
            this.tokenText = tokenText;
            this.text = text.split(" ")[0];
        }
        
        @Override
        public void actionPerformed(final ActionEvent e) {
            final String insert = (e.getActionCommand().charAt(0) == '\t') ? "\t" : " ";
            if (this.tokenText.length() >= this.text.length()) {
                JEditTextArea.this.overwriteSetSelectedText(insert);
            }
            else {
                JEditTextArea.this.overwriteSetSelectedText(this.text.substring(this.tokenText.length()) + insert);
            }
        }
    }
}
