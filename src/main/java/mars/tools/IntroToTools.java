

package mars.tools;

import java.awt.Component;
import javax.swing.JScrollPane;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JComponent;

public class IntroToTools extends AbstractMarsToolAndApplication
{
    private static String heading;
    private static String version;
    
    public IntroToTools(final String title, final String heading) {
        super(title, heading);
    }
    
    public IntroToTools() {
        super(IntroToTools.heading + ", " + IntroToTools.version, IntroToTools.heading);
    }
    
    public static void main(final String[] args) {
        new IntroToTools(IntroToTools.heading + ", " + IntroToTools.version, IntroToTools.heading).go();
    }
    
    @Override
    public String getName() {
        return "Introduction to Tools";
    }
    
    @Override
    protected JComponent buildMainDisplayArea() {
        final JTextArea message = new JTextArea();
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new Font("Ariel", 0, 12));
        message.setText("Hello!  This Tool does not do anything but you may use its source code as a starting point to build your own MARS Tool or Application.\n\nA MARS Tool is a program listed in the MARS Tools menu.  It is launched when you select its menu item and typically interacts with executing MIPS programs to do something exciting and informative or at least interesting.\n\nA MARS Application is a stand-alone program for similarly interacting with executing MIPS programs.  It uses MARS' MIPS assembler and runtime simulator in the background to control MIPS execution.\n\nThe basic requirements for building a MARS Tool are:\n  1. It must be a class that implements the MarsTool interface.  This has only two methods: 'String getName()' to return the name to be displayed in its Tools menu item, and 'void action()' which is invoked when that menu item is selected by the MARS user.\n  2. It must be stored in the mars.tools package (in folder mars/tools)\n  3. It must be successfully compiled in that package.  This normally means the MARS distribution needs to be extracted from the JAR file before you can develop your Tool.\n\nIf these requirements are met, MARS will recognize and load your Tool into its Tools menu the next time it runs.\n\nThere are no fixed requirements for building a MARS Application, a stand-alone program that utilizes the MARS API.\n\nYou can build a program that may be run as either a MARS Tool or an Application.  The easiest way is to extend an abstract class provided in the MARS distribution: mars.tools.AbstractMarsToolAndApplication.  \n  1. It defines a suite of methods and provides default definitions for all but two: getName() and buildMainDisplayArea().\n  2.  String getName() was introduced above.\n  3.  JComponent buildMainDisplayArea() returns the JComponent to be placed in the BorderLayout.CENTER region of the tool/app's user interface.  The NORTH and SOUTH are defined to contain a heading and a set of button controls, respectively.  \n  4. It defines a default 'void go()' method to launch the application.\n  5. Conventional usage is to define your application as a subclass then launch it by invoking its go() method.\n\nThe frame/dialog you are reading right now is an example of an AbstractMarsToolAndApplication subclass.  If you run it as an application, you will notice the set of controls at the bottom of the window differ from those you get when running it from MARS' Tools menu.  It includes additional controls to load and control the execution of pre-existing MIPS programs.\n\nSee the mars.tools.AbstractMarsToolAndApplication API or the source code of existing tool/apps for further information.\n");
        message.setCaretPosition(0);
        return new JScrollPane(message);
    }
    
    static {
        IntroToTools.heading = "Introduction to MARS Tools and Applications";
        IntroToTools.version = " Version 1.0";
    }
}
