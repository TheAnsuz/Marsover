

package mars;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import mars.assembler.SourceLine;

public class ErrorMessage
{
    private boolean isWarning;
    private String filename;
    private int line;
    private int position;
    private String message;
    private String macroExpansionHistory;
    public static final boolean WARNING = true;
    public static final boolean ERROR = false;
    
    @Deprecated
    public ErrorMessage(final String filename, final int line, final int position, final String message) {
        this(false, filename, line, position, message, "");
    }
    
    @Deprecated
    public ErrorMessage(final String filename, final int line, final int position, final String message, final String macroExpansionHistory) {
        this(false, filename, line, position, message, macroExpansionHistory);
    }
    
    @Deprecated
    public ErrorMessage(final boolean isWarning, final String filename, final int line, final int position, final String message, final String macroExpansionHistory) {
        this.isWarning = isWarning;
        this.filename = filename;
        this.line = line;
        this.position = position;
        this.message = message;
        this.macroExpansionHistory = macroExpansionHistory;
    }
    
    public ErrorMessage(final MIPSprogram sourceMIPSprogram, final int line, final int position, final String message) {
        this(false, sourceMIPSprogram, line, position, message);
    }
    
    public ErrorMessage(final boolean isWarning, final MIPSprogram sourceMIPSprogram, final int line, final int position, final String message) {
        this.isWarning = isWarning;
        if (sourceMIPSprogram == null) {
            this.filename = "";
            this.line = line;
        }
        else if (sourceMIPSprogram.getSourceLineList() == null) {
            this.filename = sourceMIPSprogram.getFilename();
            this.line = line;
        }
        else {
            final SourceLine sourceLine = sourceMIPSprogram.getSourceLineList().get(line - 1);
            this.filename = sourceLine.getFilename();
            this.line = sourceLine.getLineNumber();
        }
        this.position = position;
        this.message = message;
        this.macroExpansionHistory = getExpansionHistory(sourceMIPSprogram);
    }
    
    public ErrorMessage(final ProgramStatement statement, final String message) {
        this.isWarning = false;
        this.filename = ((statement.getSourceMIPSprogram() == null) ? "" : statement.getSourceMIPSprogram().getFilename());
        this.position = 0;
        this.message = message;
        final ArrayList<Integer> defineLine = this.parseMacroHistory(statement.getSource());
        if (defineLine.size() == 0) {
            this.line = statement.getSourceLine();
            this.macroExpansionHistory = "";
        }
        else {
            this.line = defineLine.get(0);
            this.macroExpansionHistory = "" + statement.getSourceLine();
        }
    }
    
    private ArrayList<Integer> parseMacroHistory(final String string) {
        final Pattern pattern = Pattern.compile("<\\d+>");
        final Matcher matcher = pattern.matcher(string);
        String verify = new String(string).trim();
        final ArrayList<Integer> macroHistory = new ArrayList<Integer>();
        while (matcher.find()) {
            final String match = matcher.group();
            if (verify.indexOf(match) != 0) {
                break;
            }
            try {
                final int line = Integer.parseInt(match.substring(1, match.length() - 1));
                macroHistory.add(line);
            }
            catch (NumberFormatException e) {
                break;
            }
            verify = verify.substring(match.length()).trim();
        }
        return macroHistory;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public int getLine() {
        return this.line;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public boolean isWarning() {
        return this.isWarning;
    }
    
    public String getMacroExpansionHistory() {
        if (this.macroExpansionHistory == null || this.macroExpansionHistory.length() == 0) {
            return "";
        }
        return this.macroExpansionHistory + "->";
    }
    
    private static String getExpansionHistory(final MIPSprogram sourceMIPSprogram) {
        if (sourceMIPSprogram == null || sourceMIPSprogram.getLocalMacroPool() == null) {
            return "";
        }
        return sourceMIPSprogram.getLocalMacroPool().getExpansionHistory();
    }
}
