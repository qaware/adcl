package core.information2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import util.CompareHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Information about a project with Root as parent
 */
@NodeEntity
public class ProjectInformation extends Information<RootInformation> {
    @Convert(VersionInformation.Converter.class)
    private final List<VersionInformation> versions = new ArrayList<>();

    @Property("internal")
    private final boolean isInternal;

    @Relationship(type = "PomDependency")
    private final Set<PomDependencyInformation> pomDependencies = new HashSet<>();

    public ProjectInformation(String name, boolean isInternal) {
        super(name);
        this.isInternal = isInternal;
    }

    /**
     * Initialize version informations correctly after being loaded from database
     */
    @PostLoad
    private void postLoad() {
        versions.forEach(v -> v.postLoad(this));
    }

    /**
     * Returns all *own* Pom Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<VersionInformation> getPomDependencies(@Nullable VersionInformation at) {
        //noinspection ConstantConditions TODO work on PomDependencyInformation so verionInfo and remoteVersionMap are in sync
        return pomDependencies.stream().filter(d -> at == null || d.exists(at)).map(d -> d.getVersionAt(at)).collect(Collectors.toSet());
    }

    /**
     * Adds a new pom dependency at given version. If version is null existence will be ensured for latest version
     */
    public final void addPomDependency(@NotNull VersionInformation to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        PomDependencyInformation dep = new PomDependencyInformation(this, to.getProject());
        dep.ensureStateAt(at, true);
        dep.addVersionMarker(at, to);
        pomDependencies.add(dep);
    }

    /**
     * Whether a Project is an internal project (a project analysed by adcl)
     */
    public boolean isInternal() {
        return isInternal;
    }

    public List<VersionInformation> getVersions() {
        return versions;
    }

    /**
     * Returns the latest version information in the project
     */
    public VersionInformation getLatestVersion() {
        return versions.get(versions.size() - 1);
    }

    // Overrides

    @Override
    public @NotNull Type getType() {
        return Type.PROJECT;
    }

    @Override
    @SuppressWarnings({"java:S1206" /* final super.equals uses hashCode */})
    public int hashCode() {
        return Objects.hash(super.hashCode(), isInternal, versions);
    }

    @Override
    void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        super.compareElements(cmp);
        cmp.casted(ProjectInformation.class).add(ProjectInformation::isInternal)
                .add(ProjectInformation::getVersions, CompareHelper.collectionComparator());
    }

    @Override
    public @NotNull String getPath() {
        return getName();
    }
}
