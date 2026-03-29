package common.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config instance = new Config();
    private static final String fileName = "config.properties";
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException(fileName + " not found");
            }

            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fileName + ": ", e);
        }
    }

    public static String getString(String key) {
        String value = instance.properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Key " + key + " not found in " + fileName);
        }

        return value;
    }

    public static int getInt(String key) {
        String value = instance.properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Key " + key + " not found in " + fileName);
        }

        return Integer.parseInt(value);
    }
}
