

package mars.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import mars.Globals;

public class EditorFont
{
    private static final String[] styleStrings;
    private static final int[] styleInts;
    public static final String DEFAULT_STYLE_STRING;
    public static final int DEFAULT_STYLE_INT;
    public static final int MIN_SIZE = 6;
    public static final int MAX_SIZE = 72;
    public static final int DEFAULT_SIZE = 12;
    private static final String[] allCommonFamilies;
    private static final String TAB_STRING = "\t";
    private static final char TAB_CHAR = '\t';
    private static final String SPACES = "                                                  ";
    private static final String[] commonFamilies;
    
    public static String[] getCommonFamilies() {
        return EditorFont.commonFamilies;
    }
    
    public static String[] getAllFamilies() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }
    
    public static String[] getFontStyleStrings() {
        return EditorFont.styleStrings;
    }
    
    public static int styleStringToStyleInt(final String style) {
        final String styleLower = style.toLowerCase();
        for (int i = 0; i < EditorFont.styleStrings.length; ++i) {
            if (styleLower.equals(EditorFont.styleStrings[i].toLowerCase())) {
                return EditorFont.styleInts[i];
            }
        }
        return EditorFont.DEFAULT_STYLE_INT;
    }
    
    public static String styleIntToStyleString(final int style) {
        for (int i = 0; i < EditorFont.styleInts.length; ++i) {
            if (style == EditorFont.styleInts[i]) {
                return EditorFont.styleStrings[i];
            }
        }
        return EditorFont.DEFAULT_STYLE_STRING;
    }
    
    public static String sizeIntToSizeString(final int size) {
        final int result = (size < 6) ? 6 : ((size > 72) ? 72 : size);
        return String.valueOf(result);
    }
    
    public static int sizeStringToSizeInt(final String size) {
        int result = 12;
        try {
            result = Integer.parseInt(size);
        }
        catch (NumberFormatException ex) {}
        return (result < 6) ? 6 : ((result > 72) ? 72 : result);
    }
    
    public static Font createFontFromStringValues(final String family, final String style, final String size) {
        return new Font(family, styleStringToStyleInt(style), sizeStringToSizeInt(size));
    }
    
    public static String substituteSpacesForTabs(final String string) {
        return substituteSpacesForTabs(string, Globals.getSettings().getEditorTabSize());
    }
    
    public static String substituteSpacesForTabs(final String string, final int tabSize) {
        if (!string.contains("\t")) {
            return string;
        }
        final StringBuffer result = new StringBuffer(string);
        for (int i = 0; i < result.length(); ++i) {
            if (result.charAt(i) == '\t') {
                result.replace(i, i + 1, "                                                  ".substring(0, tabSize - i % tabSize));
            }
        }
        return result.toString();
    }
    
    private static String[] actualCommonFamilies() {
        String[] result = new String[EditorFont.allCommonFamilies.length];
        final String[] availableFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(availableFamilies);
        int k = 0;
        for (int i = 0; i < EditorFont.allCommonFamilies.length; ++i) {
            if (Arrays.binarySearch(availableFamilies, EditorFont.allCommonFamilies[i]) >= 0) {
                result[k++] = EditorFont.allCommonFamilies[i];
            }
        }
        if (k < EditorFont.allCommonFamilies.length) {
            final String[] temp = new String[k];
            System.arraycopy(result, 0, temp, 0, k);
            result = temp;
        }
        return result;
    }
    
    static {
        styleStrings = new String[] { "Plain", "Bold", "Italic", "Bold + Italic" };
        styleInts = new int[] { 0, 1, 2, 3 };
        DEFAULT_STYLE_STRING = EditorFont.styleStrings[0];
        DEFAULT_STYLE_INT = EditorFont.styleInts[0];
        allCommonFamilies = new String[] { "Arial", "Courier New", "Georgia", "Lucida Sans Typewriter", "Times New Roman", "Verdana" };
        commonFamilies = actualCommonFamilies();
    }
}
