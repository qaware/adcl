package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from one structural component to another
 */
@RelationshipEntity("Dependency")
public class DependencyInformation<T extends Information<?>> extends RelationshipInformation<T> {
    DependencyInformation(@NotNull Information<?> from, @NotNull T to) {
        super(from, to);
    }

    @Override
    Information<?> getOwner() {
        return getFrom();
    }
}
