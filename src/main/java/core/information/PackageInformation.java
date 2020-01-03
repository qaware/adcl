package core.information;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The type Package information contains Information about the static dependencies of a java package.
 */
@NodeEntity
public class PackageInformation implements Comparable<PackageInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String packageName;
    @Relationship(type = "IS_CLASS_OF", direction = Relationship.INCOMING)
    private SortedSet<ClassInformation> classInformations;

    private boolean isInternalPackage;

    /**
     * Instantiates a new Package information.
     *
     * @param packageName the package name
     */
    public PackageInformation(String packageName) {
        this.packageName = packageName;
        this.classInformations = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
    }

    /**
     * Gets package name.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets referenced packages by extracting them from it's {@link ClassInformation}.
     *
     * @return the referenced packages
     */
    public SortedSet<PackageInformation> getPackageDependencies() {
        SortedSet<PackageInformation> packageDependencies = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        classInformations.forEach(classInformation -> packageDependencies.addAll(classInformation.getPackageDependencies()));
        return packageDependencies;
    }

    /**
     * Gets referenced classes by extracting them from it's {@link ClassInformation}.
     *
     * @return the referenced classes
     */
    public SortedSet<ClassInformation> getClassDependencies() {
        SortedSet<ClassInformation> classDependencies = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        classInformations.forEach(classInformation -> classDependencies.addAll(classInformation.getClassDependencies()));
        return classDependencies;
    }

    /**
     * Gets referenced method by extracting them from it's {@link ClassInformation}.
     *
     * @return the referenced methods
     */
    public SortedSet<MethodInformation> getMethodDependencies() {
        SortedSet<MethodInformation> methodDependencies = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        classInformations.forEach(classInformation -> methodDependencies.addAll(classInformation.getMethodDependencies()));
        return methodDependencies;
    }

    /**
     * Set if internal package or not.
     *
     * @param internalPackage true if package is interal
     */
    public void setInternalPackage(boolean internalPackage) {
        isInternalPackage = internalPackage;
    }

    /**
     * @return if internal package or not.
     */
    public boolean isInternalPackage() {
        return isInternalPackage;
    }

    /**
     * Gets class information about all the java classes that were contained in the described package.
     *
     * @return all class information.
     */
    public SortedSet<ClassInformation> getClassInformations() {
        return classInformations;
    }

    /**
     * Add a class information. Used to add {@link ClassInformation} about java classes that were originally part of the described package.
     *
     * @param classInformation the class information
     */
    public void addClassInformation(ClassInformation classInformation) {
        this.classInformations.add(classInformation);
    }

    @Override
    public int compareTo(PackageInformation packageInformation) {
        return PackageInformationComparator.getInstance().compare(this, packageInformation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackageInformation) {
            PackageInformation packageInformation = (PackageInformation) obj;
            return packageInformation.compareTo(this) == 0 && packageInformation.isInternalPackage == isInternalPackage;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Comparator for the type PackageInformation based on the package name.
     */
    public static class PackageInformationComparator implements Comparator<PackageInformation> {

        private static PackageInformationComparator instance;

        public static PackageInformationComparator getInstance() {
            if (instance == null) {
                instance = new PackageInformationComparator();
            }
            return instance;
        }

        private PackageInformationComparator() {
        }

        @Override
        public int compare(PackageInformation packageInformation, PackageInformation otherPackageInformation) {
            return packageInformation.packageName.compareTo(otherPackageInformation.packageName);
        }
    }
}
