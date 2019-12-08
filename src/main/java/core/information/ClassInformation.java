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
    private SortedSet<BehaviorInformation> behaviorInformations;

    private boolean isService;

    /**
     * Instantiates a new Class information.
     *
     * @param className the name of the java class
     */
    public ClassInformation(String className) {
        this(className, new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance()), false);
    }

    /**
     * Instantiates a new Class information.
     *
     * @param className the name of the java class
     * @param isService true if this class is a Service
     */
    public ClassInformation(String className, boolean isService) {
        this(className, new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance()), isService);
    }

    /**
     * Instantiates a new Class information.
     *
     * @param className            the name of the java class
     * @param behaviorInformations the behavior informations of the java class
     * @param isService            true if Class has service annotation
     */
    public ClassInformation(String className, SortedSet<BehaviorInformation> behaviorInformations, boolean isService) {
        this.className = className;
        this.behaviorInformations = behaviorInformations;
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
     * Gets all referenced packages by extracting them from it's {@link BehaviorInformation}.
     *
     * @return the referenced packages
     */
    public SortedSet<PackageInformation> getReferencedPackages() {
        SortedSet<PackageInformation> referencedPackages = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        behaviorInformations.forEach(behaviorInformation -> referencedPackages.addAll(behaviorInformation.getReferencedPackages()));
        return referencedPackages;
    }

    /**
     * Gets all referenced classes by extracting them from it's {@link BehaviorInformation}.
     *
     * @return the referenced classes
     */
    public SortedSet<ClassInformation> getReferencedClasses() {
        SortedSet<ClassInformation> referencedClasses = new TreeSet<>(ClassInformationComparator.getInstance());
        behaviorInformations.forEach(behaviorInformation -> referencedClasses.addAll(behaviorInformation.getReferencedClasses()));
        return referencedClasses;
    }

    /**
     * Gets Behavior information owned by the described class.
     *
     * @return a set of the behavior information
     */
    public SortedSet<BehaviorInformation> getBehaviorInformations() {
        return behaviorInformations;
    }

    /**
     * Gets all referenced Behavior by extracting them from it's {@link BehaviorInformation}.
     *
     * @return the referenced methods
     */
    public SortedSet<BehaviorInformation> getReferencedBehavior() {
        SortedSet<BehaviorInformation> referencedBehaviors = new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance());
        behaviorInformations.forEach(behaviorInformation -> referencedBehaviors.addAll(behaviorInformation.getReferencedBehavior()));
        return referencedBehaviors;
    }

    /**
     * Adds a BehaviorInformation to the set of BehaviorInformation owned by the described class.
     * @param behaviorInformation that will be added
     */
    public void addBehaviorInformation(BehaviorInformation behaviorInformation) {
        behaviorInformations.add(behaviorInformation);
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
