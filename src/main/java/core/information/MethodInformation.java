package core.information;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import util.Utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * The class Behavior information contains static dependency information about {@link javassist.CtBehavior}.
 */
@NodeEntity
public class MethodInformation implements Comparable<MethodInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Relationship(type = "USES")
    private Set<PackageInformation> packageDependencies;
    @Relationship(type = "USES")
    private Set<ClassInformation> classDependencies;
    @Relationship(type = "USES")
    private Set<MethodInformation> methodDependencies;

    /**
     * Instantiates a new Method information.
     *
     * @param name          the name of the method
     * @param isConstructor true if this method is a constructor
     */
    @Deprecated
    public MethodInformation(String name, boolean isConstructor) {
        this(name, new TreeSet<>(), new TreeSet<>(), new TreeSet<>());
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name                the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies   the referenced classes
     * @param methodDependencies  the referenced method
     * @param isConstructor       true if method is constructor
     */
    @Deprecated
    public MethodInformation(String name, Set<PackageInformation> packageDependencies, Set<ClassInformation> classDependencies, Set<MethodInformation> methodDependencies, boolean isConstructor) {
        this.name = name;
        this.packageDependencies = packageDependencies;
        this.classDependencies = classDependencies;
        this.methodDependencies = methodDependencies;
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name the name of the method
     */
    public MethodInformation(String name) {
        this(name, new TreeSet<>(), new TreeSet<>(), new TreeSet<>());
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name                the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies   the referenced classes
     * @param methodDependencies  the referenced method
     */
    public MethodInformation(String name, Set<PackageInformation> packageDependencies, Set<ClassInformation> classDependencies, Set<MethodInformation> methodDependencies) {
        this.name = name;
        this.packageDependencies = packageDependencies;
        this.classDependencies = classDependencies;
        this.methodDependencies = methodDependencies;
    }

    /**
     * Should not be used is for Spring Data
     */
    public MethodInformation() {
        this.packageDependencies = new TreeSet<>();
        this.classDependencies = new TreeSet<>();
        this.methodDependencies = new TreeSet<>();
    }

    public Set<PackageInformation> getPackageDependencies() {
        return packageDependencies;
    }

    /**
     * Set the referenced packages.
     *
     * @param packageDependencies the referenced packages
     */
    public void setPackageDependencies(Set<PackageInformation> packageDependencies) {
        this.packageDependencies = packageDependencies;
    }

    public Set<ClassInformation> getClassDependencies() {
        return classDependencies;
    }

    /**
     * Gets name of the methods.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    /**
     * Set the referenced classes
     *
     * @param classDependencies the referenced classes
     */
    public void setClassDependencies(Set<ClassInformation> classDependencies) {
        this.classDependencies = classDependencies;
    }

    public Set<MethodInformation> getMethodDependencies() {
        return methodDependencies;
    }

    /**
     * Set the referenced methods
     *
     * @param methodDependencies the referenced methods
     */
    public void setMethodDependencies(Set<MethodInformation> methodDependencies) {
        this.methodDependencies = methodDependencies;
    }

    public boolean isConstructor() {
        return name.indexOf('<') != -1;
    }

    @Override
    public int compareTo(MethodInformation methodInformation) {
        return Comparator.comparing(MethodInformation::isConstructor)
                .thenComparing(MethodInformation::getName)
                .thenComparing(MethodInformation::getPackageDependencies, Utils.setComparator(Comparator.comparing(PackageInformation::getPackageName)))
                .thenComparing(MethodInformation::getClassDependencies, Utils.setComparator(Comparator.comparing(ClassInformation::getClassName)))
                .thenComparing(MethodInformation::getMethodDependencies, Utils.setComparator(Comparator.comparing(MethodInformation::getName)))
                .compare(this, methodInformation);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MethodInformation && ((MethodInformation) obj).compareTo(this) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return ("Method " + name + " (id=" + id + ", " + (isConstructor() ? "constructor" : "no-constructor") + ") {\n"
                + "packageDependencies=[" + packageDependencies.stream().map(PackageInformation::getPackageName).collect(Collectors.joining(", ")) + "]\n"
                + "classDependencies=[" + classDependencies.stream().map(ClassInformation::getClassName).collect(Collectors.joining(", ")) + "]\n"
                + "methodDependencies=[" + methodDependencies.stream().map(MethodInformation::getName).collect(Collectors.joining(", ")) + "]"
        ).replace("\n", "\n    ") + "\n}";
    }
}
