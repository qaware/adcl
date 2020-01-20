package core.information2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.*;
import util.CompareHelper;
import util.DeepComparable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO adapt calls for search for given version
@SuppressWarnings({"unused", "java:S1452" /* Wildcards are needed */})
@NodeEntity
public abstract class Information<P extends Information<?>> implements Comparable<Information<?>>, DeepComparable<Information<?>> {
    @Relationship(type = "Parent")
    @Nullable
    final ParentInformation<P> parent;
    @Property
    @NotNull
    private final String name;
    @Transient
    private final CompareHelper<Information<?>> comparator = new CompareHelper<>();
    @Transient
    private final CompareHelper<Information<?>> deepComparator = new CompareHelper<>();
    @Relationship(type = "Parent", direction = Relationship.INCOMING)
    @NotNull Set<ParentInformation<?>> directChildren = new HashSet<>();
    @Id
    @GeneratedValue
    @Nullable
    private Long id;
    @Relationship(type = "Dependency")
    @NotNull
    private Set<DependencyInformation<ProjectInformation>> projectDependencies = new HashSet<>();
    @Relationship(type = "Dependency")
    @NotNull
    private Set<DependencyInformation<PackageInformation<?>>> packageDependencies = new HashSet<>();
    @Relationship(type = "Dependency")
    @NotNull
    private Set<DependencyInformation<ClassInformation<?>>> classDependencies = new HashSet<>();
    @Relationship(type = "Dependency")
    @NotNull
    private Set<DependencyInformation<MethodInformation>> methodDependencies = new HashSet<>();

    public Information(@NotNull P parent, @NotNull String name) {
        this.name = name;
        this.parent = new ParentInformation<>(this, parent);
        parent.directChildren.add(this.parent);
        initializeComparators();
    }

    /**
     * Needs to override getParent and exists if used
     */
    Information(@NotNull String name) {
        this.name = name;
        this.parent = null;
        initializeComparators();
    }

    @NotNull
    public final String getName() {
        return name;
    }

    @NotNull
    public abstract Type getType();

    @NotNull
    public final String getPath() {
        return (getParent() == this ? "" : getParent().getPath() + '.') + getName();
    }

    public boolean exists(VersionInformation version) {
        assert parent != null;
        return parent.exists(version);
    }

    public final ProjectInformation getProject() {
        Information<?> result = this;
        while (!(result instanceof ProjectInformation)) result = result.getParent();
        return (ProjectInformation) result;
    }

    ////////// DEPS //////////

    @NotNull
    public final Set<ProjectInformation> getProjectDependencies() {
        return projectDependencies.stream().map(d -> d.to).collect(Collectors.toSet());
    }

    public final void addProjectDependency(ProjectInformation to) {
        projectDependencies.add(new DependencyInformation<>(this, to));
    }

    @NotNull
    public final Set<PackageInformation<?>> getPackageDependencies() {
        return packageDependencies.stream().map(d -> d.to).collect(Collectors.toSet());
    }

    public final void addPackageDependency(PackageInformation<?> to) {
        packageDependencies.add(new DependencyInformation<>(this, to));
    }

    @NotNull
    public final Set<ClassInformation<?>> getClassDependencies() {
        return classDependencies.stream().map(d -> d.to).collect(Collectors.toSet());
    }

    public final void addClassDependency(ClassInformation<?> to) {
        classDependencies.add(new DependencyInformation<>(this, to));
    }

    @NotNull
    public final Set<MethodInformation> getMethodDependencies() {
        return methodDependencies.stream().map(d -> d.to).collect(Collectors.toSet());
    }

    public final void addMethodDependency(MethodInformation to) {
        methodDependencies.add(new DependencyInformation<>(this, to));
    }

    ////////// TREE //////////

    @NotNull
    public final Set<Information<?>> getDirectChildren() {
        return directChildren.stream().map(p -> (Information<?>) p.from).collect(Collectors.toSet());
    }

    @NotNull
    public final Set<Information<?>> getAllChildren() {
        return getDirectChildren().stream().flatMap(c -> Stream.concat(Stream.of(c), c.getDirectChildren().stream())).collect(Collectors.toSet());
    }

    @NotNull
    public P getParent() {
        if (parent != null) return parent.to;
        else throw new IllegalStateException(getPath() + " has no parent!");
    }

    ////////// DEFAULT //////////

    @NotNull
    @Override
    public final String toString() {
        return (getClass().getSimpleName() + " " + getName() + " {\n"
                + getDirectChildren().stream().map(Object::toString).collect(Collectors.joining(",\n"))
        ).replace("\n", "\n    ") + "\n}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getPath());
    }

    /**
     * Compares attributes of two information objects, excluding their children
     */
    void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.add(Information::getType).add(Information::getPath);
    }

    /**
     * Compares two information objects completely, including their children
     */
    void deepCompareElements(@NotNull CompareHelper<Information<?>> cmp) {
        compareElements(cmp);
        edgesCompareElements(cmp);
    }

    final void edgesCompareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.add(Information::getProjectDependencies, CompareHelper.collectionComparator());
        cmp.add(Information::getPackageDependencies, CompareHelper.collectionComparator());
        cmp.add(Information::getClassDependencies, CompareHelper.collectionComparator());
        cmp.add(Information::getMethodDependencies, CompareHelper.collectionComparator());
        cmp.add(Information::getDirectChildren, CompareHelper.deepCollectionComparator());
    }

    @Override
    public final int compareTo(@NotNull Information<?> o) {
        return comparator.compare(this, o);
    }

    @Override
    public final int deepCompareTo(@NotNull Information<?> o) {
        return deepComparator.compare(this, o);
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof Information && compareTo((Information<?>) o) == 0;
    }

    public final boolean deepEquals(Object o) {
        return o instanceof Information && deepCompareTo((Information<?>) o) == 0;
    }

    private void initializeComparators() {
        compareElements(comparator);
        deepCompareElements(deepComparator);
    }

    enum Type {
        ROOT, PROJECT, PACKAGE, CLASS, METHOD
    }
}
