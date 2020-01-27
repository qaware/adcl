package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Package Information with a Project Information as parent
 */
@NodeEntity
public class RootPackageInformation extends PackageInformation<ProjectInformation> {
    @SuppressWarnings("unused")
    private RootPackageInformation() {
        super();
    }

    public RootPackageInformation(ProjectInformation parent, @NotNull String name) {
        super(parent, name);
    }
}
