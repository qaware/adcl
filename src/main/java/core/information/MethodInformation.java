package core.information;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

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
    private SortedSet<PackageInformation> packageDependencies;
    @Relationship(type = "USES")
    private SortedSet<ClassInformation> classDependencies;
    @Relationship(type = "USES")
    private SortedSet<MethodInformation> methodDependencies;

    private boolean isConstructor;

    /**
     * Instantiates a new Method information.
     *
     * @param name          the name of the method
     * @param isConstructor true if this method is a constructor
     */
    public MethodInformation(String name, boolean isConstructor) {
        this(name, new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance()), new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance()), new TreeSet<>(MethodInformationComparator.getInstance()), isConstructor);
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name               the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies  the referenced classes
     * @param methodDependencies   the referenced method
     * @param isConstructor      true if method is constructor
     */
    public MethodInformation(String name, SortedSet<PackageInformation> packageDependencies, SortedSet<ClassInformation> classDependencies, SortedSet<MethodInformation> methodDependencies, boolean isConstructor) {
        this.name = name;
        this.packageDependencies = packageDependencies;
        this.classDependencies = classDependencies;
        this.methodDependencies = methodDependencies;
        this.isConstructor = isConstructor;
    }

    /**
     * Set the referenced packages.
     *
     * @param packageDependencies the referenced packages
     */
    public void setPackageDependencies(SortedSet<PackageInformation> packageDependencies) {
        this.packageDependencies = packageDependencies;
    }

    /**
     * Set the referenced classes
     *
     * @param classDependencies the referenced classes
     */
    public void setClassDependencies(SortedSet<ClassInformation> classDependencies) {
        this.classDependencies = classDependencies;
    }

    /**
     * Set the referenced methods
     *
     * @param methodDependencies the referenced methods
     */
    public void setMethodDependencies(SortedSet<MethodInformation> methodDependencies) {
        this.methodDependencies = methodDependencies;
    }

    /**
     * Gets name of the methods.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    public SortedSet<PackageInformation> getPackageDependencies() {
        return packageDependencies;
    }

    public SortedSet<ClassInformation> getClassDependencies() {
        return classDependencies;
    }

    public SortedSet<MethodInformation> getMethodDependencies() {
        return methodDependencies;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    @Override
    public int compareTo(MethodInformation methodInformation) {
        return MethodInformationComparator.getInstance().compare(this, methodInformation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodInformation) {
            MethodInformation methodInformation = (MethodInformation) obj;
            return methodInformation.compareTo(this) == 0 && isConstructor == methodInformation.isConstructor();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Comparator for the type MethodInformation based on the method name.
     */
    public static class MethodInformationComparator implements Comparator<MethodInformation> {

        private static MethodInformationComparator instance;

        public static MethodInformationComparator getInstance() {
            if (instance == null) {
                instance = new MethodInformationComparator();
            }
            return instance;
        }

        private MethodInformationComparator() {
        }

        @Override
        public int compare(MethodInformation methodInformation, MethodInformation otherMethodInformation) {
            return methodInformation.name.compareTo(otherMethodInformation.name);
        }
    }
}

