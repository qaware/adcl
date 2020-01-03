package core.information;

import java.util.Objects;
import java.util.SortedSet;

/**
 * * BehaviorInformation with additional change meta (whether it was added or deleted).
 */
public class ChangelogDependencyInformation extends MethodInformation {

    private ChangeStatus changeStatus;

    /**
     * Instantiates a new Behavior information.
     *
     * @param methodInformation the behavior to copy from
     * @param changeStatus        whenever this behavior was deleted or added
     */
    public ChangelogDependencyInformation(MethodInformation methodInformation, ChangeStatus changeStatus) {
        super(methodInformation.getName(), methodInformation.getPackageDependencies(), methodInformation.getClassDependencies(), methodInformation.getMethodDependencies(), methodInformation.isConstructor());
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
     * @param packageDependencies the referenced packages
     * @param classDependencies  the referenced classes
     * @param methodDependencies the referenced behavior
     * @param isConstructor      true if behavior is constructor
     * @param changeStatus       whenever this behavior was deleted or added
     */
    public ChangelogDependencyInformation(String name, SortedSet<PackageInformation> packageDependencies, SortedSet<ClassInformation> classDependencies, SortedSet<MethodInformation> methodDependencies, boolean isConstructor, ChangeStatus changeStatus) {
        super(name, packageDependencies, classDependencies, methodDependencies, isConstructor);
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
        return Objects.hash(super.hashCode(), changeStatus);
    }

    /**
     * Specifies the state of the surrounding class.
     */
    public enum ChangeStatus {
        ADDED, DELETED
    }
}
