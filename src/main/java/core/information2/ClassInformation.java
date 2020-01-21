package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import util.CompareHelper;

import java.util.Objects;

@NodeEntity
public abstract class ClassInformation<P extends Information<?>> extends Information<P> {
    @Property
    private final boolean isService;

    ClassInformation(P parent, String name, boolean isService) {
        super(parent, name);
        this.isService = isService;
    }

    @Override
    public @NotNull Type getType() {
        return Type.CLASS;
    }

    public boolean isService() {
        return isService;
    }

    @Override
    @SuppressWarnings("java:S1206" /* final super.equals uses hashCode */)
    public int hashCode() {
        return Objects.hash(super.hashCode(), isService);
    }

    @Override
    void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        super.compareElements(cmp);
        cmp.casted(ClassInformation.class).add(ClassInformation::isService);
    }
}
