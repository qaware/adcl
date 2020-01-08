package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import util.Utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * The type Class information contains Information about the static dependencies of a java class.
 */
@NodeEntity
public class ClassInformation implements Comparable<ClassInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String className;
    @Relationship(type = "IS_METHOD_OF", direction = Relationship.INCOMING)
    private SortedSet<MethodInformation> methodInformations;

    private boolean isService;
    private boolean isInternal;

    /**
     * Instantiates a new Class information.
     *
     * @param className the name of the java class
     */
    public ClassInformation(String className) {
        this(className, new TreeSet<>(), false);
    }

    /**
     * Instantiates a new Class information.
     *
     * @param className the name of the java class
     * @param isService true if this class is a Service
     */
    public ClassInformation(String className, boolean isService) {
        this(className, new TreeSet<>(), isService);
    }

    /**
     * Instantiates a new Class information.
     *
     * @param className            the name of the java class
     * @param behaviorInformations the behavior informations of the java class
     * @param isService            true if Class has service annotation
     */
    public ClassInformation(String className, SortedSet<MethodInformation> behaviorInformations, boolean isService) {
        this.className = className;
        this.methodInformations = behaviorInformations;
        this.isService = isService;
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
     * Gets all referenced packages by extracting them from it's {@link MethodInformation}.
     *
     * @return the referenced packages
     */
    public SortedSet<PackageInformation> getPackageDependencies() {
        SortedSet<PackageInformation> packageDependencies = new TreeSet<>();
        methodInformations.forEach(behaviorInformation -> packageDependencies.addAll(behaviorInformation.getPackageDependencies()));
        return packageDependencies;
    }

    /**
     * Gets all referenced classes by extracting them from it's {@link MethodInformation}.
     *
     * @return the referenced classes
     */
    public SortedSet<ClassInformation> getClassDependencies() {
        SortedSet<ClassInformation> classDependencies = new TreeSet<>();
        methodInformations.forEach(behaviorInformation -> classDependencies.addAll(behaviorInformation.getClassDependencies()));
        return classDependencies;
    }

    /**
     * Gets Behavior information owned by the described class.
     *
     * @return a set of the behavior information
     */
    public SortedSet<MethodInformation> getMethodInformations() {
        return methodInformations;
    }

    /**
     * Gets all referenced Behavior by extracting them from it's {@link MethodInformation}.
     *
     * @return the referenced methods
     */
    public SortedSet<MethodInformation> getMethodDependencies() {
        SortedSet<MethodInformation> methodDependencies = new TreeSet<>();
        methodInformations.forEach(behaviorInformation -> methodDependencies.addAll(behaviorInformation.getMethodDependencies()));
        return methodDependencies;
    }

    /**
     * Adds a BehaviorInformation to the set of BehaviorInformation owned by the described class.
     * @param methodInformation that will be added
     */
    public void addMethodInformation(MethodInformation methodInformation) {
        methodInformations.add(methodInformation);
    }

    /**
     * @return True if class has Service annotation
     */
    public boolean isService() {
        return isService;
    }

    /**
     * Set true if class is also a spring boot service
     *
     * @param isService set true if Class is spring boot service
     */
    public void setService(boolean isService) {
        this.isService = isService;
    }

    /**
     * @return True if class is in analyzed project (false for class of library)
     */
    public boolean isInternal() {
        return isInternal;
    }

    /**
     * @param internal whether the class is in the analyzed project
     */
    public void setInternal(boolean internal) {
        this.isInternal = internal;
    }

    @Override
    public int compareTo(@NotNull ClassInformation classInformation) {
        return Comparator.comparing(ClassInformation::isInternal)
                .thenComparing(ClassInformation::isService)
                .thenComparing(ClassInformation::getClassName)
                .thenComparing(ClassInformation::getMethodInformations, Utils.sortedSetComparator())
                .compare(this, classInformation);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassInformation && ((ClassInformation) obj).compareTo(this) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isInternal, isService, className, methodInformations);
    }

    @Override
    public String toString() {
        return ("Class " + className + " (id=" + id + ", " + (isInternal ? "internal" : "external") + ", " + (isService ? "service" : "no-service") + ") {\n"
                + methodInformations.stream().map(MethodInformation::toString).collect(Collectors.joining(",\n"))
        ).replace("\n", "\n    ") + "\n}";
    }
}
