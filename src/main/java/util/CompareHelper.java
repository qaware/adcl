package util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class CompareHelper<T> implements Comparator<T> {
    private final List<Comparator<T>> compares = new ArrayList<>();

    public static <E extends Comparable<? super E>> Comparator<Collection<E>> collectionComparator() {
        return collectionComparator(Comparator.<E>naturalOrder());
    }

    public static <E extends DeepComparable<? super E>> Comparator<Collection<E>> deepCollectionComparator() {
        return collectionComparator(((o1, o2) -> o1.deepCompareTo(o2)));
    }

    public static <E> Comparator<Collection<E>> collectionComparator(Comparator<E> comparator) {
        return (o1, o2) -> {
            int cmpSize = Integer.compare(o1.size(), o2.size());
            if (cmpSize != 0) return cmpSize;
            List<E> l1 = new ArrayList<>(o1);
            List<E> l2 = new ArrayList<>(o2);
            l1.sort(comparator);
            l2.sort(comparator);
            Iterator<E> it1 = l1.iterator();
            Iterator<E> it2 = l2.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                int cmpEl = comparator.compare(it1.next(), it2.next());
                if (cmpEl != 0) return cmpEl;
            }
            return 0;
        };
    }

    /**
     * Compares two elements by casting them to type U and comparing them with given comparator.
     * For elements not castable to U the return value must not be 0. In such cases the method tries
     * to differentiate the elements by it's class name. If that is 0 then by it's identity (so at least order is secured).
     * If the identity of the elements is equal we don't care about the order anymore but still won't return 0.
     */
    @NotNull
    @Contract(pure = true)
    public static <T, U> Comparator<T> castingComparator(Class<U> clazz, Comparator<U> comparator) {
        return (t1, t2) -> {
            if (clazz.isInstance(t1) && clazz.isInstance(t2)) {
                return comparator.compare(clazz.cast(t1), clazz.cast(t2));
            } else {
                int cmp = t1.getClass().getName().compareTo(t2.getClass().getName());
                if (cmp == 0) cmp = System.identityHashCode(t1) - System.identityHashCode(t2);
                if (cmp == 0) cmp = -1; // if identity is equal we don't care about order anymore
                return cmp;
            }
        };
    }

    public <U extends Comparable<? super U>> CompareHelper<T> add(Function<T, U> comp) {
        return add(Comparator.comparing(comp));
    }

    public <U> CompareHelper<T> add(Function<T, U> comp, Comparator<U> withComparator) {
        return add(Comparator.comparing(comp, withComparator));
    }

    public <U extends Comparable<? super U>> CompareHelper<T> add(Function<T, U> comp, int index) {
        return add(Comparator.comparing(comp), index);
    }

    public <U> CompareHelper<T> add(Function<T, U> comp, Comparator<U> withComparator, int index) {
        return add(Comparator.comparing(comp, withComparator), index);
    }

    public CompareHelper<T> add(Comparator<T> comp) {
        compares.add(comp);
        return this;
    }

    public CompareHelper<T> add(Comparator<T> comp, int index) {
        compares.add(index, comp);
        return this;
    }

    public <U> CompareHelper<U> casted(Class<U> clazz) {
        CompareHelper<U> result = new CompareHelper<>();
        add(castingComparator(clazz, result));
        return result;
    }

    @Override
    public int compare(T a, T b) {
        for (Comparator<T> cmp : compares) {
            int c = cmp.compare(a, b);
            if (c != 0) return c;
        }
        return 0;
    }
}
