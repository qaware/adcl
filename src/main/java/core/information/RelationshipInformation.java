package core.information;

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
 * Can be extended only once (in means of hierarchy depth) due to restrictions in neo4j
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

    /**
     * Neo4j init
     */
    @SuppressWarnings({"unused", "java:S2637", "ConstantConditions"} /* neo4jInit */)
    RelationshipInformation() {
        this.from = null;
        this.to = null;
    }

    /**
     * Creates a new RelationshipInformation. Direction of the edge has no meaning in base class but can be given a meaning by subclasses
     *
     * @param from the start of the directed edge
     * @param to   the end of the directed edge
     */
    RelationshipInformation(@NotNull Information<?> from, @NotNull T to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Initializes {@link RelationshipInformation#versionInfo} after database is loaded
     */
    @PostLoad
    void postLoad() {
        versionInfoInternal.forEach((v, c) -> versionInfoBacking.put(new VersionInformation(v, getOwner().getProject()), c));
    }

    /**
     * @return the edge start
     * @see RelationshipInformation#getTo()
     */
    @NotNull
    public final Information<?> getFrom() {
        return from;
    }

    /**
     * @return the edge end
     * @see RelationshipInformation#getFrom()
     */
    @NotNull
    public final T getTo() {
        return to;
    }

    /**
     * @return the owner of the relationship. The owner of a relation is the node (which is attached to the relation) whose existence determines the existence of the relation
     * @see RelationshipInformation#getAim()
     */
    @NotNull
    Information<?> getOwner() {
        return getFrom();
    }

    /**
     * @return the aim of the relationship. The aim of a relation is the opposite of the {@linkplain RelationshipInformation#getOwner() owner}
     * @see RelationshipInformation#getOwner()
     */
    @NotNull
    Information<?> getAim() {
        return getTo();
    }

    /**
     * @param version the version to check
     * @return whether the relation exists at given version
     * @see Information#exists(VersionInformation)
     */
    public final boolean exists(@NotNull VersionInformation version) {
        List<VersionInformation> versions = getOwner().getProject().getVersions();
        if (versions.isEmpty()) return false;
        Pair<VersionInformation, Boolean> latestChange = getLatestChangeInPath(versions.get(0), version);
        return latestChange == null || latestChange.getValue();
    }

    /**
     * ensures that at a given version the relation exists, setting an existence marker if not currently
     * default implementation will override child existences
     *
     * @param version the version to potentially set the existence if needed
     * @param aim the aimed existence value
     * @see Information#setExists(VersionInformation, boolean)
     */
    public void setExists(@NotNull VersionInformation version, boolean aim) {
        boolean curr = exists(version);
        if (aim != curr) {
            VersionInformation previous = version.previous();
            if (previous != null && exists(previous) == aim) {
                // just modify existence so previous is restored
                if (versionInfo.containsKey(version)) {
                    versionInfo.remove(version);
                } else {
                    getOwner().setExists(version, aim);
                }
            } else {
                versionInfo.put(version, aim);
            }
        }
    }

    /**
     * ensures that at a given version the relation exists, setting an existence marker if not currently.
     * Does not look whether the existence can be set by modifying the owner
     *
     * @param version the version to potentially set the existence if needed
     * @param aim     the aimed existence value
     */
    void setExistsNoInheritanceCheck(@NotNull VersionInformation version, boolean aim) {
        boolean curr = exists(version);
        if (aim != curr) versionInfo.put(version, aim);
    }

    /**
     * @param fromVersionInclusive the start version of the version range to check, inclusive
     * @param untilVersionInclusive the end version of the version range to check, inclusive
     * @return the latest change entry in the own versionInfo map which is in the specified version range
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
     * @param fromVersionInclusive the start version of the version range to check, inclusive
     * @param untilVersionInclusive the end version of the version range to check, inclusive
     * @return the latest change entry which is relevant for this relationship, traversing from root to this
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

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("java:S2159" /* wrong, types to and ro.to might be related */)
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelationshipInformation)) return false;
        RelationshipInformation<?> ro = (RelationshipInformation<?>) o;
        return from.equals(ro.from) && to.equals(ro.to) && versionInfo.equals(ro.versionInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(versionInfo, from, to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String vi = versionInfo.entrySet().stream().map(e -> (Boolean.TRUE.equals(e.getValue()) ? '+' : '-') + e.getKey().toString()).collect(Collectors.joining(","));
        return "(" + from.getPath() + ")->[" + vi + "]->(" + to.getPath() + ")";
    }
}
