package dev.amrv.marsover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adrian MRV. aka AMRV || Ansuz
 */
public class AppProperties {

    private static final String FIELD_DIVISOR = "=";
    private final File file;
    private final Map<String, String> values;

    /**
     * Creates a new properties's file reader given the path to the file in
     * disk, also uses a back up file in case the main one isnt found.
     *
     * @param file the file on disk
     * @param defaultFile the back up file containing the default values
     */
    public AppProperties(String file, String defaultFile) {
        this.file = new File(file);
        this.values = new HashMap<>();
        try {
            _read(this.file, defaultFile);
        } catch (IOException ex) {
            Logger.getLogger(AppProperties.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    // Dont you dare look at this lines, its not good but its something for now
    // just fold them and ignore them until I fix it
    private void _read(File file, String defFile) throws IOException {

        if (file.exists()) {
            try ( BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                while (fileReader.ready()) {

                    String[] data = fileReader.readLine().split(FIELD_DIVISOR);

                    if (data[0].startsWith("#") || data[0].trim().length() <= 0)
                        continue;

                    values.put(data[0].trim(), data[1].trim());
                }
            }
        }

        if (AppProperties.class
                .getClassLoader().getResource(defFile) != null) {

            try ( BufferedReader fileReader = new BufferedReader(new InputStreamReader(AppProperties.class
                    .getClassLoader().getResourceAsStream(defFile)))) {
                while (fileReader.ready()) {

                    String[] data = fileReader.readLine().split(FIELD_DIVISOR);

                    if (data[0].startsWith("#") || data[0].trim().length() <= 0)
                        continue;

                    values.putIfAbsent(data[0].trim(), data[1].trim());
                }
            }
        }
    }

    // Dont you dare look at this lines, its not good but its something for now
    // just fold them and ignore them until I fix it
    private void _save() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
            for (Entry<String, String> entry : values.entrySet())
                writer.append(entry.getKey()).append(FIELD_DIVISOR).append(entry
                        .getValue()).append(System.lineSeparator());
        }
    }

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    public String getValue(String key) {
        return values.get(key);
    }

    public boolean save() {
        try {
            this._save();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public void saveOnExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> save()));
    }

    public void put(String key, String value) {
        this.values.put(key, value);
    }

    public String get(String key, String defValue) {
        if (values.containsKey(key))
            return values.get(key);

        return defValue;
    }

    public String get(String key) {
        return this.get(key, null);
    }

    public void putBoolean(String key, boolean value) {
        this.values.put(key, value ? "true" : "false");
    }

    public boolean getBoolean(String key, boolean defValue) {
        if (values.containsKey(key))
            return Boolean.getBoolean(values.get(key));

        return defValue;
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public void putNumber(String key, Number value) {
        this.values.put(key, value.toString());
    }

    public Number getNumber(String key, Number defValue) {
        if (values.containsKey(key)) {
            try {
                return numberCheck(values.get(key));
            } catch (NumberFormatException nfe) {
                return defValue;
            }
        }

        return defValue;
    }

    public Number getNumber(String key) {
        return this.getNumber(key, Double.NaN);
    }

    private Number numberCheck(String data) {
        // The number is in hexadecimal
        if (data.startsWith("0x")) {
            return Long.parseLong(data, 16);
        }
        return Double.parseDouble(data);
    }

}
