

package mars;

import dev.amrv.marsover.AppProperties;
import dev.amrv.marsover.Marsover;
import java.util.StringTokenizer;
import mars.util.Binary;
import java.awt.Color;
import mars.util.EditorFont;
import java.awt.Font;
import mars.venus.editors.jeditsyntax.SyntaxUtilities;
import java.util.prefs.BackingStoreException;
import mars.venus.editors.jeditsyntax.SyntaxStyle;
import java.util.prefs.Preferences;
import java.util.Observable;

public class Settings extends Observable
{
    private static String settingsFile;
    public static final int EXTENDED_ASSEMBLER_ENABLED = 0;
    public static final int BARE_MACHINE_ENABLED = 1;
    public static final int ASSEMBLE_ON_OPEN_ENABLED = 2;
    public static final int ASSEMBLE_ALL_ENABLED = 3;
    public static final int LABEL_WINDOW_VISIBILITY = 4;
    public static final int DISPLAY_ADDRESSES_IN_HEX = 5;
    public static final int DISPLAY_VALUES_IN_HEX = 6;
    public static final int EXCEPTION_HANDLER_ENABLED = 7;
    public static final int DELAYED_BRANCHING_ENABLED = 8;
    public static final int EDITOR_LINE_NUMBERS_DISPLAYED = 9;
    public static final int WARNINGS_ARE_ERRORS = 10;
    public static final int PROGRAM_ARGUMENTS = 11;
    public static final int DATA_SEGMENT_HIGHLIGHTING = 12;
    public static final int REGISTERS_HIGHLIGHTING = 13;
    public static final int START_AT_MAIN = 14;
    public static final int EDITOR_CURRENT_LINE_HIGHLIGHTING = 15;
    public static final int POPUP_INSTRUCTION_GUIDANCE = 16;
    public static final int POPUP_SYSCALL_INPUT = 17;
    public static final int GENERIC_TEXT_EDITOR = 18;
    public static final int AUTO_INDENT = 19;
    public static final int SELF_MODIFYING_CODE_ENABLED = 20;
    private static String[] booleanSettingsKeys;
    public static boolean[] defaultBooleanSettingsValues;
    public static final int EXCEPTION_HANDLER = 0;
    public static final int TEXT_COLUMN_ORDER = 1;
    public static final int LABEL_SORT_STATE = 2;
    public static final int MEMORY_CONFIGURATION = 3;
    public static final int CARET_BLINK_RATE = 4;
    public static final int EDITOR_TAB_SIZE = 5;
    public static final int EDITOR_POPUP_PREFIX_LENGTH = 6;
    private static final String[] stringSettingsKeys;
    private static String[] defaultStringSettingsValues;
    public static final int EDITOR_FONT = 0;
    public static final int EVEN_ROW_FONT = 1;
    public static final int ODD_ROW_FONT = 2;
    public static final int TEXTSEGMENT_HIGHLIGHT_FONT = 3;
    public static final int TEXTSEGMENT_DELAYSLOT_HIGHLIGHT_FONT = 4;
    public static final int DATASEGMENT_HIGHLIGHT_FONT = 5;
    public static final int REGISTER_HIGHLIGHT_FONT = 6;
    private static final String[] fontFamilySettingsKeys;
    private static final String[] fontStyleSettingsKeys;
    private static final String[] fontSizeSettingsKeys;
    private static final String[] defaultFontFamilySettingsValues;
    private static final String[] defaultFontStyleSettingsValues;
    private static final String[] defaultFontSizeSettingsValues;
    public static final int EVEN_ROW_BACKGROUND = 0;
    public static final int EVEN_ROW_FOREGROUND = 1;
    public static final int ODD_ROW_BACKGROUND = 2;
    public static final int ODD_ROW_FOREGROUND = 3;
    public static final int TEXTSEGMENT_HIGHLIGHT_BACKGROUND = 4;
    public static final int TEXTSEGMENT_HIGHLIGHT_FOREGROUND = 5;
    public static final int TEXTSEGMENT_DELAYSLOT_HIGHLIGHT_BACKGROUND = 6;
    public static final int TEXTSEGMENT_DELAYSLOT_HIGHLIGHT_FOREGROUND = 7;
    public static final int DATASEGMENT_HIGHLIGHT_BACKGROUND = 8;
    public static final int DATASEGMENT_HIGHLIGHT_FOREGROUND = 9;
    public static final int REGISTER_HIGHLIGHT_BACKGROUND = 10;
    public static final int REGISTER_HIGHLIGHT_FOREGROUND = 11;
    private static final String[] colorSettingsKeys;
    private static String[] defaultColorSettingsValues;
    private boolean[] booleanSettingsValues;
    private String[] stringSettingsValues;
    private String[] fontFamilySettingsValues;
    private String[] fontStyleSettingsValues;
    private String[] fontSizeSettingsValues;
    private String[] colorSettingsValues;
    private AppProperties preferences;
    private String[] syntaxStyleColorSettingsValues;
    private boolean[] syntaxStyleBoldSettingsValues;
    private boolean[] syntaxStyleItalicSettingsValues;
    private static final String SYNTAX_STYLE_COLOR_PREFIX = "SyntaxStyleColor_";
    private static final String SYNTAX_STYLE_BOLD_PREFIX = "SyntaxStyleBold_";
    private static final String SYNTAX_STYLE_ITALIC_PREFIX = "SyntaxStyleItalic_";
    private static String[] syntaxStyleColorSettingsKeys;
    private static String[] syntaxStyleBoldSettingsKeys;
    private static String[] syntaxStyleItalicSettingsKeys;
    private static String[] defaultSyntaxStyleColorSettingsValues;
    private static boolean[] defaultSyntaxStyleBoldSettingsValues;
    private static boolean[] defaultSyntaxStyleItalicSettingsValues;
    
