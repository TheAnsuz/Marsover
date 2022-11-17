

package mars.venus.editors.jeditsyntax;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import mars.Globals;
import mars.venus.EditPane;
import mars.venus.editors.MARSTextEditingArea;
import mars.venus.editors.jeditsyntax.tokenmarker.MIPSTokenMarker;

public class JEditBasedTextArea extends JEditTextArea implements MARSTextEditingArea, CaretListener
{
    private EditPane editPane;
    private UndoManager undoManager;
    private final UndoableEditListener undoableEditListener;
    private boolean isCompoundEdit;
    private CompoundEdit compoundEdit;
    private final JEditBasedTextArea sourceCode;
    
    public JEditBasedTextArea(final EditPane editPain, final JComponent lineNumbers) {
        super(lineNumbers);
        this.isCompoundEdit = false;
        this.editPane = editPain;
        this.undoManager = new UndoManager();
        this.compoundEdit = new CompoundEdit();
        this.sourceCode = this;
        this.undoableEditListener = new UndoableEditListener() {
            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {
                if (JEditBasedTextArea.this.isCompoundEdit) {
                    JEditBasedTextArea.this.compoundEdit.addEdit(e.getEdit());
                }
                else {
                    JEditBasedTextArea.this.undoManager.addEdit(e.getEdit());
                    JEditBasedTextArea.this.editPane.updateUndoState();
                    JEditBasedTextArea.this.editPane.updateRedoState();
                }
            }
        };
        this.getDocument().addUndoableEditListener(this.undoableEditListener);
        this.setFont(Globals.getSettings().getEditorFont());
        this.setTokenMarker(new MIPSTokenMarker());
        this.addCaretListener(this);
    }
    
    @Override
    public void setFont(final Font f) {
        this.getPainter().setFont(f);
    }
    
    @Override
    public Font getFont() {
        return this.getPainter().getFont();
    }
    
    @Override
    public void setLineHighlightEnabled(final boolean highlight) {
        this.getPainter().setLineHighlightEnabled(highlight);
    }
    
    @Override
    public void setCaretBlinkRate(final int rate) {
        if (rate == 0) {
            this.caretBlinks = false;
        }
        if (rate > 0) {
            this.caretBlinks = true;
            this.caretBlinkRate = rate;
            JEditBasedTextArea.caretTimer.setDelay(rate);
            JEditBasedTextArea.caretTimer.setInitialDelay(rate);
            JEditBasedTextArea.caretTimer.restart();
        }
    }
    
    @Override
    public void setTabSize(final int chars) {
        this.painter.setTabSize(chars);
    }
    
    @Override
    public void updateSyntaxStyles() {
        this.painter.setStyles(SyntaxUtilities.getCurrentSyntaxStyles());
    }
    
    @Override
    public Component getOuterComponent() {
        return this;
    }
    
    @Override
    public void discardAllUndoableEdits() {
        this.undoManager.discardAllEdits();
    }
    
    @Override
    public void caretUpdate(final CaretEvent e) {
        this.editPane.displayCaretPosition(e.getDot());
    }
    
    @Override
    public void replaceSelection(final String replacementText) {
        this.setSelectedText(replacementText);
    }
    
    @Override
    public void setSelectionVisible(final boolean vis) {
    }
    
    @Override
    public void setSourceCode(final String s, final boolean editable) {
        this.setText(s);
        this.setBackground(editable ? Color.WHITE : Color.GRAY);
        this.setEditable(editable);
        this.setEnabled(editable);
        this.setCaretPosition(0);
        if (editable) {
            this.requestFocusInWindow();
        }
    }
    
    @Override
    public UndoManager getUndoManager() {
        return this.undoManager;
    }
    
    @Override
    public void undo() {
        this.unredoing = true;
        try {
            this.undoManager.undo();
        }
        catch (CannotUndoException ex) {
            System.out.println("Unable to undo: " + ex);
            ex.printStackTrace();
        }
        this.unredoing = false;
        this.setCaretVisible(true);
    }
    
