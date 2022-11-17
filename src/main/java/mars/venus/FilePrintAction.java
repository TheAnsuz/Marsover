

package mars.venus;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class FilePrintAction extends GuiAction
{
    public FilePrintAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final EditPane editPane = this.mainUI.getMainPane().getEditPane();
        if (editPane == null) {
            return;
        }
        final int fontsize = 10;
        final double margins = 0.5;
        HardcopyWriter out;
        try {
            out = new HardcopyWriter(this.mainUI, editPane.getFilename(), fontsize, margins, margins, margins, margins);
        }
        catch (HardcopyWriter.PrintCanceledException pce) {
            return;
        }
        final BufferedReader in = new BufferedReader(new StringReader(editPane.getSource()));
        final int lineNumberDigits = Integer.toString(editPane.getSourceLineCount()).length();
        String lineNumberString = "";
        int lineNumber = 0;
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (editPane.showingLineNumbers()) {
                    ++lineNumber;
                    for (lineNumberString = Integer.toString(lineNumber) + ": "; lineNumberString.length() < lineNumberDigits; lineNumberString += " ") {}
                }
                line = lineNumberString + line + "\n";
                out.write(line.toCharArray(), 0, line.length());
            }
            in.close();
            out.close();
        }
        catch (IOException ex) {}
    }
}
