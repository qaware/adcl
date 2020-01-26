package util;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
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
}
