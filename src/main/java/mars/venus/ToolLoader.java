

package mars.venus;

import java.lang.reflect.Modifier;
import mars.tools.MarsTool;
import java.util.HashMap;
import mars.util.FilenameFinder;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JMenu;

public class ToolLoader
{
    private static final String CLASS_PREFIX = "mars.tools.";
    private static final String TOOLS_DIRECTORY_PATH = "mars/tools";
    private static final String TOOLS_MENU_NAME = "Tools";
    private static final String MARSTOOL_INTERFACE = "MarsTool.class";
    private static final String CLASS_EXTENSION = "class";
    
    public JMenu buildToolsMenu() {
        JMenu menu = null;
        final ArrayList<MarsToolClassAndInstance> marsToolList = this.loadMarsTools();
        if (!marsToolList.isEmpty()) {
            menu = new JMenu("Tools");
            menu.setMnemonic(84);
            for (int i = 0; i < marsToolList.size(); ++i) {
                final MarsToolClassAndInstance listItem = marsToolList.get(i);
                menu.add(new ToolAction(listItem.marsToolClass, listItem.marsToolInstance.getName()));
            }
        }
        return menu;
    }
    
    private ArrayList<MarsToolClassAndInstance> loadMarsTools() {
        final ArrayList<MarsToolClassAndInstance> toolList = new ArrayList();
        final ArrayList<String> candidates = FilenameFinder.getFilenameList(this.getClass().getClassLoader(), "mars/tools", "class");
        final HashMap tools = new HashMap();
        for (int i = 0; i < candidates.size(); ++i) {
            final String file = candidates.get(i);
            if (!tools.containsKey(file)) {
                tools.put(file, file);
                if (!file.equals("MarsTool.class")) {
                    try {
                        final String toolClassName = "mars.tools." + file.substring(0, file.indexOf("class") - 1);
                        final Class clas = Class.forName(toolClassName);
                        if (MarsTool.class.isAssignableFrom(clas) && !Modifier.isAbstract(clas.getModifiers()) && !Modifier.isInterface(clas.getModifiers())) {
                            toolList.add(new MarsToolClassAndInstance(clas, (MarsTool)clas.newInstance()));
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Error instantiating MarsTool from file " + file + ": " + e);
                    }
                }
            }
        }
        return toolList;
    }
    
    private class MarsToolClassAndInstance
    {
        Class marsToolClass;
        MarsTool marsToolInstance;
        
        MarsToolClassAndInstance(final Class marsToolClass, final MarsTool marsToolInstance) {
            this.marsToolClass = marsToolClass;
            this.marsToolInstance = marsToolInstance;
        }
    }
}
