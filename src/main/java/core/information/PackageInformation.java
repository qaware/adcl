package core.information;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The type Package information contains Information about the static dependencies of a java package.
 */
public class PackageInformation implements Comparable<PackageInformation> {
    private String packageName;
    private SortedSet<ClassInformation> classInformations;

    /**
     * Instantiates a new Package information.
     *
     * @param packageName the package name
     */
    public PackageInformation(String packageName) {
        this.packageName = packageName;
        this.classInformations = new TreeSet<>();
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
    public SortedSet<String> getReferencedPackages() {
        SortedSet<String> referencedPackages = new TreeSet<>();
        classInformations.forEach(classInformation -> referencedPackages.addAll(classInformation.getReferencedPackages()));
        return referencedPackages;
    }

    /**
     * Gets referenced classes by extracting them from it's {@link ClassInformation}..
     *
     * @return the referenced classes
     */
    public SortedSet<String> getReferencedClasses() {
        SortedSet<String> referencedClasses = new TreeSet<>();
        classInformations.forEach(classInformation -> referencedClasses.addAll(classInformation.getReferencedClasses()));
        return referencedClasses;
    }

    /**
     * Gets referenced methods by extracting them from it's {@link ClassInformation}..
     *
     * @return the referenced methods
     */
    public SortedSet<String> getReferencedMethods() {
        SortedSet<String> referencedMethods = new TreeSet<>();
        classInformations.forEach(classInformation -> referencedMethods.addAll(classInformation.getReferencedMethods()));
        return referencedMethods;
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

    @java.lang.SuppressWarnings("squid:S1210")
    @Override
    public int compareTo(PackageInformation o) {
        return packageName.compareTo(o.packageName);
    }
}
