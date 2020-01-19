package core.information;

import java.util.Comparator;

public class SortComparator implements Comparator<ClassInformation> {
    @Override
    public int compare(ClassInformation o1, ClassInformation o2) {
        return o1.getClassName().compareTo(o2.getClassName());
    }
}
