package util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class Utils {
    public static <E extends Comparable<? super E>> Comparator<SortedSet<E>> sortedSetComparator() {
        return sortedSetComparator(Comparator.<E>naturalOrder());
    }

    public static <E> Comparator<SortedSet<E>> sortedSetComparator(Comparator<E> comparator) {
        return (o1, o2) -> {
            int cmpSize = Integer.compare(o1.size(), o2.size());
            if (cmpSize != 0) return cmpSize;
            Iterator<E> it1 = o1.iterator(), it2 = o2.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                int cmpEl = comparator.compare(it1.next(), it2.next());
                if (cmpEl != 0) return cmpEl;
            }
            return 0;
        };
    }
}
