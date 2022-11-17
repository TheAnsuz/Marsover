

package mars.assembler;

import java.util.ArrayList;

public final class Directives
{
    private static ArrayList directiveList;
    public static final Directives DATA;
    public static final Directives TEXT;
    public static final Directives WORD;
    public static final Directives ASCII;
    public static final Directives ASCIIZ;
    public static final Directives BYTE;
    public static final Directives ALIGN;
    public static final Directives HALF;
    public static final Directives SPACE;
    public static final Directives DOUBLE;
    public static final Directives FLOAT;
    public static final Directives EXTERN;
    public static final Directives KDATA;
    public static final Directives KTEXT;
    public static final Directives GLOBL;
    public static final Directives SET;
    public static final Directives EQV;
    public static final Directives MACRO;
    public static final Directives END_MACRO;
    public static final Directives INCLUDE;
    private String descriptor;
    private String description;
    
    private Directives() {
        this.descriptor = "generic";
        this.description = "";
        Directives.directiveList.add(this);
    }
    
    private Directives(final String name, final String description) {
        this.descriptor = name;
        this.description = description;
        Directives.directiveList.add(this);
    }
    
    public static Directives matchDirective(final String str) {
        for (int i = 0; i < Directives.directiveList.size(); ++i) {
            final Directives match = Directives.directiveList.get(i);
            if (str.equalsIgnoreCase(match.descriptor)) {
                return match;
            }
        }
        return null;
    }
    
    public static ArrayList prefixMatchDirectives(final String str) {
        ArrayList matches = null;
        for (int i = 0; i < Directives.directiveList.size(); ++i) {
            if (Directives.directiveList.get(i).descriptor.toLowerCase().startsWith(str.toLowerCase())) {
                if (matches == null) {
                    matches = new ArrayList();
                }
                matches.add(Directives.directiveList.get(i));
            }
        }
        return matches;
    }
    
    @Override
    public String toString() {
        return this.descriptor;
    }
    
    public String getName() {
        return this.descriptor;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public static ArrayList getDirectiveList() {
        return Directives.directiveList;
    }
    
    public static boolean isIntegerDirective(final Directives direct) {
        return direct == Directives.WORD || direct == Directives.HALF || direct == Directives.BYTE;
    }
    
    public static boolean isFloatingDirective(final Directives direct) {
        return direct == Directives.FLOAT || direct == Directives.DOUBLE;
    }
    
    static {
        Directives.directiveList = new ArrayList();
        DATA = new Directives(".data", "Subsequent items stored in Data segment at next available address");
        TEXT = new Directives(".text", "Subsequent items (instructions) stored in Text segment at next available address");
        WORD = new Directives(".word", "Store the listed value(s) as 32 bit words on word boundary");
        ASCII = new Directives(".ascii", "Store the string in the Data segment but do not add null terminator");
        ASCIIZ = new Directives(".asciiz", "Store the string in the Data segment and add null terminator");
        BYTE = new Directives(".byte", "Store the listed value(s) as 8 bit bytes");
        ALIGN = new Directives(".align", "Align next data item on specified byte boundary (0=byte, 1=half, 2=word, 3=double)");
        HALF = new Directives(".half", "Store the listed value(s) as 16 bit halfwords on halfword boundary");
        SPACE = new Directives(".space", "Reserve the next specified number of bytes in Data segment");
        DOUBLE = new Directives(".double", "Store the listed value(s) as double precision floating point");
        FLOAT = new Directives(".float", "Store the listed value(s) as single precision floating point");
        EXTERN = new Directives(".extern", "Declare the listed label and byte length to be a global data field");
        KDATA = new Directives(".kdata", "Subsequent items stored in Kernel Data segment at next available address");
        KTEXT = new Directives(".ktext", "Subsequent items (instructions) stored in Kernel Text segment at next available address");
        GLOBL = new Directives(".globl", "Declare the listed label(s) as global to enable referencing from other files");
        SET = new Directives(".set", "Set assembler variables.  Currently ignored but included for SPIM compatability");
        EQV = new Directives(".eqv", "Substitute second operand for first. First operand is symbol, second operand is expression (like #define)");
        MACRO = new Directives(".macro", "Begin macro definition.  See .end_macro");
        END_MACRO = new Directives(".end_macro", "End macro definition.  See .macro");
        INCLUDE = new Directives(".include", "Insert the contents of the specified file.  Put filename in quotes.");
    }
}
