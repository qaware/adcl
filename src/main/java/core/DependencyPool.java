package core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import util.NameParserUtil;

import java.util.Set;
import java.util.TreeSet;

/**
 * A container for PackageInformation, ClassInformation and MethodInformation to avoid duplicates of these.
 */
public class DependencyPool {
    private final BiMap<String, PackageInformation> packageInformationMap = HashBiMap.create();
    private final BiMap<String, ClassInformation> classInformationMap = HashBiMap.create();
    private final BiMap<String, MethodInformation> methodInformationMap = HashBiMap.create();

    /**
     * Gets the PackageInformation with the given packageName, if its not available it will be created.
     *
     * @param packageName the name of the package
     * @return the PackageInformation for the given name
     */
    PackageInformation getOrCreatePackageInformation(String packageName) {
        return packageInformationMap.computeIfAbsent(packageName, PackageInformation::new);
    }

    /**
     * Gets the ClassInformation with the given className, if its not available it will be created.
     *
     * @param className the name of the class
     * @return the ClassInformation for the given name
     */
    ClassInformation getOrCreateClassInformation(String className, boolean internal) {
        return getOrCreateClassInformation(className, false, internal);
    }

    /**
     * Gets the ClassInformation with the given className, if its not available it will be created.
     *
     * @param className the name of the class
     * @param isService set true if the given class a spring boot service
     * @param internal  set true if the given class is internal to the analysed project
     * @return the ClassInformation for the given name
     */
    ClassInformation getOrCreateClassInformation(String className, boolean isService, boolean internal) {
        ClassInformation result = classInformationMap.computeIfAbsent(className, name -> {
            ClassInformation newClass = new ClassInformation(className);
            getOrCreatePackageInformation(NameParserUtil.extractPackageName(className)).addClassInformation(newClass);
            return newClass;
        });
        if (isService) result.setService(true);
        if (internal) result.setInternal(true);
        return result;
    }

    /**
     * Gets the MethodInformation with the given MethodName, if its not available it will be created.
     *
     * @param methodName the name of the method
     * @return the MethodInformation for the given name
     */
    MethodInformation getOrCreateMethodInformation(String methodName, boolean internal) {
        return methodInformationMap.computeIfAbsent(methodName, name -> {
            MethodInformation result = new MethodInformation(name);
            getOrCreateClassInformation(NameParserUtil.extractClassName(NameParserUtil.cutOffParamaterList(methodName)), internal).addMethodInformation(result);
            return result;
        });
    }

    /**
     * Retrieve all acquired PackageInformation.
     *
     * @return all created PackageInformation.
     */
    Set<PackageInformation> retrievePackageInformation() {
        return new TreeSet<>(packageInformationMap.values());
    }
}
