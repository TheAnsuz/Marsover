

package mars.venus.editors.jeditsyntax;

import javax.swing.text.BadLocationException;
import java.awt.event.KeyEvent;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.util.EventObject;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;

public abstract class InputHandler extends KeyAdapter
{
    public static final String SMART_HOME_END_PROPERTY = "InputHandler.homeEnd";
    public static final ActionListener BACKSPACE;
    public static final ActionListener BACKSPACE_WORD;
    public static final ActionListener DELETE;
    public static final ActionListener DELETE_WORD;
    public static final ActionListener END;
    public static final ActionListener DOCUMENT_END;
    public static final ActionListener SELECT_ALL;
    public static final ActionListener SELECT_END;
    public static final ActionListener SELECT_DOC_END;
    public static final ActionListener INSERT_BREAK;
    public static final ActionListener INSERT_TAB;
    public static final ActionListener HOME;
    public static final ActionListener DOCUMENT_HOME;
    public static final ActionListener SELECT_HOME;
    public static final ActionListener SELECT_DOC_HOME;
    public static final ActionListener NEXT_CHAR;
    public static final ActionListener NEXT_LINE;
    public static final ActionListener NEXT_PAGE;
    public static final ActionListener NEXT_WORD;
    public static final ActionListener SELECT_NEXT_CHAR;
    public static final ActionListener SELECT_NEXT_LINE;
    public static final ActionListener SELECT_NEXT_PAGE;
    public static final ActionListener SELECT_NEXT_WORD;
    public static final ActionListener OVERWRITE;
    public static final ActionListener PREV_CHAR;
    public static final ActionListener PREV_LINE;
    public static final ActionListener PREV_PAGE;
    public static final ActionListener PREV_WORD;
    public static final ActionListener SELECT_PREV_CHAR;
    public static final ActionListener SELECT_PREV_LINE;
    public static final ActionListener SELECT_PREV_PAGE;
    public static final ActionListener SELECT_PREV_WORD;
    public static final ActionListener REPEAT;
    public static final ActionListener TOGGLE_RECT;
    public static final ActionListener CLIP_COPY;
    public static final ActionListener CLIP_PASTE;
    public static final ActionListener CLIP_CUT;
    public static final ActionListener INSERT_CHAR;
    private static Hashtable<String,ActionListener> actions;
    protected ActionListener grabAction;
    protected boolean repeat;
    protected int repeatCount;
    protected MacroRecorder recorder;
    
    public static ActionListener getAction(final String name) {
        return InputHandler.actions.get(name);
    }
    
    public static String getActionName(final ActionListener listener) {
        final Enumeration<String> enumeration = getActions();
        while (enumeration.hasMoreElements()) {
            final String name = enumeration.nextElement();
            final ActionListener _listener = getAction(name);
            if (_listener == listener) {
                return name;
            }
        }
        return null;
    }
    
    public static Enumeration getActions() {
        return InputHandler.actions.keys();
    }
    
    public abstract void addDefaultKeyBindings();
    
    public abstract void addKeyBinding(final String p0, final ActionListener p1);
    
    public abstract void removeKeyBinding(final String p0);
    
    public abstract void removeAllKeyBindings();
    
    public void grabNextKeyStroke(final ActionListener listener) {
        this.grabAction = listener;
    }
    
    public boolean isRepeatEnabled() {
        return this.repeat;
    }
    
    public void setRepeatEnabled(final boolean repeat) {
        this.repeat = repeat;
    }
    
    public int getRepeatCount() {
        return this.repeat ? Math.max(1, this.repeatCount) : 1;
    }
    
    public void setRepeatCount(final int repeatCount) {
        this.repeatCount = repeatCount;
    }
    
    public MacroRecorder getMacroRecorder() {
        return this.recorder;
    }
    
    public void setMacroRecorder(final MacroRecorder recorder) {
        this.recorder = recorder;
    }
    