    public Settings() {
        this(true);
    }
    
    public Settings(final boolean gui) {
        this.booleanSettingsValues = new boolean[Settings.booleanSettingsKeys.length];
        this.stringSettingsValues = new String[Settings.stringSettingsKeys.length];
        this.fontFamilySettingsValues = new String[Settings.fontFamilySettingsKeys.length];
        this.fontStyleSettingsValues = new String[Settings.fontStyleSettingsKeys.length];
        this.fontSizeSettingsValues = new String[Settings.fontSizeSettingsKeys.length];
        this.colorSettingsValues = new String[Settings.colorSettingsKeys.length];
        this.preferences = new AppProperties(Marsover.FILE_SETTINGS, Marsover.RES_SETTINGS);
        this.initialize();
    }
    
    public boolean getBackSteppingEnabled() {
        return Globals.program != null && Globals.program.getBackStepper() != null && Globals.program.getBackStepper().enabled();
    }
    
    public void reset(final boolean gui) {
        this.initialize();
    }
    
    public void setEditorSyntaxStyleByPosition(final int index, final SyntaxStyle syntaxStyle) {
        this.syntaxStyleColorSettingsValues[index] = syntaxStyle.getColorAsHexString();
        this.syntaxStyleItalicSettingsValues[index] = syntaxStyle.isItalic();
        this.syntaxStyleBoldSettingsValues[index] = syntaxStyle.isBold();
        this.saveEditorSyntaxStyle(index);
    }
    
    public SyntaxStyle getEditorSyntaxStyleByPosition(final int index) {
        return new SyntaxStyle(this.getColorValueByPosition(index, this.syntaxStyleColorSettingsValues), this.syntaxStyleItalicSettingsValues[index], this.syntaxStyleBoldSettingsValues[index]);
    }
    
    public SyntaxStyle getDefaultEditorSyntaxStyleByPosition(final int index) {
        return new SyntaxStyle(this.getColorValueByPosition(index, Settings.defaultSyntaxStyleColorSettingsValues), Settings.defaultSyntaxStyleItalicSettingsValues[index], Settings.defaultSyntaxStyleBoldSettingsValues[index]);
    }
    
