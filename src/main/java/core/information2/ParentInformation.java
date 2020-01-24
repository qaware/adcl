package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing the structural hierarchy of the java project
 */
@RelationshipEntity("Parent")
public final class ParentInformation<T extends Information<?>> extends RelationshipInformation<T> {
    ParentInformation(@NotNull Information<T> from, @NotNull T to) {
        super(from, to);
    }

    @Override
    Information<?> getOwner() {
        return getTo();
    }
}
