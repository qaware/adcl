package core.information;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.annotation.*;
import util.CompareHelper;
import util.DeepComparable;
import util.Utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The default neo4j node. Holds information about a structural element of java code.
 *
 * @param <P> the parent type (i.e. MethodInformation's parent type is ClassInformation)
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

    @Property
    @NotNull
    private final String path;

    @Transient
    private final CompareHelper<Information<?>> comparator = new CompareHelper<>();

    @Relationship(type = "Parent", direction = Relationship.INCOMING)
    @NotNull
    final Set<ParentInformation<?>> directChildren = new HashSet<>();

    @Relationship(type = "ProjectDependency")
    @NotNull
    private final Set<ProjectDependency> projectDependencies = new HashSet<>();
    @Relationship(type = "PackageDependency")
    @NotNull
    private final Set<PackageDependency> packageDependencies = new HashSet<>();
    @Relationship(type = "ClassDependency")
    @NotNull
    private final Set<ClassDependency> classDependencies = new HashSet<>();
    @Relationship(type = "MethodDependency")
    @NotNull
    private final Set<MethodDependency> methodDependencies = new HashSet<>();
    @Transient
    private final CompareHelper<Information<?>> deepComparator = new CompareHelper<>();
    @Id
    @GeneratedValue
    @Nullable Long id;

    /**
     * neo4j init
     */
    Information() {
        this("<neo4jInit>");
    }

    /**
     * Creates a new Information instance
     *
     * @param parent the parent element
     * @param name   it's *own* name (e.g. {@link Class#getSimpleName()} for {@link ClassInformation})
     */
    Information(@NotNull P parent, @NotNull String name) {
        this.name = name;
        String parentPath = parent.getPath();
        this.path = parentPath.isEmpty() ? name : (parentPath + '.' + name);
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
        this.path = "";
        this.parent = null;
        initializeComparators();
    }

    /**
     * @return the node name (only name, not the path)
     */
    @Contract(pure = true)
    @NotNull
    public final String getName() {
        return name;
    }

    /**
     * Type specification: which kind of java structure is represented by this?
     *
     * @return the represented {@link Type} of this class
     */
    @NotNull
    public abstract Type getType();

    /**
     * @return the absolute path of the node (e.g. for a {@link MethodInformation}: {@code projectA.packageA.packageB.Class1.InnerClass.foo(java.lang.String, int)})
     */
    @Contract(pure = true)
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * @param version the version to check
     * @return whether the node exists at a given version.
     * @see RelationshipInformation#exists(VersionInformation)
     */
    public boolean exists(@NotNull VersionInformation version) {
        assert parent != null;
        return parent.exists(version);
    }

    /**
     * Set the existence of an information at a given version
     *
     * @param at     the version at which the node should exist
     * @param exists the aimed existence
     * @see RelationshipInformation#setExists(VersionInformation, boolean)
     */
    public void setExists(@NotNull VersionInformation at, boolean exists) {
        assert parent != null;
        parent.setExists(at, exists);
    }

    /**
     * @return the first version where the information exists
     * @see RelationshipInformation#firstExistence()
     */
    @NotNull
    public VersionInformation firstExistence() {
        assert parent != null;
        return parent.firstExistence();
    }

    /**
     * @return the last version where the information exists
     * @see RelationshipInformation#lastExistence()
     */
    @NotNull
    public VersionInformation lastExistence() {
        assert parent != null;
        return parent.lastExistence();
    }

    ////////// DEPS //////////

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @return all *own* project dependencies at a given version
     */
    @NotNull
    public final Set<ProjectInformation> getProjectDependencies(@Nullable VersionInformation at) {
        return projectDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* project dependencies at a given version
     */
    @NotNull
    public final Set<ProjectInformation> getAllProjectDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getProjectDependencies(at).stream()).distinct().filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* project dependencies - aggregating lower type dependencies to unbound higher type dependencies - at a given version
     */
    public final Set<ProjectInformation> getAllProjectDependenciesAggregated(@Nullable VersionInformation at, boolean includeInternal) {
        return Stream.concat(
                getAllProjectDependencies(at, includeInternal).stream(),
                getAllPackageDependencies(at, includeInternal).stream().map(mi -> mi.getParent(ProjectInformation.class))
        ).distinct().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Adds a new project dependency at given version
     * @param to the dependency
     * @param at The start version on which the dependency should exist. If null existence will be ensured for latest version
     */
    public final void addProjectDependency(@NotNull ProjectInformation to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        ProjectDependency dep = new ProjectDependency(this, to);
        dep.setExists(at, true);
        projectDependencies.add(dep);
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @return all *own* package Dependencies at a given version
     */
    @NotNull
    public final Set<PackageInformation<?>> getPackageDependencies(@Nullable VersionInformation at) {
        return packageDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* package Dependencies at a given version
     */
    @NotNull
    public final Set<PackageInformation<?>> getAllPackageDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getPackageDependencies(at).stream()).distinct().filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* package dependencies - aggregating lower type dependencies to unbound higher type dependencies - at a given version
     * @implSpec Aggregation stops at the lowest found Package, so proj.packageA.packageB.ClassC aggregates to a dependency to proj.packageA.packageB
     */
    public final Set<PackageInformation<?>> getAllPackageDependenciesAggregated(@Nullable VersionInformation at, boolean includeInternal) {
        return Stream.concat(
                getAllPackageDependencies(at, includeInternal).stream(),
                getAllClassDependenciesAggregated(at, includeInternal).stream().map(mi -> (PackageInformation<?>) mi.getParent(PackageInformation.class))
        ).distinct().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Adds a new package dependency at given version
     * @param to the dependency
     * @param at The start version on which the dependency should exist. If null existence will be ensured for latest version
     */
    public final void addPackageDependency(@NotNull PackageInformation<?> to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        PackageDependency dep = new PackageDependency(this, to);
        dep.setExists(at, true);
        packageDependencies.add(dep);
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @return all *own* class dependencies at a given version
     */
    @NotNull
    public final Set<ClassInformation<?>> getClassDependencies(@Nullable VersionInformation at) {
        return classDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* class dependencies at a given version
     */
    @NotNull
    public final Set<ClassInformation<?>> getAllClassDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getClassDependencies(at).stream()).distinct().filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* class dependencies - aggregating lower type dependencies to unbound higher type dependencies - at a given version
     * @implSpec Aggregation stops at the lowest found Package, so proj.packageA.packageB.ClassC aggregates to a dependency to proj.packageA.packageB
     */
    public final Set<ClassInformation<?>> getAllClassDependenciesAggregated(@Nullable VersionInformation at, boolean includeInternal) {
        return Stream.concat(
                getAllClassDependencies(at, includeInternal).stream(),
                getAllMethodDependencies(at, includeInternal).stream().map(mi -> (ClassInformation<?>) mi.getParent(ClassInformation.class))
        ).distinct().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Adds a new class dependency at given version
     * @param to the dependency
     * @param at The start version on which the dependency should exist. If null existence will be ensured for latest version
     */
    public final void addClassDependency(@NotNull ClassInformation<?> to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        ClassDependency dep = new ClassDependency(this, to);
        dep.setExists(at, true);
        classDependencies.add(dep);
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @return all *own* method dependencies at a given version
     */
    @NotNull
    public final Set<MethodInformation> getMethodDependencies(@Nullable VersionInformation at) {
        return methodDependencies.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getTo).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null dependencies at any time are returned
     * @param includeInternal whether dependencies that lead to another child node of this should be included
     * @return *all* method dependencies at a given version
     */
    @NotNull
    public final Set<MethodInformation> getAllMethodDependencies(@Nullable VersionInformation at, boolean includeInternal) {
        return getAllChildren(at).stream().flatMap(i -> i.getMethodDependencies(at).stream()).distinct().filter(p -> includeInternal || !p.hasParent(this)).collect(Collectors.toSet());
    }

    /**
     * Adds a new method dependency at given version
     * @param to the dependency
     * @param at The start version on which the dependency should exist. If null existence will be ensured for latest version
     */
    public final void addMethodDependency(@NotNull MethodInformation to, @Nullable VersionInformation at) {
        if (at == null) at = getProject().getLatestVersion();
        MethodDependency dep = new MethodDependency(this, to);
        dep.setExists(at, true);
        methodDependencies.add(dep);
    }

    ////////// TREE //////////

    /**
     * @return the parent of the node; returns root for root
     */
    @NotNull
    public P getParent() {
        assert parent != null;
        return parent.getTo();
    }

    /**
     * @param <T>        The type of the parent that should be returned
     * @param parentType the corresponding class for the type parameter
     * @return the first parent of given type traveling up the path, returns null if none was found
     */
    @SuppressWarnings("unchecked" /* checked by Class#isInstance */)
    @Nullable
    public <T> T getParent(@NotNull Class<T> parentType) {
        P directParent = getParent();
        return parentType.isInstance(directParent) ? (T) directParent : directParent.getParent(parentType);
    }

    /**
     * @param potentialParent the parent to check for
     * @return whether the information has {@code potentialParent} in its path, including itself
     */
    public boolean hasParent(@NotNull Information<?> potentialParent) {
        return equals(potentialParent) || getParent().hasParent(potentialParent);
    }

    /**
     * @param at the version to check. If null children at any time are returned.
     * @return the direct children of the node at given version. Direct children are represented by an incoming parent edge in the graph.
     * @apiNote Keep in mind that it is unknown of which concrete type the children are (e.g. a direct children of a package can be a package or a class)
     */
    @NotNull
    public final Set<Information<?>> getDirectChildren(@Nullable VersionInformation at) {
        return directChildren.stream().filter(d -> at == null || d.exists(at)).map(RelationshipInformation::getFrom).collect(Collectors.toSet());
    }

    /**
     * @param at the version to check. If null children at any time are returned.
     * @return all children of the node at given version, recursively.
     * @apiNote Keep in mind that it is unknown of which concrete type the children are (e.g. a direct children of a package can be a package or a class)
     */
    @NotNull
    public final Set<Information<?>> getAllChildren(@Nullable VersionInformation at) {
        return getDirectChildren(at).stream().flatMap(c -> Stream.concat(Stream.of(c), c.getAllChildren(at).stream())).collect(Collectors.toSet());
    }

    /**
     * @return the root node
     */
    @NotNull
    public final RootInformation getRoot() {
        Information<?> result = this;
        while (!(result instanceof RootInformation)) result = result.getParent();
        return (RootInformation) result;
    }

    /**
     * @return the project node.
     * @throws UnsupportedOperationException if called on root node
     */
    @NotNull
    public ProjectInformation getProject() {
        Information<?> result = this;
        while (!(result instanceof ProjectInformation)) result = result.getParent();
        return (ProjectInformation) result;
    }

    /**
     * @param <T> the type of children to search for
     * @param clazz the corresponding class for the type parameter
     * @param at the version to check. If null fitting children at any time are returned
     * @return all direct children which are fitting to the given type at given version
     */
    @NotNull
    public <T extends Information<?>> Set<T> find(@NotNull Class<T> clazz, @Nullable VersionInformation at) {
        return Utils.cast(getDirectChildren(at), clazz);
    }

    /**
     * @param <T>   the type of children to search for
     * @param clazz the corresponding class for the type parameter
     * @param at    the version to check. If null fitting children at any time are returned
     * @return *all* children which are fitting to the given type at given version
     * <br>e.g. {@code findAll(MethodInformation.class, null)} returns all methods in the class/package/project at any time
     */
    @NotNull
    public <T extends Information<?>> Set<T> findAll(@NotNull Class<T> clazz, @Nullable VersionInformation at) {
        return Utils.cast(getAllChildren(at), clazz);
    }

    /**
     * Tries to find a sub node located at the sub path relative to this node by its path. If you want to match a whole path use {@code getRoot().findByPath(path, at)}
     *
     * @param subPath a sub path (e.g. if you want to find a.b.c.d and this is a.b, subPath has to be c.d)
     * @param at      the version to check. If null children at any time are taken into consideration
     * @return The node at this subPath. Null otherwise
     * @see Information#findOrCreate(String, VersionInformation, Type) version with creation
     */
    @Nullable
    public Information<?> find(@NotNull final String subPath, @Nullable VersionInformation at) {
        if (subPath.isEmpty()) return this;
        Pair<String, String> split = splitSegment(subPath);
        for (Information<?> i : getDirectChildren(at)) {
            if (i.getName().equals(split.getKey())) return i.find(split.getValue(), at);
        }
        return null;
    }

    /**
     * Tries to find a sub node located at the sub path relative to this node by its path. If element is not found the method tries to create a new node based on creationType
     *
     * @param subPath      a sub path (e.g. if you want to find a.b.c.d and this is a.b, subPath has to be c.d)
     * @param version      the version at which the element should exist. If null the latest version will be used
     * @param creationType the {@link Type} of the new node, if creation needed. Does not ensure that the returned node has that type
     * @return the found or newly created node
     * @throws UnsupportedOperationException if creationType and child logic given by subPath are mutually exclusive
     * @see Information#find(String, VersionInformation) version without node creation
     */
    @NotNull
    public Information<?> findOrCreate(@NotNull String subPath, @Nullable VersionInformation version, Type creationType) {
        if (subPath.isEmpty()) return this;
        if (version == null && getType() != Type.ROOT) version = getProject().getLatestVersion();
        Pair<String, String> split = splitSegment(subPath);
        Information<?> result = find(split.getKey(), null);
        if (result == null) {
            result = createChild(typeOfNextSegment(subPath, creationType), split.getKey());
            if (getType().isSub(Type.ROOT)) result.setExists(result.firstExistence(), false);
        }
        if (version != null) result.setExists(version, true);
        return result.findOrCreate(split.getValue(), version, creationType);
    }

    /**
     * @param path a path
     * @return the path, split by its next segment and the rest
     */
    @NotNull
    private Pair<String, String> splitSegment(@NotNull String path) {
        int paramsStart = path.indexOf('(');
        int splitPoint = path.indexOf('.');
        return (splitPoint < 0 || (paramsStart >= 0 && paramsStart < splitPoint)) ? Pair.of(path, "") : Pair.of(path.substring(0, splitPoint), path.substring(splitPoint + 1));
    }

    /**
     * Tries to resolve the type of a potential direct child given on an imaginary subPath and the subPath's leaf type
     * Example1: if this is a PACKAGE a.b and subPath is c.d and lastSegmentType is METHOD c has to be a CLASS
     * Example2: if this is a CLASS a.b and subPath is c.d and lastSegmentType is METHOD it will throw an exception as c is undefined
     *
     * @param subPath         the imaginary sub path
     * @param lastSegmentType the leaf type of the sub path
     * @return the type of the next
     * @throws UnsupportedOperationException if lastSegmentType and subPath cannot be put in a logical correlation
     */
    private Type typeOfNextSegment(@NotNull String subPath, @NotNull Type lastSegmentType) {
        int paramsStart = subPath.indexOf('(');
        if (paramsStart != -1) subPath = subPath.substring(0, paramsStart);
        String[] segments = subPath.split("\\.");

        if (lastSegmentType.isSuper(getType())) throw new IllegalArgumentException();

        if (segments.length == 1) return lastSegmentType;

        if (getType() == Type.ROOT) return Type.PROJECT;
        if (getType() == Type.PROJECT) {
            if (segments.length == 2 && lastSegmentType == Type.METHOD) return Type.CLASS;
            return Type.PACKAGE;
        }
        if (getType() == Type.PACKAGE) {
            if (segments.length == 2 && lastSegmentType == Type.METHOD) return Type.CLASS;
            return Type.PACKAGE;
        }
        throw new IllegalArgumentException("Parameters cannot be put in logical context: " + getType() + " -> " + subPath + " -> " + lastSegmentType);
    }

    /**
     * Creates a new direct child to this. Won't check for an already existing child
     *
     * @param childType the creation type
     * @param name      the name of the new child
     * @return the newly created child
     * @throws UnsupportedOperationException if the childType cannot cannot serve as child (e.g. METHOD as child to ROOT)
     */
    public Information<?> createChild(Type childType, String name) {
        if (getType() == Type.ROOT && childType == Type.PROJECT)
            return new ProjectInformation((RootInformation) this, name, false, "<unknown>");
        if (getType() == Type.PROJECT && childType == Type.PACKAGE)
            return new RootPackageInformation((ProjectInformation) this, name);
        if (getType() == Type.PROJECT && childType == Type.CLASS)
            return new RootClassInformation((ProjectInformation) this, name, false);
        if (getType() == Type.PACKAGE && childType == Type.PACKAGE)
            return new SubPackageInformation((PackageInformation<?>) this, name);
        if (getType() == Type.PACKAGE && childType == Type.CLASS)
            return new OuterClassInformation((PackageInformation<?>) this, name, false);
        if (getType() == Type.CLASS && childType == Type.METHOD)
            return new MethodInformation((ClassInformation<?>) this, name);
        throw new UnsupportedOperationException("There is no child of type " + childType + " for parent " + getType());
    }

    ////////// DEFAULT //////////

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder(getType().toString() + " " + getName());
        Stream.of(projectDependencies, packageDependencies, classDependencies, methodDependencies)
                .flatMap(Collection::stream).map(d -> d.getAim().getPath()).sorted().forEach(d -> sb.append("\n  => ").append(d));
        directChildren.stream().map(ParentInformation::getAim).sorted().forEach(c -> sb.append("\n  ").append(c.toString().replace("\n", "\n  ")));
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getPath());
    }

    /**
     * Fills a {@link CompareHelper} with comparators so that it's compareTo compares the Information instances
     *
     * @param cmp the CompareHelper that should be filled with the comparator elements
     */
    protected void compareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.add(Information::getType).add(Information::getPath);
    }

    /**
     * Fills a {@link CompareHelper} with comparators so that it's compareTo deep compares the Information instances
     *
     * @param cmp the CompareHelper that should be filled with the comparator elements
     */
    protected void deepCompareElements(@NotNull CompareHelper<Information<?>> cmp) {
        compareElements(cmp);
        edgesCompareElements(cmp);
    }

    /**
     * Fills a {@link CompareHelper} with comparators so that it's compareTo compares the edges (dependencies, children) of the Information instances
     *
     * @param cmp the CompareHelper that should be filled with the comparator elements
     */
    final void edgesCompareElements(@NotNull CompareHelper<Information<?>> cmp) {
        cmp.add(i -> i.getProjectDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getPackageDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getClassDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getMethodDependencies(null), CompareHelper.collectionComparator());
        cmp.add(i -> i.getDirectChildren(null), CompareHelper.deepCollectionComparator());
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public final int compareTo(@NotNull Information<?> o) {
        return o == this ? 0 : comparator.compare(this, o);
    }


    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public final int deepCompareTo(@NotNull Information<?> o) {
        return deepComparator.compare(this, o);
    }


    /**
     * {@inheritDoc}
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public final boolean equals(Object o) {
        return o instanceof Information && compareTo((Information<?>) o) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public final boolean deepEquals(Object o) {
        return o instanceof Information && deepCompareTo((Information<?>) o) == 0;
    }

    /**
     * Initializes the comparator fields for compareTo and deepCompareTo to work
     */
    private void initializeComparators() {
        compareElements(comparator);
        deepCompareElements(deepComparator);
    }

    /**
     * The types the java structure has ({@link Type#ROOT} for internal purposes)
     */
    public enum Type {
        ROOT, PROJECT, PACKAGE, CLASS, METHOD;

        /**
         * @param type the type to compare
         * @return whether own type can be a parent of given type
         */
        public boolean isSuper(@NotNull Type type) {
            return ordinal() < type.ordinal();
        }

        /**
         * @param type the type to compare
         * @return whether own type can be a child of given type
         */
        public boolean isSub(@NotNull Type type) {
            return ordinal() > type.ordinal();
        }
    }
}