    private void saveEditorSyntaxStyle(final int index) {

            this.preferences.put(Settings.syntaxStyleColorSettingsKeys[index], this.syntaxStyleColorSettingsValues[index]);
            this.preferences.putBoolean(Settings.syntaxStyleBoldSettingsKeys[index], this.syntaxStyleBoldSettingsValues[index]);
            this.preferences.putBoolean(Settings.syntaxStyleItalicSettingsKeys[index], this.syntaxStyleItalicSettingsValues[index]);

    }
    
    private void initializeEditorSyntaxStyles() {
        final SyntaxStyle[] syntaxStyle = SyntaxUtilities.getDefaultSyntaxStyles();
        final int tokens = syntaxStyle.length;
        Settings.syntaxStyleColorSettingsKeys = new String[tokens];
        Settings.syntaxStyleBoldSettingsKeys = new String[tokens];
        Settings.syntaxStyleItalicSettingsKeys = new String[tokens];
        Settings.defaultSyntaxStyleColorSettingsValues = new String[tokens];
        Settings.defaultSyntaxStyleBoldSettingsValues = new boolean[tokens];
        Settings.defaultSyntaxStyleItalicSettingsValues = new boolean[tokens];
        this.syntaxStyleColorSettingsValues = new String[tokens];
        this.syntaxStyleBoldSettingsValues = new boolean[tokens];
        this.syntaxStyleItalicSettingsValues = new boolean[tokens];
        for (int i = 0; i < tokens; ++i) {
            Settings.syntaxStyleColorSettingsKeys[i] = "SyntaxStyleColor_" + i;
            Settings.syntaxStyleBoldSettingsKeys[i] = "SyntaxStyleBold_" + i;
            Settings.syntaxStyleItalicSettingsKeys[i] = "SyntaxStyleItalic_" + i;
            this.syntaxStyleColorSettingsValues[i] = (Settings.defaultSyntaxStyleColorSettingsValues[i] = syntaxStyle[i].getColorAsHexString());
            this.syntaxStyleBoldSettingsValues[i] = (Settings.defaultSyntaxStyleBoldSettingsValues[i] = syntaxStyle[i].isBold());
            this.syntaxStyleItalicSettingsValues[i] = (Settings.defaultSyntaxStyleItalicSettingsValues[i] = syntaxStyle[i].isItalic());
        }
    }
    
    private void getEditorSyntaxStyleSettingsFromPreferences() {
        for (int i = 0; i < Settings.syntaxStyleColorSettingsKeys.length; ++i) {
            this.syntaxStyleColorSettingsValues[i] = this.preferences.get(Settings.syntaxStyleColorSettingsKeys[i], this.syntaxStyleColorSettingsValues[i]);
            this.syntaxStyleBoldSettingsValues[i] = this.preferences.getBoolean(Settings.syntaxStyleBoldSettingsKeys[i], this.syntaxStyleBoldSettingsValues[i]);
            this.syntaxStyleItalicSettingsValues[i] = this.preferences.getBoolean(Settings.syntaxStyleItalicSettingsKeys[i], this.syntaxStyleItalicSettingsValues[i]);
        }
    }
    
    public boolean getBooleanSetting(final int id) {
        if (id >= 0 && id < this.booleanSettingsValues.length) {
            return this.booleanSettingsValues[id];
        }
        throw new IllegalArgumentException("Invalid boolean setting ID");
    }
    
    @Deprecated
    public boolean getBareMachineEnabled() {
        return this.booleanSettingsValues[1];
    }
    
    @Deprecated
    public boolean getExtendedAssemblerEnabled() {
        return this.booleanSettingsValues[0];
    }
    
    @Deprecated
    public boolean getAssembleOnOpenEnabled() {
        return this.booleanSettingsValues[2];
    }
    
    @Deprecated
    public boolean getDisplayAddressesInHex() {
        return this.booleanSettingsValues[5];
    }
    
    @Deprecated
    public boolean getDisplayValuesInHex() {
        return this.booleanSettingsValues[6];
    }
    
    @Deprecated
    public boolean getAssembleAllEnabled() {
        return this.booleanSettingsValues[3];
    }
    
