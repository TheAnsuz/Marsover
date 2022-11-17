

package mars.venus;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import mars.Globals;
import mars.venus.editors.MARSTextEditingArea;
import mars.venus.editors.generic.GenericTextArea;
import mars.venus.editors.jeditsyntax.JEditBasedTextArea;

public class EditPane extends JPanel implements Observer
{
    private MARSTextEditingArea sourceCode;
    private VenusUI mainUI;
    private String currentDirectoryPath;
    private final JLabel caretPositionLabel;
    private JCheckBox showLineNumbers;
    private JLabel lineNumbers;
    private static int count;
    private boolean isCompoundEdit;
    private CompoundEdit compoundEdit;
    private FileStatus fileStatus;
    private static final String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    private static final char newline = '\n';
    
    public EditPane(final VenusUI appFrame) {
        super(new BorderLayout());
        this.isCompoundEdit = false;
        this.mainUI = appFrame;
        this.currentDirectoryPath = System.getProperty("user.dir");
        Globals.getSettings().addObserver(this);
        this.fileStatus = new FileStatus();
        this.lineNumbers = new JLabel();
        if (Globals.getSettings().getBooleanSetting(18)) {
            this.sourceCode = new GenericTextArea(this, this.lineNumbers);
        }
        else {
            this.sourceCode = new JEditBasedTextArea(this, this.lineNumbers);
        }
        this.add(this.sourceCode.getOuterComponent(), "Center");
        this.sourceCode.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent evt) {
                if (FileStatus.get() == 8) {
                    EditPane.this.setFileStatus(3);
                    FileStatus.set(3);
                    if (EditPane.this.showingLineNumbers()) {
                        EditPane.this.lineNumbers.setText(EditPane.this.getLineNumbersList(EditPane.this.sourceCode.getDocument()));
                    }
                    return;
                }
                if (EditPane.this.getFileStatus() == 1) {
                    EditPane.this.setFileStatus(2);
                }
                if (EditPane.this.getFileStatus() == 3) {
                    EditPane.this.setFileStatus(4);
                }
                if (EditPane.this.getFileStatus() == 2) {
                    EditPane.this.mainUI.editor.setTitle("", EditPane.this.getFilename(), EditPane.this.getFileStatus());
                }
                else {
                    EditPane.this.mainUI.editor.setTitle(EditPane.this.getPathname(), EditPane.this.getFilename(), EditPane.this.getFileStatus());
                }
                FileStatus.setEdited(true);
                switch (FileStatus.get()) {
                    case 1: {
                        FileStatus.set(2);
                        break;
                    }
                    case 2: {
                        break;
                    }
                    default: {
                        FileStatus.set(4);
                        break;
                    }
                }
                Globals.getGui().getMainPane().getExecutePane().clearPane();
                if (EditPane.this.showingLineNumbers()) {
                    EditPane.this.lineNumbers.setText(EditPane.this.getLineNumbersList(EditPane.this.sourceCode.getDocument()));
                }
            }
            
            @Override
            public void removeUpdate(final DocumentEvent evt) {
                this.insertUpdate(evt);
            }
            
