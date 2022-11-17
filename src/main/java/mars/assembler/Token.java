

package mars.assembler;

import mars.MIPSprogram;

public class Token
{
    private TokenTypes type;
    private String value;
    private MIPSprogram sourceMIPSprogram;
    private int sourceLine;
    private int sourcePos;
    private MIPSprogram originalMIPSprogram;
    private int originalSourceLine;
    
    public Token(final TokenTypes type, final String value, final MIPSprogram sourceMIPSprogram, final int line, final int start) {
        this.type = type;
        this.value = value;
        this.sourceMIPSprogram = sourceMIPSprogram;
        this.sourceLine = line;
        this.sourcePos = start;
        this.originalMIPSprogram = sourceMIPSprogram;
        this.originalSourceLine = line;
    }
    
    public void setOriginal(final MIPSprogram origProgram, final int origSourceLine) {
        this.originalMIPSprogram = origProgram;
        this.originalSourceLine = origSourceLine;
    }
    
    public MIPSprogram getOriginalProgram() {
        return this.originalMIPSprogram;
    }
    
    public int getOriginalSourceLine() {
        return this.originalSourceLine;
    }
    
    public TokenTypes getType() {
        return this.type;
    }
    
    public void setType(final TokenTypes type) {
        this.type = type;
    }
    
    public String getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
    public MIPSprogram getSourceMIPSprogram() {
        return this.sourceMIPSprogram;
    }
    
    public int getSourceLine() {
        return this.sourceLine;
    }
    
    public int getStartPos() {
        return this.sourcePos;
    }
}
