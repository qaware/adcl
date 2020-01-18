package util;

import java.util.*;

public class Utils {
    public static <E extends Comparable<? super E>> Comparator<Set<E>> setComparator() {
        return setComparator(Comparator.<E>naturalOrder());
    }

    public static <E> Comparator<Set<E>> setComparator(Comparator<E> comparator) {
        return (o1, o2) -> {
            int cmpSize = Integer.compare(o1.size(), o2.size());
            if (cmpSize != 0) return cmpSize;
            List<E> l1 = new ArrayList<>(o1), l2 = new ArrayList<>(o2);
            l1.sort(comparator);
            l2.sort(comparator);
            Iterator<E> it1 = l1.iterator(), it2 = l2.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                int cmpEl = comparator.compare(it1.next(), it2.next());
                if (cmpEl != 0) return cmpEl;
            }
            return 0;
        };
    }
}
