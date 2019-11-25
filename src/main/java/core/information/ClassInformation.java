package core.information;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The type Class information contains Information about the static dependencies of a java class.
 */
public class ClassInformation implements Comparable<ClassInformation> {

    private String className;
    private SortedSet<String> referencedPackages;
    private SortedSet<String> referencedClasses;
    private SortedSet<ConstructorInformation> constructorInformations;
    private SortedSet<MethodInformation> methodInformations;

    /**
     * Instantiates a new Class information.
     *
     * @param className               the name of the java class
     * @param referencedPackages      the referenced packages of the java class
     * @param referencedClasses       the referenced classes of the java class
     * @param constructorInformations the constructor informations of the java class
     * @param methodInformations      the method informations of the java class
     */
    public ClassInformation(String className, SortedSet<String> referencedPackages, SortedSet<String> referencedClasses, SortedSet<ConstructorInformation> constructorInformations, SortedSet<MethodInformation> methodInformations) {
        this.className = className;
        this.referencedPackages = referencedPackages;
        this.referencedClasses = referencedClasses;
        this.constructorInformations = constructorInformations;
        this.methodInformations = methodInformations;
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets referenced packages.
     *
     * @return the referenced packages
     */
    public SortedSet<String> getReferencedPackages() {
        return referencedPackages;
    }

    /**
     * Gets referenced classes.
     *
     * @return the referenced classes
     */
    public SortedSet<String> getReferencedClasses() {
        return referencedClasses;
    }

    /**
     * Gets constructor informations.
     *
     * @return the constructor informations
     */
    public Collection<ConstructorInformation> getConstructorInformations() {
        return constructorInformations;
    }

    /**
     * Gets method informations.
     *
     * @return the method informations
     */
    public Collection<MethodInformation> getMethodInformations() {
        return methodInformations;
    }

    /**
     * Gets all referenced methods by extracting them from it's {@link MethodInformation} and {@link ConstructorInformation}.
     *
     * @return the referenced methods
     */
    public SortedSet<String> getReferencedMethods() {
        SortedSet<String> referencedMethods = new TreeSet<>();
        methodInformations.forEach(methodInformation -> referencedMethods.addAll(methodInformation.getReferencedMethods()));
        constructorInformations.forEach(constructorInformation -> referencedMethods.addAll(constructorInformation.getReferencedMethods()));
        return referencedMethods;
    }

    @java.lang.SuppressWarnings("squid:S1210")
    @Override
    public int compareTo(ClassInformation o) {
        return className.compareTo(o.className);
    }

}