    public abstract InputHandler copy();
    
    public void executeAction(final ActionListener listener, final Object source, final String actionCommand) {
        final ActionEvent evt = new ActionEvent(source, 1001, actionCommand);
        if (listener instanceof Wrapper) {
            listener.actionPerformed(evt);
            return;
        }
        final boolean _repeat = this.repeat;
        final int _repeatCount = this.getRepeatCount();
        if (listener instanceof NonRepeatable) {
            listener.actionPerformed(evt);
        }
        else {
            for (int i = 0; i < Math.max(1, this.repeatCount); ++i) {
                listener.actionPerformed(evt);
            }
        }
        if (this.grabAction == null) {
            if (this.recorder != null && !(listener instanceof NonRecordable)) {
                if (_repeatCount != 1) {
                    this.recorder.actionPerformed(InputHandler.REPEAT, String.valueOf(_repeatCount));
                }
                this.recorder.actionPerformed(listener, actionCommand);
            }
            if (_repeat) {
                this.repeat = false;
                this.repeatCount = 0;
            }
        }
    }
    
    public static JEditTextArea getTextArea(final EventObject evt) {
        Label_0066: {
            if (evt != null) {
                final Object o = evt.getSource();
                if (o instanceof Component) {
                    Component c = (Component)o;
                    while (!(c instanceof JEditTextArea)) {
                        if (c == null) {
                            break Label_0066;
                        }
                        if (c instanceof JPopupMenu) {
                            c = ((JPopupMenu)c).getInvoker();
                        }
                        else {
                            c = c.getParent();
                        }
                    }
                    return (JEditTextArea)c;
                }
            }
        }
        System.err.println("BUG: getTextArea() returning null");
        System.err.println("Report this to Slava Pestov <sp@gjt.org>");
        return null;
    }
    
    protected void handleGrabAction(final KeyEvent evt) {
        final ActionListener _grabAction = this.grabAction;
        this.grabAction = null;
        this.executeAction(_grabAction, evt.getSource(), String.valueOf(evt.getKeyChar()));
    }
    
