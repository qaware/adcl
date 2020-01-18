package core.information;

import java.util.Objects;
import java.util.Set;

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
        super(methodInformation.getName(), methodInformation.getPackageDependencies(), methodInformation.getClassDependencies(), methodInformation.getMethodDependencies());
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new ChangelogDependencyInformation.
     *
     * @param name          the name of the method
     * @param isConstructor true if this method is a constructor
     * @param changeStatus  whenever this method was deleted or added
     */
    @Deprecated
    public ChangelogDependencyInformation(String name, boolean isConstructor, ChangeStatus changeStatus) {
        super(name);
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
    @Deprecated
    public ChangelogDependencyInformation(String name, Set<PackageInformation> packageDependencies, Set<ClassInformation> classDependencies, Set<MethodInformation> methodDependencies, boolean isConstructor, ChangeStatus changeStatus) {
        super(name, packageDependencies, classDependencies, methodDependencies);
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new ChangelogDependencyInformation.
     *
     * @param name         the name of the method
     * @param changeStatus whenever this method was deleted or added
     */
    public ChangelogDependencyInformation(String name, ChangeStatus changeStatus) {
        super(name);
        this.changeStatus = changeStatus;
    }

    /**
     * Instantiates a new ChangelogDependencyInformation.
     *
     * @param name                the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies   the referenced classes
     * @param methodDependencies  the referenced method
     * @param changeStatus        whenever this method was deleted or added
     */
    public ChangelogDependencyInformation(String name, Set<PackageInformation> packageDependencies, Set<ClassInformation> classDependencies, Set<MethodInformation> methodDependencies, ChangeStatus changeStatus) {
        super(name, packageDependencies, classDependencies, methodDependencies);
        this.changeStatus = changeStatus;
    }

    /**
     * Should not be used is for Spring Data
     */
    public ChangelogDependencyInformation() {
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