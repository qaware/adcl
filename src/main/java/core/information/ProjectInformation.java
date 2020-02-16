package core.information;

import core.IndexBuilder;
import core.database.Purgeable;
import core.pm.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CompareHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Information about a project with Root as parent
 */
@NodeEntity
public class ProjectInformation extends Information<RootInformation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectInformation.class);

    @Convert(VersionInformation.Converter.class)
    private final List<VersionInformation> versions = new ArrayList<>();

    @Property("internal")
    private final boolean isInternal;

    @Relationship(type = "PomDependency")
    private final Set<PomDependencyInformation> pomDependencies = new HashSet<>();

    @Transient
    @Properties
    private final Map<String, String> externalIndices = new HashMap<>();

    @SuppressWarnings("unused")
    private ProjectInformation() {
        super();
        isInternal = false;
    }

    public ProjectInformation(@NotNull RootInformation root, @NotNull String name, boolean isInternal, @NotNull String initialVersion) {
        super(root, name);
        this.isInternal = isInternal;
        addVersion(initialVersion);
    }

    /**
     * Initializes versions correctly after being loaded from database
     */
    @PostLoad
    void postLoad() {
        versions.forEach(v -> v.postLoad(this));
        if (versions.isEmpty()) throw new IllegalStateException("project loaded with no versions");
    }

    /**
     * @return whether a Project is an internal project (a project analysed by adcl)
     */
    public boolean isInternal() {
        return isInternal;
    }

    /**
     * Returns all *own* Pom Dependencies at a given version.
     *
     * @param at The version to check.
     * @return the pom dependencies at given version
     */
    @NotNull
    public final Set<VersionInformation> getPomDependencies(@NotNull VersionInformation at) {
        return pomDependencies.stream().map(d -> d.getVersionAt(at)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public final Set<PomDependencyInformation> getPomDependenciesRaw() {
        return pomDependencies;
    }

    /**
     * Adds a new pom dependency at given version.
     *
     * @param at the version at which the dependency is to be added. If null existence will be ensured for latest version
     * @param to the version the new dependency should point to
     */
    public final void addPomDependency(@NotNull VersionInformation to, @Nullable VersionInformation at) {
        VersionInformation fAt = at == null ? getProject().getLatestVersion() : at;
        PomDependencyInformation dep = pomDependencies.stream().filter(d -> d.getTo().equals(to.getProject())).findAny().orElseGet(() -> {
            PomDependencyInformation result = new PomDependencyInformation(this, to);
            pomDependencies.add(result);
            return result;
        });
        dep.setVersionAt(fAt, to);
    }

    @NotNull
    public List<VersionInformation> getVersions() {
        return versions;
    }

    /**
     * @return the latest version information in the project
     */
    @NotNull
    public VersionInformation getLatestVersion() {
        return versions.get(versions.size() - 1);
    }

    /**
     * @param name the name of the requested versionInformation
     * @return the corresponding version to that name, if exists
     */
    @Nullable
    public VersionInformation getVersion(String name) {
        return versions.stream().filter(v -> v.getName().equals(name)).findAny().orElse(null);
    }

    /**
     * Add a new version for the project to the end of the version history
     *
     * @param name the name of the new version
     * @return the newly added version
     */
    @NotNull
    public VersionInformation addVersion(@NotNull String name) {
        if (getVersion(name) != null) throw new IllegalArgumentException("Version " + name + " already exists");
        VersionInformation result = new VersionInformation(name, this);
        versions.add(result);
        return result;
    }

    public VersionInformation getOrCreateVersion(String name) {
        VersionInformation result = getVersion(name);
        return result == null ? addVersion(name) : result;
    }

    @Nullable
    public String resolveProjectByClassName(@NotNull String className) {
        return externalIndices.getOrDefault(className, "null");
    }

    public void updateIndices(Path projectOutput, ProjectManager projectManager) throws IOException {
        externalIndices.clear();
        IndexBuilder.indexDirectory(projectOutput, getName(), externalIndices);
        if (projectManager != null) {
            projectManager.getCompileDependencies().forEach((d, p) -> {
                try {
                    IndexBuilder.index(d, p, externalIndices);
                } catch (IOException e) {
                    LOGGER.error("Could not index dependency {}", d, e);
                }
            });
        }
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
    protected void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        super.compareElements(cmp);
        cmp.casted(ProjectInformation.class).add(ProjectInformation::isInternal)
                .add(ProjectInformation::getVersions, CompareHelper.collectionComparator());
    }

    @Override
    public void purgeIds() {
        super.purgeIds();
        pomDependencies.forEach(Purgeable::purgeIds);
    }

    @Override
    public Stream<Purgeable> getOutgoingRelations() {
        return Stream.concat(super.getOutgoingRelations(), pomDependencies.stream());
    }
}
