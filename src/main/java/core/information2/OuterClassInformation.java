package core.information2;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class OuterClassInformation extends ClassInformation<PackageInformation<?>> {
    public OuterClassInformation(PackageInformation<?> parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}
