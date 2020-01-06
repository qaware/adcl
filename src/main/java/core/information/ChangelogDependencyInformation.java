package core.information;

import java.util.Objects;
import java.util.SortedSet;

/**
 * MethodInformation with additional change meta (whether it was added or deleted).
 */
public class ChangelogDependencyInformation extends MethodInformation {

    private ChangeStatus changeStatus;

    /**
     * Instantiates a new ChangelogDependencyInformation.
     *
     * @param methodInformation the method to copy from
     * @param changeStatus      whenever this method was deleted or added
     */
    public ChangelogDependencyInformation(MethodInformation methodInformation, ChangeStatus changeStatus) {
        super(methodInformation.getName(), methodInformation.getPackageDependencies(), methodInformation.getClassDependencies(), methodInformation.getMethodDependencies(), methodInformation.isConstructor());
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new ChangelogDependencyInformation.
     *
     * @param name          the name of the method
     * @param isConstructor true if this method is a constructor
     * @param changeStatus  whenever this method was deleted or added
     */
    public ChangelogDependencyInformation(String name, boolean isConstructor, ChangeStatus changeStatus) {
        super(name, isConstructor);
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new ChangelogDependencyInformation.
     *
     * @param name                the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies   the referenced classes
     * @param methodDependencies  the referenced method
     * @param isConstructor       true if method is constructor
     * @param changeStatus        whenever this method was deleted or added
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