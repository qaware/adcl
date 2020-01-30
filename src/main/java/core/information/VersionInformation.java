package core.information;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VersionInformation implements Comparable<VersionInformation> {
    @NotNull
    private final String name;

    @Nullable
    private ProjectInformation project;

    /**
     * Creates a new Version Information. Can be done safely
     *
     * @param name    the version name
     * @param project the corresponding project
     * @see ProjectInformation#addVersion(String)
     * @see ProjectInformation#getVersions()
     * @see ProjectInformation#getLatestVersion()
     */
    public VersionInformation(@NotNull String name, @NotNull ProjectInformation project) {
        this.name = name;
        this.project = project;
    }

    /**
     * For {@link Converter} only. Project has to be {@linkplain VersionInformation#postLoad(ProjectInformation) postloaded}
     *
     * @param name the version name
     * @see VersionInformation#postLoad(ProjectInformation)
     */
    private VersionInformation(@NotNull String name) {
        this.name = name;
        this.project = null;
    }

    /**
     * @return the version name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * @return the corresponding project
     */
    @NotNull
    public ProjectInformation getProject() {
        assert project != null;
        return project;
    }

    /**
     * @return the next version in the project version history or null if this is the latest version
     */
    @Nullable
    public VersionInformation next() {
        assert project != null;
        int index = project.getVersions().indexOf(this);
        return (index >= project.getVersions().size() - 1) ? null : project.getVersions().get(index + 1);
    }

    /**
     * @return the previous version in the project version history or null if this is the first version
     */
    @Nullable
    public VersionInformation previous() {
        assert project != null;
        int index = project.getVersions().indexOf(this);
        return (index <= 0) ? null : project.getVersions().get(index - 1);
    }

    /**
     * @param o the version to compare to
     * @return whether the version is chronologically before the given version
     */
    public boolean isBefore(@NotNull VersionInformation o) {
        return compareTo(o) < 0;
    }

    /**
     * @param o the version to compare to
     * @return whether the version is chronologically after the given version
     */
    public boolean isAfter(@NotNull VersionInformation o) {
        return compareTo(o) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VersionInformation)) return false;
        VersionInformation vi = (VersionInformation) o;
        return name.equals(vi.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@NotNull VersionInformation o) {
        if (o == this) return 0;
        if (project == null || o.project == null)
            return -1; //Objects while being loaded from database are different to each other
        if (Objects.equals(project.getName(), o.project.getName())) {
            return Integer.compare(project.getVersions().indexOf(this), project.getVersions().indexOf(o));
        } else {
            throw new UnsupportedOperationException("Comparing versions of different projects " + project.getName() + " and " + o.project.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (project == null ? "<uninitialized>" : project.getName()) + '@' + name;
    }

    /**
     * Registers the project, to be called when created using {@link Converter}
     *
     * @param project the project that should be attached to this
     * @see Converter
     */
    void postLoad(ProjectInformation project) {
        this.project = project;
    }

    /**
     * A {@link AttributeConverter} for Version lists
     * When using this converter make sure to {@linkplain org.neo4j.ogm.annotation.PostLoad postload} the project with {@link VersionInformation#postLoad(ProjectInformation)}
     */
    public static class Converter implements AttributeConverter<List<VersionInformation>, String[]> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String[] toGraphProperty(@NotNull List<VersionInformation> value) {
            return value.stream().map(VersionInformation::getName).toArray(String[]::new);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<VersionInformation> toEntityAttribute(String[] value) {
            return Stream.of(value).map(VersionInformation::new).collect(Collectors.toList());
        }
    }
}
