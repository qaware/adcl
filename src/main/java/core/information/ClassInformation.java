package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import util.CompareHelper;

import java.util.Objects;

/**
 * A Class Information
 */
@NodeEntity
public abstract class ClassInformation<P extends Information<?>> extends Information<P> {
    @Property
    private boolean isService;

    ClassInformation() {
        super();
    }

    ClassInformation(@NotNull P parent, @NotNull String name, boolean isService) {
        super(parent, name);
        this.isService = isService;
    }

    /**
     * Whether the class is a service (annotated with @Service)
     */
    public boolean isService() {
        return isService;
    }

    public void setIsService(boolean isService) {
        this.isService = isService;
    }

    // Overrides

    @Override
    public @NotNull Type getType() {
        return Type.CLASS;
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
