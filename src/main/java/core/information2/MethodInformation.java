package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class MethodInformation extends Information<ClassInformation<?>> {
    public MethodInformation(@NotNull ClassInformation<?> parent, @NotNull String name) {
        super(parent, name);
    }

    @Override
    public @NotNull Type getType() {
        return Type.METHOD;
    }

    public boolean isConstructor() {
        return getName().startsWith("<");
    }
}
