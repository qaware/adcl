package core.information;

import java.util.SortedSet;

/**
 * The type Constructor information contains static dependency information about {@link javassist.CtConstructor}.
 */
public class ConstructorInformation implements BehaviorInformation, Comparable<ConstructorInformation> {
    private String constructorSignature;
    private SortedSet<String> referencedPackages;
    private SortedSet<String> referencedClasses;
    private SortedSet<String> referencedMethods;

    /**
     * Instantiates a new Constructor information.
     *
     * @param constructorSignature the constructor signature
     * @param referencedPackages   the referenced packages
     * @param referencedClasses    the referenced classes
     * @param referencedMethods    the referenced methods
     */
    public ConstructorInformation(String constructorSignature, SortedSet<String> referencedPackages, SortedSet<String> referencedClasses, SortedSet<String> referencedMethods) {
        this.constructorSignature = constructorSignature;
        this.referencedPackages = referencedPackages;
        this.referencedClasses = referencedClasses;
        this.referencedMethods = referencedMethods;
    }

    /**
     * Gets constructor signature.
     *
     * @return the constructor signature
     */
    public String getConstructorSignature() {
        return constructorSignature;
    }

    public SortedSet<String> getReferencedPackages() {
        return referencedPackages;
    }

    public SortedSet<String> getReferencedMethods() {
        return referencedMethods;
    }

    public SortedSet<String> getReferencedClasses() {
        return referencedClasses;
    }

    @java.lang.SuppressWarnings("squid:S1210")
    @Override
    public int compareTo(ConstructorInformation o) {
        return constructorSignature.compareTo(o.constructorSignature);
    }
}
