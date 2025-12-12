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

    private String getValue(String key) {
        // Приоритет 1: системное свойство
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }

        // Приоритет 2: переменная окружения
        String envValue = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envValue != null) {
            return envValue;
        }

        // Приоритет 3: config.properties
        String configValue = instance.properties.getProperty(key);
        if (configValue != null) {
            return configValue;
        }
        throw new IllegalArgumentException("Env variable " + key.toUpperCase().replace('.', '_') + " does not exist and key "
                + key + " not found.");
    }

    public static String getString(String key) {
        return instance.getValue(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(instance.getValue(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(instance.getValue(key));
    }
}