    static {
        BACKSPACE = new backspace();
        BACKSPACE_WORD = new backspace_word();
        DELETE = new delete();
        DELETE_WORD = new delete_word();
        END = new end(false);
        DOCUMENT_END = new document_end(false);
        SELECT_ALL = new select_all();
        SELECT_END = new end(true);
        SELECT_DOC_END = new document_end(true);
        INSERT_BREAK = new insert_break();
        INSERT_TAB = new insert_tab();
        HOME = new home(false);
        DOCUMENT_HOME = new document_home(false);
        SELECT_HOME = new home(true);
        SELECT_DOC_HOME = new document_home(true);
        NEXT_CHAR = new next_char(false);
        NEXT_LINE = new next_line(false);
        NEXT_PAGE = new next_page(false);
        NEXT_WORD = new next_word(false);
        SELECT_NEXT_CHAR = new next_char(true);
        SELECT_NEXT_LINE = new next_line(true);
        SELECT_NEXT_PAGE = new next_page(true);
        SELECT_NEXT_WORD = new next_word(true);
        OVERWRITE = new overwrite();
        PREV_CHAR = new prev_char(false);
        PREV_LINE = new prev_line(false);
        PREV_PAGE = new prev_page(false);
        PREV_WORD = new prev_word(false);
        SELECT_PREV_CHAR = new prev_char(true);
        SELECT_PREV_LINE = new prev_line(true);
        SELECT_PREV_PAGE = new prev_page(true);
        SELECT_PREV_WORD = new prev_word(true);
        REPEAT = new repeat();
        TOGGLE_RECT = new toggle_rect();
        CLIP_COPY = new clip_copy();
        CLIP_PASTE = new clip_paste();
        CLIP_CUT = new clip_cut();
        INSERT_CHAR = new insert_char();
        (InputHandler.actions = new Hashtable()).put("backspace", InputHandler.BACKSPACE);
        InputHandler.actions.put("backspace-word", InputHandler.BACKSPACE_WORD);
        InputHandler.actions.put("delete", InputHandler.DELETE);
        InputHandler.actions.put("delete-word", InputHandler.DELETE_WORD);
        InputHandler.actions.put("end", InputHandler.END);
        InputHandler.actions.put("select-all", InputHandler.SELECT_ALL);
        InputHandler.actions.put("select-end", InputHandler.SELECT_END);
        InputHandler.actions.put("document-end", InputHandler.DOCUMENT_END);
        InputHandler.actions.put("select-doc-end", InputHandler.SELECT_DOC_END);
        InputHandler.actions.put("insert-break", InputHandler.INSERT_BREAK);
        InputHandler.actions.put("insert-tab", InputHandler.INSERT_TAB);
        InputHandler.actions.put("home", InputHandler.HOME);
        InputHandler.actions.put("select-home", InputHandler.SELECT_HOME);
        InputHandler.actions.put("document-home", InputHandler.DOCUMENT_HOME);
        InputHandler.actions.put("select-doc-home", InputHandler.SELECT_DOC_HOME);
        InputHandler.actions.put("next-char", InputHandler.NEXT_CHAR);
        InputHandler.actions.put("next-line", InputHandler.NEXT_LINE);
        InputHandler.actions.put("next-page", InputHandler.NEXT_PAGE);
        InputHandler.actions.put("next-word", InputHandler.NEXT_WORD);
        InputHandler.actions.put("select-next-char", InputHandler.SELECT_NEXT_CHAR);
        InputHandler.actions.put("select-next-line", InputHandler.SELECT_NEXT_LINE);
        InputHandler.actions.put("select-next-page", InputHandler.SELECT_NEXT_PAGE);
        InputHandler.actions.put("select-next-word", InputHandler.SELECT_NEXT_WORD);
        InputHandler.actions.put("overwrite", InputHandler.OVERWRITE);
        InputHandler.actions.put("prev-char", InputHandler.PREV_CHAR);
        InputHandler.actions.put("prev-line", InputHandler.PREV_LINE);
        InputHandler.actions.put("prev-page", InputHandler.PREV_PAGE);
        InputHandler.actions.put("prev-word", InputHandler.PREV_WORD);
        InputHandler.actions.put("select-prev-char", InputHandler.SELECT_PREV_CHAR);
        InputHandler.actions.put("select-prev-line", InputHandler.SELECT_PREV_LINE);
        InputHandler.actions.put("select-prev-page", InputHandler.SELECT_PREV_PAGE);
        InputHandler.actions.put("select-prev-word", InputHandler.SELECT_PREV_WORD);
        InputHandler.actions.put("repeat", InputHandler.REPEAT);
        InputHandler.actions.put("toggle-rect", InputHandler.TOGGLE_RECT);
        InputHandler.actions.put("insert-char", InputHandler.INSERT_CHAR);
        InputHandler.actions.put("clipboard-copy", InputHandler.CLIP_COPY);
        InputHandler.actions.put("clipboard-paste", InputHandler.CLIP_PASTE);
        InputHandler.actions.put("clipboard-cut", InputHandler.CLIP_CUT);
    }
    
