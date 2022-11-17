

package mars.assembler;

import java.util.ArrayList;

public class TokenList implements Cloneable
{
    private ArrayList<Token> tokenList;
    private String processedLine;
    
    public TokenList() {
        this.tokenList = new ArrayList();
        this.processedLine = "";
    }
    
    public void setProcessedLine(final String line) {
        this.processedLine = line;
    }
    
    public String getProcessedLine() {
        return this.processedLine;
    }
    
    public Token get(final int pos) {
        return this.tokenList.get(pos);
    }
    
    public void set(final int pos, final Token replacement) {
        this.tokenList.set(pos, replacement);
    }
    
    public int size() {
        return this.tokenList.size();
    }
    
    public void add(final Token token) {
        this.tokenList.add(token);
    }
    
    public void remove(final int pos) {
        this.tokenList.remove(pos);
    }
    
    public boolean isEmpty() {
        return this.tokenList.isEmpty();
    }
    
    @Override
    public String toString() {
        String stringified = "";
        for (int i = 0; i < this.tokenList.size(); ++i) {
            stringified = stringified + this.tokenList.get(i).toString() + " ";
        }
        return stringified;
    }
    
    public String toTypeString() {
        String stringified = "";
        for (int i = 0; i < this.tokenList.size(); ++i) {
            stringified = stringified + this.tokenList.get(i).getType().toString() + " ";
        }
        return stringified;
    }
    
    public Object clone() {
        try {
            final TokenList t = (TokenList)super.clone();
            t.tokenList = (ArrayList)this.tokenList.clone();
            return t;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
