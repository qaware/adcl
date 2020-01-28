package core.information;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from one structural component to another
 */
@RelationshipEntity("ProjectDependency")
public final class ProjectDependency extends RelationshipInformation<ProjectInformation> {
    @SuppressWarnings("unused")
    private ProjectDependency() {
        super();
    }

    ProjectDependency(@NotNull Information<?> from, @NotNull ProjectInformation to) {
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
