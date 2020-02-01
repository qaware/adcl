package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Package node with a Project node as parent
 */
@NodeEntity
public class RootPackageInformation extends PackageInformation<ProjectInformation> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private RootPackageInformation() {
        super();
    }

    /**
     * Creates a new package information and registers itself in parent
     *
     * @param parent the parent node
     * @param name   the package name (only own name, no dots allowed)
     * @see Information#createChild(Type, String)
     */
    public RootPackageInformation(ProjectInformation parent, @NotNull String name) {
        super(parent, name);
    }
}
