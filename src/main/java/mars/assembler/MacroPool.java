

package mars.assembler;

import java.util.ArrayList;
import mars.MIPSprogram;

public class MacroPool
{
    private final MIPSprogram program;
    private final ArrayList<Macro> macroList;
    private Macro current;
    private final ArrayList<Integer> callStack;
    private final ArrayList<Integer> callStackOrigLines;
    private int counter;
    
    public MacroPool(final MIPSprogram mipsProgram) {
        this.program = mipsProgram;
        this.macroList = new ArrayList<>();
        this.callStack = new ArrayList<>();
        this.callStackOrigLines = new ArrayList<>();
        this.current = null;
        this.counter = 0;
    }
    
    public void beginMacro(final Token nameToken) {
        (this.current = new Macro()).setName(nameToken.getValue());
        this.current.setFromLine(nameToken.getSourceLine());
        this.current.setOriginalFromLine(nameToken.getOriginalSourceLine());
        this.current.setProgram(this.program);
    }
    
    public void commitMacro(final Token endToken) {
        this.current.setToLine(endToken.getSourceLine());
        this.current.setOriginalToLine(endToken.getOriginalSourceLine());
        this.current.readyForCommit();
        this.macroList.add(this.current);
        this.current = null;
    }
    
    public Macro getMatchingMacro(final TokenList tokens, final int callerLine) {
        if (tokens.size() < 1) {
            return null;
        }
        Macro ret = null;
        final Token firstToken = tokens.get(0);
        for (final Macro macro : this.macroList) {
            if (macro.getName().equals(firstToken.getValue()) && macro.getArgs().size() + 1 == tokens.size() && (ret == null || ret.getFromLine() < macro.getFromLine())) {
                ret = macro;
            }
        }
        return ret;
    }
    
    public boolean matchesAnyMacroName(final String value) {
        for (final Macro macro : this.macroList) {
            if (macro.getName().equals(value)) {
                return true;
            }
        }
        return false;
    }
    
    public Macro getCurrent() {
        return this.current;
    }
    
    public void setCurrent(final Macro current) {
        this.current = current;
    }
    
    public int getNextCounter() {
        return this.counter++;
    }
    
    public ArrayList<Integer> getCallStack() {
        return this.callStack;
    }
    
    public boolean pushOnCallStack(final Token token) {
        final int sourceLine = token.getSourceLine();
        final int origSourceLine = token.getOriginalSourceLine();
        if (this.callStack.contains(sourceLine)) {
            return true;
        }
        this.callStack.add(sourceLine);
        this.callStackOrigLines.add(origSourceLine);
        return false;
    }
    
    public void popFromCallStack() {
        this.callStack.remove(this.callStack.size() - 1);
        this.callStackOrigLines.remove(this.callStackOrigLines.size() - 1);
    }
    
    public String getExpansionHistory() {
        String ret = "";
        for (int i = 0; i < this.callStackOrigLines.size(); ++i) {
            if (i > 0) {
                ret += "->";
            }
            ret += this.callStackOrigLines.get(i).toString();
        }
        return ret;
    }
}
