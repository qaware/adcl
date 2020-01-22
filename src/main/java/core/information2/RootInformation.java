package core.information2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.Property;
import util.CompareHelper;

import java.util.Objects;
import java.util.Set;

/**
 * The database root element. ModelVersion holds the current database model version. Children have to be ProjectInformation
 */
public class RootInformation extends Information<RootInformation> {
    @SuppressWarnings("java:S1170" /* has to be stored in database */)
    @Property
    private final int modelVersion = 2;

    public RootInformation() {
        super("");
    }

    public int getModelVersion() {
        return modelVersion;
    }

    /**
     * See {@link Information#getDirectChildren(VersionInformation)}
     */
    @NotNull
    public final Set<ProjectInformation> getProjects(@Nullable VersionInformation at) {
        return find(ProjectInformation.class, at);
    }

    // Overrides

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
    public @NotNull String getPath() {
        return "";
    }

    @Override
    public boolean exists(@NotNull VersionInformation version) {
        return true;
    }

    @Override
    @SuppressWarnings("java:S1206" /* final super.equals uses hashCode */)
    public int hashCode() {
        return Objects.hash(super.hashCode(), modelVersion);
    }

    @Override
    void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.casted(RootInformation.class).add(RootInformation::getModelVersion);
        super.compareElements(cmp);
    }

    @Override
    public boolean hasParent(@NotNull Information<?> potentialParent) {
        return equals(potentialParent);
    }

    @Override
    public @NotNull ProjectInformation getProject() {
        throw new UnsupportedOperationException();
    }
}
