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
public class BehaviorInformation implements Comparable<BehaviorInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Relationship(type = "USES")
    private SortedSet<PackageInformation> referencedPackages;
    @Relationship(type = "USES")
    private SortedSet<ClassInformation> referencedClasses;
    @Relationship(type = "USES")
    private SortedSet<BehaviorInformation> referencedBehavior;

    private boolean isConstructor;

    /**
     * Instantiates a new Behavior information.
     *
     * @param name          the name of the behavior
     * @param isConstructor true if this behavior is a constructor
     */
    public BehaviorInformation(String name, boolean isConstructor) {
        this(name, new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance()), new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance()), new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance()), isConstructor);
    }

    /**
     * Instantiates a new Behavior information.
     *
     * @param name               the name of the behavior
     * @param referencedPackages the referenced packages
     * @param referencedClasses  the referenced classes
     * @param referencedBehavior the referenced behavior
     * @param isConstructor      true if behavior is constructor
     */
    public BehaviorInformation(String name, SortedSet<PackageInformation> referencedPackages, SortedSet<ClassInformation> referencedClasses, SortedSet<BehaviorInformation> referencedBehavior, boolean isConstructor) {
        this.name = name;
        this.referencedPackages = referencedPackages;
        this.referencedClasses = referencedClasses;
        this.referencedBehavior = referencedBehavior;
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
     * Set the referenced behaviors
     *
     * @param referencedBehavior the referenced behaviors
     */
    public void setReferencedBehavior(SortedSet<BehaviorInformation> referencedBehavior) {
        this.referencedBehavior = referencedBehavior;
    }

    /**
     * Gets name of the behavior.
     *
     * @return the name of the behavior
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

    public SortedSet<BehaviorInformation> getReferencedBehavior() {
        return referencedBehavior;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    @Override
    public int compareTo(BehaviorInformation behaviorInformation) {
        return BehaviorInformationComparator.getInstance().compare(this, behaviorInformation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BehaviorInformation) {
            BehaviorInformation behaviorInformation = (BehaviorInformation) obj;
            return behaviorInformation.compareTo(this) == 0 && isConstructor == behaviorInformation.isConstructor();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Comparator for the type BehaviorInformation based on the behavior name.
     */
    public static class BehaviorInformationComparator implements Comparator<BehaviorInformation> {

        private static BehaviorInformationComparator instance;

        public static BehaviorInformationComparator getInstance() {
            if (instance == null) {
                instance = new BehaviorInformationComparator();
            }
            return instance;
        }

        private BehaviorInformationComparator() {
        }

        @Override
        public int compare(BehaviorInformation behaviorInformation, BehaviorInformation otherBehaviorInformation) {
            return behaviorInformation.name.compareTo(otherBehaviorInformation.name);
        }
    }
}

