package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from any node to a project node
 */
@RelationshipEntity("ProjectDependency")
public final class ProjectDependency extends RelationshipInformation<ProjectInformation> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private ProjectDependency() {
        super();
    }

    /**
     * Creates a new project dependency
     *
     * @param from the node which has the dependency
     * @param to   the dependency
     * @see Information#addProjectDependency(ProjectInformation, VersionInformation)
     * @see Information#getProjectDependencies(VersionInformation)
     * @see Information#getAllProjectDependencies(VersionInformation, boolean)
     * @see Information#getAllProjectDependenciesAggregated(VersionInformation, boolean)
     */
    ProjectDependency(@NotNull Information<?> from, @NotNull ProjectInformation to) {
        super(from, to);
    }
}
