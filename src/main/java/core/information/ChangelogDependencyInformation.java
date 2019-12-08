package core.information;

import java.util.SortedSet;

/**
 * Used then creating cheating to add additional meta information about the behavior, whenever it was added or deleted.
 */
public class ChangelogDependencyInformation extends BehaviorInformation {

    private ChangeStatus changeStatus;

    /**
     * Instantiates a new Behavior information.
     *
     * @param behaviorInformation the behavior to copy from
     * @param changeStatus        whenever this behavior was deleted or added
     */
    public ChangelogDependencyInformation(BehaviorInformation behaviorInformation, ChangeStatus changeStatus) {
        super(behaviorInformation.getName(), behaviorInformation.getReferencedPackages(), behaviorInformation.getReferencedClasses(), behaviorInformation.getReferencedBehavior(), behaviorInformation.isConstructor());
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new Behavior information.
     *
     * @param name          the name of the behavior
     * @param isConstructor true if this behavior is a constructor
     * @param changeStatus  whenever this behavior was deleted or added
     */
    public ChangelogDependencyInformation(String name, boolean isConstructor, ChangeStatus changeStatus) {
        super(name, isConstructor);
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new Behavior information.
     *
     * @param name               the name of the behavior
     * @param referencedPackages the referenced packages
     * @param referencedClasses  the referenced classes
     * @param referencedBehavior the referenced behavior
     * @param isConstructor      true if behavior is constructor
     * @param changeStatus       whenever this behavior was deleted or added
     */
    public ChangelogDependencyInformation(String name, SortedSet<PackageInformation> referencedPackages, SortedSet<ClassInformation> referencedClasses, SortedSet<BehaviorInformation> referencedBehavior, boolean isConstructor, ChangeStatus changeStatus) {
        super(name, referencedPackages, referencedClasses, referencedBehavior, isConstructor);
        this.changeStatus = changeStatus;
    }

    /**
     * Get the change status, this field is used then creating the Changelog to determine whenever a dependency was deleted or added.
     *
     * @return the current status
     */
    public ChangeStatus getChangeStatus() {
        return changeStatus;
    }

    /**
     * Set the change status, this field is used then creating the Changelog to determine whenever a dependency was deleted or added.
     */
    public void setChangeStatus(ChangeStatus changeStatus) {
        this.changeStatus = changeStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChangelogDependencyInformation) {
            return super.equals(obj) && this.changeStatus == ((ChangelogDependencyInformation) obj).changeStatus;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Specifies the state of the surrounding class.
     */
    public enum ChangeStatus {
        ADDED, DELETED
    }
}
