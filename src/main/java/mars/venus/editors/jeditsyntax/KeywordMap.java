

package mars.venus.editors.jeditsyntax;

import javax.swing.text.Segment;

public class KeywordMap
{
    protected int mapLength;
    private Keyword[] map;
    private boolean ignoreCase;
    
    public KeywordMap(final boolean ignoreCase) {
        this(ignoreCase, 52);
        this.ignoreCase = ignoreCase;
    }
    
    public KeywordMap(final boolean ignoreCase, final int mapLength) {
        this.mapLength = mapLength;
        this.ignoreCase = ignoreCase;
        this.map = new Keyword[mapLength];
    }
    
    public byte lookup(final Segment text, final int offset, final int length) {
        if (length == 0) {
            return 0;
        }
        if (text.array[offset] == '%') {
            return 11;
        }
        for (Keyword k = this.map[this.getSegmentMapKey(text, offset, length)]; k != null; k = k.next) {
            if (length == k.keyword.length) {
                if (SyntaxUtilities.regionMatches(this.ignoreCase, text, offset, k.keyword)) {
                    return k.id;
                }
            }
        }
        return 0;
    }
    
    public void add(final String keyword, final byte id) {
        final int key = this.getStringMapKey(keyword);
        this.map[key] = new Keyword(keyword.toCharArray(), id, this.map[key]);
    }
    
    public boolean getIgnoreCase() {
        return this.ignoreCase;
    }
    
    public void setIgnoreCase(final boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
    protected int getStringMapKey(final String s) {
        return (Character.toUpperCase(s.charAt(0)) + Character.toUpperCase(s.charAt(s.length() - 1))) % this.mapLength;
    }
    
    protected int getSegmentMapKey(final Segment s, final int off, final int len) {
        return (Character.toUpperCase(s.array[off]) + Character.toUpperCase(s.array[off + len - 1])) % this.mapLength;
    }
    
    class Keyword
    {
        public char[] keyword;
        public byte id;
        public Keyword next;
        
        public Keyword(final char[] keyword, final byte id, final Keyword next) {
            this.keyword = keyword;
            this.id = id;
            this.next = next;
        }
    }
}
