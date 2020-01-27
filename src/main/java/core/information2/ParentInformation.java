package core.information2;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing the structural hierarchy of the java project
 */
@RelationshipEntity("Parent")
public final class ParentInformation<T extends Information<?>> extends RelationshipInformation<T> {
    @SuppressWarnings("unused")
    private ParentInformation() {
        super();
    }

    ParentInformation(@NotNull Information<T> from, @NotNull T to) {
        super(from, to);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    Information<?> getOwner() {
        return getTo();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    Information<?> getAim() {
        return getFrom();
    }

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
