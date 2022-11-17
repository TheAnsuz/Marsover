

package mars.venus.editors.jeditsyntax.tokenmarker;

import java.util.ArrayList;
import javax.swing.text.Segment;

public abstract class TokenMarker
{
    protected Token firstToken;
    protected Token lastToken;
    protected LineInfo[] lineInfo;
    protected int length;
    protected int lastLine;
    protected boolean nextLineRequested;
    
    public Token markTokens(final Segment line, final int lineIndex) {
        if (lineIndex >= this.length) {
            throw new IllegalArgumentException("Tokenizing invalid line: " + lineIndex);
        }
        this.lastToken = null;
        final LineInfo info = this.lineInfo[lineIndex];
        LineInfo prev;
        if (lineIndex == 0) {
            prev = null;
        }
        else {
            prev = this.lineInfo[lineIndex - 1];
        }
        final byte oldToken = info.token;
        final byte token = this.markTokensImpl(((prev == null) ? 0 : prev.token), line, lineIndex);
        info.token = token;
        if (this.lastLine != lineIndex || !this.nextLineRequested) {
            this.nextLineRequested = (oldToken != token);
        }
        this.lastLine = lineIndex;
        this.addToken(0, (byte)127);
        return this.firstToken;
    }
    
    protected abstract byte markTokensImpl(final byte p0, final Segment p1, final int p2);
    
    public boolean supportsMultilineTokens() {
        return true;
    }
    
    public void insertLines(final int index, final int lines) {
        if (lines <= 0) {
            return;
        }
        this.ensureCapacity(this.length += lines);
        final int len = index + lines;
        System.arraycopy(this.lineInfo, index, this.lineInfo, len, this.lineInfo.length - len);
        for (int i = index + lines - 1; i >= index; --i) {
            this.lineInfo[i] = new LineInfo();
        }
    }
    
    public void deleteLines(final int index, final int lines) {
        if (lines <= 0) {
            return;
        }
        final int len = index + lines;
        this.length -= lines;
        System.arraycopy(this.lineInfo, len, this.lineInfo, index, this.lineInfo.length - len);
    }
    
    public int getLineCount() {
        return this.length;
    }
    
    public boolean isNextLineRequested() {
        return this.nextLineRequested;
    }
    
    public ArrayList getTokenExactMatchHelp(final Token token, final String tokenText) {
        return null;
    }
    
    public ArrayList getTokenPrefixMatchHelp(final String line, final Token tokenList, final Token tokenAtOffset, final String tokenText) {
        return null;
    }
    
    protected TokenMarker() {
        this.lastLine = -1;
    }
    
    protected void ensureCapacity(final int index) {
        if (this.lineInfo == null) {
            this.lineInfo = new LineInfo[index + 1];
        }
        else if (this.lineInfo.length <= index) {
            final LineInfo[] lineInfoN = new LineInfo[(index + 1) * 2];
            System.arraycopy(this.lineInfo, 0, lineInfoN, 0, this.lineInfo.length);
            this.lineInfo = lineInfoN;
        }
    }
    
    protected void addToken(final int length, final byte id) {
        if (id >= 100 && id <= 126) {
            throw new InternalError("Invalid id: " + id);
        }
        if (length == 0 && id != 127) {
            return;
        }
        if (this.firstToken == null) {
            this.firstToken = new Token(length, id);
            this.lastToken = this.firstToken;
        }
        else if (this.lastToken == null) {
            this.lastToken = this.firstToken;
            this.firstToken.length = length;
            this.firstToken.id = id;
        }
        else if (this.lastToken.next == null) {
            this.lastToken.next = new Token(length, id);
            this.lastToken = this.lastToken.next;
        }
        else {
            this.lastToken = this.lastToken.next;
            this.lastToken.length = length;
            this.lastToken.id = id;
        }
    }
    
    public class LineInfo
    {
        public byte token;
        public Object obj;
        
        public LineInfo() {
        }
        
        public LineInfo(final byte token, final Object obj) {
            this.token = token;
            this.obj = obj;
        }
    }
}
