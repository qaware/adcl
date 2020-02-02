package core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MapTool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final Map<String, String> properties = new HashMap<>();
    private static final Pattern stringToArgsPattern = Pattern.compile("(?<key>[^=\\s]+)=?(?:(?<quoted>\".+?\")|(?<unquoted>[^\\s]+))?\\s");
    private static final String PREFIX = "adcl.";

    private Config() {
    }

    /**
     * Use this if you want to differentiate between a non-existent value and an invalid value if get() returns null.
     *
     * @param key the option key
     * @return whether a given key has a value in the Config.
     */
    public static boolean valuePresent(String key) {
        return properties.containsKey(key);
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

    /**
     * To be called from the main class. Loads all config sources
     *
     * @param args the program arguments of the main class
     */
    static void load(@NotNull String[] args) {
        properties.clear();
        properties.putAll(argsToMap(args));
        properties.putAll(propertiesToMap()); // overrides options from args

        Path configPath = getPath("configPath", null);
        String raw = get("configPath", null);
        if (configPath == null && raw != null) {
            logger.error("configPath is present but invalid. Is: \"{}\"", raw);
        } else if (configPath != null && Files.notExists(configPath)) {
            logger.error("configPath points to a non-existent file");
        } else if (configPath != null && Files.isDirectory(configPath)) {
            logger.error("configPath points to a directory");
        } else {
            if (configPath == null) {
                Path alternate = Paths.get("config.properties");
                if (Files.exists(alternate) && !Files.isDirectory(alternate)) configPath = alternate;
            }
            if (configPath != null) fileToMap(configPath).forEach(properties::putIfAbsent); // does not override
        }

        logger.info("Configuration loaded: {}", properties);
    }

    /**
     * @param configPath a file in .properties format
     * @return config entries loaded from configPath file
     */
    @NotNull
    private static Map<String, String> fileToMap(Path configPath) {
        try {
            Properties prop = new Properties();
            prop.load(Files.newBufferedReader(configPath, StandardCharsets.UTF_8));
            return new MapTool<>(prop)
                    .castKeys(String.class)
                    .castValues(String.class)
                    .mapValues(in -> in.matches("\".*\"") ? in.substring(1, in.length() - 1) : in)
                    .get();
        } catch (IOException e) {
            logger.error("Cloud not open config file at {}", configPath, e);
            return Collections.emptyMap();
        }
    }

    /**
     * @return config entries loaded from JVM system properties
     */
    @NotNull
    private static Map<String, String> propertiesToMap() {
        return new MapTool<>(System.getProperties())
                .castKeys(String.class)
                .castValues(String.class)
                .filterKeys(k -> k.startsWith(PREFIX))
                .mapKeys(k -> k.substring(PREFIX.length()))
                .get();
    }

    /**
     * @param args CLI arguments
     * @return config entries loaded from CLI
     */
    @NotNull
    private static Map<String, String> argsToMap(String[] args) {
        Map<String, String> result = new HashMap<>();
        Matcher matcher = stringToArgsPattern.matcher(Arrays.stream(args).collect(Collectors.joining(" ", "", " ")));
        while (matcher.find()) {
            String val = matcher.group("quoted");
            if (val == null) {
                val = matcher.group("unquoted");
            } else {
                val = val.substring(1, val.length() - 1);
            }
            result.put(matcher.group("key"), val == null ? "" : val);
        }
        return result;
    }

    /**
     * Tries to retrieve a config entry
     *
     * @param key    the config key
     * @param parser the parser from the raw string config value to T
     * @param def    the default type if the config entry does not exist or cannot be parsed
     * @param <T>    the desired retrieval type
     * @return the retrieved config entry or def if retrieval was not successful
     */
    private static <T> T tryParse(String key, @NotNull Function<String, T> parser, T def) {
        try {
            String raw = key == null ? null : properties.get(key);
            if (raw == null) return def;
            T res = parser.apply(raw);
            return res == null ? def : res;
        } catch (NumberFormatException e) {
            return def;
        }
    }
}



