package core.information;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing the structural hierarchy of the java project
 *
 * @param <P> the parent type
 */
@RelationshipEntity("Parent")
public final class ParentInformation<P extends Information<?>> extends RelationshipInformation<P> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private ParentInformation() {
        super();
    }

    /**
     * Creates a new parent information. Does not register itself in parent nor child
     * @param from the child information
     * @param to the parent information
     */
    ParentInformation(@NotNull Information<P> from, @NotNull P to) {
        super(from, to);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Contract(pure = true)
    @Override
    Information<?> getOwner() {
        return getTo();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Contract(pure = true)
    @Override
    Information<?> getAim() {
        return getFrom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExists(@NotNull VersionInformation version, boolean aim) {
        if (aim) {
            if (!exists(version)) {
                super.setExists(version, true);
                getFrom().directChildren.forEach(i -> i.setExistsNoInheritanceCheck(version, false));
            }
        } else {
            super.setExists(version, false);
        }
    }
}
