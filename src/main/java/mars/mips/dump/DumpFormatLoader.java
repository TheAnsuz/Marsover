

package mars.mips.dump;

import java.lang.reflect.Modifier;
import mars.util.FilenameFinder;
import java.util.ArrayList;

public class DumpFormatLoader
{
    private static final String CLASS_PREFIX = "mars.mips.dump.";
    private static final String DUMP_DIRECTORY_PATH = "mars/mips/dump";
    private static final String SYSCALL_INTERFACE = "DumpFormat.class";
    private static final String CLASS_EXTENSION = "class";
    private static ArrayList formatList;
    
    public ArrayList loadDumpFormats() {
        if (DumpFormatLoader.formatList == null) {
            DumpFormatLoader.formatList = new ArrayList();
            final ArrayList candidates = FilenameFinder.getFilenameList(this.getClass().getClassLoader(), "mars/mips/dump", "class");
            for (int i = 0; i < candidates.size(); ++i) {
                final String file = candidates.get(i);
                try {
                    final String formatClassName = "mars.mips.dump." + file.substring(0, file.indexOf("class") - 1);
                    final Class clas = Class.forName(formatClassName);
                    if (DumpFormat.class.isAssignableFrom(clas) && !Modifier.isAbstract(clas.getModifiers()) && !Modifier.isInterface(clas.getModifiers())) {
                        DumpFormatLoader.formatList.add(clas.newInstance());
                    }
                }
                catch (Exception e) {
                    System.out.println("Error instantiating DumpFormat from file " + file + ": " + e);
                }
            }
        }
        return DumpFormatLoader.formatList;
    }
    
    public static DumpFormat findDumpFormatGivenCommandDescriptor(final ArrayList formatList, final String formatCommandDescriptor) {
        DumpFormat match = null;
        for (int i = 0; i < formatList.size(); ++i) {
            if (formatList.get(i).getCommandDescriptor().equals(formatCommandDescriptor)) {
                match = formatList.get(i);
                break;
            }
        }
        return match;
    }
    
    static {
        DumpFormatLoader.formatList = null;
    }
}
