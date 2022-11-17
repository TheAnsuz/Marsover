

package mars.venus.editors.jeditsyntax.tokenmarker;

public class Token
{
    public static final byte NULL = 0;
    public static final byte COMMENT1 = 1;
    public static final byte COMMENT2 = 2;
    public static final byte LITERAL1 = 3;
    public static final byte LITERAL2 = 4;
    public static final byte LABEL = 5;
    public static final byte KEYWORD1 = 6;
    public static final byte KEYWORD2 = 7;
    public static final byte KEYWORD3 = 8;
    public static final byte OPERATOR = 9;
    public static final byte INVALID = 10;
    public static final byte MACRO_ARG = 11;
    public static final byte ID_COUNT = 12;
    public static final byte INTERNAL_FIRST = 100;
    public static final byte INTERNAL_LAST = 126;
    public static final byte END = Byte.MAX_VALUE;
    public int length;
    public byte id;
    public Token next;
    
    public Token(final int length, final byte id) {
        this.length = length;
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "[id=" + this.id + ",length=" + this.length + "]";
    }
}
