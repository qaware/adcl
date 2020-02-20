package com.github.qaware.adcl.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from any node to a class node
 */
@RelationshipEntity("ClassDependency")
public final class ClassDependency extends RelationshipInformation<ClassInformation<?>> {
    /**
     * Needed for neo4j initialization
     */
    @SuppressWarnings("unused")
    private ClassDependency() {
        super();
    }

    /**
     * Creates a new class dependency
     *
     * @param from the node which has the dependency
     * @param to   the dependency
     * @see Information#addClassDependency(ClassInformation, VersionInformation)
     * @see Information#getClassDependencies(VersionInformation)
     * @see Information#getAllClassDependencies(VersionInformation, boolean)
     * @see Information#getAllClassDependenciesAggregated(VersionInformation, boolean)
     */
    ClassDependency(@NotNull Information<?> from, @NotNull ClassInformation<?> to) {
        super(from, to);
    }
}
