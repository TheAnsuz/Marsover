

package mars.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFile
{
    public static Properties loadPropertiesFromFile(final String file) {
        final Properties properties = new Properties();
        try {
            final InputStream is = PropertiesFile.class.getResourceAsStream("/" + file + ".properties");
            properties.load(is);
        }
        catch (IOException ex) {}
        catch (NullPointerException ex2) {}
        return properties;
    }
}
