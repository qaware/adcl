package core.information2;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import util.CompareHelper;

import java.util.*;
import java.util.stream.Collectors;

@NodeEntity
public class ProjectInformation extends Information<RootInformation> {
    @Convert(VersionInformation.Converter.class)
    public final List<VersionInformation> versions = new ArrayList<>();
    @Property("internal")
    private final boolean isInternal;
    @Relationship(type = "PomDependency")
    private final Set<PomDependencyInformation> pomDependencies = new HashSet<>();

    public ProjectInformation(String name, boolean isInternal) {
        super(name);
        this.isInternal = isInternal;
    }

    @PostLoad
    private void postLoad() {
        versions.forEach(v -> v.postLoad(this));
    }

    @NotNull
    public final Set<ProjectInformation> getPomDependencies() {
        return pomDependencies.stream().map(d -> d.to).collect(Collectors.toSet());
    }

    public final void addPomDependency(ProjectInformation to) {
        pomDependencies.add(new PomDependencyInformation(this, to));
    }

    public boolean isInternal() {
        return isInternal;
    }

    public List<VersionInformation> getVersions() {
        return versions;
    }

    public VersionInformation getLatestVersion() {
        return versions.get(versions.size() - 1);
    }

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
