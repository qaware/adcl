package core.information2;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Class Information with a Package Information as parent
 */
@NodeEntity
public class OuterClassInformation extends ClassInformation<PackageInformation<?>> {
    @SuppressWarnings("unused")
    private OuterClassInformation() {
        super();
    }

    public OuterClassInformation(PackageInformation<?> parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}