            @Override
            public void changedUpdate(final DocumentEvent evt) {
                this.insertUpdate(evt);
            }
        });
        (this.showLineNumbers = new JCheckBox("Show Line Numbers")).setToolTipText("If checked, will display line number for each line of text.");
        this.showLineNumbers.setEnabled(false);
        this.showLineNumbers.setSelected(Globals.getSettings().getEditorLineNumbersDisplayed());
        this.setSourceCode("", false);
        this.lineNumbers.setFont(this.getLineNumberFont(this.sourceCode.getFont()));
        this.lineNumbers.setVerticalAlignment(1);
        this.lineNumbers.setText("");
        this.lineNumbers.setVisible(true);
        this.showLineNumbers.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (EditPane.this.showLineNumbers.isSelected()) {
                    EditPane.this.lineNumbers.setText(EditPane.this.getLineNumbersList(EditPane.this.sourceCode.getDocument()));
                    EditPane.this.lineNumbers.setVisible(true);
                }
                else {
                    EditPane.this.lineNumbers.setText("");
                    EditPane.this.lineNumbers.setVisible(false);
                }
                EditPane.this.sourceCode.revalidate();
                Globals.getSettings().setEditorLineNumbersDisplayed(EditPane.this.showLineNumbers.isSelected());
                EditPane.this.sourceCode.setCaretVisible(true);
                EditPane.this.sourceCode.requestFocusInWindow();
            }
        });
        final JPanel editInfo = new JPanel(new BorderLayout());
        (this.caretPositionLabel = new JLabel()).setToolTipText("Tracks the current position of the text editing cursor.");
        this.displayCaretPosition(new Point());
        editInfo.add(this.caretPositionLabel, "West");
        editInfo.add(this.showLineNumbers, "Center");
        this.add(editInfo, "South");
    }
    
    public void setSourceCode(final String s, final boolean editable) {
        this.sourceCode.setSourceCode(s, editable);
    }
    
    public void discardAllUndoableEdits() {
        this.sourceCode.discardAllUndoableEdits();
    }
    
    public String getLineNumbersList(final Document doc) {
        final StringBuffer lineNumberList = new StringBuffer("<html>");
        final int lineCount = doc.getDefaultRootElement().getElementCount();
        final int digits = Integer.toString(lineCount).length();
        for (int i = 1; i <= lineCount; ++i) {
            final String lineStr = Integer.toString(i);
            final int leadingSpaces = digits - lineStr.length();
            if (leadingSpaces == 0) {
                lineNumberList.append(lineStr).append("&nbsp;<br>");
            }
            else {
                lineNumberList.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;".substring(0, leadingSpaces * 6))
                        .append(lineStr).append("&nbsp;<br>");
            }
        }
        lineNumberList.append("<br></html>");
        return lineNumberList.toString();
    }
    
    public int getSourceLineCount() {
        final BufferedReader bufStringReader = new BufferedReader(new StringReader(this.sourceCode.getText()));
        int lineNums = 0;
        try {
            while (bufStringReader.readLine() != null) {
                ++lineNums;
            }
        }
        catch (IOException ex) {}
        return lineNums;
    }
    
    public String getSource() {
        return this.sourceCode.getText();
    }
    
    public void setFileStatus(final int fileStatus) {
        this.fileStatus.setFileStatus(fileStatus);
    }
    
    public int getFileStatus() {
        return this.fileStatus.getFileStatus();
    }
    
    public String getFilename() {
        return this.fileStatus.getFilename();
    }
    
    public String getPathname() {
        return this.fileStatus.getPathname();
    }
    
    public void setPathname(final String pathname) {
        this.fileStatus.setPathname(pathname);
    }
    
    public boolean hasUnsavedEdits() {
        return this.fileStatus.hasUnsavedEdits();
    }
    
    public boolean isNew() {
        return this.fileStatus.isNew();
    }
    
    public void tellEditingComponentToRequestFocusInWindow() {
        this.sourceCode.requestFocusInWindow();
    }
    
    public void updateStaticFileStatus() {
        this.fileStatus.updateStaticFileStatus();
    }
    
    public UndoManager getUndoManager() {
        return this.sourceCode.getUndoManager();
    }
    
    public void copyText() {
        this.sourceCode.copy();
        this.sourceCode.setCaretVisible(true);
        this.sourceCode.setSelectionVisible(true);
    }
    
    public void cutText() {
        this.sourceCode.cut();
        this.sourceCode.setCaretVisible(true);
    }
    
    public void pasteText() {
        this.sourceCode.paste();
        this.sourceCode.setCaretVisible(true);
    }
    
    public void selectAllText() {
        this.sourceCode.selectAll();
        this.sourceCode.setCaretVisible(true);
        this.sourceCode.setSelectionVisible(true);
    }
    
    public void undo() {
        this.sourceCode.undo();
    }
    
    public void redo() {
        this.sourceCode.redo();
    }
    
    public void updateUndoState() {
        this.mainUI.editUndoAction.updateUndoState();
    }
    
    public void updateRedoState() {
        this.mainUI.editRedoAction.updateRedoState();
    }
    
    public boolean showingLineNumbers() {
        return this.showLineNumbers.isSelected();
    }
    
    public void setShowLineNumbersEnabled(final boolean enabled) {
        this.showLineNumbers.setEnabled(enabled);
    }
    
    public void displayCaretPosition(final int pos) {
        this.displayCaretPosition(this.convertStreamPositionToLineColumn(pos));
    }
    
    public void displayCaretPosition(final Point p) {
        this.caretPositionLabel.setText("Line: " + p.y + " Column: " + p.x);
    }
    
    public Point convertStreamPositionToLineColumn(final int position) {
        final String textStream = this.sourceCode.getText();
        int line = 1;
        int column = 1;
        for (int i = 0; i < position; ++i) {
            if (textStream.charAt(i) == '\n') {
                ++line;
                column = 1;
            }
            else {
                ++column;
            }
        }
        return new Point(column, line);
    }
    
    public int convertLineColumnToStreamPosition(final int line, final int column) {
        final String textStream = this.sourceCode.getText();
        final int textLength = textStream.length();
        int textLine = 1;
        int textColumn = 1;
        for (int i = 0; i < textLength; ++i) {
            if (textLine == line && textColumn == column) {
                return i;
            }
            if (textStream.charAt(i) == '\n') {
                ++textLine;
                textColumn = 1;
            }
            else {
                ++textColumn;
            }
        }
        return -1;
    }
    
    public void selectLine(final int line) {
        if (line > 0) {
            final int lineStartPosition = this.convertLineColumnToStreamPosition(line, 1);
            int lineEndPosition = this.convertLineColumnToStreamPosition(line + 1, 1) - 1;
            if (lineEndPosition < 0) {
                lineEndPosition = this.sourceCode.getText().length() - 1;
            }
            if (lineStartPosition >= 0) {
                this.sourceCode.select(lineStartPosition, lineEndPosition);
                this.sourceCode.setSelectionVisible(true);
            }
        }
    }
    
    public void selectLine(final int line, final int column) {
        this.selectLine(line);
    }
    
    public int doFindText(final String find, final boolean caseSensitive) {
        return this.sourceCode.doFindText(find, caseSensitive);
    }
    
    public int doReplace(final String find, final String replace, final boolean caseSensitive) {
        return this.sourceCode.doReplace(find, replace, caseSensitive);
    }
    
    public int doReplaceAll(final String find, final String replace, final boolean caseSensitive) {
        return this.sourceCode.doReplaceAll(find, replace, caseSensitive);
    }
    
    @Override
    public void update(final Observable fontChanger, final Object arg) {
        this.sourceCode.setFont(Globals.getSettings().getEditorFont());
        this.sourceCode.setLineHighlightEnabled(Globals.getSettings().getBooleanSetting(15));
        this.sourceCode.setCaretBlinkRate(Globals.getSettings().getCaretBlinkRate());
        this.sourceCode.setTabSize(Globals.getSettings().getEditorTabSize());
        this.sourceCode.updateSyntaxStyles();
        this.sourceCode.revalidate();
        this.lineNumbers.setFont(this.getLineNumberFont(this.sourceCode.getFont()));
        this.lineNumbers.revalidate();
    }
    
    private Font getLineNumberFont(final Font sourceFont) {
        return (this.sourceCode.getFont().getStyle() == 0) ? sourceFont : new Font(sourceFont.getFamily(), 0, sourceFont.getSize());
    }
    
    static {
        EditPane.count = 0;
    }
}
