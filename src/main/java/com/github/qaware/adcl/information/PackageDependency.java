package com.github.qaware.adcl.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from any node to a package node
 */
@RelationshipEntity("PackageDependency")
public final class PackageDependency extends RelationshipInformation<PackageInformation<?>> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private PackageDependency() {
        super();
    }

    /**
     * Creates a new package dependency
     *
     * @param from the node which has the dependency
     * @param to   the dependency
     * @see Information#addPackageDependency(PackageInformation, VersionInformation)
     * @see Information#getPackageDependencies(VersionInformation)
     * @see Information#getAllPackageDependencies(VersionInformation, boolean)
     * @see Information#getAllPackageDependenciesAggregated(VersionInformation, boolean)
     */
    PackageDependency(@NotNull Information<?> from, @NotNull PackageInformation<?> to) {
        super(from, to);
    }
}
