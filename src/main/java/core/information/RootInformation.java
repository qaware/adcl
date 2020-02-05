package core.information;

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

    /**
     * Creates a new root information
     */
    public RootInformation() {
        super("");
    }

    /**
     * @return the current data model version
     */
    public int getModelVersion() {
        return modelVersion;
    }

    /**
     * @param at the version to check. If null children at any time are returned.
     * @return the direct children of the node at given version. Direct children are represented by an incoming parent edge in the graph.
     * @see Information#getDirectChildren(VersionInformation)
     * @see Information#find(Class, VersionInformation)
     */
    @NotNull
    public final Set<ProjectInformation> getProjects(@Nullable VersionInformation at) {
        return find(ProjectInformation.class, at);
    }

    // Overrides

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Type getType() {
        return Type.ROOT;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public RootInformation getParent() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public <T> T getParent(@NotNull Class<T> parentType) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(@NotNull VersionInformation version) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExists(@NotNull VersionInformation at, boolean exists) {
        if (!exists) errCallOnExistence();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull VersionInformation firstExistence() {
        return errCallOnExistence();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull VersionInformation lastExistence() {
        return errCallOnExistence();
    }

    /**
     * Error helper method. Call if existence operation is not logical for root
     *
     * @param <T> ignored (will throw)
     * @return nothing (will throw)
     */
    private <T> T errCallOnExistence() {
        throw new UnsupportedOperationException("Root always exists");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("java:S1206" /* final super.equals uses hashCode */)
    public int hashCode() {
        return Objects.hash(super.hashCode(), modelVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.casted(RootInformation.class).add(RootInformation::getModelVersion);
        super.compareElements(cmp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent(@NotNull Information<?> potentialParent) {
        return equals(potentialParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ProjectInformation getProject() {
        throw new UnsupportedOperationException("root is super project");
    }
}
