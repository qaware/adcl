package core.information2;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.Transient;
import util.MapWithListeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RelationshipEntity("PomDependency")
public class PomDependencyInformation extends DependencyInformation<ProjectInformation> {
    @Properties(prefix = "remoteVersions")
    private final Map<String, String> remoteVersionMapInternal = new HashMap<>();

    @Transient
    private final Map<VersionInformation, VersionInformation> remoteVersionMapBacking = new HashMap<>();

    @Transient
    private final MapWithListeners<VersionInformation, VersionInformation> remoteVersionMap = new MapWithListeners<>(remoteVersionMapBacking,
            (k, v) -> remoteVersionMapInternal.put(k.getName(), v.getName()),
            (k, v) -> remoteVersionMapInternal.remove(k.getName())
    );

    PomDependencyInformation(@NotNull Information<?> from, @NotNull ProjectInformation to) {
        super(from, to);
    }

    @PostLoad
    private void postLoad() {
        remoteVersionMapInternal.forEach((v, r) -> remoteVersionMapBacking.put(new VersionInformation(v, from.getProject()), new VersionInformation(r, to.getProject())));
    }

    public void addVersionMarker(VersionInformation version, VersionInformation remoteVersion) {
        remoteVersionMap.put(version, remoteVersion);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (!((o instanceof PomDependencyInformation) && super.equals(o))) return false;
        return remoteVersionMap.equals(((PomDependencyInformation) o).remoteVersionMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), remoteVersionMap);
    }
}
