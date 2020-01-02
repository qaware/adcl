package core.information;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The type Class information contains Information about the static dependencies of a java class.
 */
@NodeEntity
public class ClassInformation implements Comparable<ClassInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String className;
    @Relationship(type = "IS_BEHAVIOR_OF")
    private SortedSet<MethodInformation> methodInformations;

    private boolean isService;

    /**
     * Instantiates a new Class information.
     *
     * @param className the name of the java class
     */
    public ClassInformation(String className) {
        this(className, new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance()), false);
    }

    /**
     * Instantiates a new Class information.
     *
     * @param className the name of the java class
     * @param isService true if this class is a Service
     */
    public ClassInformation(String className, boolean isService) {
        this(className, new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance()), isService);
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
    public SortedSet<PackageInformation> getReferencedPackages() {
        SortedSet<PackageInformation> referencedPackages = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        methodInformations.forEach(behaviorInformation -> referencedPackages.addAll(behaviorInformation.getReferencedPackages()));
        return referencedPackages;
    }

    /**
     * Gets all referenced classes by extracting them from it's {@link MethodInformation}.
     *
     * @return the referenced classes
     */
    public SortedSet<ClassInformation> getReferencedClasses() {
        SortedSet<ClassInformation> referencedClasses = new TreeSet<>(ClassInformationComparator.getInstance());
        methodInformations.forEach(behaviorInformation -> referencedClasses.addAll(behaviorInformation.getReferencedClasses()));
        return referencedClasses;
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
    public SortedSet<MethodInformation> getReferencedMethods() {
        SortedSet<MethodInformation> referencedBehaviors = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodInformations.forEach(behaviorInformation -> referencedBehaviors.addAll(behaviorInformation.getReferencedMethods()));
        return referencedBehaviors;
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

    @Override
    public int compareTo(ClassInformation classInformation) {
        return ClassInformationComparator.getInstance().compare(this, classInformation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassInformation)
            return this.compareTo((ClassInformation) obj) == 0 && isService == ((ClassInformation) obj).isService;
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Comparator for the type ClassInformation based on the class name.
     */
    public static class ClassInformationComparator implements Comparator<ClassInformation> {

        private static ClassInformation.ClassInformationComparator instance;

        public static ClassInformation.ClassInformationComparator getInstance() {
            if (instance == null) {
                instance = new ClassInformation.ClassInformationComparator();
            }
            return instance;
        }

        private ClassInformationComparator() {
        }

        @Override
        public int compare(ClassInformation classInformation, ClassInformation otherClassInformation) {
            if (classInformation.isService && !otherClassInformation.isService) {
                return 1;
            }
            if (!classInformation.isService && otherClassInformation.isService) {
                return -1;
            }
            return classInformation.className.compareTo(otherClassInformation.className);
        }
    }
}
