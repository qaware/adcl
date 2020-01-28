package core.information;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from one structural component to another
 */
@RelationshipEntity("MethodDependency")
public final class MethodDependency extends RelationshipInformation<MethodInformation> {
    @SuppressWarnings("unused")
    private MethodDependency() {
        super();
    }

    MethodDependency(@NotNull Information<?> from, @NotNull MethodInformation to) {
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
