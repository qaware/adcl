package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * An information about a method
 */
@NodeEntity
public class MethodInformation extends Information<ClassInformation<?>> {
    @SuppressWarnings("unused")
    private MethodInformation() {
        super();
    }

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
     * @return whether this method is a constructor
     */
    public boolean isConstructor() {
        return getName().startsWith("<");
    }
}
