

package mars.venus.editors.jeditsyntax;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.KeyStroke;
import mars.Globals;

public class DefaultInputHandler extends InputHandler
{
    private Hashtable bindings;
    private Hashtable currentBindings;
    
    public DefaultInputHandler() {
        final Hashtable hashtable = new Hashtable();
        this.currentBindings = hashtable;
        this.bindings = hashtable;
    }
    
    @Override
    public void addDefaultKeyBindings() {
        this.addKeyBinding("BACK_SPACE", DefaultInputHandler.BACKSPACE);
        this.addKeyBinding("C+BACK_SPACE", DefaultInputHandler.BACKSPACE_WORD);
        this.addKeyBinding("DELETE", DefaultInputHandler.DELETE);
        this.addKeyBinding("C+DELETE", DefaultInputHandler.DELETE_WORD);
        this.addKeyBinding("ENTER", DefaultInputHandler.INSERT_BREAK);
        this.addKeyBinding("TAB", DefaultInputHandler.INSERT_TAB);
        this.addKeyBinding("INSERT", DefaultInputHandler.OVERWRITE);
        this.addKeyBinding("C+\\", DefaultInputHandler.TOGGLE_RECT);
        this.addKeyBinding("HOME", DefaultInputHandler.HOME);
        this.addKeyBinding("END", DefaultInputHandler.END);
        this.addKeyBinding("C+A", DefaultInputHandler.SELECT_ALL);
        this.addKeyBinding("S+HOME", DefaultInputHandler.SELECT_HOME);
        this.addKeyBinding("S+END", DefaultInputHandler.SELECT_END);
        this.addKeyBinding("C+HOME", DefaultInputHandler.DOCUMENT_HOME);
        this.addKeyBinding("C+END", DefaultInputHandler.DOCUMENT_END);
        this.addKeyBinding("CS+HOME", DefaultInputHandler.SELECT_DOC_HOME);
        this.addKeyBinding("CS+END", DefaultInputHandler.SELECT_DOC_END);
        this.addKeyBinding("PAGE_UP", DefaultInputHandler.PREV_PAGE);
        this.addKeyBinding("PAGE_DOWN", DefaultInputHandler.NEXT_PAGE);
        this.addKeyBinding("S+PAGE_UP", DefaultInputHandler.SELECT_PREV_PAGE);
        this.addKeyBinding("S+PAGE_DOWN", DefaultInputHandler.SELECT_NEXT_PAGE);
        this.addKeyBinding("LEFT", DefaultInputHandler.PREV_CHAR);
        this.addKeyBinding("S+LEFT", DefaultInputHandler.SELECT_PREV_CHAR);
        this.addKeyBinding("C+LEFT", DefaultInputHandler.PREV_WORD);
        this.addKeyBinding("CS+LEFT", DefaultInputHandler.SELECT_PREV_WORD);
        this.addKeyBinding("RIGHT", DefaultInputHandler.NEXT_CHAR);
        this.addKeyBinding("S+RIGHT", DefaultInputHandler.SELECT_NEXT_CHAR);
        this.addKeyBinding("C+RIGHT", DefaultInputHandler.NEXT_WORD);
        this.addKeyBinding("CS+RIGHT", DefaultInputHandler.SELECT_NEXT_WORD);
        this.addKeyBinding("UP", DefaultInputHandler.PREV_LINE);
        this.addKeyBinding("S+UP", DefaultInputHandler.SELECT_PREV_LINE);
        this.addKeyBinding("DOWN", DefaultInputHandler.NEXT_LINE);
        this.addKeyBinding("S+DOWN", DefaultInputHandler.SELECT_NEXT_LINE);
        this.addKeyBinding("C+ENTER", DefaultInputHandler.REPEAT);
        this.addKeyBinding("C+C", DefaultInputHandler.CLIP_COPY);
        this.addKeyBinding("C+V", DefaultInputHandler.CLIP_PASTE);
        this.addKeyBinding("C+X", DefaultInputHandler.CLIP_CUT);
    }
    
    @Override
    public void addKeyBinding(final String keyBinding, final ActionListener action) {
        Hashtable current = this.bindings;
        final StringTokenizer st = new StringTokenizer(keyBinding);
        while (st.hasMoreTokens()) {
            final KeyStroke keyStroke = parseKeyStroke(st.nextToken());
            if (keyStroke == null) {
                return;
            }
            if (st.hasMoreTokens()) {
                Object o = current.get(keyStroke);
                if (o instanceof Hashtable) {
                    current = (Hashtable)o;
                }
                else {
                    o = new Hashtable();
                    current.put(keyStroke, o);
                    current = (Hashtable)o;
                }
            }
            else {
                current.put(keyStroke, action);
            }
        }
    }
    
