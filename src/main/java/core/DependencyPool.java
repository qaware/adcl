package core;

import core.information.MethodInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import util.NameParserUtil;

import java.util.Collection;
import java.util.TreeMap;

/**
 * A container for PackageInformation, ClassInformation and MethodInformation to avoid duplicates of these.
 */
public class DependencyPool {
    private static DependencyPool instance;

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
     * Gets the PackageInformation with the given packageName, if its not available it will be created.
     *
     * @param packageName the name of the package
     * @return the PackageInformation for the given name
     */
    PackageInformation getOrCreatePackageInformation(String packageName) {
        return getOrCreatePackageInformation(packageName, false);
    }

    /**
     * Gets the PackageInformation with the given packageName, if its not available it will be created.
     * if isInternal is true it will be set in the the created/retrieved PackageInformation.
     *
     * @param packageName the name of the package
     * @param isInternal  set true if the given package is internal to the analysed project
     * @return the PackageInformation for the given name
     */
    private PackageInformation getOrCreatePackageInformation(String packageName, boolean isInternal) {
        if (packageInformationMap.containsKey(packageName)) {
            if (isInternal) {
                PackageInformation packageInformation = packageInformationMap.get(packageName);
                packageInformation.setInternalPackage(true);
                return packageInformation;
            }
            return packageInformationMap.get(packageName);
        }
        PackageInformation packageInformation = new PackageInformation(packageName);
        packageInformation.setInternalPackage(isInternal);
        packageInformationMap.put(packageName, packageInformation);
        return packageInformation;
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
     * @param className       the name of the class
     * @param isService       set true if the given class a spring boot service
     * @param packageInternal set true if the given class is internal to the analysed project
     * @return the ClassInformation for the given name
     */
    ClassInformation getOrCreateClassInformation(String className, boolean isService, boolean packageInternal) {
        if (classInformationMap.containsKey(className)) {
            if (isService) {
                ClassInformation classInformation = classInformationMap.get(className);
                classInformation.setService(true);
                return classInformation;
            }
            return classInformationMap.get(className);
        }
        PackageInformation packageInformation = getOrCreatePackageInformation(NameParserUtil.extractPackageName(className), packageInternal);

        ClassInformation classInformation = new ClassInformation(className, isService);
        packageInformation.addClassInformation(classInformation);
        classInformationMap.put(className, classInformation);
        return classInformation;
    }

    /**
     * Gets the MethodInformation with the given MethodName, if its not available it will be created.
     *
     * @param methodName  the name of the method
     * @param isConstructor set true if the method is a constructor
     * @return the MethodInformation for the given name
     */
    MethodInformation getOrCreateMethodInformation(String methodName, boolean isConstructor) {
        if (methodInformationMap.containsKey(methodName)) {
            return methodInformationMap.get(methodName);
        }
        ClassInformation classInformation;
        if (isConstructor) {
            classInformation = getOrCreateClassInformation(NameParserUtil.cutOffParamaterList(methodName));
        } else {
            classInformation = getOrCreateClassInformation(NameParserUtil.extractClassName(NameParserUtil.cutOffParamaterList(methodName)));
        }
        MethodInformation methodInformation = new MethodInformation(methodName, isConstructor);
        classInformation.addMethodInformation(methodInformation);
        methodInformationMap.put(methodName, methodInformation);
        return methodInformation;
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