    @Override
    public void redo() {
        this.unredoing = true;
        try {
            this.undoManager.redo();
        }
        catch (CannotRedoException ex) {
            System.out.println("Unable to redo: " + ex);
            ex.printStackTrace();
        }
        this.unredoing = false;
        this.setCaretVisible(true);
    }
    
    @Override
    public int doFindText(final String find, final boolean caseSensitive) {
        final int findPosn = this.sourceCode.getCaretPosition();
        int nextPosn = 0;
        nextPosn = this.nextIndex(this.sourceCode.getText(), find, findPosn, caseSensitive);
        if (nextPosn >= 0) {
            this.sourceCode.requestFocus();
            this.sourceCode.setSelectionStart(nextPosn);
            this.sourceCode.setSelectionEnd(nextPosn + find.length());
            this.sourceCode.setSelectionStart(nextPosn);
            return 1;
        }
        return 0;
    }
    
    public int nextIndex(final String input, final String find, final int start, final boolean caseSensitive) {
        int textPosn = -1;
        if (input != null && find != null && start < input.length()) {
            if (caseSensitive) {
                textPosn = input.indexOf(find, start);
                if (start > 0 && textPosn < 0) {
                    textPosn = input.indexOf(find);
                }
            }
            else {
                final String lowerCaseText = input.toLowerCase();
                textPosn = lowerCaseText.indexOf(find.toLowerCase(), start);
                if (start > 0 && textPosn < 0) {
                    textPosn = lowerCaseText.indexOf(find.toLowerCase());
                }
            }
        }
        return textPosn;
    }
    
    @Override
    public int doReplace(final String find, final String replace, final boolean caseSensitive) {
        int nextPosn = 0;
        if (find == null || !find.equals(this.sourceCode.getSelectedText()) || this.sourceCode.getSelectionEnd() != this.sourceCode.getCaretPosition()) {
            return this.doFindText(find, caseSensitive);
        }
        nextPosn = this.sourceCode.getSelectionStart();
        this.sourceCode.grabFocus();
        this.sourceCode.setSelectionStart(nextPosn);
        this.sourceCode.setSelectionEnd(nextPosn + find.length());
        this.sourceCode.setSelectionStart(nextPosn);
        this.isCompoundEdit = true;
        this.compoundEdit = new CompoundEdit();
        this.sourceCode.replaceSelection(replace);
        this.compoundEdit.end();
        this.undoManager.addEdit(this.compoundEdit);
        this.editPane.updateUndoState();
        this.editPane.updateRedoState();
        this.isCompoundEdit = false;
        this.sourceCode.setCaretPosition(nextPosn + replace.length());
        if (this.doFindText(find, caseSensitive) == 0) {
            return 3;
        }
        return 2;
    }
    
    @Override
    public int doReplaceAll(final String find, final String replace, final boolean caseSensitive) {
        int nextPosn = 0;
        int findPosn = 0;
        int replaceCount = 0;
        this.compoundEdit = null;
        this.isCompoundEdit = true;
        while (nextPosn >= 0) {
            nextPosn = this.nextIndex(this.sourceCode.getText(), find, findPosn, caseSensitive);
            if (nextPosn >= 0) {
                if (nextPosn < findPosn) {
                    break;
                }
                this.sourceCode.grabFocus();
                this.sourceCode.setSelectionStart(nextPosn);
                this.sourceCode.setSelectionEnd(nextPosn + find.length());
                this.sourceCode.setSelectionStart(nextPosn);
                if (this.compoundEdit == null) {
                    this.compoundEdit = new CompoundEdit();
                }
                this.sourceCode.replaceSelection(replace);
                findPosn = nextPosn + replace.length();
                ++replaceCount;
            }
        }
        this.isCompoundEdit = false;
        if (this.compoundEdit != null) {
            this.compoundEdit.end();
            this.undoManager.addEdit(this.compoundEdit);
            this.editPane.updateUndoState();
            this.editPane.updateRedoState();
        }
        return replaceCount;
    }
}
