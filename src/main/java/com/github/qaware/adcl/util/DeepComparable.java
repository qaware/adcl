package com.github.qaware.adcl.util;

import org.jetbrains.annotations.NotNull;

/**
 * A mirror interface for {@link Comparable}, propagating deep comparison.
 * Also includes deepEquals as mirror to {@link Object#equals(Object)}.
 * A object is deep equal to another if they are equal and their fields are also deep equal
 *
 * @param <T> The type which the elements will be compared
 */
public interface DeepComparable<T> {
    /**
     * @param o the object to compare against
     * @return 0 if objects are deep equal, any other integer otherwise
     */
    int deepCompareTo(@NotNull T o);

    /**
     * @param o the object to check against
     * @return whether this and o are deep equal
     */
    boolean deepEquals(Object o);
}
