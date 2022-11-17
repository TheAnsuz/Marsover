

package mars.venus.editors.generic;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.Color;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Insets;
import mars.Globals;
import javax.swing.JComponent;
import javax.swing.undo.CompoundEdit;
import javax.swing.JScrollPane;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import mars.venus.EditPane;
import mars.venus.editors.MARSTextEditingArea;
import javax.swing.JTextArea;

public class GenericTextArea extends JTextArea implements MARSTextEditingArea
{
    private EditPane editPane;
    private UndoManager undoManager;
    private UndoableEditListener undoableEditListener;
    private JTextArea sourceCode;
    private JScrollPane editAreaScrollPane;
    private boolean isCompoundEdit;
    private CompoundEdit compoundEdit;
    
    public GenericTextArea(final EditPane editPain, final JComponent lineNumbers) {
        this.isCompoundEdit = false;
        this.editPane = editPain;
        (this.sourceCode = this).setFont(Globals.getSettings().getEditorFont());
        this.setTabSize(Globals.getSettings().getEditorTabSize());
        this.setMargin(new Insets(0, 3, 3, 3));
        this.setCaretBlinkRate(Globals.getSettings().getCaretBlinkRate());
        final JPanel source = new JPanel(new BorderLayout());
        source.add(lineNumbers, "West");
        source.add(this, "Center");
        this.editAreaScrollPane = new JScrollPane(source, 22, 32);
        this.editAreaScrollPane.getVerticalScrollBar().setUnitIncrement(this.sourceCode.getFontMetrics(this.sourceCode.getFont()).getHeight());
        this.undoManager = new UndoManager();
        this.getCaret().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                GenericTextArea.this.editPane.displayCaretPosition(GenericTextArea.this.getCaretPosition());
            }
        });
        this.undoableEditListener = new UndoableEditListener() {
            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {
                if (GenericTextArea.this.isCompoundEdit) {
                    GenericTextArea.this.compoundEdit.addEdit(e.getEdit());
                }
                else {
                    GenericTextArea.this.undoManager.addEdit(e.getEdit());
                    GenericTextArea.this.editPane.updateUndoState();
                    GenericTextArea.this.editPane.updateRedoState();
                }
            }
        };
        this.getDocument().addUndoableEditListener(this.undoableEditListener);
    }
    
    @Override
    public void setLineHighlightEnabled(final boolean highlight) {
    }
    
    @Override
    public void updateSyntaxStyles() {
    }
    
    @Override
    public void setCaretBlinkRate(final int rate) {
        if (rate >= 0) {
            this.getCaret().setBlinkRate(rate);
        }
    }
    
    @Override
    public Component getOuterComponent() {
        return this.editAreaScrollPane;
    }
    
    @Override
    public void setSourceCode(final String s, final boolean editable) {
        this.setText(s);
        this.setBackground(editable ? Color.WHITE : Color.GRAY);
        this.setEditable(editable);
        this.setEnabled(editable);
        this.getCaret().setVisible(editable);
        this.setCaretPosition(0);
        if (editable) {
            this.requestFocusInWindow();
        }
    }
    
    @Override
    public void discardAllUndoableEdits() {
        this.undoManager.discardAllEdits();
    }
    
    @Override
    public void setText(final String s) {
        this.getDocument().removeUndoableEditListener(this.undoableEditListener);
        super.setText(s);
        this.getDocument().addUndoableEditListener(this.undoableEditListener);
    }
    
    @Override
    public void setCaretVisible(final boolean vis) {
        this.getCaret().setVisible(vis);
    }
    
    @Override
    public void setSelectionVisible(final boolean vis) {
        this.getCaret().setSelectionVisible(vis);
    }
    
    @Override
    public UndoManager getUndoManager() {
        return this.undoManager;
    }
    
    @Override
    public void undo() {
        try {
            this.undoManager.undo();
        }
        catch (CannotUndoException ex) {
            System.out.println("Unable to undo: " + ex);
            ex.printStackTrace();
        }
        this.setCaretVisible(true);
    }
    
    @Override
    public void redo() {
        try {
            this.undoManager.redo();
        }
        catch (CannotRedoException ex) {
            System.out.println("Unable to redo: " + ex);
            ex.printStackTrace();
        }
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