    @Deprecated
    public boolean getExceptionHandlerEnabled() {
        return this.booleanSettingsValues[7];
    }
    
    @Deprecated
    public boolean getDelayedBranchingEnabled() {
        return this.booleanSettingsValues[8];
    }
    
    @Deprecated
    public boolean getLabelWindowVisibility() {
        return this.booleanSettingsValues[4];
    }
    
    @Deprecated
    public boolean getEditorLineNumbersDisplayed() {
        return this.booleanSettingsValues[9];
    }
    
    @Deprecated
    public boolean getWarningsAreErrors() {
        return this.booleanSettingsValues[10];
    }
    
    @Deprecated
    public boolean getProgramArguments() {
        return this.booleanSettingsValues[11];
    }
    
    @Deprecated
    public boolean getDataSegmentHighlighting() {
        return this.booleanSettingsValues[12];
    }
    
    @Deprecated
    public boolean getRegistersHighlighting() {
        return this.booleanSettingsValues[13];
    }
    
    @Deprecated
    public boolean getStartAtMain() {
        return this.booleanSettingsValues[14];
    }
    
    public String getExceptionHandler() {
        return this.stringSettingsValues[0];
    }
    
    public String getMemoryConfiguration() {
        return this.stringSettingsValues[3];
    }
    
    public Font getEditorFont() {
        return this.getFontByPosition(0);
    }
    
    public Font getFontByPosition(final int fontSettingPosition) {
        if (fontSettingPosition >= 0 && fontSettingPosition < this.fontFamilySettingsValues.length) {
            return EditorFont.createFontFromStringValues(this.fontFamilySettingsValues[fontSettingPosition], this.fontStyleSettingsValues[fontSettingPosition], this.fontSizeSettingsValues[fontSettingPosition]);
        }
        return null;
    }
    
    public Font getDefaultFontByPosition(final int fontSettingPosition) {
        if (fontSettingPosition >= 0 && fontSettingPosition < Settings.defaultFontFamilySettingsValues.length) {
            return EditorFont.createFontFromStringValues(Settings.defaultFontFamilySettingsValues[fontSettingPosition], Settings.defaultFontStyleSettingsValues[fontSettingPosition], Settings.defaultFontSizeSettingsValues[fontSettingPosition]);
        }
        return null;
    }
    
    public int[] getTextColumnOrder() {
        return this.getTextSegmentColumnOrder(this.stringSettingsValues[1]);
    }
    
    public int getCaretBlinkRate() {
        int rate = 500;
        try {
            rate = Integer.parseInt(this.stringSettingsValues[4]);
        }
        catch (NumberFormatException nfe) {
            rate = Integer.parseInt(Settings.defaultStringSettingsValues[4]);
        }
        return rate;
    }
    
    public int getEditorTabSize() {
        int size = 8;
        try {
            size = Integer.parseInt(this.stringSettingsValues[5]);
        }
        catch (NumberFormatException nfe) {
            size = this.getDefaultEditorTabSize();
        }
        return size;
    }
    
    public int getEditorPopupPrefixLength() {
        int length = 2;
        try {
            length = Integer.parseInt(this.stringSettingsValues[6]);
        }
        catch (NumberFormatException ex) {}
        return length;
    }
    
    public int getDefaultEditorTabSize() {
        return Integer.parseInt(Settings.defaultStringSettingsValues[5]);
    }
    
    public String getLabelSortState() {
        return this.stringSettingsValues[2];
    }
    
    public Color getColorSettingByKey(final String key) {
        return this.getColorValueByKey(key, this.colorSettingsValues);
    }
    
    public Color getDefaultColorSettingByKey(final String key) {
        return this.getColorValueByKey(key, Settings.defaultColorSettingsValues);
    }
    
    public Color getColorSettingByPosition(final int position) {
        return this.getColorValueByPosition(position, this.colorSettingsValues);
    }
    
    public Color getDefaultColorSettingByPosition(final int position) {
        return this.getColorValueByPosition(position, Settings.defaultColorSettingsValues);
    }
    
