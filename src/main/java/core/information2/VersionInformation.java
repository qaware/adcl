package core.information2;

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

    private ProjectInformation project;

    public VersionInformation(@NotNull String name, @NotNull ProjectInformation project) {
        this.name = name;
        this.project = project;
    }

    private VersionInformation(@NotNull String name) {
        this.name = name;
        this.project = null;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public ProjectInformation getProject() {
        assert project != null;
        return project;
    }

    @Nullable
    public VersionInformation next() {
        assert project != null;
        int index = project.getVersions().indexOf(this);
        return (index >= project.getVersions().size() - 1) ? null : project.getVersions().get(index + 1);
    }

    @Nullable
    public VersionInformation previous() {
        assert project != null;
        int index = project.getVersions().indexOf(this);
        return (index <= 0) ? null : project.getVersions().get(index - 1);
    }

    public boolean isBefore(@NotNull VersionInformation o) {
        return compareTo(o) > 0;
    }

    public boolean isAfter(@NotNull VersionInformation o) {
        return compareTo(o) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VersionInformation)) return false;
        VersionInformation vi = (VersionInformation) o;
        return name.equals(vi.name) && Objects.equals(project, vi.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, project);
    }

    @Override
    public int compareTo(@NotNull VersionInformation o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return project.getName() + '@' + name;
    }

    void postLoad(ProjectInformation project) {
        this.project = project;
    }

    public static class Converter implements AttributeConverter<List<VersionInformation>, String[]> {
        @Override
        public String[] toGraphProperty(@NotNull List<VersionInformation> value) {
            return value.stream().map(VersionInformation::getName).toArray(String[]::new);
        }

        @Override
        public List<VersionInformation> toEntityAttribute(String[] value) {
            return Stream.of(value).map(VersionInformation::new).collect(Collectors.toList());
        }
    }
}
