package core;

import core.information.BehaviorInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import util.StringNameUtil;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Manages all dependencies that have been found.
 */
public class DependencyPool {
    private static DependencyPool instance;

    private TreeMap<String, PackageInformation> packageInformationMap;
    private TreeMap<String, ClassInformation> classInformationMap;
    private TreeMap<String, BehaviorInformation> behaviorInformationMap;

    private DependencyPool() {
        initializeDataStorage();
    }

    private void initializeDataStorage() {
        packageInformationMap = new TreeMap<>();
        classInformationMap = new TreeMap<>();
        behaviorInformationMap = new TreeMap<>();
    }

    public static DependencyPool getInstance() {
        if (instance == null) {
            instance = new DependencyPool();
        }
        return instance;
    }

    public PackageInformation getOrCreatePackageInformation(String packageName) {
        return getOrCreatePackageInformation(packageName, false);
    }

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

    public ClassInformation getOrCreateClassInformation(String className) {
        return getOrCreateClassInformation(className, false, false);
    }

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

    public Collection<PackageInformation> retrieveData() {
        Collection<PackageInformation> data = packageInformationMap.values();
        initializeDataStorage();
        return data;
    }
}