    public void setBooleanSetting(final int id, final boolean value) {
        if (id >= 0 && id < this.booleanSettingsValues.length) {
            this.internalSetBooleanSetting(id, value);
            return;
        }
        throw new IllegalArgumentException("Invalid boolean setting ID");
    }
    
    @Deprecated
    public void setExtendedAssemblerEnabled(final boolean value) {
        this.internalSetBooleanSetting(0, value);
    }
    
    @Deprecated
    public void setAssembleOnOpenEnabled(final boolean value) {
        this.internalSetBooleanSetting(2, value);
    }
    
    @Deprecated
    public void setAssembleAllEnabled(final boolean value) {
        this.internalSetBooleanSetting(3, value);
    }
    
    @Deprecated
    public void setDisplayAddressesInHex(final boolean value) {
        this.internalSetBooleanSetting(5, value);
    }
    
    @Deprecated
    public void setDisplayValuesInHex(final boolean value) {
        this.internalSetBooleanSetting(6, value);
    }
    
    @Deprecated
    public void setLabelWindowVisibility(final boolean value) {
        this.internalSetBooleanSetting(4, value);
    }
    
    @Deprecated
    public void setExceptionHandlerEnabled(final boolean value) {
        this.internalSetBooleanSetting(7, value);
    }
    
    @Deprecated
    public void setDelayedBranchingEnabled(final boolean value) {
        this.internalSetBooleanSetting(8, value);
    }
    
    @Deprecated
    public void setEditorLineNumbersDisplayed(final boolean value) {
        this.internalSetBooleanSetting(9, value);
    }
    
    @Deprecated
    public void setWarningsAreErrors(final boolean value) {
        this.internalSetBooleanSetting(10, value);
    }
    
    @Deprecated
    public void setProgramArguments(final boolean value) {
        this.internalSetBooleanSetting(11, value);
    }
    
    @Deprecated
    public void setDataSegmentHighlighting(final boolean value) {
        this.internalSetBooleanSetting(12, value);
    }
    
    @Deprecated
    public void setRegistersHighlighting(final boolean value) {
        this.internalSetBooleanSetting(13, value);
    }
    
    @Deprecated
    public void setStartAtMain(final boolean value) {
        this.internalSetBooleanSetting(14, value);
    }
    
    public void setBooleanSettingNonPersistent(final int id, final boolean value) {
        if (id >= 0 && id < this.booleanSettingsValues.length) {
            this.booleanSettingsValues[id] = value;
            return;
        }
        throw new IllegalArgumentException("Invalid boolean setting ID");
    }
    
    @Deprecated
    public void setDelayedBranchingEnabledNonPersistent(final boolean value) {
        this.booleanSettingsValues[8] = value;
    }
    
    public void setExceptionHandler(final String newFilename) {
        this.setStringSetting(0, newFilename);
    }
    
    public void setMemoryConfiguration(final String config) {
        this.setStringSetting(3, config);
    }
    
    public void setCaretBlinkRate(final int rate) {
        this.setStringSetting(4, "" + rate);
    }
    
    public void setEditorTabSize(final int size) {
        this.setStringSetting(5, "" + size);
    }
    
    public void setEditorPopupPrefixLength(final int length) {
        this.setStringSetting(6, "" + length);
    }
    
    public void setEditorFont(final Font font) {
        this.setFontByPosition(0, font);
    }
    
    public void setFontByPosition(final int fontSettingPosition, final Font font) {
        if (fontSettingPosition >= 0 && fontSettingPosition < this.fontFamilySettingsValues.length) {
            this.fontFamilySettingsValues[fontSettingPosition] = font.getFamily();
            this.fontStyleSettingsValues[fontSettingPosition] = EditorFont.styleIntToStyleString(font.getStyle());
            this.fontSizeSettingsValues[fontSettingPosition] = EditorFont.sizeIntToSizeString(font.getSize());
            this.saveFontSetting(fontSettingPosition, Settings.fontFamilySettingsKeys, this.fontFamilySettingsValues);
            this.saveFontSetting(fontSettingPosition, Settings.fontStyleSettingsKeys, this.fontStyleSettingsValues);
            this.saveFontSetting(fontSettingPosition, Settings.fontSizeSettingsKeys, this.fontSizeSettingsValues);
        }
        if (fontSettingPosition == 0) {
            this.setChanged();
            this.notifyObservers();
        }
    }
    
