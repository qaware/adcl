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
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    @NotNull
    public static <T, U> Set<U> cast(@NotNull Set<T> set, @NotNull Class<U> toClass) {
        return set.stream().filter(toClass::isInstance).map(toClass::cast).collect(Collectors.toSet());
    }

    public static int minIndexOf(String s, @NotNull String chars) {
        int result = -1;
        for (char c : chars.toCharArray()) {
            int i = s.indexOf(c);
            if (i != -1 && (i < result || result == -1)) result = i;
        }
        return result;
    }

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
}
