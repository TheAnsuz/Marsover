

package mars.venus.editors.jeditsyntax;

import java.awt.Color;
import javax.swing.JPopupMenu;
import mars.Globals;

public class TextAreaDefaults
{
    private static TextAreaDefaults DEFAULTS;
    public InputHandler inputHandler;
    public SyntaxDocument document;
    public boolean editable;
    public boolean caretVisible;
    public boolean caretBlinks;
    public boolean blockCaret;
    public int caretBlinkRate;
    public int electricScroll;
    public int tabSize;
    public int cols;
    public int rows;
    public SyntaxStyle[] styles;
    public Color caretColor;
    public Color selectionColor;
    public Color lineHighlightColor;
    public boolean lineHighlight;
    public Color bracketHighlightColor;
    public boolean bracketHighlight;
    public Color eolMarkerColor;
    public boolean eolMarkers;
    public boolean paintInvalid;
    public JPopupMenu popup;
    
    public static TextAreaDefaults getDefaults() {
        TextAreaDefaults.DEFAULTS = new TextAreaDefaults();
        (TextAreaDefaults.DEFAULTS.inputHandler = new DefaultInputHandler()).addDefaultKeyBindings();
        TextAreaDefaults.DEFAULTS.editable = true;
        TextAreaDefaults.DEFAULTS.blockCaret = false;
        TextAreaDefaults.DEFAULTS.caretVisible = true;
        TextAreaDefaults.DEFAULTS.caretBlinks = (Globals.getSettings().getCaretBlinkRate() != 0);
        TextAreaDefaults.DEFAULTS.caretBlinkRate = Globals.getSettings().getCaretBlinkRate();
        TextAreaDefaults.DEFAULTS.tabSize = Globals.getSettings().getEditorTabSize();
        TextAreaDefaults.DEFAULTS.electricScroll = 0;
        TextAreaDefaults.DEFAULTS.cols = 80;
        TextAreaDefaults.DEFAULTS.rows = 25;
        TextAreaDefaults.DEFAULTS.styles = SyntaxUtilities.getCurrentSyntaxStyles();
        TextAreaDefaults.DEFAULTS.caretColor = Color.black;
        TextAreaDefaults.DEFAULTS.selectionColor = new Color(13421823);
        TextAreaDefaults.DEFAULTS.lineHighlightColor = new Color(15658734);
        TextAreaDefaults.DEFAULTS.lineHighlight = Globals.getSettings().getBooleanSetting(15);
        TextAreaDefaults.DEFAULTS.bracketHighlightColor = Color.black;
        TextAreaDefaults.DEFAULTS.bracketHighlight = false;
        TextAreaDefaults.DEFAULTS.eolMarkerColor = new Color(39321);
        TextAreaDefaults.DEFAULTS.eolMarkers = false;
        TextAreaDefaults.DEFAULTS.paintInvalid = false;
        TextAreaDefaults.DEFAULTS.document = new SyntaxDocument();
        return TextAreaDefaults.DEFAULTS;
    }
}
