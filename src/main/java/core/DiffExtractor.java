package core;

import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * The DiffExtractor searches for differences between two Collections of PackageInformations and writes the result in changed.
 * Diff gets computed upon constructor invocation.
 */
public class DiffExtractor {
    private final DependencyPool changelistDependencyPool = new DependencyPool();

    /**
     * Instantiates a new DiffExtractor.
     *
     * @param old      the old
     * @param analysed the analysed
     */
    public DiffExtractor(Collection<PackageInformation> old, Collection<PackageInformation> analysed) {
        Set<PackageInformation> before = new TreeSet<>(old);
        Set<PackageInformation> after = new TreeSet<>(analysed);
        diff(before, after);
    }

    /**
     * Helper Method to add a change into the changelist without making duplicates
     *
     * @param methodDependency   the changed dependency
     * @param methodInformation  which refers to methodDependency
     * @param classInformation   which has methodInformation
     * @param status             signals if the dependency was added or deleted
     */
    private void addToChangelist(MethodInformation methodDependency, MethodInformation methodInformation, ClassInformation classInformation,
                                 ChangelogDependencyInformation.ChangeStatus status) {

        //create a copy of package- and classInformation
        changelistDependencyPool.getOrCreateClassInformation(classInformation.getClassName(), classInformation.isService(), classInformation.isInternal());

        //create a copy of methodInformation
        MethodInformation mi = changelistDependencyPool.getOrCreateMethodInformation(methodInformation.getName(), classInformation.isInternal());

        //addToChangelist ChangelogDependencyInformation to the copy of methodInformation
        MethodInformation md = new ChangelogDependencyInformation(methodDependency, status);

        mi.getMethodDependencies().add(md);
    }

