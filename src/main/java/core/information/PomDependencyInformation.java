package core.information;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.Transient;
import util.MapWithListeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Like {@link ProjectDependency}, but also stores remote version names for each version
 */
@RelationshipEntity("PomDependency")
public final class PomDependencyInformation extends RelationshipInformation<ProjectInformation> {
    @Properties(prefix = "remoteVersions")
    private final Map<String, String> remoteVersionMapInternal = new HashMap<>();

    @Transient
    private final Map<VersionInformation, VersionInformation> remoteVersionMapBacking = new HashMap<>();

    @Transient
    private final MapWithListeners<VersionInformation, VersionInformation> remoteVersionMap = new MapWithListeners<>(remoteVersionMapBacking,
            (k, v) -> remoteVersionMapInternal.put(k.getName(), v.getName()),
            (k, v) -> remoteVersionMapInternal.remove(k.getName())
    );

    /**
     * Neo4j init
     */
    @SuppressWarnings("unused")
    private PomDependencyInformation() {
        super();
    }

    /**
     * Creates a new pom dependency.
     *
     * @param from the node which has the dependency
     * @param to   the dependency
     * @see ProjectInformation#addPomDependency(VersionInformation, VersionInformation)
     * @see ProjectInformation#getPomDependencies(VersionInformation)
     */
    PomDependencyInformation(@NotNull Information<?> from, @NotNull ProjectInformation to) {
        super(from, to);
    }

    /**
     * Initializes {@link PomDependencyInformation#remoteVersionMap} after database initialization
     */
    @Override
    @PostLoad
    void postLoad() {
        super.postLoad();
        remoteVersionMapInternal.forEach((v, r) -> remoteVersionMapBacking.put(new VersionInformation(v, getFrom().getProject()), new VersionInformation(r, getTo().getProject())));
    }

    /**
     * Adds a remote version marker at given version
     * @param version       the version to add the marker
     * @param remoteVersion the version this marker points to
     */
    public void addVersionMarker(@NotNull VersionInformation version, @NotNull VersionInformation remoteVersion) {
        if (!remoteVersion.getProject().equals(getTo().getProject()))
            throw new IllegalArgumentException("Project of remoteVersion (" + remoteVersion.getProject().getName() + ") does not fit to dependency project (" + getTo().getProject().toString() + ")");
        remoteVersionMap.put(version, remoteVersion);
    }

    /**
     * Retrieves a remote version marker at given version
     * @param at the version to query
     * @return the remote version
     */
    @Nullable
    public VersionInformation getVersionAt(@NotNull VersionInformation at) {
        return remoteVersionMap.get(at);
    }

    // overrides

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("java:S2159" /* wrong, types might be related */)
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        return o instanceof PomDependencyInformation && super.equals(o) && remoteVersionMap.equals(((PomDependencyInformation) o).remoteVersionMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), remoteVersionMap);
    }
}
