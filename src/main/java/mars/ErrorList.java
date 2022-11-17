

package mars;

import java.io.File;
import java.util.ArrayList;

public class ErrorList
{
    private ArrayList messages;
    private int errorCount;
    private int warningCount;
    public static final String ERROR_MESSAGE_PREFIX = "Error";
    public static final String WARNING_MESSAGE_PREFIX = "Warning";
    public static final String FILENAME_PREFIX = " in ";
    public static final String LINE_PREFIX = " line ";
    public static final String POSITION_PREFIX = " column ";
    public static final String MESSAGE_SEPARATOR = ": ";
    
    public ErrorList() {
        this.messages = new ArrayList();
        this.errorCount = 0;
        this.warningCount = 0;
    }
    
    public ArrayList getErrorMessages() {
        return this.messages;
    }
    
    public boolean errorsOccurred() {
        return this.errorCount != 0;
    }
    
    public boolean warningsOccurred() {
        return this.warningCount != 0;
    }
    
    public void add(final ErrorMessage mess) {
        this.add(mess, this.messages.size());
    }
    
    public void add(final ErrorMessage mess, final int index) {
        if (this.errorCount > this.getErrorLimit()) {
            return;
        }
        if (this.errorCount == this.getErrorLimit()) {
            this.messages.add(new ErrorMessage((MIPSprogram)null, mess.getLine(), mess.getPosition(), "Error Limit of " + this.getErrorLimit() + " exceeded."));
            ++this.errorCount;
            return;
        }
        this.messages.add(index, mess);
        if (mess.isWarning()) {
            ++this.warningCount;
        }
        else {
            ++this.errorCount;
        }
    }
    
    public int errorCount() {
        return this.errorCount;
    }
    
    public int warningCount() {
        return this.warningCount;
    }
    
    public boolean errorLimitExceeded() {
        return this.errorCount > this.getErrorLimit();
    }
    
    public int getErrorLimit() {
        return Globals.maximumErrorMessages;
    }
    
    public String generateErrorReport() {
        return this.generateReport(false);
    }
    
    public String generateWarningReport() {
        return this.generateReport(true);
    }
    
    public String generateErrorAndWarningReport() {
        return this.generateWarningReport() + this.generateErrorReport();
    }
    
    private String generateReport(final boolean isWarning) {
        final StringBuffer report = new StringBuffer("");
        for (int i = 0; i < this.messages.size(); ++i) {
            final ErrorMessage m = this.messages.get(i);
            if ((isWarning && m.isWarning()) || (!isWarning && !m.isWarning())) {
                String reportLine = (isWarning ? "Warning" : "Error") + " in ";
                if (m.getFilename().length() > 0) {
                    reportLine += new File(m.getFilename()).getPath();
                }
                if (m.getLine() > 0) {
                    reportLine = reportLine + " line " + m.getMacroExpansionHistory() + m.getLine();
                }
                if (m.getPosition() > 0) {
                    reportLine = reportLine + " column " + m.getPosition();
                }
                reportLine = reportLine + ": " + m.getMessage() + "\n";
                report.append(reportLine);
            }
        }
        return report.toString();
    }
}