    public void setTextColumnOrder(final int[] columnOrder) {
        String stringifiedOrder = new String();
        for (int i = 0; i < columnOrder.length; ++i) {
            stringifiedOrder = stringifiedOrder + Integer.toString(columnOrder[i]) + " ";
        }
        this.setStringSetting(1, stringifiedOrder);
    }
    
    public void setLabelSortState(final String state) {
        this.setStringSetting(2, state);
    }
    
    public void setColorSettingByKey(final String key, final Color color) {
        for (int i = 0; i < Settings.colorSettingsKeys.length; ++i) {
            if (key.equals(Settings.colorSettingsKeys[i])) {
                this.setColorSettingByPosition(i, color);
                return;
            }
        }
    }
    
    public void setColorSettingByPosition(final int position, final Color color) {
        if (position >= 0 && position < Settings.colorSettingsKeys.length) {
            this.setColorSetting(position, color);
        }
    }
    
    private void initialize() {
        this.applyDefaultSettings();
        if (!this.readSettingsFromPropertiesFile(Settings.settingsFile)) {
            System.out.println("MARS System error: unable to read Settings.properties defaults. Using built-in defaults.");
        }
        this.getSettingsFromPreferences();
    }
    
    private void applyDefaultSettings() {
        for (int i = 0; i < this.booleanSettingsValues.length; ++i) {
            this.booleanSettingsValues[i] = Settings.defaultBooleanSettingsValues[i];
        }
        for (int i = 0; i < this.stringSettingsValues.length; ++i) {
            this.stringSettingsValues[i] = Settings.defaultStringSettingsValues[i];
        }
        for (int i = 0; i < this.fontFamilySettingsValues.length; ++i) {
            this.fontFamilySettingsValues[i] = Settings.defaultFontFamilySettingsValues[i];
            this.fontStyleSettingsValues[i] = Settings.defaultFontStyleSettingsValues[i];
            this.fontSizeSettingsValues[i] = Settings.defaultFontSizeSettingsValues[i];
        }
        for (int i = 0; i < this.colorSettingsValues.length; ++i) {
            this.colorSettingsValues[i] = Settings.defaultColorSettingsValues[i];
        }
        this.initializeEditorSyntaxStyles();
    }
    
    private void internalSetBooleanSetting(final int settingIndex, final boolean value) {
        if (value != this.booleanSettingsValues[settingIndex]) {
            this.booleanSettingsValues[settingIndex] = value;
            this.saveBooleanSetting(settingIndex);
            this.setChanged();
            this.notifyObservers();
        }
    }
    
    private void setStringSetting(final int settingIndex, final String value) {
        this.stringSettingsValues[settingIndex] = value;
        this.saveStringSetting(settingIndex);
    }
    
    private void setColorSetting(final int settingIndex, final Color color) {
        this.colorSettingsValues[settingIndex] = Binary.intToHexString(color.getRed() << 16 | color.getGreen() << 8 | color.getBlue());
        this.saveColorSetting(settingIndex);
    }
    
    private Color getColorValueByKey(final String key, final String[] values) {
        final Color color = null;
        for (int i = 0; i < Settings.colorSettingsKeys.length; ++i) {
            if (key.equals(Settings.colorSettingsKeys[i])) {
                return this.getColorValueByPosition(i, values);
            }
        }
        return null;
    }
    
    private Color getColorValueByPosition(final int position, final String[] values) {
        Color color = null;
        if (position >= 0 && position < Settings.colorSettingsKeys.length) {
            try {
                color = Color.decode(values[position]);
            }
            catch (NumberFormatException nfe) {
                color = null;
            }
        }
        return color;
    }
    
