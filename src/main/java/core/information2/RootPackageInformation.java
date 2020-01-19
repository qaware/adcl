package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class RootPackageInformation extends PackageInformation<ProjectInformation> {
    public RootPackageInformation(ProjectInformation parent, @NotNull String name) {
        super(parent, name);
    }
}
