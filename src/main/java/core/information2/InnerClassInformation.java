package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Class Information with another Class Information as parent
 */
@NodeEntity
public class InnerClassInformation extends ClassInformation<ClassInformation<?>> {
    public InnerClassInformation(ClassInformation<?> parent, String name, boolean isService) {
        super(parent, name, isService);
    }

    @Override
    public @NotNull String getPath() {
        return getParent().getPath() + '$' + getName();
    }
}
