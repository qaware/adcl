package core;

import core.information.BehaviorInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import util.StringNameUtil;

import java.util.Collection;
import java.util.TreeMap;

/**
 * A container for PackageInformation, ClassInformation and BehaviorInformation to avoid duplicates of these.
 */
public class DependencyPool {
    private static DependencyPool instance;

    private TreeMap<String, PackageInformation> packageInformationMap;
    private TreeMap<String, ClassInformation> classInformationMap;
    private TreeMap<String, BehaviorInformation> behaviorInformationMap;

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
        behaviorInformationMap = new TreeMap<>();
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
    public PackageInformation getOrCreatePackageInformation(String packageName) {
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
    public PackageInformation getOrCreatePackageInformation(String packageName, boolean isInternal) {
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
    public ClassInformation getOrCreateClassInformation(String className) {
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
    public ClassInformation getOrCreateClassInformation(String className, boolean isService, boolean packageInternal) {
        if (classInformationMap.containsKey(className)) {
            if (isService) {
                ClassInformation classInformation = classInformationMap.get(className);
                classInformation.setService(true);
                return classInformation;
            }
            return classInformationMap.get(className);
        }
        PackageInformation packageInformation = getOrCreatePackageInformation(StringNameUtil.extractPackageName(className), packageInternal);

        ClassInformation classInformation = new ClassInformation(className, isService);
        packageInformation.addClassInformation(classInformation);
        classInformationMap.put(className, classInformation);
        return classInformation;
    }

    /**
     * Gets the BehaviorInformation with the given BehaviorName, if its not available it will be created.
     *
     * @param behaviorName  the name of the behavior
     * @param isConstructor set true if the behavior is a constructor
     * @return the BehaviorInformation for the given name
     */
    public BehaviorInformation getOrCreateBehaviorInformation(String behaviorName, boolean isConstructor) {
        if (behaviorInformationMap.containsKey(behaviorName)) {
            return behaviorInformationMap.get(behaviorName);
        }
        ClassInformation classInformation;
        if (isConstructor) {
            classInformation = getOrCreateClassInformation(StringNameUtil.cutOffParamaterList(behaviorName));
        } else {
            classInformation = getOrCreateClassInformation(StringNameUtil.extractClassName(StringNameUtil.cutOffParamaterList(behaviorName)));
        }
        BehaviorInformation behaviorInformation = new BehaviorInformation(behaviorName, isConstructor);
        classInformation.addBehaviorInformation(behaviorInformation);
        behaviorInformationMap.put(behaviorName, behaviorInformation);
        return behaviorInformation;
    }

    /**
     * removes all acquired PackageInformation, ClassInformation and BehaviorInformation
     */
    public void resetDataStorage() {
        initializeDataStorage();
    }

    /**
     * Retrieve all acquired PackageInformation.
     *
     * @return the PackageInformation.
     */
    public Collection<PackageInformation> retrievePackageInformation() {
        return packageInformationMap.values();
    }
}
