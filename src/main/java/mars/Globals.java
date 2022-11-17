

package mars;

import java.util.Enumeration;
import mars.mips.instructions.syscalls.SyscallNumberOverride;
import java.util.StringTokenizer;
import java.util.Properties;
import mars.util.PropertiesFile;
import java.util.ArrayList;
import mars.venus.VenusUI;
import mars.mips.hardware.Memory;
import mars.assembler.SymbolTable;
import mars.mips.instructions.InstructionSet;

public class Globals
{
    private static String configPropertiesFile;
    private static String syscallPropertiesFile;
    public static InstructionSet instructionSet;
    public static MIPSprogram program;
    public static SymbolTable symbolTable;
    public static Memory memory;
    public static Object memoryAndRegistersLock;
    public static boolean debug;
    static Settings settings;
    public static String userInputAlert;
    public static final String imagesPath = "/images/";
    public static final String helpPath = "/help/";
    private static boolean initialized;
    static VenusUI gui;
    public static final String version = "4.5";
    public static final ArrayList fileExtensions;
    public static final int maximumMessageCharacters;
    public static final int maximumErrorMessages;
    public static final int maximumBacksteps;
    public static final String copyrightYears;
    public static final String copyrightHolders;
    public static final String ASCII_NON_PRINT;
    public static final String[] ASCII_TABLE;
    public static int exitCode;
    public static boolean runSpeedPanelExists;
    
    private static String getCopyrightYears() {
        return "2003-2014";
    }
    
    private static String getCopyrightHolders() {
        return "Pete Sanderson and Kenneth Vollmar";
    }
    
    public static void setGui(final VenusUI g) {
        Globals.gui = g;
    }
    
    public static VenusUI getGui() {
        return Globals.gui;
    }
    
    public static Settings getSettings() {
        return Globals.settings;
    }
    
    public static void initialize(final boolean gui) {
        if (!Globals.initialized) {
            Globals.memory = Memory.getInstance();
            (Globals.instructionSet = new InstructionSet()).populate();
            Globals.symbolTable = new SymbolTable("global");
            Globals.settings = new Settings(gui);
            Globals.initialized = true;
            Globals.debug = false;
            Globals.memory.clear();
        }
    }
    
    private static int getMessageLimit() {
        return getIntegerProperty(Globals.configPropertiesFile, "MessageLimit", 1000000);
    }
    
    private static int getErrorLimit() {
        return getIntegerProperty(Globals.configPropertiesFile, "ErrorLimit", 200);
    }
    
    private static int getBackstepLimit() {
        return getIntegerProperty(Globals.configPropertiesFile, "BackstepLimit", 1000);
    }
    
    public static String getAsciiNonPrint() {
        final String anp = getPropertyEntry(Globals.configPropertiesFile, "AsciiNonPrint");
        return (anp == null) ? "." : (anp.equals("space") ? " " : anp);
    }
    
    public static String[] getAsciiStrings() {
        final String let = getPropertyEntry(Globals.configPropertiesFile, "AsciiTable");
        final String placeHolder = getAsciiNonPrint();
        final String[] lets = let.split(" +");
        int maxLength = 0;
        for (int i = 0; i < lets.length; ++i) {
            if (lets[i].equals("null")) {
                lets[i] = placeHolder;
            }
            if (lets[i].equals("space")) {
                lets[i] = " ";
            }
            if (lets[i].length() > maxLength) {
                maxLength = lets[i].length();
            }
        }
        final String padding = "        ";
        ++maxLength;
        for (int j = 0; j < lets.length; ++j) {
            lets[j] = padding.substring(0, maxLength - lets[j].length()) + lets[j];
        }
        return lets;
    }
    
    private static int getIntegerProperty(final String propertiesFile, final String propertyName, final int defaultValue) {
        int limit = defaultValue;
        final Properties properties = PropertiesFile.loadPropertiesFromFile(propertiesFile);
        try {
            limit = Integer.parseInt(properties.getProperty(propertyName, Integer.toString(defaultValue)));
        }
        catch (NumberFormatException ex) {}
        return limit;
    }
    
    private static ArrayList getFileExtensions() {
        final ArrayList extensionsList = new ArrayList();
        final String extensions = getPropertyEntry(Globals.configPropertiesFile, "Extensions");
        if (extensions != null) {
            final StringTokenizer st = new StringTokenizer(extensions);
            while (st.hasMoreTokens()) {
                extensionsList.add(st.nextToken());
            }
        }
        return extensionsList;
    }
    
    public static ArrayList getExternalTools() {
        final ArrayList toolsList = new ArrayList();
        final String delimiter = ";";
        final String tools = getPropertyEntry(Globals.configPropertiesFile, "ExternalTools");
        if (tools != null) {
            final StringTokenizer st = new StringTokenizer(tools, delimiter);
            while (st.hasMoreTokens()) {
                toolsList.add(st.nextToken());
            }
        }
        return toolsList;
    }
    
    public static String getPropertyEntry(final String propertiesFile, final String propertyName) {
        return PropertiesFile.loadPropertiesFromFile(propertiesFile).getProperty(propertyName);
    }
    
    public ArrayList getSyscallOverrides() {
        final ArrayList overrides = new ArrayList();
        final Properties properties = PropertiesFile.loadPropertiesFromFile(Globals.syscallPropertiesFile);
        final Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement().toString();
            overrides.add(new SyscallNumberOverride(key, properties.getProperty(key)));
        }
        return overrides;
    }
    
    static {
        Globals.configPropertiesFile = "Config";
        Globals.syscallPropertiesFile = "Syscall";
        Globals.memoryAndRegistersLock = new Object();
        Globals.debug = false;
        Globals.userInputAlert = "**** user input : ";
        Globals.initialized = false;
        Globals.gui = null;
        fileExtensions = getFileExtensions();
        maximumMessageCharacters = getMessageLimit();
        maximumErrorMessages = getErrorLimit();
        maximumBacksteps = getBackstepLimit();
        copyrightYears = getCopyrightYears();
        copyrightHolders = getCopyrightHolders();
        ASCII_NON_PRINT = getAsciiNonPrint();
        ASCII_TABLE = getAsciiStrings();
        Globals.exitCode = 0;
        Globals.runSpeedPanelExists = false;
    }
}
