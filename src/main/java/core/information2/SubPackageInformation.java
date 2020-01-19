package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class SubPackageInformation extends PackageInformation<PackageInformation<?>> {
    public SubPackageInformation(PackageInformation<?> parent, @NotNull String name) {
        super(parent, name);
    }
}