package com.github.qaware.adcl.information;

import com.github.qaware.adcl.database.Purgeable;
import com.github.qaware.adcl.util.MapTool;
import com.github.qaware.adcl.util.MapWithListeners;
import com.github.qaware.adcl.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Like {@link ProjectDependency}, but also stores remote version names for each version
 */
@RelationshipEntity("PomDependency")
public final class PomDependencyInformation implements Purgeable {
    @StartNode
    @NotNull
    private final ProjectInformation from;

    @EndNode
    @NotNull
    private final ProjectInformation to;

    @Properties(prefix = "remoteVersions")
    private final Map<String, Object> remoteVersionMapInternal = new HashMap<>();

    @Transient
    private final Map<VersionInformation, VersionInformation> remoteVersionMapBacking = new HashMap<>();

    @Transient
    private final MapWithListeners<VersionInformation, VersionInformation> remoteVersionMap = new MapWithListeners<>(remoteVersionMapBacking,
            (k, v) -> remoteVersionMapInternal.put(k.getName(), v == null ? "null" : v.getName()),
            (k, v) -> remoteVersionMapInternal.remove(k.getName())
    );

    @Id
    @GeneratedValue
    private Long id;

    /**
     *Needed for neo4j initialization
     */
    @SuppressWarnings({"unused", "java:S2637", "ConstantConditions"} /* neo4jInit */)
    PomDependencyInformation() {
        this.from = null;
        this.to = null;
    }

    /**
     * Creates a new pom dependency. This dependency has to be added to a project
     *
     * @param from the node which has the dependency
     * @param to   the dependency
     * @see ProjectInformation#addPomDependency(VersionInformation, VersionInformation)
     * @see ProjectInformation#getPomDependencies(VersionInformation)
     */
    PomDependencyInformation(@NotNull ProjectInformation from, @NotNull VersionInformation to) {
        this.from = from;
        this.to = to.getProject();
    }

    public PomDependencyInformation(@NotNull RootInformation root, @NotNull PomDependencyInformation dep) {
        from = (ProjectInformation) root.findOrCreate(dep.from);
        to = (ProjectInformation) root.findOrCreate(dep.to);
        remoteVersionMapInternal.putAll(dep.remoteVersionMapInternal);
        postLoad();
    }

    /**
     * @return the edge start
     * @see PomDependencyInformation#getTo()
     */
    @NotNull
    public ProjectInformation getFrom() {
        return from;
    }

    /**
     * @return the edge end
     * @see PomDependencyInformation#getFrom()
     */
    @NotNull
    public ProjectInformation getTo() {
        return to;
    }

    /**
     * Initializes {@link PomDependencyInformation#remoteVersionMap} after database initialization
     */
    @PostLoad
    void postLoad() {
        new MapTool<>(Utils.resolveNestedMaps(String.class, null, remoteVersionMapInternal)).mapKeys(k -> new VersionInformation(k, from.getProject())).mapValues(v -> v.equals("null") ? null : new VersionInformation(v, to.getProject())).overrideTo(remoteVersionMapBacking);
    }

    /**
     * ensured that at a given version the dependency points to parameter 'aim', setting a marker if needed
     *
     * @param version the version to potentially set the aim if needed
     * @param aim     the aimed remote version
     * @see RelationshipInformation#setExists(VersionInformation, boolean) same logic
     */
    public void setVersionAt(@NotNull VersionInformation version, @Nullable VersionInformation aim) {
        if (!Objects.equals(getVersionAt(version), aim)) {
            if (remoteVersionMap.containsKey(version) && Objects.equals(getVersionAt(version.previous()), aim)) {
                remoteVersionMap.remove(version);
            } else {
                remoteVersionMap.put(version, aim);
            }
        }
    }

    /**
     * Retrieves a remote version marker at given version
     *
     * @param at the version to query. If null latest version will be queried
     * @return the remote version the project depends at that version
     * @see RelationshipInformation#exists(VersionInformation) same logic
     */
    @Nullable
    public VersionInformation getVersionAt(@Nullable VersionInformation at) {
        VersionInformation curr = at == null ? from.getLatestVersion() : at;
        do {
            if (remoteVersionMap.containsKey(curr)) return remoteVersionMap.get(curr);
        } while ((curr = curr.previous()) != null);
        return null;
    }

    // overrides

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PomDependencyInformation)) return false;
        PomDependencyInformation ro = (PomDependencyInformation) o;
        return from.equals(ro.from) && to.equals(ro.to) && remoteVersionMap.equals(ro.remoteVersionMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(remoteVersionMap, from, to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purgeIds() {
        id = null;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String toString() {
        String vi = remoteVersionMap.entrySet().stream().map(e -> e.getKey().getName() + "->" + (e.getValue() == null ? "removed" : e.getValue().getName())).collect(Collectors.joining(","));
        return "(" + from.getPath() + ")->[" + vi + "]->(" + to.getPath() + ")";
    }
}
