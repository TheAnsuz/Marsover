

package mars.venus.editors.jeditsyntax;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class TextUtilities
{
    public static int findMatchingBracket(final Document doc, int offset) throws BadLocationException {
        if (doc.getLength() == 0) {
            return -1;
        }
        final char c = doc.getText(offset, 1).charAt(0);
        char cprime = '\0';
        boolean direction = false;
        switch (c) {
            case '(': {
                cprime = ')';
                direction = false;
                break;
            }
            case ')': {
                cprime = '(';
                direction = true;
                break;
            }
            case '[': {
                cprime = ']';
                direction = false;
                break;
            }
            case ']': {
                cprime = '[';
                direction = true;
                break;
            }
            case '{': {
                cprime = '}';
                direction = false;
                break;
            }
            case '}': {
                cprime = '{';
                direction = true;
                break;
            }
            default: {
                return -1;
            }
        }
        if (direction) {
            int count = 1;
            final String text = doc.getText(0, offset);
            for (int i = offset - 1; i >= 0; --i) {
                final char x = text.charAt(i);
                if (x == c) {
                    ++count;
                }
                else if (x == cprime && --count == 0) {
                    return i;
                }
            }
        }
        else {
            int count = 1;
            ++offset;
            final int len = doc.getLength() - offset;
            final String text2 = doc.getText(offset, len);
            for (int j = 0; j < len; ++j) {
                final char x2 = text2.charAt(j);
                if (x2 == c) {
                    ++count;
                }
                else if (x2 == cprime && --count == 0) {
                    return j + offset;
                }
            }
        }
        return -1;
    }
    
    public static int findWordStart(final String line, final int pos, String noWordSep) {
        char ch = line.charAt(pos - 1);
        if (noWordSep == null) {
            noWordSep = "";
        }
        final boolean selectNoLetter = !Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1;
        int wordStart = 0;
        for (int i = pos - 1; i >= 0; --i) {
            ch = line.charAt(i);
            if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
                wordStart = i + 1;
                break;
            }
        }
        return wordStart;
    }
    
    public static int findWordEnd(final String line, final int pos, String noWordSep) {
        char ch = line.charAt(pos);
        if (noWordSep == null) {
            noWordSep = "";
        }
        final boolean selectNoLetter = !Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1;
        int wordEnd = line.length();
        for (int i = pos; i < line.length(); ++i) {
            ch = line.charAt(i);
            if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
                wordEnd = i;
                break;
            }
        }
        return wordEnd;
    }
}
