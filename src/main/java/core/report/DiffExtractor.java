package core.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.information.Information;
import core.information.VersionInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiffExtractor {
    @Nullable
    private final VersionInformation from;
    @NotNull
    private final VersionInformation to;

    public DiffExtractor(@Nullable VersionInformation from, @NotNull VersionInformation to) {
        this.from = from;
        this.to = to;
    }

    @NotNull
    private Stream<DependencyEntry> generateDependencySet(VersionInformation at, @NotNull Information<?> info) {
        Set<Information<?>> deps = Utils.concatStreams(
                info.getMethodDependencies(at).stream(),
                info.getClassDependencies(at).stream(),
                info.getPackageDependencies(at).stream(),
                info.getProjectDependencies(at).stream()
        ).collect(Collectors.toSet());
        return Utils.concatStreams(
                Utils.concatStreams(
                        deps.stream().map(i -> new DependencyEntry(false, info, i)), // original dependencies
                        deps.stream().flatMap(this::allParents).map(i -> new DependencyEntry(true, info, i)), // aggregated end notes
                        info.getDirectChildren(at).stream().flatMap(i -> generateDependencySet(at, i)).flatMap(e -> Stream.of(e, new DependencyEntry(info, e))) // children plus children with aggregated start nodes
                ));
    }

    private Stream<Information<?>> allParents(@NotNull Information<?> info) {
        return info.getParent().getType() == Information.Type.ROOT ? Stream.empty() : Stream.concat(allParents(info.getParent()), Stream.of(info.getParent()));
    }

    @NotNull
    private Set<DependencyEntry> generateChangelist() {
        Set<DependencyEntry> before = from == null ? Collections.emptySet() : generateDependencySet(from, to.getProject()).collect(Collectors.toSet());
        Set<DependencyEntry> after = generateDependencySet(to, to.getProject()).collect(Collectors.toSet());
        Set<DependencyEntry> removed = new HashSet<>(before);
        removed.removeAll(after);
        Set<DependencyEntry> added = new HashSet<>(after);
        added.removeAll(before);
        added.forEach(de -> de.change = true);
        removed.addAll(added);
        return removed;
    }

    public String generateJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(generateChangelist());
    }

    public static class DependencyEntry {
        @JsonProperty("syntheticStart")
        final boolean syntheticStart;
        @JsonProperty("syntheticEnd")
        final boolean syntheticEnd;
        @JsonProperty("usedByType")
        @NotNull
        final Information.Type startType;
        @JsonProperty("usedByPath")
        @NotNull
        final String startPath;
        @JsonProperty("usedByName")
        @NotNull
        final String startName;
        @JsonProperty("dependencyType")
        @NotNull
        final Information.Type endType;
        @JsonProperty("dependencyPath")
        @NotNull
        final String endPath;
        @JsonProperty("dependencyName")
        @NotNull
        final String endName;
        @JsonProperty("changeStatus")
        boolean change;

        private DependencyEntry(boolean syntheticEnd, @NotNull Information<?> from, @NotNull Information<?> to) {
            this.syntheticStart = false;
            this.syntheticEnd = syntheticEnd;
            this.startType = from.getType();
            this.startPath = from.getPath();
            this.startName = from.getName();
            this.endType = to.getType();
            this.endPath = to.getPath();
            this.endName = to.getName();
        }

        @NotNull
        private DependencyEntry(@NotNull Information<?> from, @NotNull DependencyEntry to) {
            this.syntheticStart = true;
            this.syntheticEnd = false;
            this.startType = from.getType();
            this.startPath = from.getPath();
            this.startName = from.getName();
            this.endType = to.endType;
            this.endPath = to.endPath;
            this.endName = to.endName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DependencyEntry)) return false;
            DependencyEntry that = (DependencyEntry) o;
            return startPath.equals(that.startPath) && endPath.equals(that.endPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startPath, endPath);
        }

        @Override
        public String toString() {
            return startPath + "->" + endPath;
        }
    }
}
