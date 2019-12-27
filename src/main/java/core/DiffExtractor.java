package core;

import core.information.BehaviorInformation;
import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;

import java.util.*;

/**
 * The DiffExtractor searches for differences between two Collections of PackageInformations and writes the result in changed.
 */
public class DiffExtractor {

    private SortedSet<PackageInformation> changed = new TreeSet<>();

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
     * Helper Method to add a change into changed without making duplicates
     *
     * @param referencedMethod    the changed dependency
     * @param behaviorInformation which refers to referenceMethod
     * @param classInformation    which has behaviorInformation
     * @param packageInformation  which has classInformation
     * @param status              signals if the dependency was added or deleted
     */
    private void add(BehaviorInformation referencedMethod, BehaviorInformation behaviorInformation, ClassInformation classInformation,
                     PackageInformation packageInformation, ChangelogDependencyInformation.ChangeStatus status) {

        PackageInformation pi = new PackageInformation(packageInformation.getPackageName());
        ClassInformation ci = new ClassInformation(classInformation.getClassName());
        BehaviorInformation bi = new BehaviorInformation(behaviorInformation.getName(), behaviorInformation.isConstructor());
        BehaviorInformation referencedbi = new ChangelogDependencyInformation(referencedMethod, status);

        if (changed.contains(packageInformation)) {
            pi = changed.stream().filter(packageInformation1 -> packageInformation1.equals(packageInformation)).findFirst().orElse(pi);
            if (pi.getClassInformations().contains(classInformation)) {
                ci = packageInformation.getClassInformations().stream().filter(classInformation1 -> classInformation1.equals(classInformation)).findFirst().orElse(ci);
                if (ci.getBehaviorInformations().contains(behaviorInformation)) {
                    bi = classInformation.getBehaviorInformations().stream().filter(behaviorInformation1 -> behaviorInformation1.equals(behaviorInformation)).findFirst().orElse(bi);
                } else {
                    ci.getBehaviorInformations().add(bi);
                }
                bi.getReferencedBehavior().add(referencedbi);
            } else {

                bi.getReferencedBehavior().add(referencedbi);
                ci.getBehaviorInformations().add(bi);
                pi.getClassInformations().add(ci);
            }

        } else {


            bi.getReferencedBehavior().add(referencedbi);
            ci.getBehaviorInformations().add(bi);
            pi.getClassInformations().add(ci);
            changed.add(pi);
        }
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
        PackageInformation afterNext = null;
        PackageInformation beforeNext = null;

        while (beforeIt.hasNext() || afterIt.hasNext() || afterNext != null || beforeNext != null) {
            CompareIterator<PackageInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt, afterNext, beforeNext).invoke();
            afterNext = compareIterator.getAfterNext();
            beforeNext = compareIterator.getBeforeNext();
            int compare = compareIterator.getCompare();

            switch (compare) {
                case 0:
                    packageDiff(beforeNext, afterNext);
                    beforeNext = null;
                    afterNext = null;
                    break;
                case -1:
                    packageChange(afterNext, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    afterNext = null;
                    break;
                case 1:
                    packageChange(beforeNext, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    beforeNext = null;
                    break;
                default:
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
        ClassInformation afterNext = null;
        ClassInformation beforeNext = null;

        while (beforeIt.hasNext() || afterIt.hasNext() || afterNext != null || beforeNext != null) {
            CompareIterator<ClassInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt, afterNext, beforeNext).invoke();
            afterNext = compareIterator.getAfterNext();
            beforeNext = compareIterator.getBeforeNext();
            int compare = compareIterator.getCompare();

            switch (compare) {
                case 0:
                    classDiff(beforeNext, afterNext, after);
                    beforeNext = null;
                    afterNext = null;
                    break;
                case -1:
                    classChange(afterNext, after, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    afterNext = null;
                    break;
                case 1:
                    classChange(beforeNext, before, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    beforeNext = null;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Builds the difference between the behaviors in before and after
     *
     * @param before    class before commit
     * @param after     class after commit
     * @param inPackage package in which before and after are
     */
    private void classDiff(ClassInformation before, ClassInformation after, PackageInformation inPackage) {
        Iterator<BehaviorInformation> beforeIt = before.getBehaviorInformations().iterator();
        Iterator<BehaviorInformation> afterIt = after.getBehaviorInformations().iterator();
        BehaviorInformation afterNext = null;
        BehaviorInformation beforeNext = null;

        while (beforeIt.hasNext() || afterIt.hasNext() || afterNext != null || beforeNext != null) {
            CompareIterator<BehaviorInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt, afterNext, beforeNext).invoke();
            afterNext = compareIterator.getAfterNext();
            beforeNext = compareIterator.getBeforeNext();
            int compare = compareIterator.getCompare();

            switch (compare) {
                case 0:
                    behaviorDiff(beforeNext, afterNext, inPackage, after);
                    beforeNext = null;
                    afterNext = null;
                    break;
                case -1:
                    behaviourChange(afterNext, after, inPackage, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    afterNext = null;
                    break;
                case 1:
                    behaviourChange(beforeNext, before, inPackage, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    beforeNext = null;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Builds the difference between the Dependencies in before and after
     *
     * @param before    behavior before commit
     * @param after     behavior after commit
     * @param inPackage package in which inClass is
     * @param inClass   class in which before and after are
     */
    private void behaviorDiff(BehaviorInformation before, BehaviorInformation after,
                              PackageInformation inPackage, ClassInformation inClass) {
        Iterator<BehaviorInformation> beforeIt = before.getReferencedBehavior().iterator();
        Iterator<BehaviorInformation> afterIt = after.getReferencedBehavior().iterator();
        BehaviorInformation afterNext = null;
        BehaviorInformation beforeNext = null;

        while (beforeIt.hasNext() || afterIt.hasNext() || afterNext != null || beforeNext != null) {
            CompareIterator<BehaviorInformation> compareIterator = new CompareIterator<>(beforeIt, afterIt, afterNext, beforeNext).invoke();
            afterNext = compareIterator.getAfterNext();
            beforeNext = compareIterator.getBeforeNext();
            int compare = compareIterator.getCompare();

            switch (compare) {
                case -1:
                    add(afterNext, after, inClass, inPackage, ChangelogDependencyInformation.ChangeStatus.ADDED);
                    afterNext = null;
                    break;
                case 1:
                    add(beforeNext, before, inClass, inPackage, ChangelogDependencyInformation.ChangeStatus.DELETED);
                    beforeNext = null;
                    break;
                default:
                    beforeNext = null;
                    afterNext = null;
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
        for (BehaviorInformation behaviorInformation : classInformation.getBehaviorInformations())
            behaviourChange(behaviorInformation, classInformation, packageInformation, changeStatus);
    }

    /**
     * Adds all Dependencies in behaviorInformation to changed
     *
     * @param behaviorInformation which has been changed
     * @param classInformation    in which behaviorInformation is
     * @param packageInformation  in which classInformation is
     * @param changeStatus        signals if the dependency was added or deleted
     */
    private void behaviourChange(BehaviorInformation behaviorInformation, ClassInformation classInformation, PackageInformation packageInformation, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        for (BehaviorInformation dependency : behaviorInformation.getReferencedBehavior())
            add(dependency, behaviorInformation, classInformation, packageInformation, changeStatus);
    }

    /**
     * Gets the change set.
     *
     * @return the changelist in form of a set
     */
    public SortedSet<PackageInformation> getChanged() {
        return changed;
    }

    /**
     * HelperClass for comparing two tree structures while iterating through them.
     *
     * @param <T> should be one of {@link PackageInformation}, {@link ClassInformation}, {@link BehaviorInformation}
     */
    private class CompareIterator<T extends Comparable<T>> {
        private Iterator<T> beforeIt;
        private Iterator<T> afterIt;
        private T afterNext;
        private T beforeNext;
        private int compare;

        /**
         * Instantiates a new CompareIterator.
         *
         * @param beforeIt   the Iterator for the before tree structure
         * @param afterIt    the Iterator for the after tree structure
         * @param afterNext  the current object of after
         * @param beforeNext the current object of before
         */
        CompareIterator(Iterator<T> beforeIt, Iterator<T> afterIt, T afterNext, T beforeNext) {
            this.beforeIt = beforeIt;
            this.afterIt = afterIt;
            this.afterNext = afterNext;
            this.beforeNext = beforeNext;
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
         * Gets the result of comparing afterNext and beforeNext.
         *
         * @return the compare between afterNext and beforeNext
         */
        int getCompare() {
            return compare;
        }

        /**
         * Invoke CompareIterator so it iterates one time and saves the result in compare.
         *
         * @return the CompareIterator
         */
        CompareIterator<T> invoke() {
            if (beforeNext == null)
                beforeNext = beforeIt.hasNext() ? beforeIt.next() : null;
            if (afterNext == null)
                afterNext = afterIt.hasNext() ? afterIt.next() : null;

            if (beforeNext != null && afterNext != null)
                compare = Integer.signum(beforeNext.compareTo(afterNext));
            else if (beforeNext != null)
                compare = 1;
            else
                compare = -1;
            return this;
        }
    }
}
