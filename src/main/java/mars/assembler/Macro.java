

package mars.assembler;

import java.util.ArrayList;
import java.util.Collections;
import mars.ErrorList;
import mars.ErrorMessage;
import mars.MIPSprogram;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;

public class Macro
{
    private String name;
    private MIPSprogram program;
    private final ArrayList<String> labels;
    private int fromLine;
    private int toLine;
    private int origFromLine;
    private int origToLine;
    private ArrayList<String> args;
    
    public Macro() {
        this.name = "";
        this.program = null;
        final int n = 0;
        this.toLine = n;
        this.fromLine = n;
        final int n2 = 0;
        this.origToLine = n2;
        this.origFromLine = n2;
        this.args = new ArrayList<>();
        this.labels = new ArrayList<>();
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public MIPSprogram getProgram() {
        return this.program;
    }
    
    public void setProgram(final MIPSprogram program) {
        this.program = program;
    }
    
    public int getFromLine() {
        return this.fromLine;
    }
    
    public int getOriginalFromLine() {
        return this.origFromLine;
    }
    
    public void setFromLine(final int fromLine) {
        this.fromLine = fromLine;
    }
    
    public void setOriginalFromLine(final int origFromLine) {
        this.origFromLine = origFromLine;
    }
    
    public int getToLine() {
        return this.toLine;
    }
    
    public int getOriginalToLine() {
        return this.origToLine;
    }
    
    public void setToLine(final int toLine) {
        this.toLine = toLine;
    }
    
    public void setOriginalToLine(final int origToLine) {
        this.origToLine = origToLine;
    }
    
    public ArrayList<String> getArgs() {
        return this.args;
    }
    
    public void setArgs(final ArrayList<String> args) {
        this.args = args;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Macro) {
            final Macro macro = (Macro)obj;
            return macro.getName().equals(this.name) && macro.args.size() == this.args.size();
        }
        return super.equals(obj);
    }
    
    public void addArg(final String value) {
        this.args.add(value);
    }
    
    public String getSubstitutedLine(final int line, final TokenList args, final long counter, final ErrorList errors) {
        final TokenList tokens = this.program.getTokenList().get(line - 1);
        String s = this.program.getSourceLine(line);
        for (int i = tokens.size() - 1; i >= 0; --i) {
            final Token token = tokens.get(i);
            if (tokenIsMacroParameter(token.getValue(), true)) {
                int repl = -1;
                for (int j = 0; j < this.args.size(); ++j) {
                    if (this.args.get(j).equals(token.getValue())) {
                        repl = j;
                        break;
                    }
                }
                String substitute = token.getValue();
                if (repl != -1) {
                    substitute = args.get(repl + 1).toString();
                }
                else {
                    errors.add(new ErrorMessage(this.program, token.getSourceLine(), token.getStartPos(), "Unknown macro parameter"));
                }
                s = this.replaceToken(s, token, substitute);
            }
            else if (this.tokenIsMacroLabel(token.getValue())) {
                final String substitute2 = token.getValue() + "_M" + counter;
                s = this.replaceToken(s, token, substitute2);
            }
        }
        return s;
    }
    
    private boolean tokenIsMacroLabel(final String value) {
        return Collections.binarySearch(this.labels, value) >= 0;
    }
    
    private String replaceToken(final String source, final Token tokenToBeReplaced, final String substitute) {
        final String stringToBeReplaced = tokenToBeReplaced.getValue();
        final int pos = source.indexOf(stringToBeReplaced);
        return (pos < 0) ? source : (source.substring(0, pos) + substitute + source.substring(pos + stringToBeReplaced.length()));
    }
    
    public static boolean tokenIsMacroParameter(final String tokenValue, final boolean acceptSpimStyleParameters) {
        return (acceptSpimStyleParameters && tokenValue.length() > 0 && tokenValue.charAt(0) == '$' && RegisterFile.getUserRegister(tokenValue) == null && Coprocessor0.getRegister(tokenValue) == null && Coprocessor1.getRegister(tokenValue) == null) || (tokenValue.length() > 1 && tokenValue.charAt(0) == '%');
    }
    
    public void addLabel(final String value) {
        this.labels.add(value);
    }
    
    public void readyForCommit() {
        Collections.sort(this.labels);
    }
}
