package core.information2;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from one structural component to another
 */
@RelationshipEntity("Dependency")
public final class DependencyInformation<T extends Information<?>> extends RelationshipInformation<T> {
    DependencyInformation(@NotNull Information<?> from, @NotNull T to) {
        super(from, to);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    Information<?> getOwner() {
        return getFrom();
    }

    @NotNull
    @Contract(pure = true)
    @Override
    Information<?> getAim() {
        return getTo();
    }
}
