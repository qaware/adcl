package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A general Package Information (for neo4j purposes)
 */
@NodeEntity
public abstract class PackageInformation<P extends Information<?>> extends Information<P> {
    PackageInformation(P parent, @NotNull String name) {
        super(parent, name);
    }

    @Override
    public @NotNull Type getType() {
        return Type.PACKAGE;
    }
}
