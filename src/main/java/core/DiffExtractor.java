package core;

import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The DiffExtractor searches for differences between two Collections of PackageInformations and writes the result in changed.
 * Diff gets computed upon constructor invocation.
 */
public class DiffExtractor {

    private DependencyPool changelistDependencyPool = DependencyPool.getExtractorInstance();


    /**
     * Instantiates a new DiffExtractor.
     *
     * @param old      the old
     * @param analysed the analysed
     */
    public DiffExtractor(Collection<PackageInformation> old, Collection<PackageInformation> analysed) {

        SortedSet<PackageInformation> before = new TreeSet<>(old);
        SortedSet<PackageInformation> after = new TreeSet<>(analysed);
        diff(before, after);
    }


    /**
     * Helper Method to add a change into the changelist without making duplicates
     *
     * @param methodDependency   the changed dependency
     * @param methodInformation  which refers to methodDependency
     * @param classInformation   which has methodInformation
     * @param packageInformation which has classInformation
     * @param status             signals if the dependency was added or deleted
     */
    private void addToChangelist(MethodInformation methodDependency, MethodInformation methodInformation, ClassInformation classInformation,
                                 PackageInformation packageInformation, ChangelogDependencyInformation.ChangeStatus status) {

        //create a copy of package- and classInformation
        changelistDependencyPool.getOrCreateClassInformation(classInformation.getClassName(), classInformation.isService(), packageInformation.isInternalPackage());

        //create a copy of methodInformation
        MethodInformation mi = changelistDependencyPool.getOrCreateMethodInformation(methodInformation.getName(), methodInformation.isConstructor());

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
    private void diff(SortedSet<PackageInformation> before, SortedSet<PackageInformation> after) {
        Iterator<PackageInformation> beforeIt = before.iterator();
        Iterator<PackageInformation> afterIt = after.iterator();

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

        Iterator<ClassInformation> beforeIt = before.getClassInformations().iterator();
        Iterator<ClassInformation> afterIt = after.getClassInformations().iterator();

        CompareIterator<ClassInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);

        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();
            ClassInformation afterNext = compareIterator.getAfterNext();
            ClassInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    classChange(afterNext, after, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    classChange(beforeNext, before, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    compareIterator.iterateBefore();
                    break;
                default:
                    classDiff(beforeNext, afterNext, after);
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
     * @param inPackage package in which before and after are
     */
    private void classDiff(ClassInformation before, ClassInformation after, PackageInformation inPackage) {

        Iterator<MethodInformation> beforeIt = before.getMethodInformations().iterator();
        Iterator<MethodInformation> afterIt = after.getMethodInformations().iterator();

        CompareIterator<MethodInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);
        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();

            MethodInformation afterNext = compareIterator.getAfterNext();
            MethodInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    behaviourChange(afterNext, after, inPackage, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    behaviourChange(beforeNext, before, inPackage, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    compareIterator.iterateBefore();
                    break;
                default:
                    methodDiff(beforeNext, afterNext, inPackage, after);
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
     * @param inPackage package in which inClass is
     * @param inClass   class in which before and after are
     */
    private void methodDiff(MethodInformation before, MethodInformation after, PackageInformation inPackage, ClassInformation inClass) {

        Iterator<MethodInformation> beforeIt = before.getMethodDependencies().iterator();
        Iterator<MethodInformation> afterIt = after.getMethodDependencies().iterator();

        CompareIterator<MethodInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt);

        while (compareIterator.hasNext()) {

            int compare = compareIterator.next();

            MethodInformation afterNext = compareIterator.getAfterNext();
            MethodInformation beforeNext = compareIterator.getBeforeNext();

            switch (compare) {
                case -1:
                    addToChangelist(afterNext, after, inClass, inPackage, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    compareIterator.iterateAfter();
                    break;
                case 1:
                    addToChangelist(beforeNext, before, inClass, inPackage, ChangelogDependencyInformation.ChangeStatus.DELETED);
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
            classChange(classInformation, packageInformation, changeStatus);
    }


    /**
     * Adds all Dependencies in classInformation to changed
     *
     * @param classInformation   which has been changed
     * @param packageInformation in which classInformation is
     * @param changeStatus       signals if the dependency was added or deleted
     */
    private void classChange(ClassInformation classInformation, PackageInformation packageInformation, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        for (MethodInformation MethodInformation : classInformation.getMethodInformations())
            behaviourChange(MethodInformation, classInformation, packageInformation, changeStatus);
    }


    /**
     * Adds all Dependencies in methodInformation to changed
     *
     * @param methodInformation  which has been changed
     * @param classInformation   in which methodInformation is
     * @param packageInformation in which classInformation is
     * @param changeStatus       signals if the dependency was added or deleted
     */
    private void behaviourChange(MethodInformation methodInformation, ClassInformation classInformation, PackageInformation packageInformation, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        for (MethodInformation dependency : methodInformation.getMethodDependencies())
            addToChangelist(dependency, methodInformation, classInformation, packageInformation, changeStatus);
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
    private class CompareIterator<T extends Comparable<T>> implements Iterator<Integer> {
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
