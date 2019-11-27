package core.information;

import java.util.SortedSet;

/**
 * The interface Behavior information contains static dependency information about {@link javassist.CtBehavior}.
 */
public interface BehaviorInformation {

    /**
     * Gets referenced packages.
     *
     * @return the referenced packages
     */
    SortedSet<String> getReferencedPackages();

    /**
     * Gets referenced classes.
     *
     * @return the referenced classes
     */
    SortedSet<String> getReferencedClasses();

    /**
     * Gets referenced methods.
     *
     * @return the referenced methods
     */
    SortedSet<String> getReferencedMethods();

}
