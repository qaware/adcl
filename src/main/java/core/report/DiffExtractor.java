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

/**
 * Generates a list of differences between two versions of a project
 */
public class DiffExtractor {
    @Nullable
    private final VersionInformation from;
    @NotNull
    private final VersionInformation to;

    /**
     * Creates a new diff extractor. Does not take version order into consideration
     *
     * @param from the start version to create a diff
     * @param to   the end version to create a diff
     */
    public DiffExtractor(@Nullable VersionInformation from, @NotNull VersionInformation to) {
        this.from = from;
        this.to = to;
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
     * @param aggregateDepStart whether a dependency starting at a level should be displayed for higher levels
     * @param aggregateDepEnd   whether a dependency ending at a level should be displayed for higher levels
     * @return a set describing the differences between the specified versions in constructor
     */
    @NotNull
    public Set<DependencyEntry> generateChangelist(boolean aggregateDepStart, boolean aggregateDepEnd) {
        Set<DependencyEntry> before = from == null ? Collections.emptySet() : generateDependencySet(to.getProject(), from, aggregateDepStart, aggregateDepEnd).collect(Collectors.toSet());
        Set<DependencyEntry> after = generateDependencySet(to.getProject(), to, aggregateDepStart, aggregateDepEnd).collect(Collectors.toSet());
        Set<DependencyEntry> removed = new HashSet<>(before);
        removed.removeAll(after);
        Set<DependencyEntry> added = new HashSet<>(after);
        added.removeAll(before);
        added.forEach(de -> de.change = true);
        removed.addAll(added);
        return removed;
    }

    /**
     * @param aggregateDepStart whether a dependency starting at a level should be displayed for higher levels
     * @param aggregateDepEnd   whether a dependency ending at a level should be displayed for higher levels
     * @return a json serialized form of {@link DiffExtractor#generateChangelist(boolean, boolean)}
     * @throws JsonProcessingException on json generation failure
     * @see DiffExtractor#generateChangelist(boolean, boolean)
     */
    public String generateJson(boolean aggregateDepStart, boolean aggregateDepEnd) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(generateChangelist(aggregateDepStart, aggregateDepEnd));
    }

    /**
     * Serialization class
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
}
