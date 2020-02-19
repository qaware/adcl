package com.github.qaware.adcl.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.qaware.adcl.information.Information;
import com.github.qaware.adcl.information.VersionInformation;
import com.github.qaware.adcl.util.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates a list of differences between two versions of a project
 */
public class DiffExtractor {
    @Nullable
    private final VersionInformation from;
    @NotNull
    private final VersionInformation to;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new diff extractor. Does not take version order into consideration
     *
     * @param from the start version to create a diff
     * @param to   the end version to create a diff
     */
    public DiffExtractor(@Nullable VersionInformation from, @NotNull VersionInformation to) {
        this.from = from;
        this.to = to;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * @param info              the information of whose parents should be listed
     * @param at                the version to create the dependency set
     * @param aggregateDepStart whether a dependency starting at a level should be displayed for higher levels
     * @param aggregateDepEnd   whether a dependency ending at a level should be displayed for higher levels
     * @return all dependencies starting at this node or at one of its children
     */
    @NotNull
    private static Stream<DependencyEntry> generateDependencySet(@NotNull Information<?> info, VersionInformation at, boolean aggregateDepStart, boolean aggregateDepEnd) {
        Set<Information<?>> deps = Utils.concatStreams(
                info.getMethodDependencies(at).stream(),
                info.getClassDependencies(at).stream(),
                info.getPackageDependencies(at).stream(),
                info.getProjectDependencies(at).stream()
        ).collect(Collectors.toSet());

        Stream<DependencyEntry> ownEntries = deps.stream().map(i -> // original dependencies
                new DependencyEntry(new DependencyEntry.DependencyNode(false, info), new DependencyEntry.DependencyNode(false, i))
        );

        if (aggregateDepEnd) ownEntries = Stream.concat(ownEntries, // aggregated end notes
                deps.stream().flatMap(DiffExtractor::allParents).map(i ->
                        new DependencyEntry(new DependencyEntry.DependencyNode(false, info), new DependencyEntry.DependencyNode(true, i))
                )
        );

        Stream<DependencyEntry> childEntries = info.getDirectChildren(at).stream().flatMap(i -> // children
                generateDependencySet(i, at, aggregateDepStart, aggregateDepEnd)
        );

        if (aggregateDepStart)
            childEntries = childEntries.flatMap(e -> Stream.of(e, // children with aggregated start nodes
                    new DependencyEntry(new DependencyEntry.DependencyNode(true, info), new DependencyEntry.DependencyNode(e, true))
            ));

        return Stream.concat(ownEntries, childEntries);
    }

    /**
     * @param info the information of whose parents should be listed
     * @return All parent information of parameter 'info', self not included, root not included
     */
    private static Stream<Information<?>> allParents(@NotNull Information<?> info) {
        return info.getParent().getType() == Information.Type.ROOT ? Stream.empty() : Stream.concat(allParents(info.getParent()), Stream.of(info.getParent()));
    }

    /**
     * Utility method to intersect two sets
     *
     * @param before elements present before
     * @param after  elements present after
     * @param <T>    element type
     * @return a map with added elements having value true, removed elements having value false and same elements not present
     */
    @NotNull
    private static <T> Map<T, Boolean> generateDiff(Set<T> before, Set<T> after) {
        Map<T, Boolean> result = new HashMap<>();

        Set<T> removed = new HashSet<>(before);
        removed.removeAll(after);
        removed.forEach(r -> result.put(r, false));

        Set<T> added = new HashSet<>(after);
        added.removeAll(before);
        added.forEach(a -> result.put(a, true));
        return result;
    }

    /**
     * @param aggregateDepStart whether a dependency starting at a level should be displayed for higher levels
     * @param aggregateDepEnd   whether a dependency ending at a level should be displayed for higher levels
     * @return a set describing the code differences between the specified versions in constructor
     */
    @NotNull
    public Set<DependencyEntry> generateDependencyDiff(boolean aggregateDepStart, boolean aggregateDepEnd) {
        Map<DependencyEntry, Boolean> result = generateDiff(
                from == null ? Collections.emptySet() : generateDependencySet(from.getProject(), from, aggregateDepStart, aggregateDepEnd).collect(Collectors.toSet()),
                generateDependencySet(to.getProject(), to, aggregateDepStart, aggregateDepEnd).collect(Collectors.toSet())
        );
        result.entrySet().stream().filter(Map.Entry::getValue).forEach(e -> e.getKey().change = true);
        return result.keySet();
    }
    /**
     * @param aggregateDepStart whether a dependency starting at a level should be displayed for higher levels
     * @param aggregateDepEnd   whether a dependency ending at a level should be displayed for higher levels
     * @return a set describing the code differences between the specified versions in constructor as json string
     * @throws  JsonProcessingException on json generation failure
     */
    @NotNull
    public String generateDependencyDiffAsJson(boolean aggregateDepStart, boolean aggregateDepEnd) throws JsonProcessingException {
        return  objectMapper.writeValueAsString(generateDependencyDiff(aggregateDepStart, aggregateDepEnd));
    }

    /**
     * @return a set describing the pom differences between the specified versions in constructor
     */
    @NotNull
    public Set<PomDependencyEntry> generatePomDiff() {
        return new HashSet<>(generateDiff(
                from == null ? Collections.emptySet() : from.getProject().getPomDependencies(from),
                to.getProject().getPomDependencies(to)
        ).entrySet().stream().collect(Collectors.groupingBy(e -> e.getKey().getProject().getName(),
                Collectors.reducing(null, e -> new PomDependencyEntry(e.getKey(), e.getValue()), (c, n) -> {
                    if (c != null) {
                        return c.newVersion != null ? c.flagUpdated() : n.flagUpdated();
                    } else {
                        return n;
                    }
                }))).values());
    }

    /**
     * @return a set describing the pom differences between the specified versions in constructor as json string
     * @throws JsonProcessingException on json generation failure
     */
    @NotNull
    public String generatePomDiffAsJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(generatePomDiff());
    }