    /**
     * Builds the difference between the packages in before and after.
     *
     * @param before packages before commit
     * @param after  packages after commit
     */
    private void diff(Set<PackageInformation> before, Set<PackageInformation> after) {
        Iterator<PackageInformation> beforeIt = before.stream().sorted().iterator();
        Iterator<PackageInformation> afterIt = after.stream().sorted().iterator();

        CompareIterator<PackageInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);

        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();
            PackageInformation afterNext = compareIterator.getAfterNext();
            PackageInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    packageChange(afterNext, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    packageChange(beforeNext, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    compareIterator.iterateBefore();
                    break;
                default:
                    packageDiff(beforeNext, afterNext);
                    compareIterator.iterateBefore();
                    compareIterator.iterateAfter();
                    break;
            }
        }
    }

    /**
     * Builds the difference between the classes in before and after.
     *
     * @param before package before commit
     * @param after  package after commit
     */
    private void packageDiff(PackageInformation before, PackageInformation after) {
        Iterator<ClassInformation> beforeIt = before.getClassInformations().stream().sorted().iterator();
        Iterator<ClassInformation> afterIt = after.getClassInformations().stream().sorted().iterator();

        CompareIterator<ClassInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);

        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();
            ClassInformation afterNext = compareIterator.getAfterNext();
            ClassInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    classChange(afterNext, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    classChange(beforeNext, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    compareIterator.iterateBefore();
                    break;
                default:
                    classDiff(beforeNext, afterNext);
                    compareIterator.iterateBefore();
                    compareIterator.iterateAfter();
                    break;
            }
        }
    }

    /**
     * Builds the difference between the Methods in before and after
     *
     * @param before    class before commit
     * @param after     class after commit
     */
    private void classDiff(ClassInformation before, ClassInformation after) {
        Iterator<MethodInformation> beforeIt = before.getMethodInformations().stream().sorted().iterator();
        Iterator<MethodInformation> afterIt = after.getMethodInformations().stream().sorted().iterator();

        CompareIterator<MethodInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);
        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();

            MethodInformation afterNext = compareIterator.getAfterNext();
            MethodInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    behaviourChange(afterNext, after, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    behaviourChange(beforeNext, before, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    compareIterator.iterateBefore();
                    break;
                default:
                    methodDiff(beforeNext, afterNext, after);
                    compareIterator.iterateBefore();
                    compareIterator.iterateAfter();
                    break;
            }
        }
    }

    /**
     * Builds the difference between the Dependencies in before and after
     *
     * @param before    Method before commit
     * @param after     Method after commit
     * @param inClass   class in which before and after are
     */
    private void methodDiff(MethodInformation before, MethodInformation after, ClassInformation inClass) {
        Iterator<MethodInformation> beforeIt = before.getMethodDependencies().stream().sorted().iterator();
        Iterator<MethodInformation> afterIt = after.getMethodDependencies().stream().sorted().iterator();

        CompareIterator<MethodInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);

        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();

            MethodInformation afterNext = compareIterator.getAfterNext();
            MethodInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    addToChangelist(afterNext, after, inClass, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    addToChangelist(beforeNext, before, inClass, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    compareIterator.iterateBefore();
                    break;
                default:
                    compareIterator.iterateBefore();
                    compareIterator.iterateAfter();
                    break;
            }
        }
    }

    /**
     * Adds all Dependencies in packageInformation to changed
     *
     * @param packageInformation which has been changed
     * @param changeStatus       signals if the dependency was added or deleted
     */
    private void packageChange(PackageInformation packageInformation, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        for (ClassInformation classInformation : packageInformation.getClassInformations())
            classChange(classInformation, changeStatus);
    }

    /**
     * Adds all Dependencies in classInformation to changed
     *
     * @param classInformation   which has been changed
     * @param changeStatus       signals if the dependency was added or deleted
     */
    private void classChange(ClassInformation classInformation, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        for (MethodInformation MethodInformation : classInformation.getMethodInformations())
            behaviourChange(MethodInformation, classInformation, changeStatus);
    }

    /**
     * Adds all Dependencies in methodInformation to changed
     *
     * @param methodInformation  which has been changed
     * @param classInformation   in which methodInformation is
     * @param changeStatus       signals if the dependency was added or deleted
     */
    private void behaviourChange(MethodInformation methodInformation, ClassInformation classInformation, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        for (MethodInformation dependency : methodInformation.getMethodDependencies())
            addToChangelist(dependency, methodInformation, classInformation, changeStatus);
    }

    /**
     * Gets the changelist.
     *
     * @return the changelist in form of a collection
     */
    public Collection<PackageInformation> getChangelist() {
        return changelistDependencyPool.retrievePackageInformation();
    }

    /**
     * HelperClass for comparing two tree structures while iterating through them. Iteration result is an Integer.
     * Meaning of the results:
     * 0: Items are the same
     * 1: Item in before is bigger in value
     * -1: Item in before is lower in value
     *
     * @param <T> should be one of {@link PackageInformation}, {@link ClassInformation}, {@link MethodInformation}
     */
    private static class CompareIterator<T extends Comparable<T>> implements Iterator<Integer> {
        private Iterator<T> beforeIt;
        private Iterator<T> afterIt;
        private T afterNext;
        private T beforeNext;

        /**
         * Instantiates a new CompareIterator.
         *
         * @param beforeIt the Iterator for the before tree structure
         * @param afterIt  the Iterator for the after tree structure
         */
        CompareIterator(Iterator<T> beforeIt, Iterator<T> afterIt) {
            this.beforeIt = beforeIt;
            this.afterIt = afterIt;
            this.afterNext = null;
            this.beforeNext = null;
        }

        /**
         * Gets the next Object of after.
         *
         * @return the next Object of after
         */
        T getAfterNext() {
            return afterNext;
        }

        /**
         * Gets the next Object of before.
         *
         * @return the next Object of before
         */
        T getBeforeNext() {
            return beforeNext;
        }

        /**
         * Result in after has been processed and next Item can be compared
         */
        void iterateAfter() {
            afterNext = null;
        }

        /**
         * Result in before has been processed and next Item can be compared
         */
        void iterateBefore() {
            beforeNext = null;
        }

        @Override
        public boolean hasNext() {
            return beforeIt.hasNext() || afterIt.hasNext() || afterNext != null || beforeNext != null;
        }

        @Override
        public Integer next() {
            if (beforeNext == null)
                beforeNext = beforeIt.hasNext() ? beforeIt.next() : null;
            if (afterNext == null)
                afterNext = afterIt.hasNext() ? afterIt.next() : null;

            int compare;

            if (beforeNext != null && afterNext != null)
                compare = Integer.signum(beforeNext.compareTo(afterNext));
            else if (beforeNext != null)
                compare = 1;
            else
                compare = -1;

            return compare;
        }
    }
}
