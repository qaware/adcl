package core.information;

import java.util.SortedSet;

/**
 * The type Method information contains static dependency information about {@link javassist.CtMethod}.
 */
public class MethodInformation implements BehaviorInformation, Comparable<MethodInformation> {
    private String methodName;
    private SortedSet<String> referencedPackages;
    private SortedSet<String> referencedClasses;
    private SortedSet<String> referencedMethods;

    /**
     * Instantiates a new Method information.
     *
     * @param methodName         the method name
     * @param referencedPackages the referenced packages
     * @param referencedClasses  the referenced classes
     * @param referencedMethods  the referenced methods
     */
    public MethodInformation(String methodName, SortedSet<String> referencedPackages, SortedSet<String> referencedClasses, SortedSet<String> referencedMethods) {
        this.methodName = methodName;
        this.referencedPackages = referencedPackages;
        this.referencedClasses = referencedClasses;
        this.referencedMethods = referencedMethods;
    }

    /**
     * Gets method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    public SortedSet<String> getReferencedPackages() {
        return referencedPackages;
    }

    public SortedSet<String> getReferencedClasses() {
        return referencedClasses;
    }

    public SortedSet<String> getReferencedMethods() {
        return referencedMethods;
    }

    @java.lang.SuppressWarnings("squid:S1210")
    @Override
    public int compareTo(MethodInformation o) {
        return methodName.compareTo(o.methodName);
    }
}
