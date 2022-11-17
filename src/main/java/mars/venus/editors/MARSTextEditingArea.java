

package mars.venus.editors;

import java.awt.Component;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Font;
import javax.swing.undo.UndoManager;
import javax.swing.text.Document;

public interface MARSTextEditingArea
{
    public static final int TEXT_NOT_FOUND = 0;
    public static final int TEXT_FOUND = 1;
    public static final int TEXT_REPLACED_FOUND_NEXT = 2;
    public static final int TEXT_REPLACED_NOT_FOUND_NEXT = 3;
    
    void copy();
    
    void cut();
    
    int doFindText(final String p0, final boolean p1);
    
    int doReplace(final String p0, final String p1, final boolean p2);
    
    int doReplaceAll(final String p0, final String p1, final boolean p2);
    
    int getCaretPosition();
    
    Document getDocument();
    
    String getSelectedText();
    
    int getSelectionEnd();
    
    int getSelectionStart();
    
    void select(final int p0, final int p1);
    
    void selectAll();
    
    String getText();
    
    UndoManager getUndoManager();
    
    void paste();
    
    void replaceSelection(final String p0);
    
    void setCaretPosition(final int p0);
    
    void setEditable(final boolean p0);
    
    void setSelectionEnd(final int p0);
    
    void setSelectionStart(final int p0);
    
    void setText(final String p0);
    
    void setFont(final Font p0);
    
    Font getFont();
    
    boolean requestFocusInWindow();
    
    FontMetrics getFontMetrics(final Font p0);
    
    void setBackground(final Color p0);
    
    void setEnabled(final boolean p0);
    
    void grabFocus();
    
    void redo();
    
    void revalidate();
    
    void setSourceCode(final String p0, final boolean p1);
    
    void setCaretVisible(final boolean p0);
    
    void setSelectionVisible(final boolean p0);
    
    void undo();
    
    void discardAllUndoableEdits();
    
    void setLineHighlightEnabled(final boolean p0);
    
    void setCaretBlinkRate(final int p0);
    
    void setTabSize(final int p0);
    
    void updateSyntaxStyles();
    
    Component getOuterComponent();
}
