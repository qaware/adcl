package util;

import core.information2.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "java:S1452" /* Wildcards are needed */})
public class DataGenerationUtil2 {
    private DataGenerationUtil2() {
    }

    @SafeVarargs
    @NotNull
    public static RootInformation root(Ref<?, RootInformation>... projects) {
        RootInformation res = new RootInformation();
        Stream.of(projects).forEach(f -> f.apply(res));
        return res;
    }

    /**
     * create a version point
     */
    @NotNull
    @SafeVarargs
    public static Ref<ProjectInformation, RootInformation> project(String name, boolean internal, String initialVersion, Ref<?, ProjectInformation>... packages) {
        return new Ref<>(root -> {
            ProjectInformation res = new ProjectInformation(root, name, internal, initialVersion);
            Stream.of(packages).forEach(f -> f.apply(res));
            return res;
        });
    }

    /**
     * PackageInformation: create a PackageInformation
     */
    @NotNull
    @Contract(pure = true)
    @SafeVarargs
    public static Ref<PackageInformation<ProjectInformation>, ProjectInformation> pir(String name, Ref<?, PackageInformation<?>>... classes) {
        return new Ref<>(project -> {
            PackageInformation<ProjectInformation> res = new RootPackageInformation(project, name);
            Stream.of(classes).forEach(f -> f.apply(res));
            return res;
        });
    }

    /**
     * PackageInformation: create a PackageInformation
     */
    @NotNull
    @Contract(pure = true)
    @SafeVarargs
    public static Ref<PackageInformation<PackageInformation<?>>, PackageInformation<?>> pis(String name, Ref<?, PackageInformation<?>>... classes) {
        return new Ref<>(pack -> {
            PackageInformation<PackageInformation<?>> res = new SubPackageInformation(pack, name);
            Stream.of(classes).forEach(f -> f.apply(res));
            return res;
        });
    }

    /**
     * ClassInforamtion: create a ClassInformation
     */
    @NotNull
    @SafeVarargs
    public static Ref<ClassInformation<PackageInformation<?>>, PackageInformation<?>> cio(String name, boolean service, Ref<?, ClassInformation<?>>... methods) {
        return new Ref<>(pack -> {
            ClassInformation<PackageInformation<?>> res = new OuterClassInformation(pack, name, service);
            Stream.of(methods).forEach(f -> f.apply(res));
            return res;
        });
    }

    /**
     * ClassInforamtion: create a ClassInformation
     */
    @NotNull
    @SafeVarargs
    public static Ref<ClassInformation<ProjectInformation>, ProjectInformation> cir(String name, boolean service, Ref<?, ClassInformation<?>>... methods) {
        return new Ref<>(proj -> {
            ClassInformation<ProjectInformation> res = new RootClassInformation(proj, name, service);
            Stream.of(methods).forEach(f -> f.apply(res));
            return res;
        });
    }

    /**
     * MethodInforamtion: create a MethodInformation
     */
    @NotNull
    @Contract(pure = true)
    public static Ref<MethodInformation, ClassInformation<?>> mi(String name) {
        return new Ref<>(clazz -> new MethodInformation(clazz, name));
    }

    /**
     * Point: connect a method with dependency
     *
     * @param f from: the start information
     * @param t to: Any PackageInformation, ClassInformation or MethodInformation
     */
    public static void p(Ref<?, ?> f, @NotNull Ref<?, ?>... t) {
        for (Ref<?, ?> wo : t) {
            Information<?> o = wo.getStored();
            if (o instanceof ProjectInformation) f.getStored().addProjectDependency((ProjectInformation) o, null);
            else if (o instanceof PackageInformation)
                f.getStored().addPackageDependency((PackageInformation<?>) o, null);
            else if (o instanceof ClassInformation) f.getStored().addClassDependency((ClassInformation<?>) o, null);
            else if (o instanceof MethodInformation) f.getStored().addMethodDependency((MethodInformation) o, null);
            else throw new IllegalStateException("Unknown pointer aim " + o.getClass().getSimpleName());
        }
    }

    public static class Ref<T extends Information<P>, P extends Information<?>> implements Function<P, T> {
        private final Function<P, T> getter;
        private T obj;

        public Ref(Function<P, T> getter) {
            this.getter = getter;
        }

        public T getStored() {
            return obj;
        }

        @Override
        public T apply(P p) {
            obj = getter.apply(p);
            return obj;
        }
    }
}