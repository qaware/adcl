package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Package node with another Package node as parent
 */
@NodeEntity
public class SubPackageInformation extends PackageInformation<PackageInformation<?>> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private SubPackageInformation() {
        super();
    }

    /**
     * Creates a new package information and registers itself in parent
     *
     * @param parent the parent node
     * @param name   the package name (only own name, no dots allowed)
     * @see Information#createChild(Type, String)
     */
    public SubPackageInformation(PackageInformation<?> parent, @NotNull String name) {
        super(parent, name);
    }
}