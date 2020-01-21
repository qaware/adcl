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
}
