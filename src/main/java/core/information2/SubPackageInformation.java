package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Package Information with another Package Information as parent
 */
@NodeEntity
public class SubPackageInformation extends PackageInformation<PackageInformation<?>> {
    @SuppressWarnings("unused")
    private SubPackageInformation() {
        super();
    }

    public SubPackageInformation(PackageInformation<?> parent, @NotNull String name) {
        super(parent, name);
    }
}