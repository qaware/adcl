package core.information;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Represents an edge on the graph describing a dependency from one structural component to another
 */
@RelationshipEntity("PackageDependency")
public final class PackageDependency extends RelationshipInformation<PackageInformation<?>> {
    @SuppressWarnings("unused")
    private PackageDependency() {
        super();
    }

    PackageDependency(@NotNull Information<?> from, @NotNull PackageInformation<?> to) {
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
