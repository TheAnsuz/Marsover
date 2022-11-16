package dev.amrv.marsover;

import com.formdev.flatlaf.FlatLightLaf;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import mars.MarsLaunch;

/**
 *
 * @author Adrian MRV. aka AMRV || Ansuz
 */
public class Marsover {

    private static MarsLaunch launcherInstance;
    private static AppProperties props;

    public static final String FILE_SETTINGS = "marsover\\settings.properties";
    public static final String RES_SETTINGS = "settings.properties";

    public static void main(String[] args) {
        FlatLightLaf.setup();
        props = new AppProperties(FILE_SETTINGS, RES_SETTINGS);

        // enable openGL render pipeline
        System.setProperty("sun.java2d.opengl", "true");

        // For debugging purposes I will remove the files on each run
        new File(FILE_SETTINGS).delete();

        launcherInstance = new MarsLaunch(args);
    }

    public static void setLaf() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Marsover.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