    /**
     * @param aggregateDepStart whether a dependency starting at a level should be displayed for higher levels
     * @param aggregateDepEnd   whether a dependency starting at a level should be displayed for higher levels
     * @return a diff describing the differences between the specified versions in constructor
     * @throws JsonProcessingException on json generation failure
     */
    public Diff generateDiff(boolean aggregateDepStart, boolean aggregateDepEnd) throws JsonProcessingException {
        return new Diff(generateDependencyDiffAsJson(aggregateDepStart, aggregateDepEnd), generatePomDiffAsJson(), to.getProject().getName(), to.getName());
    }

    /**
     * Serialization class. Represents a change in code dependencies
     * syntheticStart / syntheticEnd describe whether this dependency entry's start / end node represents the actual dependency or represents an aggregated version of another dependency entry
     */
    public static class DependencyEntry {
        @JsonProperty("syntheticStart")
        public final boolean syntheticStart;
        @JsonProperty("usedByType")
        @NotNull
        public final Information.Type startType;
        @JsonProperty("usedByPath")
        @NotNull
        public final String startPath;
        @JsonProperty("usedByName")
        @NotNull
        public final String startName;
        @JsonProperty("syntheticEnd")
        public final boolean syntheticEnd;
        @JsonProperty("dependencyType")
        @NotNull
        public final Information.Type endType;
        @JsonProperty("dependencyPath")
        @NotNull
        public final String endPath;
        @JsonProperty("dependencyName")
        @NotNull
        public final String endName;
        @JsonProperty("changeStatus")
        public boolean change;

        private DependencyEntry(@NotNull DependencyNode from, @NotNull DependencyNode to) {
            this.syntheticStart = from.isSynthetic();
            this.startType = from.getType();
            this.startPath = from.getPath();
            this.startName = from.getName();
            this.syntheticEnd = to.isSynthetic();
            this.endType = to.getType();
            this.endPath = to.getPath();
            this.endName = to.getName();
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
            return startPath + (change ? '+' : '-') + '>' + endPath;
        }

        /**
         * * Serialization class. Represents in a code dependency
         */
        private static class DependencyNode {
            private final boolean synthetic;
            private final Information.Type type;
            private final String path;
            private final String name;

            private DependencyNode(boolean synthetic, Information.Type type, String path, String name) {
                this.synthetic = synthetic;
                this.type = type;
                this.path = path;
                this.name = name;
            }

            private DependencyNode(boolean synthetic, @NotNull Information<?> info) {
                this(synthetic, info.getType(), info.getPath(), info.getName());
            }

            /**
             * Reconstructs a node from a dependency entry
             *
             * @param end   whether to create the start or the end node
             * @param entry the Dependency entry to copy from
             */
            private DependencyNode(DependencyEntry entry, boolean end) {
                this(end ? entry.syntheticEnd : entry.syntheticStart, end ? entry.endType : entry.startType, end ? entry.endPath : entry.startPath, end ? entry.endName : entry.startName);
            }

            public boolean isSynthetic() {
                return synthetic;
            }

            public Information.Type getType() {
                return type;
            }

            public String getPath() {
                return path;
            }

            public String getName() {
                return name;
            }
        }
    }

    /**
     * Serialization class. Represents a change in pom dependencies
     */
    public static class PomDependencyEntry {
        @JsonProperty("newVersion")
        @Nullable
        public final String newVersion;
        @JsonProperty("toProject")
        @NotNull
        public final String toProject;
        @JsonProperty("updated")
        public boolean updated;

        @Contract(pure = true)
        public PomDependencyEntry(@Nullable String newVersion, @NotNull String toProject) {
            this.newVersion = newVersion;
            this.toProject = toProject;
        }

        public PomDependencyEntry(@NotNull VersionInformation version, boolean added) {
            this(added ? version.getName() : null, version.getProject().getName());
        }

        @Override
        public String toString() {
            return "-> " + newVersion + '@' + toProject;
        }

        public PomDependencyEntry flagUpdated() {
            updated = true;
            return this;
        }
    }

    /**
     * Serialization class. Represents a diff containing A diff of DependencyEntries and a diff of PomDependencyEntries
     */
    public static class Diff {
        @JsonProperty("dependencies")
        @NotNull
        public final String changedDependencies;

        @JsonProperty("pomDependencies")
        @NotNull
        public final String changedPomDependencies;

        @JsonProperty("projectName")
        @NotNull
        public final String projectName;

        @JsonProperty("projectVersion")
        @NotNull
        public final String projectVersion;

        public Diff(@JsonProperty("dependencies") @NotNull String changedDependencies,  @JsonProperty("pomDependencies") @NotNull String changedPomDependencies, @NotNull  @JsonProperty("projectName") String projectName, @NotNull @JsonProperty("projectVersion") String projectVersion) {
            this.changedDependencies = changedDependencies;
            this.changedPomDependencies = changedPomDependencies;
            this.projectName  = projectName;
            this.projectVersion = projectVersion;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Diff.class.getSimpleName() + "[", "]")
                    .add("changedDependencies=" + changedDependencies)
                    .add("changedPomDependencies=" + changedPomDependencies)
                    .toString();
        }
    }
}
