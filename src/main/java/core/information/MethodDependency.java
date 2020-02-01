package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from any node to a method node
 */
@RelationshipEntity("MethodDependency")
public final class MethodDependency extends RelationshipInformation<MethodInformation> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private MethodDependency() {
        super();
    }

    /**
     * Creates a new class dependency
     *
     * @param from the node which has the dependency
     * @param to   the dependency
     * @see Information#addMethodDependency(MethodInformation, VersionInformation)
     * @see Information#getMethodDependencies(VersionInformation)
     * @see Information#getAllMethodDependencies(VersionInformation, boolean)
     */
    MethodDependency(@NotNull Information<?> from, @NotNull MethodInformation to) {
        super(from, to);
    }
}
