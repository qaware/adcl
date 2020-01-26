package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * An information about a method
 */
@NodeEntity
public class MethodInformation extends Information<ClassInformation<?>> {
    public MethodInformation(@NotNull ClassInformation<?> parent, @NotNull String name) {
        super(parent, name);
        if (name.indexOf('(') == -1)
            throw new IllegalArgumentException("Method name is missing parameters. Is: " + name);
    }

    @Override
    public @NotNull Type getType() {
        return Type.METHOD;
    }

    /**
     * Whether this method is a constructor
     */
    public boolean isConstructor() {
        return getName().startsWith("<");
    }
}
