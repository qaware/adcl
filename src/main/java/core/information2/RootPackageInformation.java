package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Package Information with a Project Information as parent
 */
@NodeEntity
public class RootPackageInformation extends PackageInformation<ProjectInformation> {
    public RootPackageInformation(ProjectInformation parent, @NotNull String name) {
        super(parent, name);
    }
}
