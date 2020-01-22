package core.information2;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.*;
import util.CompareHelper;
import util.DeepComparable;
import util.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The default neo4j node. Holds information about a structural element of java code.
 *
 * @param <P> the parent type (like for MethodInformation -> ClassInformation)
 */
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

    @Relationship(type = "Parent", direction = Relationship.INCOMING)
    @NotNull Set<ParentInformation<?>> directChildren = new HashSet<>();

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
    @Transient
    private final CompareHelper<Information<?>> deepComparator = new CompareHelper<>();
    @Id
    @GeneratedValue
    @Nullable
    private Long id;

    /**
     * Creates a new Information instance
     *
     * @param parent the parent element
     * @param name   it's *own* name (e.g. {@link Class#getSimpleName()} for {@link ClassInformation})
     */
    Information(@NotNull P parent, @NotNull String name) {
        this.name = name;
        this.parent = new ParentInformation<>(this, parent);
        parent.directChildren.add(this.parent);
        initializeComparators();
    }

    /**
     * Creates a new Information instance
     * Only for {@link RootInformation}, which has no parent.
     * Needs to override getParent and exists if used
     * @param name it's *own* name (e.g. {@link Class#getSimpleName()} for {@link ClassInformation})
     */
    Information(@NotNull String name) {
        this.name = name;
        this.parent = null;
        initializeComparators();
    }

    @Contract(pure = true)
    @NotNull
    public final String getName() {
        return name;
    }

    /**
     * Type specification: which kind of java structure is represented by this?
     */
    @NotNull
    public abstract Type getType();

    /**
     * Gets the absolute path of the node
     * e.g. for MethodInformation "projectA.packageA.packageB.Class1.InnerClass.foo(java.lang.String, int)
     */
    @NotNull
    public String getPath() {
        return getParent().getPath() + '.' + getName();
    }

    /**
     * Determines whether the node exists at a given version.
     */
    public boolean exists(@NotNull VersionInformation version) {
        assert parent != null;
        return parent.exists(version);
    }

    ////////// DEPS //////////

    /**
     * Returns all *own* Project Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<ProjectInformation> getProjectDependencies(@Nullable VersionInformation at) {
        return projectDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * Returns *all* Project Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<ProjectInformation> getAllProjectDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getProjectDependencies(at).stream()).filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * Adds a new project dependency at given version. If version is null existence will be ensured for latest version
     */
    public final void addProjectDependency(@NotNull ProjectInformation to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        DependencyInformation<ProjectInformation> dep = new DependencyInformation<>(this, to);
        dep.ensureStateAt(at, true);
        projectDependencies.add(dep);
    }

    /**
     * Returns all *own* Package Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<PackageInformation<?>> getPackageDependencies(@Nullable VersionInformation at) {
        return packageDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * Returns *all* Package Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<PackageInformation<?>> getAllPackageDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getPackageDependencies(at).stream()).filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * Adds a new package dependency at given version. If version is null existence will be ensured for latest version
     */
    public final void addPackageDependency(@NotNull PackageInformation<?> to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        DependencyInformation<PackageInformation<?>> dep = new DependencyInformation<>(this, to);
        dep.ensureStateAt(at, true);
        packageDependencies.add(dep);
    }

    /**
     * Returns all *own* Class Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<ClassInformation<?>> getClassDependencies(@Nullable VersionInformation at) {
        return classDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * Returns *all* Class Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<ClassInformation<?>> getAllClassDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getClassDependencies(at).stream()).filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * Adds a new class dependency at given version. If version is null existence will be ensured for latest version
     */
    public final void addClassDependency(@NotNull ClassInformation<?> to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        DependencyInformation<ClassInformation<?>> dep = new DependencyInformation<>(this, to);
        dep.ensureStateAt(at, true);
        classDependencies.add(dep);
    }

    /**
     * Returns all *own* Method Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<MethodInformation> getMethodDependencies(@Nullable VersionInformation at) {
        return methodDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * Returns *all* Method Dependencies at a given version. If version is null dependencies at any time are returned.
     */
    @NotNull
    public final Set<MethodInformation> getAllMethodDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getMethodDependencies(at).stream()).filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * Adds a new method dependency at given version. If version is null existence will be ensured for latest version
     */
    public final void addMethodDependency(@NotNull MethodInformation to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        DependencyInformation<MethodInformation> dep = new DependencyInformation<>(this, to);
        dep.ensureStateAt(at, true);
        methodDependencies.add(dep);
    }

    ////////// TREE //////////

    /**
     * Returns the parent of the node; returns root for root
     */
    @NotNull
    public P getParent() {
        return Objects.requireNonNull(parent, "parent of " + getName() + " is null").getTo();
    }

    /**
     * Checks whether the information has {@code potentialParent} in its path, including itself
     */
    public boolean hasParent(@NotNull Information<?> potentialParent) {
        return equals(potentialParent) || getParent().hasParent(potentialParent);
    }

    /**
     * Returns the direct children of the node at given version. Direct children are represented by an incoming parent edge in the graph.
     * If version is null children at any time are returned.
     * Keep in mind that it is unknown of which concrete type the children are (e.g. a direct children of a class can be a method or an inner class)
     */
    @NotNull
    public final Set<Information<?>> getDirectChildren(@Nullable VersionInformation at) {
        return directChildren.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getFrom).collect(Collectors.toSet());
    }

    /**
     * Returns all children of the node at given version, recursively
     * The version is null all children at any time are returned.
     */
    @NotNull
    public final Set<Information<?>> getAllChildren(@Nullable VersionInformation at) {
        return getDirectChildren(at).stream().flatMap(c -> Stream.concat(Stream.of(c), c.getAllChildren(at).stream())).collect(Collectors.toSet());
    }

    /**
     * Returns the root node
     */
    @NotNull
    public final RootInformation getRoot() {
        Information<?> result = this;
        while (!(result instanceof RootInformation)) result = result.getParent();
        return (RootInformation) result;
    }

    /**
     * Returns the project node.
     *
     * @throws UnsupportedOperationException if called on root node
     */
    @NotNull
    public ProjectInformation getProject() {
        Information<?> result = this;
        while (!(result instanceof ProjectInformation)) result = result.getParent();
        return (ProjectInformation) result;
    }

    /**
     * Returns all direct children which are fitting to the given type at given version
     */
    @NotNull
    public <T extends Information<?>> Set<T> find(@NotNull Class<T> clazz, @Nullable VersionInformation at) {
        return Utils.cast(getDirectChildren(at), clazz);
    }

    /**
     * Returns *all* children which are fitting to the given type
     * e.g. findAll(MethodInformation.class, null) returns all methods in the class/package/project at any time
     */
    @NotNull
    public <T extends Information<?>> Set<T> findAll(@NotNull Class<T> clazz, @Nullable VersionInformation at) {
        return Utils.cast(getAllChildren(at), clazz);
    }

    /**
     * Tries to find an information by its sub-path. If you want to match a whole path use {@code getRoot().findByPath(path, at)}
     */
    @Nullable
    public Information<?> findByPath(@NotNull String path, @Nullable VersionInformation at) {
        if (!path.startsWith(name)) return null;
        path = path.substring(name.length());
        if (path.startsWith(".") || path.startsWith("$")) path = path.substring(1);
        if (path.isEmpty()) return this;
        String finalPath = path;
        return getDirectChildren(at).stream().map(i -> i.findByPath(finalPath, at)).filter(Objects::nonNull).findAny().orElse(null);
    }

    ////////// DEFAULT //////////

    @NotNull
    @Override
    public final String toString() {
        return (getClass().getSimpleName() + " " + getName() + " {\n"
                + getDirectChildren(null).stream().map(Object::toString).collect(Collectors.joining(",\n"))
        ).replace("\n", "\n    ") + "\n}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getPath());
    }

    void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.add(Information::getType).add(Information::getPath);
    }

    void deepCompareElements(@NotNull CompareHelper<Information<?>> cmp) {
        compareElements(cmp);
        edgesCompareElements(cmp);
    }

    final void edgesCompareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.add(i -> i.getProjectDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getPackageDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getClassDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getMethodDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getDirectChildren(null), CompareHelper.deepCollectionComparator());
    }

    /**
     * Compares attributes of two information objects only by its own attributes
     */
    @Contract(pure = true)
    @Override
    public final int compareTo(@NotNull Information<?> o) {
        return o == this ? 0 : comparator.compare(this, o);
    }

    /**
     * Compares two information objects completely, including their children
     */
    @Contract(pure = true)
    @Override
    public final int deepCompareTo(@NotNull Information<?> o) {
        return deepComparator.compare(this, o);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public final boolean equals(Object o) {
        return o instanceof Information && compareTo((Information<?>) o) == 0;
    }

    @Contract(value = "null -> false", pure = true)
    public final boolean deepEquals(Object o) {
        return o instanceof Information && deepCompareTo((Information<?>) o) == 0;
    }

    private void initializeComparators() {
        compareElements(comparator);
        deepCompareElements(deepComparator);
    }

    /**
     * The types the java structure has (root for internal purposes)
     */
    enum Type {
        ROOT, PROJECT, PACKAGE, CLASS, METHOD
    }
}
