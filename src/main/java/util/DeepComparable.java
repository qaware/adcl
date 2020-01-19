package util;

import org.jetbrains.annotations.NotNull;

public interface DeepComparable<T> {
    int deepCompareTo(@NotNull T o);
}
