package core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Configuration class. The amount of options is indefinite.
 * Options are final on startup.
 * <p>
 * Options can be set on three ways (Priority from high to low):
 * 1) Through JVM properties (option has to be prefixed with 'adcl.': -Dadcl.key=value
 * 2) Through program arguments: key=value
 * 3) Through options .property file provided with 'configPath'-option (default search pos: config.properties)
 * <p>
 * Options are case sensitive; values are trimmed.
 * Options with no value (e.g. "-Dadcl.key") have an empty String as value
 */
@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    @NotNull
    private static Properties properties = new Properties();

    /**
     * To be called from the main class. Loads all config sources
     *
     * @param args the program arguments of the main class
     */
    static void load(@NotNull String[] args) {
        properties = new Properties();

        // Prio 2: args
        for (String arg : args) {
            int pos = arg.indexOf('=');
            if (pos == -1) {
                properties.setProperty(arg, "");
            } else if (pos > 0 && pos < arg.length()) {
                properties.setProperty(arg.substring(0, pos), arg.substring(pos + 1));
            }
        }

        // Prio 1: properties !properties have to start with 'adcl.'
        for (Map.Entry<Object, Object> property : System.getProperties().entrySet()) {
            if (!(property.getKey() instanceof String) || !(property.getValue() instanceof String)) continue;
            String key = (String) property.getKey();
            if (key.startsWith("adcl.")) properties.put(key.substring(5), property.getValue());
        }

        // Prio 3: properties file
        Path configPath = getPath("configPath", Paths.get("config.properties"));

        if (Files.exists(configPath)) {
            Properties tmp = properties;
            properties = new Properties();

            try {
                properties.load(Files.newBufferedReader(configPath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.error("Cloud not open config file " + configPath, e);
            }

            properties.putAll(tmp);
        }

        properties.replaceAll((k, v) -> ((String) v).trim());
    }

    /**
     * Gets a boolean value from the config.
     * true and false are evaluated case-insensitive. Empty string counts as true.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static boolean get(String key, boolean def) {
        return tryParse(key, s -> {
            switch (s.toLowerCase()) {
                case "true":
                case "":
                    return true;
                case "false":
                    return false;
                default:
                    return null;
            }
        }, def);
    }

    /**
     * Gets a int value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static int get(String key, int def) {
        return tryParse(key, Integer::parseInt, def);
    }

    /**
     * Gets a long value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static long get(String key, long def) {
        return tryParse(key, Long::parseLong, def);
    }

    /**
     * Gets a float value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static float get(String key, float def) {
        return tryParse(key, Float::parseFloat, def);
    }

    /**
     * Gets a double value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static double get(String key, double def) {
        return tryParse(key, Double::parseDouble, def);
    }

    /**
     * Gets a byte value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static byte get(String key, byte def) {
        return tryParse(key, Byte::parseByte, def);
    }

    /**
     * Gets a short value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static short get(String key, short def) {
        return tryParse(key, Short::parseShort, def);
    }

    /**
     * Gets a char value from the config.
     * Only one-character values are valid
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static char get(String key, char def) {
        return tryParse(key, s -> s.length() == 1 ? s.charAt(0) : null, def);
    }

    /**
     * Gets a String value from the config.
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static String get(String key, String def) {
        return tryParse(key, Function.identity(), def);
    }

    /**
     * Gets a String array value from the config. The option's value is a comma-separated list of the string elements
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static String[] getStringList(String key, String[] def) {
        return tryParse(key, s -> s.split(","), def);
    }

    /**
     * Gets a Path value from the config. The Path has to be valid
     *
     * @param key the option key
     * @param def the default value if not present or invalid value
     * @return the option value
     */
    public static Path getPath(String key, Path def) {
        return tryParse(key, s -> {
            try {
                return Paths.get(s);
            } catch (InvalidPathException e) {
                return null;
            }
        }, def);
    }

    private static <T> T tryParse(String key, @NotNull Function<String, T> parser, T def) {
        try {
            String raw = key == null ? null : properties.getProperty(key);
            if (raw == null) return def;
            T res = parser.apply(raw);
            return res == null ? def : res;
        } catch (NumberFormatException e) {
            return def;
        }
    }
}



