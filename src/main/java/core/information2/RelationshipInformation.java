package core.information2;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.*;
import util.MapWithListeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a versioned edge on the graph
 */
@RelationshipEntity
@SuppressWarnings("java:S1452" /* Wildcards are needed */)
public abstract class RelationshipInformation<T extends Information<?>> {
    @SuppressWarnings("rawtypes" /* Compatibility for neo4j */)
    @StartNode
    @NotNull
    private final Information from;

    @EndNode
    @NotNull
    private final T to;

    @Properties(prefix = "versionInfo")
    private final Map<String, Boolean> versionInfoInternal = new HashMap<>();

    @Transient
    private final Map<VersionInformation, Boolean> versionInfoBacking = new HashMap<>();

    @Transient
    private final MapWithListeners<VersionInformation, Boolean> versionInfo = new MapWithListeners<>(versionInfoBacking,
            (k, v) -> versionInfoInternal.put(k.getName(), v),
            (k, v) -> versionInfoInternal.remove(k.getName())
    );

    @Id
    @GeneratedValue
    private Long id;

    RelationshipInformation(@NotNull Information<?> from, @NotNull T to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Initialize versionInternal after database is loaded
     */
    @PostLoad
    private void postLoad() {
        versionInfoInternal.forEach((v, c) -> versionInfoBacking.put(new VersionInformation(v, getOwner().getProject()), c));
    }

    @NotNull
    public Information<?> getFrom() {
        return from;
    }

    @NotNull
    public T getTo() {
        return to;
    }

    /**
     * Returns the owner of the relationship. The owner of a relation is the node (which is attached to the relation) whose existence determines the existence of the relation
     */
    abstract Information<?> getOwner();

    /**
     * Whether the relation exists at given version
     */
    public final boolean exists(@NotNull VersionInformation version) {
        List<VersionInformation> versions = getOwner().getProject().getVersions();
        if (versions.isEmpty()) return false;
        Pair<VersionInformation, Boolean> latestChange = getLatestChangeInPath(versions.get(0), version);
        return latestChange == null || latestChange.getValue();
    }

    /**
     * ensures that at a given version the relation exists, setting an existence marker if not currently
     * ATTENTION: will potentially override child existences (TODO)
     */
    public final void ensureStateAt(@NotNull VersionInformation version, boolean exists) {
        if (exists(version) != exists) {
            addVersionMarker(version, exists);
        }
    }

    /**
     * add a new version marker into the version map, forcing the state
     * ATTENTION: will potentially override child existences (TODO)
     */
    public final void addVersionMarker(@NotNull VersionInformation version, boolean exists) {
        versionInfo.put(version, exists);
    }

    /**
     * returns the latest change entry in the own versionInfo map which is in the specified version range
     */
    @Nullable
    final Pair<VersionInformation, Boolean> getLatestChange(@NotNull VersionInformation fromVersionInclusive, @NotNull VersionInformation untilVersionInclusive) {
        if (fromVersionInclusive.isAfter(untilVersionInclusive))
            throw new IllegalStateException(String.format("Search range too small (startVersion >= endVersion). is: %s >= %s", fromVersionInclusive, untilVersionInclusive));

        Boolean changed;
        VersionInformation curr = untilVersionInclusive;
        do {
            changed = versionInfo.get(curr);
            if (changed != null) return Pair.of(curr, changed);
        } while ((curr = curr.previous()) != null);
        return null;
    }

    /**
     * returns the latest change entry which is relevant for this relationship, traversing from root to this
     */
    @Nullable
    final Pair<VersionInformation, Boolean> getLatestChangeInPath(@NotNull VersionInformation fromVersionInclusive, @NotNull VersionInformation untilVersionInclusive) {
        Pair<VersionInformation, Boolean> ownLatestChange = getLatestChange(fromVersionInclusive, untilVersionInclusive);
        if (getOwner().parent == null) return ownLatestChange;
        Pair<VersionInformation, Boolean> parentLatestChange = getOwner().parent.getLatestChange(fromVersionInclusive, untilVersionInclusive);
        if (parentLatestChange == null) return ownLatestChange;
        else if (ownLatestChange == null) return parentLatestChange;
        return ownLatestChange.getKey().isBefore(parentLatestChange.getKey()) ? parentLatestChange : ownLatestChange;
    }

    // Overrides

    @SuppressWarnings("java:S2159" /* wrong, types to and ro.to might be related */)
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelationshipInformation)) return false;
        RelationshipInformation<?> ro = (RelationshipInformation<?>) o;
        return from.equals(ro.from) && to.equals(ro.to) && versionInfo.equals(ro.versionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionInfo, from, to);
    }

    @Override
    public String toString() {
        String vi = versionInfo.entrySet().stream().map(e -> (Boolean.TRUE.equals(e.getValue()) ? '+' : '-') + e.getKey().toString()).collect(Collectors.joining(","));
        return "(" + from.getPath() + ")->[" + vi + "]->(" + to.getPath() + ")";
    }
}
