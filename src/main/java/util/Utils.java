package util;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    /**
     * Casts a Set of type T to type U, dropping not castable entries
     *
     * @param set     the original set
     * @param toClass the corresponding class for type parameter {@code <U>}
     * @param <T>     the old type
     * @param <U>     the new type
     * @return a new set of type U
     */
    @NotNull
    public static <T, U> Set<U> cast(@NotNull Set<T> set, @NotNull Class<U> toClass) {
        return set.stream().filter(toClass::isInstance).map(toClass::cast).collect(Collectors.toSet());
    }

    /**
     * Tries to resolve a CLI command to the binary that provides it
     *
     * @param cmd the command to be resolved
     * @return the binary that provides the command, or null if no such binary found
     */
    @Nullable
    public static Path searchInPath(String cmd) {
        try {
            String raw = new BufferedReader(new InputStreamReader(new ProcessBuilder(SystemUtils.IS_OS_WINDOWS ? "where" : "which", cmd).start().getInputStream())).readLine();
            if (raw == null) return null;
            return Paths.get(raw);
        } catch (IOException | InvalidPathException e) {
            LOGGER.error("Exception while searching for {} in PATH", cmd, e);
            return null;
        }
    }

    /**
     * @param thr   the exception
     * @param cause the Throwable type to search for
     * @return whether the exception has given cause type in its (transitive) causes
     */
    public static boolean hasCause(Throwable thr, @NotNull Class<? extends Throwable> cause) {
        if (cause.isInstance(thr)) return true;
        else if (thr.getCause() != null) return hasCause(thr.getCause(), cause);
        else return false;
    }

    /**
     * Resolves a Map with String keys and either Boolean or other such Maps as values to a {@code Map<String, Boolean>}, concatenating the keys to one "super" key
     *
     * @param <T>       the value type to be expected in the map
     * @param valueType the corresponding class for type parameter {@code <T>}
     * @param prefix    the prefix for the final super keys
     * @param map       the map to resolve
     * @return the map with "super" keys
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> resolveNestedMaps(@NotNull Class<T> valueType, @Nullable String prefix, @NotNull Map<String, Object> map) {
        Map<String, T> result = new HashMap<>();
        map.forEach((k, v) -> {
            String key = prefix == null ? k : (prefix + '.' + k);
            if (valueType.isInstance(v)) result.put(key, (T) v);
            else if (v instanceof Map) resolveNestedMaps(valueType, key, (Map<String, Object>) v);
            else
                throw new IllegalStateException("versionInfoInternal contains invalid element of type " + (v == null ? Void.class : v.getClass()));
        });
        return result;
    }
}
