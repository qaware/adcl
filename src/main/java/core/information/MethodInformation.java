package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * An Method node
 */
@NodeEntity
public class MethodInformation extends Information<ClassInformation<?>> {
    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private MethodInformation() {
        super();
    }

    /**
     * Creates a new method information and registers itself in parent
     *
     * @param parent the parent node
     * @param name   the method name with parameters
     * @see Information#createChild(Type, String)
     */
    public MethodInformation(@NotNull ClassInformation<?> parent, @NotNull String name) {
        super(parent, name);
        if (name.indexOf('(') == -1)
            throw new IllegalArgumentException("Method name is missing parameters. Is: " + name);
    }

    /**
     * {@inheritDoc}
     */
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
