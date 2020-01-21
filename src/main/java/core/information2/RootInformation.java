package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.Property;

public class RootInformation extends Information<RootInformation> {
    @Property
    private final int modelVersion = 2;

    public RootInformation(@NotNull RootInformation parent) {
        super(parent, "");
    }

    public int getModelVersion() {
        return modelVersion;
    }

    @Override
    public @NotNull Type getType() {
        return Type.ROOT;
    }

    @NotNull
    @Override
    public RootInformation getParent() {
        return this;
    }

    @Override
    public boolean exists(@NotNull VersionInformation version) {
        return true;
    }
}
