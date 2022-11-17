

package mars.assembler;

import mars.MIPSprogram;

public class SourceLine
{
    private final String source;
    private String filename;
    private final MIPSprogram mipsProgram;
    private final int lineNumber;
    
    public SourceLine(final String source, final MIPSprogram mipsProgram, final int lineNumber) {
        this.source = source;
        this.mipsProgram = mipsProgram;
        if (mipsProgram != null) {
            this.filename = mipsProgram.getFilename();
        }
        this.lineNumber = lineNumber;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public int getLineNumber() {
        return this.lineNumber;
    }
    
    public MIPSprogram getMIPSprogram() {
        return this.mipsProgram;
    }
}