    private int getIndexOfKey(final String key, final String[] array) {
        int index = -1;
        for (int i = 0; i < array.length; ++i) {
            if (array[i].equals(key)) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    private boolean readSettingsFromPropertiesFile(final String filename) {
        try {
            for (int i = 0; i < Settings.booleanSettingsKeys.length; ++i) {
                final String settingValue = Globals.getPropertyEntry(filename, Settings.booleanSettingsKeys[i]);
                if (settingValue != null) {
                    this.booleanSettingsValues[i] = (Settings.defaultBooleanSettingsValues[i] = Boolean.valueOf(settingValue));
                }
            }
            for (int i = 0; i < Settings.stringSettingsKeys.length; ++i) {
                final String settingValue = Globals.getPropertyEntry(filename, Settings.stringSettingsKeys[i]);
                if (settingValue != null) {
                    this.stringSettingsValues[i] = (Settings.defaultStringSettingsValues[i] = settingValue);
                }
            }
            for (int i = 0; i < this.fontFamilySettingsValues.length; ++i) {
                String settingValue = Globals.getPropertyEntry(filename, Settings.fontFamilySettingsKeys[i]);
                if (settingValue != null) {
                    this.fontFamilySettingsValues[i] = (Settings.defaultFontFamilySettingsValues[i] = settingValue);
                }
                settingValue = Globals.getPropertyEntry(filename, Settings.fontStyleSettingsKeys[i]);
                if (settingValue != null) {
                    this.fontStyleSettingsValues[i] = (Settings.defaultFontStyleSettingsValues[i] = settingValue);
                }
                settingValue = Globals.getPropertyEntry(filename, Settings.fontSizeSettingsKeys[i]);
                if (settingValue != null) {
                    this.fontSizeSettingsValues[i] = (Settings.defaultFontSizeSettingsValues[i] = settingValue);
                }
            }
            for (int i = 0; i < Settings.colorSettingsKeys.length; ++i) {
                final String settingValue = Globals.getPropertyEntry(filename, Settings.colorSettingsKeys[i]);
                if (settingValue != null) {
                    this.colorSettingsValues[i] = (Settings.defaultColorSettingsValues[i] = settingValue);
                }
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private void getSettingsFromPreferences() {
        for (int i = 0; i < Settings.booleanSettingsKeys.length; ++i) {
            this.booleanSettingsValues[i] = this.preferences.getBoolean(Settings.booleanSettingsKeys[i], this.booleanSettingsValues[i]);
        }
        for (int i = 0; i < Settings.stringSettingsKeys.length; ++i) {
            this.stringSettingsValues[i] = this.preferences.get(Settings.stringSettingsKeys[i], this.stringSettingsValues[i]);
        }
        for (int i = 0; i < Settings.fontFamilySettingsKeys.length; ++i) {
            this.fontFamilySettingsValues[i] = this.preferences.get(Settings.fontFamilySettingsKeys[i], this.fontFamilySettingsValues[i]);
            this.fontStyleSettingsValues[i] = this.preferences.get(Settings.fontStyleSettingsKeys[i], this.fontStyleSettingsValues[i]);
            this.fontSizeSettingsValues[i] = this.preferences.get(Settings.fontSizeSettingsKeys[i], this.fontSizeSettingsValues[i]);
        }
        for (int i = 0; i < Settings.colorSettingsKeys.length; ++i) {
            this.colorSettingsValues[i] = this.preferences.get(Settings.colorSettingsKeys[i], this.colorSettingsValues[i]);
        }
        this.getEditorSyntaxStyleSettingsFromPreferences();
    }
    
    private void saveBooleanSetting(final int index) {

            this.preferences.putBoolean(Settings.booleanSettingsKeys[index], this.booleanSettingsValues[index]);
    }
    
    private void saveStringSetting(final int index) {

            this.preferences.put(Settings.stringSettingsKeys[index], this.stringSettingsValues[index]);

    }
    
    private void saveFontSetting(final int index, final String[] settingsKeys, final String[] settingsValues) {
            this.preferences.put(settingsKeys[index], settingsValues[index]);

    }
    
    private void saveColorSetting(final int index) {

            this.preferences.put(Settings.colorSettingsKeys[index], this.colorSettingsValues[index]);

    }
    
    private int[] getTextSegmentColumnOrder(final String stringOfColumnIndexes) {
        final StringTokenizer st = new StringTokenizer(stringOfColumnIndexes);
        final int[] list = new int[st.countTokens()];
        int index = 0;
        boolean valuesOK = true;
        while (st.hasMoreTokens()) {
            int value;
            try {
                value = Integer.parseInt(st.nextToken());
            }
            catch (Exception e) {
                valuesOK = false;
                break;
            }
            list[index++] = value;
        }
        if (!valuesOK && !stringOfColumnIndexes.equals(Settings.defaultStringSettingsValues[1])) {
            return this.getTextSegmentColumnOrder(Settings.defaultStringSettingsValues[1]);
        }
        return list;
    }
    
    static {
        Settings.settingsFile = "Settings";
        Settings.booleanSettingsKeys = new String[] { "ExtendedAssembler", "BareMachine", "AssembleOnOpen", "AssembleAll", "LabelWindowVisibility", "DisplayAddressesInHex", "DisplayValuesInHex", "LoadExceptionHandler", "DelayedBranching", "EditorLineNumbersDisplayed", "WarningsAreErrors", "ProgramArguments", "DataSegmentHighlighting", "RegistersHighlighting", "StartAtMain", "EditorCurrentLineHighlighting", "PopupInstructionGuidance", "PopupSyscallInput", "GenericTextEditor", "AutoIndent", "SelfModifyingCode" };
        Settings.defaultBooleanSettingsValues = new boolean[] { true, false, false, false, false, true, true, false, false, true, false, false, true, true, false, true, true, false, false, true, false };
        stringSettingsKeys = new String[] { "ExceptionHandler", "TextColumnOrder", "LabelSortState", "MemoryConfiguration", "CaretBlinkRate", "EditorTabSize", "EditorPopupPrefixLength" };
        Settings.defaultStringSettingsValues = new String[] { "", "0 1 2 3 4", "0", "", "500", "8", "2" };
        fontFamilySettingsKeys = new String[] { "EditorFontFamily", "EvenRowFontFamily", "OddRowFontFamily", " TextSegmentHighlightFontFamily", "TextSegmentDelayslotHighightFontFamily", "DataSegmentHighlightFontFamily", "RegisterHighlightFontFamily" };
        fontStyleSettingsKeys = new String[] { "EditorFontStyle", "EvenRowFontStyle", "OddRowFontStyle", " TextSegmentHighlightFontStyle", "TextSegmentDelayslotHighightFontStyle", "DataSegmentHighlightFontStyle", "RegisterHighlightFontStyle" };
        fontSizeSettingsKeys = new String[] { "EditorFontSize", "EvenRowFontSize", "OddRowFontSize", " TextSegmentHighlightFontSize", "TextSegmentDelayslotHighightFontSize", "DataSegmentHighlightFontSize", "RegisterHighlightFontSize" };
        defaultFontFamilySettingsValues = new String[] { "Monospaced", "Monospaced", "Monospaced", "Monospaced", "Monospaced", "Monospaced", "Monospaced" };
        defaultFontStyleSettingsValues = new String[] { "Plain", "Plain", "Plain", "Plain", "Plain", "Plain", "Plain" };
        defaultFontSizeSettingsValues = new String[] { "12", "12", "12", "12", "12", "12", "12" };
        colorSettingsKeys = new String[] { "EvenRowBackground", "EvenRowForeground", "OddRowBackground", "OddRowForeground", "TextSegmentHighlightBackground", "TextSegmentHighlightForeground", "TextSegmentDelaySlotHighlightBackground", "TextSegmentDelaySlotHighlightForeground", "DataSegmentHighlightBackground", "DataSegmentHighlightForeground", "RegisterHighlightBackground", "RegisterHighlightForeground" };
        Settings.defaultColorSettingsValues = new String[] { "0x00e0e0e0", "0", "0x00ffffff", "0", "0x00ffff99", "0", "0x0033ff00", "0", "0x0099ccff", "0", "0x0099cc55", "0" };
    }
}
