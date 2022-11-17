

package mars.venus;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import mars.Globals;

public class HelpAboutAction extends GuiAction
{
    public HelpAboutAction(final String name, final Icon icon, final String descrip, final Integer mnemonic, final KeyStroke accel, final VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        JOptionPane.showMessageDialog(this.mainUI, "MARS 4.5    Copyright " + Globals.copyrightYears + "\n" + Globals.copyrightHolders + "\nMARS is the Mips Assembler and Runtime Simulator.\n\nMars image courtesy of NASA/JPL.\nToolbar and menu icons are from:\n  *  Tango Desktop Project (tango.freedesktop.org),\n  *  glyFX (www.glyfx.com) Common Toolbar Set,\n  *  KDE-Look (www.kde-look.org) crystalline-blue-0.1,\n  *  Icon-King (www.icon-king.com) Nuvola 1.0.\nPrint feature adapted from HardcopyWriter class in David Flanagan's\nJava Examples in a Nutshell 3rd Edition, O'Reilly, ISBN 0-596-00620-9.", "About Mars", 1, new ImageIcon("images/RedMars50.gif"));
    }
}