    @Override
    public void removeKeyBinding(final String keyBinding) {
        throw new InternalError("Not yet implemented");
    }
    
    @Override
    public void removeAllKeyBindings() {
        this.bindings.clear();
    }
    
    @Override
    public InputHandler copy() {
        return new DefaultInputHandler(this);
    }
    
    @Override
    public void keyPressed(final KeyEvent evt) {
        final int keyCode = evt.getKeyCode();
        final int modifiers = evt.getModifiers();
        if (keyCode == 17 || keyCode == 16 || keyCode == 18 || keyCode == 157) {
            return;
        }
        if ((modifiers & 0xFFFFFFFE) != 0x0 || evt.isActionKey() || keyCode == 8 || keyCode == 127 || keyCode == 10 || keyCode == 9 || keyCode == 27) {
            if (this.grabAction != null) {
                this.handleGrabAction(evt);
                return;
            }
            final KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            final Object o = this.currentBindings.get(keyStroke);
            if (o == null) {
                if (this.currentBindings != this.bindings) {
                    Toolkit.getDefaultToolkit().beep();
                    this.repeatCount = 0;
                    this.repeat = false;
                    evt.consume();
                }
                this.currentBindings = this.bindings;
                Globals.getGui().dispatchEventToMenu(evt);
                evt.consume();
                return;
            }
            if (o instanceof ActionListener) {
                this.currentBindings = this.bindings;
                this.executeAction((ActionListener)o, evt.getSource(), null);
                evt.consume();
                return;
            }
            if (o instanceof Hashtable) {
                this.currentBindings = (Hashtable)o;
                evt.consume();
            }
        }
    }
    
    @Override
    public void keyTyped(final KeyEvent evt) {
        final int modifiers = evt.getModifiers();
        final char c = evt.getKeyChar();
        if ((modifiers & 0x4) != 0x0) {
            return;
        }
        if (c != '\uffff' && ((modifiers & 0x8) == 0x0 || System.getProperty("os.name").contains("OS X")) && c >= ' ' && c != '\u007f') {
            final KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(c));
            final Object o = this.currentBindings.get(keyStroke);
            if (o instanceof Hashtable) {
                this.currentBindings = (Hashtable)o;
                return;
            }
            if (o instanceof ActionListener) {
                this.currentBindings = this.bindings;
                this.executeAction((ActionListener)o, evt.getSource(), String.valueOf(c));
                return;
            }
            this.currentBindings = this.bindings;
            if (this.grabAction != null) {
                this.handleGrabAction(evt);
                return;
            }
            if (this.repeat && Character.isDigit(c)) {
                this.repeatCount *= 10;
                this.repeatCount += c - '0';
                return;
            }
            this.executeAction(DefaultInputHandler.INSERT_CHAR, evt.getSource(), String.valueOf(evt.getKeyChar()));
            this.repeatCount = 0;
            this.repeat = false;
        }
    }
    
    public static KeyStroke parseKeyStroke(final String keyStroke) {
        if (keyStroke == null) {
            return null;
        }
        int modifiers = 0;
        final int index = keyStroke.indexOf(43);
        if (index != -1) {
            for (int i = 0; i < index; ++i) {
                switch (Character.toUpperCase(keyStroke.charAt(i))) {
                    case 'A': {
                        modifiers |= 0x8;
                        break;
                    }
                    case 'C': {
                        modifiers |= 0x2;
                        break;
                    }
                    case 'M': {
                        modifiers |= 0x4;
                        break;
                    }
                    case 'S': {
                        modifiers |= 0x1;
                        break;
                    }
                }
            }
        }
        final String key = keyStroke.substring(index + 1);
        if (key.length() == 1) {
            final char ch = Character.toUpperCase(key.charAt(0));
            if (modifiers == 0) {
                return KeyStroke.getKeyStroke(ch);
            }
            return KeyStroke.getKeyStroke(ch, modifiers);
        }
        else {
            if (key.length() == 0) {
                System.err.println("Invalid key stroke: " + keyStroke);
                return null;
            }
            int ch2;
            try {
                ch2 = KeyEvent.class.getField("VK_".concat(key)).getInt(null);
            }
            catch (Exception e) {
                System.err.println("Invalid key stroke: " + keyStroke);
                return null;
            }
            return KeyStroke.getKeyStroke(ch2, modifiers);
        }
    }
    
    private DefaultInputHandler(final DefaultInputHandler copy) {
        final Hashtable bindings = copy.bindings;
        this.currentBindings = bindings;
        this.bindings = bindings;
    }
}
