

package mars.venus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.undo.UndoableEdit;
import mars.Globals;
import mars.simulator.Simulator;

public class MessagesPane extends JTabbedPane
{
    JTextArea assemble;
    JTextArea run;
    JPanel assembleTab;
    JPanel runTab;
    public static final int MAXIMUM_SCROLLED_CHARACTERS;
    public static final int NUMBER_OF_CHARACTERS_TO_CUT;
    
    public MessagesPane() {
        this.setMinimumSize(new Dimension(0, 0));
        this.assemble = new JTextArea();
        this.run = new JTextArea();
        this.assemble.setEditable(false);
        this.run.setEditable(false);
        final Font monoFont = new Font("Monospaced", 0, 12);
        this.assemble.setFont(monoFont);
        this.run.setFont(monoFont);
        final JButton assembleTabClearButton = new JButton("Clear");
        assembleTabClearButton.setToolTipText("Clear the Mars Messages area");
        assembleTabClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MessagesPane.this.assemble.setText("");
            }
        });
        (this.assembleTab = new JPanel(new BorderLayout())).add(this.createBoxForButton(assembleTabClearButton), "West");
        this.assembleTab.add(new JScrollPane(this.assemble, 20, 30), "Center");
        this.assemble.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                int lineStart = 0;
                int lineEnd = 0;
                String text;
                try {
                    final int line = MessagesPane.this.assemble.getLineOfOffset(MessagesPane.this.assemble.viewToModel(e.getPoint()));
                    lineStart = MessagesPane.this.assemble.getLineStartOffset(line);
                    lineEnd = MessagesPane.this.assemble.getLineEndOffset(line);
                    text = MessagesPane.this.assemble.getText(lineStart, lineEnd - lineStart);
                }
                catch (BadLocationException ble) {
                    text = "";
                }
                if (text.length() > 0 && (text.startsWith("Error") || text.startsWith("Warning"))) {
                    MessagesPane.this.assemble.select(lineStart, lineEnd);
                    MessagesPane.this.assemble.setSelectionColor(Color.YELLOW);
                    MessagesPane.this.assemble.repaint();
                    final int separatorPosition = text.indexOf(": ");
                    if (separatorPosition >= 0) {
                        text = text.substring(0, separatorPosition);
                    }
                    final String[] stringTokens = text.split("\\s");
                    final String lineToken = " line ".trim();
                    final String columnToken = " column ".trim();
                    String lineString = "";
                    String columnString = "";
                    for (int i = 0; i < stringTokens.length; ++i) {
                        if (stringTokens[i].equals(lineToken) && i < stringTokens.length - 1) {
                            lineString = stringTokens[i + 1];
                        }
                        if (stringTokens[i].equals(columnToken) && i < stringTokens.length - 1) {
                            columnString = stringTokens[i + 1];
                        }
                    }
                    int line2 = 0;
                    int column = 0;
                    try {
                        line2 = Integer.parseInt(lineString);
                    }
                    catch (NumberFormatException nfe) {
                        line2 = 0;
                    }
                    try {
                        column = Integer.parseInt(columnString);
                    }
                    catch (NumberFormatException nfe) {
                        column = 0;
                    }
                    final int fileNameStart = text.indexOf(" in ") + " in ".length();
                    final int fileNameEnd = text.indexOf(" line ");
                    String fileName = "";
                    if (fileNameStart < fileNameEnd && fileNameStart >= " in ".length()) {
                        fileName = text.substring(fileNameStart, fileNameEnd).trim();
                    }
                    if (fileName != null && fileName.length() > 0) {
                        MessagesPane.this.selectEditorTextLine(fileName, line2, column);
                        MessagesPane.this.selectErrorMessage(fileName, line2, column);
                    }
                }
            }
        });
        final JButton runTabClearButton = new JButton("Clear");
        runTabClearButton.setToolTipText("Clear the Run I/O area");
        runTabClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MessagesPane.this.run.setText("");
            }
        });
        (this.runTab = new JPanel(new BorderLayout())).add(this.createBoxForButton(runTabClearButton), "West");
        this.runTab.add(new JScrollPane(this.run, 20, 30), "Center");
        this.addTab("Mars Messages", this.assembleTab);
        this.addTab("Run I/O", this.runTab);
        this.setToolTipTextAt(0, "Messages produced by Run menu. Click on assemble error message to select erroneous line");
        this.setToolTipTextAt(1, "Simulated MIPS console input and output");
    }
    
    private Box createBoxForButton(final JButton button) {
        final Box buttonRow = Box.createHorizontalBox();
        buttonRow.add(Box.createHorizontalStrut(6));
        buttonRow.add(button);
        buttonRow.add(Box.createHorizontalStrut(6));
        final Box buttonBox = Box.createVerticalBox();
        buttonBox.add(Box.createVerticalGlue());
        buttonBox.add(buttonRow);
        buttonBox.add(Box.createVerticalGlue());
        return buttonBox;
    }
    
    public void selectErrorMessage(final String fileName, final int line, final int column) {
        final String errorReportSubstring = new File(fileName).getName() + " line " + line + " column " + column;
        final int textPosition = this.assemble.getText().lastIndexOf(errorReportSubstring);
        if (textPosition >= 0) {
            int textLine = 0;
            int lineStart = 0;
            int lineEnd = 0;
            try {
                textLine = this.assemble.getLineOfOffset(textPosition);
                lineStart = this.assemble.getLineStartOffset(textLine);
                lineEnd = this.assemble.getLineEndOffset(textLine);
                this.assemble.setSelectionColor(Color.YELLOW);
                this.assemble.select(lineStart, lineEnd);
                this.assemble.getCaret().setSelectionVisible(true);
                this.assemble.repaint();
            }
            catch (BadLocationException ex) {}
        }
    }
    
    public void selectEditorTextLine(final String fileName, final int line, final int column) {
        final EditTabbedPane editTabbedPane = (EditTabbedPane)Globals.getGui().getMainPane().getEditTabbedPane();
        EditPane currentPane = null;
        final EditPane editPane = editTabbedPane.getEditPaneForFile(new File(fileName).getPath());
        if (editPane != null) {
            if (editPane != editTabbedPane.getCurrentEditTab()) {
                editTabbedPane.setCurrentEditTab(editPane);
            }
            currentPane = editPane;
        }
        else if (editTabbedPane.openFile(new File(fileName))) {
            currentPane = editTabbedPane.getCurrentEditTab();
        }
        if (editPane != null && currentPane != null) {
            currentPane.selectLine(line, column);
        }
    }
    
    public JTextArea getAssembleTextArea() {
        return this.assemble;
    }
    
    public JTextArea getRunTextArea() {
        return this.run;
    }
    
    public void postMarsMessage(final String message) {
        this.assemble.append(message);
        if (this.assemble.getDocument().getLength() > MessagesPane.MAXIMUM_SCROLLED_CHARACTERS) {
            try {
                this.assemble.getDocument().remove(0, MessagesPane.NUMBER_OF_CHARACTERS_TO_CUT);
            }
            catch (BadLocationException ex) {}
        }
        this.assemble.setCaretPosition(this.assemble.getDocument().getLength());
        this.setSelectedComponent(this.assembleTab);
    }
    
    public void postRunMessage(final String message) {
        final String mess = message;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MessagesPane.this.setSelectedComponent(MessagesPane.this.runTab);
                MessagesPane.this.run.append(mess);
                if (MessagesPane.this.run.getDocument().getLength() > MessagesPane.MAXIMUM_SCROLLED_CHARACTERS) {
                    try {
                        MessagesPane.this.run.getDocument().remove(0, MessagesPane.NUMBER_OF_CHARACTERS_TO_CUT);
                    }
                    catch (BadLocationException ex) {}
                }
            }
        });
    }
    
    public void selectMarsMessageTab() {
        this.setSelectedComponent(this.assembleTab);
    }
    
    public void selectRunMessageTab() {
        this.setSelectedComponent(this.runTab);
    }
    
    public String getInputString(final String prompt) {
        final JOptionPane pane = new JOptionPane(prompt, 3, -1);
        pane.setWantsInput(true);
        final JDialog dialog = pane.createDialog(Globals.getGui(), "MIPS Keyboard Input");
        dialog.setVisible(true);
        final String input = (String)pane.getInputValue();
        this.postRunMessage(Globals.userInputAlert + input + "\n");
        return input;
    }
    
    public String getInputString(final int maxLen) {
        final Asker asker = new Asker(maxLen);
        return asker.response();
    }
    
    static {
        MAXIMUM_SCROLLED_CHARACTERS = Globals.maximumMessageCharacters;
        NUMBER_OF_CHARACTERS_TO_CUT = Globals.maximumMessageCharacters / 10;
    }
    
    class Asker implements Runnable
    {
        ArrayBlockingQueue<String> resultQueue;
        int initialPos;
        int maxLen;
        final DocumentListener listener;
        final NavigationFilter navigationFilter;
        final Simulator.StopListener stopListener;
        
        Asker(final int maxLen) {
            this.resultQueue = new ArrayBlockingQueue<>(1);
            this.listener = new DocumentListener() {
                @Override
                public void insertUpdate(final DocumentEvent e) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String inserted = e.getDocument().getText(e.getOffset(), e.getLength());
                                final int i = inserted.indexOf(10);
                                if (i >= 0) {
                                    final int offset = e.getOffset() + i;
                                    if (offset + 1 == e.getDocument().getLength()) {
                                        Asker.this.returnResponse();
                                    }
                                    else {
                                        e.getDocument().remove(offset, 1);
                                        e.getDocument().insertString(e.getDocument().getLength(), "\n", null);
                                    }
                                }
                                else if (Asker.this.maxLen >= 0 && e.getDocument().getLength() - Asker.this.initialPos >= Asker.this.maxLen) {
                                    Asker.this.returnResponse();
                                }
                            }
                            catch (BadLocationException ex) {
                                Asker.this.returnResponse();
                            }
                        }
                    });
                }
                
                @Override
                public void removeUpdate(final DocumentEvent e) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if ((e.getDocument().getLength() < Asker.this.initialPos || e.getOffset() < Asker.this.initialPos) && e instanceof UndoableEdit) {
                                ((UndoableEdit)e).undo();
                                MessagesPane.this.run.setCaretPosition(e.getOffset() + e.getLength());
                            }
                        }
                    });
                }
                
                @Override
                public void changedUpdate(final DocumentEvent e) {
                }
            };
            this.navigationFilter = new NavigationFilter() {
                @Override
                public void moveDot(final FilterBypass fb, int dot, final Position.Bias bias) {
                    if (dot < Asker.this.initialPos) {
                        dot = Math.min(Asker.this.initialPos, MessagesPane.this.run.getDocument().getLength());
                    }
                    fb.moveDot(dot, bias);
                }
                
                @Override
                public void setDot(final FilterBypass fb, int dot, final Position.Bias bias) {
                    if (dot < Asker.this.initialPos) {
                        dot = Math.min(Asker.this.initialPos, MessagesPane.this.run.getDocument().getLength());
                    }
                    fb.setDot(dot, bias);
                }
            };
            this.stopListener = new Simulator.StopListener() {
                @Override
                public void stopped(final Simulator s) {
                    Asker.this.returnResponse();
                }
            };
            this.maxLen = maxLen;
        }
        
        @Override
        public void run() {
            MessagesPane.this.setSelectedComponent(MessagesPane.this.runTab);
            MessagesPane.this.run.setEditable(true);
            MessagesPane.this.run.requestFocusInWindow();
            MessagesPane.this.run.setCaretPosition(MessagesPane.this.run.getDocument().getLength());
            this.initialPos = MessagesPane.this.run.getCaretPosition();
            MessagesPane.this.run.setNavigationFilter(this.navigationFilter);
            MessagesPane.this.run.getDocument().addDocumentListener(this.listener);
            Simulator.getInstance().addStopListener(this.stopListener);
        }
        
        void cleanup() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MessagesPane.this.run.getDocument().removeDocumentListener(Asker.this.listener);
                    MessagesPane.this.run.setEditable(false);
                    MessagesPane.this.run.setNavigationFilter(null);
                    MessagesPane.this.run.setCaretPosition(MessagesPane.this.run.getDocument().getLength());
                    Simulator.getInstance().removeStopListener(Asker.this.stopListener);
                }
            });
        }
        
        void returnResponse() {
            try {
                final int p = Math.min(this.initialPos, MessagesPane.this.run.getDocument().getLength());
                final int l = Math.min(MessagesPane.this.run.getDocument().getLength() - p, (this.maxLen >= 0) ? this.maxLen : Integer.MAX_VALUE);
                this.resultQueue.offer(MessagesPane.this.run.getText(p, l));
            }
            catch (BadLocationException ex) {
                this.resultQueue.offer("");
            }
        }
        
        String response() {
            EventQueue.invokeLater(this);
            try {
                return this.resultQueue.take();
            }
            catch (InterruptedException ex) {
                return null;
            }
            finally {
                this.cleanup();
            }
        }
    }
}
