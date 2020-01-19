package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public abstract class PackageInformation<P extends Information<?>> extends Information<P> {
    public PackageInformation(P parent, @NotNull String name) {
        super(parent, name);
    }

    @Override
    public @NotNull Type getType() {
        return Type.PACKAGE;
    }
}
