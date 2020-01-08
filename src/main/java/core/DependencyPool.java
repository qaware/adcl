package core;

import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import util.NameParserUtil;

import java.util.Collection;
import java.util.TreeMap;

/**
 * A container for PackageInformation, ClassInformation and MethodInformation to avoid duplicates of these.
 */
public class DependencyPool {
    private static DependencyPool instance;
    private static DependencyPool extractorInstance;

    private TreeMap<String, PackageInformation> packageInformationMap;
    private TreeMap<String, ClassInformation> classInformationMap;
    private TreeMap<String, MethodInformation> methodInformationMap;

    /**
     * private to avoid multiple instances
     */
    private DependencyPool() {
        initializeDataStorage();
    }

    /**
     * Initializes the Maps containing the information
     */
    private void initializeDataStorage() {
        packageInformationMap = new TreeMap<>();
        classInformationMap = new TreeMap<>();
        methodInformationMap = new TreeMap<>();
    }

    /**
     * Gets the available instance and creates it if necessary
     *
     * @return a instance of DependencyPool
     */
    public static DependencyPool getInstance() {
        if (instance == null) {
            instance = new DependencyPool();
        }
        return instance;
    }

    /**
     * Gets the available instance for DiffExtractor or creates it if necessary
     *
     * @return a instance of DependencyPool
     */
    public static DependencyPool getExtractorInstance() {
        if (extractorInstance == null) {
            extractorInstance = new DependencyPool();
        }
        return extractorInstance;
    }

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
    ClassInformation getOrCreateClassInformation(String className) {
        return getOrCreateClassInformation(className, false, false);
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
     * @param methodName  the name of the method
     * @return the MethodInformation for the given name
     */
    MethodInformation getOrCreateMethodInformation(String methodName) {
        return methodInformationMap.computeIfAbsent(methodName, name -> {
            MethodInformation result = new MethodInformation(name);
            getOrCreateClassInformation(NameParserUtil.extractClassName(NameParserUtil.cutOffParamaterList(methodName))).addMethodInformation(result);
            return result;
        });
    }

    /**
     * removes all acquired PackageInformation, ClassInformation and MethodInformation
     */
    void resetDataStorage() {
        initializeDataStorage();
    }

    /**
     * Retrieve all acquired PackageInformation.
     *
     * @return all created PackageInformation.
     */
    Collection<PackageInformation> retrievePackageInformation() {
        return packageInformationMap.values();
    }
}
