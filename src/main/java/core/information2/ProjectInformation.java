package core.information2;

import core.IndexBuilder;
import core.PomDependencyReader;
import org.apache.maven.shared.invoker.MavenInvocationException;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
     * Initialize version informations correctly after being loaded from database
     */
    @PostLoad
    private void postLoad() {
        versions.forEach(v -> v.postLoad(this));
    }

    /**
     * Add a new version for the project to the end of the version history
     */
    @NotNull
    public VersionInformation addVersion(@NotNull String name) {
        VersionInformation result = new VersionInformation(name, this);
        versions.add(result);
        return result;
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
        dep.setExists(at, true);
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

    @Nullable
    public String resolveProjectByClassName(@NotNull String className) {
        return externalIndices.getOrDefault(className, "null");
    }

    public void updateIndices(Path pathToOwnFiles) throws MavenInvocationException, IOException {
        externalIndices.clear();
        IndexBuilder.index(pathToOwnFiles, getName(), externalIndices);
        new PomDependencyReader(Paths.get("pom.xml")).readAllCompilationRelevantDependencies().forEach(d -> {
            try {
                IndexBuilder.index(d, externalIndices);
            } catch (IOException e) {
                LOGGER.error("Could not index dependency {}", d, e);
            }
        });
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