    public static class backspace implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            if (textArea.getSelectionStart() != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
            }
            else {
                final int caret = textArea.getCaretPosition();
                if (caret == 0) {
                    textArea.getToolkit().beep();
                    return;
                }
                try {
                    textArea.getDocument().remove(caret - 1, 1);
                }
                catch (BadLocationException bl) {
                    bl.printStackTrace();
                }
            }
        }
    }
    
    public static class backspace_word implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            final int start = textArea.getSelectionStart();
            if (start != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
            }
            final int line = textArea.getCaretLine();
            final int lineStart = textArea.getLineStartOffset(line);
            int caret = start - lineStart;
            final String lineText = textArea.getLineText(textArea.getCaretLine());
            if (caret == 0) {
                if (lineStart == 0) {
                    textArea.getToolkit().beep();
                    return;
                }
                --caret;
            }
            else {
                final String noWordSep = (String)textArea.getDocument().getProperty("noWordSep");
                caret = TextUtilities.findWordStart(lineText, caret, noWordSep);
            }
            try {
                textArea.getDocument().remove(caret + lineStart, start - (caret + lineStart));
            }
            catch (BadLocationException bl) {
                bl.printStackTrace();
            }
        }
    }
    
    public static class delete implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            if (textArea.getSelectionStart() != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
            }
            else {
                final int caret = textArea.getCaretPosition();
                if (caret == textArea.getDocumentLength()) {
                    textArea.getToolkit().beep();
                    return;
                }
                try {
                    textArea.getDocument().remove(caret, 1);
                }
                catch (BadLocationException bl) {
                    bl.printStackTrace();
                }
            }
        }
    }
    
    public static class delete_word implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            final int start = textArea.getSelectionStart();
            if (start != textArea.getSelectionEnd()) {
                textArea.setSelectedText("");
            }
            final int line = textArea.getCaretLine();
            final int lineStart = textArea.getLineStartOffset(line);
            int caret = start - lineStart;
            final String lineText = textArea.getLineText(textArea.getCaretLine());
            if (caret == lineText.length()) {
                if (lineStart + caret == textArea.getDocumentLength()) {
                    textArea.getToolkit().beep();
                    return;
                }
                ++caret;
            }
            else {
                final String noWordSep = (String)textArea.getDocument().getProperty("noWordSep");
                caret = TextUtilities.findWordEnd(lineText, caret, noWordSep);
            }
            try {
                textArea.getDocument().remove(start, caret + lineStart - start);
            }
            catch (BadLocationException bl) {
                bl.printStackTrace();
            }
        }
    }
    
    public static class end implements ActionListener
    {
        private boolean select;
        
        public end(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int caret = textArea.getCaretPosition();
            final int lastOfLine = textArea.getLineEndOffset(textArea.getCaretLine()) - 1;
            int lastVisibleLine = textArea.getFirstLine() + textArea.getVisibleLines();
            if (lastVisibleLine >= textArea.getLineCount()) {
                lastVisibleLine = Math.min(textArea.getLineCount() - 1, lastVisibleLine);
            }
            else {
                lastVisibleLine -= textArea.getElectricScroll() + 1;
            }
            final int lastVisible = textArea.getLineEndOffset(lastVisibleLine) - 1;
            final int lastDocument = textArea.getDocumentLength();
            if (caret == lastDocument) {
                textArea.getToolkit().beep();
                return;
            }
            if (!Boolean.TRUE.equals(textArea.getClientProperty("InputHandler.homeEnd"))) {
                caret = lastOfLine;
            }
            else if (caret == lastVisible) {
                caret = lastDocument;
            }
            else if (caret == lastOfLine) {
                caret = lastVisible;
            }
            else {
                caret = lastOfLine;
            }
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret);
            }
            else {
                textArea.setCaretPosition(caret);
            }
        }
    }
    
    public static class select_all implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.selectAll();
        }
    }
    
    public static class document_end implements ActionListener
    {
        private boolean select;
        
        public document_end(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), textArea.getDocumentLength());
            }
            else {
                textArea.setCaretPosition(textArea.getDocumentLength());
            }
        }
    }
    
    public static class home implements ActionListener
    {
        private boolean select;
        
        public home(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int caret = textArea.getCaretPosition();
            final int firstLine = textArea.getFirstLine();
            final int firstOfLine = textArea.getLineStartOffset(textArea.getCaretLine());
            final int firstVisibleLine = (firstLine == 0) ? 0 : (firstLine + textArea.getElectricScroll());
            final int firstVisible = textArea.getLineStartOffset(firstVisibleLine);
            if (caret == 0) {
                textArea.getToolkit().beep();
                return;
            }
            if (!Boolean.TRUE.equals(textArea.getClientProperty("InputHandler.homeEnd"))) {
                caret = firstOfLine;
            }
            else if (caret == firstVisible) {
                caret = 0;
            }
            else if (caret == firstOfLine) {
                caret = firstVisible;
            }
            else {
                caret = firstOfLine;
            }
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret);
            }
            else {
                textArea.setCaretPosition(caret);
            }
        }
    }
    
    public static class document_home implements ActionListener
    {
        private boolean select;
        
        public document_home(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), 0);
            }
            else {
                textArea.setCaretPosition(0);
            }
        }
    }
    
    public static class insert_break implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            textArea.setSelectedText("\n" + textArea.getAutoIndent());
        }
    }
    
    public static class insert_tab implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            if (!textArea.isEditable()) {
                textArea.getToolkit().beep();
                return;
            }
            textArea.overwriteSetSelectedText("\t");
        }
    }
    
    public static class next_char implements ActionListener
    {
        private boolean select;
        
        public next_char(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            final int caret = textArea.getCaretPosition();
            if (caret == textArea.getDocumentLength()) {
                textArea.getToolkit().beep();
                return;
            }
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret + 1);
            }
            else {
                textArea.setCaretPosition(caret + 1);
            }
        }
    }
    
    public static class next_line implements ActionListener
    {
        private boolean select;
        
        public next_line(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int caret = textArea.getCaretPosition();
            final int line = textArea.getCaretLine();
            if (line == textArea.getLineCount() - 1) {
                textArea.getToolkit().beep();
                return;
            }
            int magic = textArea.getMagicCaretPosition();
            if (magic == -1) {
                magic = textArea.offsetToX(line, caret - textArea.getLineStartOffset(line));
            }
            caret = textArea.getLineStartOffset(line + 1) + textArea.xToOffset(line + 1, magic);
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret);
            }
            else {
                textArea.setCaretPosition(caret);
            }
            textArea.setMagicCaretPosition(magic);
        }
    }
    
    public static class next_page implements ActionListener
    {
        private boolean select;
        
        public next_page(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            final int lineCount = textArea.getLineCount();
            int firstLine = textArea.getFirstLine();
            final int visibleLines = textArea.getVisibleLines();
            final int line = textArea.getCaretLine();
            firstLine += visibleLines;
            if (firstLine + visibleLines >= lineCount - 1) {
                firstLine = lineCount - visibleLines;
            }
            textArea.setFirstLine(firstLine);
            final int caret = textArea.getLineStartOffset(Math.min(textArea.getLineCount() - 1, line + visibleLines));
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret);
            }
            else {
                textArea.setCaretPosition(caret);
            }
        }
    }
    
    public static class next_word implements ActionListener
    {
        private boolean select;
        
        public next_word(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int caret = textArea.getCaretPosition();
            final int line = textArea.getCaretLine();
            final int lineStart = textArea.getLineStartOffset(line);
            caret -= lineStart;
            final String lineText = textArea.getLineText(textArea.getCaretLine());
            if (caret == lineText.length()) {
                if (lineStart + caret == textArea.getDocumentLength()) {
                    textArea.getToolkit().beep();
                    return;
                }
                ++caret;
            }
            else {
                final String noWordSep = (String)textArea.getDocument().getProperty("noWordSep");
                caret = TextUtilities.findWordEnd(lineText, caret, noWordSep);
            }
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), lineStart + caret);
            }
            else {
                textArea.setCaretPosition(lineStart + caret);
            }
        }
    }
    
    public static class overwrite implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.setOverwriteEnabled(!textArea.isOverwriteEnabled());
        }
    }
    
    public static class prev_char implements ActionListener
    {
        private boolean select;
        
        public prev_char(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            final int caret = textArea.getCaretPosition();
            if (caret == 0) {
                textArea.getToolkit().beep();
                return;
            }
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret - 1);
            }
            else {
                textArea.setCaretPosition(caret - 1);
            }
        }
    }
    
    public static class prev_line implements ActionListener
    {
        private boolean select;
        
        public prev_line(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int caret = textArea.getCaretPosition();
            final int line = textArea.getCaretLine();
            if (line == 0) {
                textArea.getToolkit().beep();
                return;
            }
            int magic = textArea.getMagicCaretPosition();
            if (magic == -1) {
                magic = textArea.offsetToX(line, caret - textArea.getLineStartOffset(line));
            }
            caret = textArea.getLineStartOffset(line - 1) + textArea.xToOffset(line - 1, magic);
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret);
            }
            else {
                textArea.setCaretPosition(caret);
            }
            textArea.setMagicCaretPosition(magic);
        }
    }
    
    public static class prev_page implements ActionListener
    {
        private boolean select;
        
        public prev_page(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int firstLine = textArea.getFirstLine();
            final int visibleLines = textArea.getVisibleLines();
            final int line = textArea.getCaretLine();
            if (firstLine < visibleLines) {
                firstLine = visibleLines;
            }
            textArea.setFirstLine(firstLine - visibleLines);
            final int caret = textArea.getLineStartOffset(Math.max(0, line - visibleLines));
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), caret);
            }
            else {
                textArea.setCaretPosition(caret);
            }
        }
    }
    
    public static class prev_word implements ActionListener
    {
        private boolean select;
        
        public prev_word(final boolean select) {
            this.select = select;
        }
        
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            int caret = textArea.getCaretPosition();
            final int line = textArea.getCaretLine();
            final int lineStart = textArea.getLineStartOffset(line);
            caret -= lineStart;
            final String lineText = textArea.getLineText(textArea.getCaretLine());
            if (caret == 0) {
                if (lineStart == 0) {
                    textArea.getToolkit().beep();
                    return;
                }
                --caret;
            }
            else {
                final String noWordSep = (String)textArea.getDocument().getProperty("noWordSep");
                caret = TextUtilities.findWordStart(lineText, caret, noWordSep);
            }
            if (this.select) {
                textArea.select(textArea.getMarkPosition(), lineStart + caret);
            }
            else {
                textArea.setCaretPosition(lineStart + caret);
            }
        }
    }
    
    public static class repeat implements ActionListener, NonRecordable
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.getInputHandler().setRepeatEnabled(true);
            final String actionCommand = evt.getActionCommand();
            if (actionCommand != null) {
                textArea.getInputHandler().setRepeatCount(Integer.parseInt(actionCommand));
            }
        }
    }
    
    public static class toggle_rect implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.setSelectionRectangular(!textArea.isSelectionRectangular());
        }
    }
    
    public static class insert_char implements ActionListener, NonRepeatable
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            final String str = evt.getActionCommand();
            final int repeatCount = textArea.getInputHandler().getRepeatCount();
            if (textArea.isEditable()) {
                final StringBuffer buf = new StringBuffer();
                for (int i = 0; i < repeatCount; ++i) {
                    buf.append(str);
                }
                textArea.overwriteSetSelectedText(buf.toString());
            }
            else {
                textArea.getToolkit().beep();
            }
        }
    }
    
    public static class clip_copy implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.copy();
        }
    }
    
    public static class clip_paste implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.paste();
        }
    }
    
    public static class clip_cut implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            final JEditTextArea textArea = InputHandler.getTextArea(evt);
            textArea.cut();
        }
    }
    
    public interface NonRepeatable
    {
    }
    
    public interface NonRecordable
    {
    }
    
    public interface MacroRecorder
    {
        void actionPerformed(final ActionListener p0, final String p1);
    }
    
    public interface Wrapper
    {
    }
}
