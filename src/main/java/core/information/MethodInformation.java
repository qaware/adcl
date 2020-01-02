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
    private SortedSet<PackageInformation> referencedPackages;
    @Relationship(type = "USES")
    private SortedSet<ClassInformation> referencedClasses;
    @Relationship(type = "USES")
    private SortedSet<MethodInformation> referencedMethods;

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
     * @param referencedPackages the referenced packages
     * @param referencedClasses  the referenced classes
     * @param referencedMethods   the referenced method
     * @param isConstructor      true if method is constructor
     */
    public MethodInformation(String name, SortedSet<PackageInformation> referencedPackages, SortedSet<ClassInformation> referencedClasses, SortedSet<MethodInformation> referencedMethods, boolean isConstructor) {
        this.name = name;
        this.referencedPackages = referencedPackages;
        this.referencedClasses = referencedClasses;
        this.referencedMethods = referencedMethods;
        this.isConstructor = isConstructor;
    }

    /**
     * Set the referenced packages.
     *
     * @param referencedPackages the referenced packages
     */
    public void setReferencedPackages(SortedSet<PackageInformation> referencedPackages) {
        this.referencedPackages = referencedPackages;
    }

    /**
     * Set the referenced classes
     *
     * @param referencedClasses the referenced classes
     */
    public void setReferencedClasses(SortedSet<ClassInformation> referencedClasses) {
        this.referencedClasses = referencedClasses;
    }

    /**
     * Set the referenced methods
     *
     * @param referencedMethods the referenced methods
     */
    public void setReferencedMethods(SortedSet<MethodInformation> referencedMethods) {
        this.referencedMethods = referencedMethods;
    }

    /**
     * Gets name of the methods.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    public SortedSet<PackageInformation> getReferencedPackages() {
        return referencedPackages;
    }

    public SortedSet<ClassInformation> getReferencedClasses() {
        return referencedClasses;
    }

    public SortedSet<MethodInformation> getReferencedMethods() {
        return referencedMethods;
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

