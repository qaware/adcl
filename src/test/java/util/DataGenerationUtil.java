package util;

import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class DataGenerationUtil {
    /**
     * create a version point
     */
    public static Set<PackageInformation> version(PackageInformation... packages) {
        return new TreeSet<>(Arrays.asList(packages));
    }

    /**
     * PackageInformation: create a PackageInformation
     */
    public static PackageInformation pi(String name, ClassInformation... classes) {
        PackageInformation pi = new PackageInformation(name);
        Arrays.asList(classes).forEach(pi::addClassInformation);
        return pi;
    }

    /**
     * ClassInforamtion: create a ClassInformation
     */
    public static ClassInformation ci(String name, boolean service, boolean internal, MethodInformation... methods) {
        TreeSet<MethodInformation> mi = new TreeSet<>(Arrays.asList(methods));
        ClassInformation result = new ClassInformation(name, mi, service);
        result.setInternal(internal);
        return result;
    }

    /**
     * MethodInforamtion: create a MethodInformation
     */
    public static MethodInformation mi(String name) {
        return new MethodInformation(name);
    }

    /**
     * Point: connect a method with dependency
     *
     * @param mi the method
     * @param i  information: Any PackageInformation, ClassInformation or MethodInformation
     */
    public static void p(MethodInformation mi, Object... i) {
        Set<PackageInformation> pis = mi.getPackageDependencies();
        Set<ClassInformation> cis = mi.getClassDependencies();
        Set<MethodInformation> mis = mi.getMethodDependencies();

        for (Object o : i) {
            if (o instanceof PackageInformation) pis.add((PackageInformation) o);
            if (o instanceof ClassInformation) cis.add((ClassInformation) o);
            if (o instanceof MethodInformation) mis.add((MethodInformation) o);
        }

        mi.setPackageDependencies(pis);
        mi.setClassDependencies(cis);
        mi.setMethodDependencies(mis);
    }
}