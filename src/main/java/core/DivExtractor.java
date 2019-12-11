package core;

import core.information.BehaviorInformation;
import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The type DivExtractor.
 */
public class DivExtractor {
    private List<PackageInformation> added = new ArrayList<>();
    private List<PackageInformation> deleted = new ArrayList<>();

    /**
     * Instantiates a new DivExtractor.
     *
     * @param old      the old
     * @param analysed the analysed
     */
    public DivExtractor(Collection<PackageInformation> old, Collection<PackageInformation> analysed) {

        fillList(analysed, old, added, ChangelogDependencyInformation.ChangeStatus.ADDED);
        fillList(old, analysed, deleted, ChangelogDependencyInformation.ChangeStatus.DELETED);
    }

    /**
     * @param lookUp
     * @param toBeSearched
     * @param statusList
     * @param changeStatus
     */
    private void fillList(Collection<PackageInformation> lookUp, Collection<PackageInformation> toBeSearched,
                          List<PackageInformation> statusList, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        lookUp.forEach(packageInformation ->
                packageInformation.getClassInformations().forEach(classInformation ->
                        classInformation.getBehaviorInformations().forEach(behaviorInformation ->
                                behaviorInformation.getReferencedBehavior().forEach(referencedMethod -> {
                                    if (!searchInPackage(referencedMethod, toBeSearched)) {
                                        add(referencedMethod, behaviorInformation, classInformation, packageInformation, statusList, changeStatus);
                                    }
                                })
                        )
                )
        );
    }

    /**
     * @param referencedMethod
     * @param behaviorInformation
     * @param classInformation
     * @param packageInformation
     * @param list
     * @param status
     */
    private void add(BehaviorInformation referencedMethod, BehaviorInformation behaviorInformation, ClassInformation classInformation,
                     PackageInformation packageInformation, List<PackageInformation> list, ChangelogDependencyInformation.ChangeStatus status) {
        PackageInformation pi = new PackageInformation(packageInformation.getPackageName());
        ClassInformation ci = new ClassInformation(classInformation.getClassName());
        BehaviorInformation bi = new BehaviorInformation(behaviorInformation.getName(), behaviorInformation.isConstructor());
        BehaviorInformation referencedbi = new ChangelogDependencyInformation(referencedMethod,
                status);
        if (list.contains(packageInformation)) {
            pi = list.stream().filter(packageInformation1 -> packageInformation1.equals(packageInformation)).findFirst().orElse(pi);
            if (packageInformation.getClassInformations().contains(classInformation)) {
                ci = packageInformation.getClassInformations().stream().filter(classInformation1 -> classInformation1.equals(classInformation)).findFirst().orElse(ci);
                if (classInformation.getBehaviorInformations().contains(behaviorInformation)) {
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
            list.add(pi);
        }
    }

    /**
     * @param referencedMethod
     * @param analysed
     * @return if the referencedMethod is in analysed
     */
    private boolean searchInPackage(BehaviorInformation referencedMethod, Collection<PackageInformation> analysed) {
        final boolean[] result = {false};
        analysed.forEach(packageInformation ->
                packageInformation.getClassInformations().forEach(classInformation ->
                        classInformation.getBehaviorInformations().forEach(behaviorInformation ->
                                result[0] = behaviorInformation.getReferencedBehavior().contains(referencedMethod)
                        )
                )
        );
        return result[0];
    }

    /**
     * Gets added.
     *
     * @return the added
     */
    public List<PackageInformation> getAdded() {
        return added;
    }

    /**
     * Gets deleted.
     *
     * @return the deleted
     */
    public List<PackageInformation> getDeleted() {
        return deleted;
    }
}
