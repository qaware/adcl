package core;


import core.information.BehaviorInformation;
import core.information.ClassInformation;
import core.information.ConstructorInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DependencyExtractor can extract static dependencies from class files such as referenced packages, classes, methods.
 */
public class DependencyExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyExtractor.class);

    private ClassPool classPool;

    /**
     * Instantiates a new DependencyExtractor.
     */
    public DependencyExtractor() {
        this.classPool = new ClassPool(true);
    }

    /**
     * Analyse classes collection.
     *
     * @param classFiles all list of class files that should be analysed for dependencies.
     * @return the collection
     */
    public Collection<PackageInformation> analyseClasses(List<String> classFiles) {
        HashMap<String, PackageInformation> packageInformations = new HashMap<>();

        classFiles.forEach(classFilename -> {
            try {
                addToPackgeInformation(packageInformations, createClassInformation(classFilename));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
        return packageInformations.values();
    }

    /**
     * Adds the ClassInformation into the PackageInformation, if the PackageInformation does not exist in the Map it a new PackageInformation is created.
     *
     * @param packageInformations a Map containing all available PackageInformation
     * @param classInformation    the ClassInformtion to be added
     */
    private void addToPackgeInformation(HashMap<String, PackageInformation> packageInformations, ClassInformation classInformation) {
        String packageName = extractPackageName(classInformation.getClassName());
        if (!packageInformations.containsKey(packageName)) {
            packageInformations.put(packageName, new PackageInformation(packageName));
        }
        packageInformations.get(packageName).addClassInformation(classInformation);
    }

    /**
     * Gets CtClass from the ClassPool, if the Class is not contained in the ClassPool it gets created and added to the ClassPool.
     */
    private CtClass getOrCreateCtClass(String classFile) throws IOException {
        CtClass ctClass = classPool.getOrNull(classFile);
        if (ctClass == null) {
            ctClass = classPool.makeClass(new FileInputStream(classFile));
        }
        return ctClass;
    }

    /**
     * Creates ClassInformation for a Class.
     *
     * @param className Canonical name of the class
     * @return ClassInformation
     * @throws IOException if class not contained in ClassPool and could not be loaded through the given path.
     */
    private ClassInformation createClassInformation(String className) throws IOException {
        CtClass ctClass = getOrCreateCtClass(className);

        Collection<BehaviorInformation> behaviorInformations = createBehaviorInformations(ctClass);
        SortedSet<ConstructorInformation> constructorInformations = new TreeSet<>();
        SortedSet<MethodInformation> methodInformations = new TreeSet<>();
        behaviorInformations.stream().filter(behaviorInformation -> behaviorInformation instanceof ConstructorInformation).forEach(cInformation -> constructorInformations.add((ConstructorInformation) cInformation));
        behaviorInformations.stream().filter(behaviorInformation -> behaviorInformation instanceof MethodInformation).forEach(mInformation -> methodInformations.add((MethodInformation) mInformation));
        boolean isService = ctClass.hasAnnotation("org.springframework.stereotype.Service");
        return new ClassInformation(ctClass.getName(), extractReferencedPackages(ctClass), extractReferencedClasses(ctClass), constructorInformations, methodInformations, isService);
    }

    /**
     * Extracts CtBehaviors from the CtClass and creates for a BehaviorInformation.
     *
     * @param ctClass CtClass from which CtBehaviors are taken from.
     * @return all BehaviorInformation that could be created for the CtClass.
     */
    private Collection<BehaviorInformation> createBehaviorInformations(CtClass ctClass) {
        Collection<BehaviorInformation> behaviorInformations = new HashSet<>();
        CtBehavior[] ctBehaviors = ctClass.getDeclaredBehaviors();
        Arrays.stream(ctBehaviors).forEach(ctBehavior -> {
            try {
                behaviorInformations.add(createBehaviorInformation(ctBehavior));
            } catch (CannotCompileException | NotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        });
        return behaviorInformations;
    }

    /**
     * Extracts the package name from a complete class name.
     *
     * @param completeClassName a complete class name
     * @return package name
     */
    private String extractPackageName(String completeClassName) {
        int startOfClassName = completeClassName.lastIndexOf('.');
        //in case of default
        if (startOfClassName == -1) {
            return "default";
        }
        return completeClassName.substring(0, startOfClassName);
    }

    /**
     * Extracts all classes referenced by the class represented by the CtClass.
     *
     * @param ctClass from which we extract the referenced classes from.
     * @return a set of String containing all referenced classes.
     */
    private SortedSet<String> extractReferencedClasses(CtClass ctClass) {
        return new TreeSet<>(ctClass.getRefClasses());
    }

    /**
     * Extracts all packages referenced by the class represented by the CtClass.
     *
     * @param ctClass from which we extract the referenced packages from.
     * @return a set of String containing all referenced packages.
     */
    private SortedSet<String> extractReferencedPackages(CtClass ctClass) {
        SortedSet<String> packages = new TreeSet<>();
        SortedSet<String> refClasses = extractReferencedClasses(ctClass);

        refClasses.forEach(packAge -> packages.add(extractPackageName(packAge)));

        return packages;
    }

    /**
     * Extracts all methods and classes referenced by the method or constructor represented by the CtBehavior.
     *
     * @param ctBehavior from which we extract the referenced methods from.
     * @return BehaviorInformation containing all references.
     * @throws CannotCompileException CtBehavior body cannot be compiled
     * @throws NotFoundException      if parameter types not set in CtBehavior
     */
    private BehaviorInformation createBehaviorInformation(CtBehavior ctBehavior) throws CannotCompileException, NotFoundException {
        CtBehaviorBodyAnalyzer ctBehaviorBodyAnalyzer = new CtBehaviorBodyAnalyzer();
        ctBehaviorBodyAnalyzer.analyse(ctBehavior);

        SortedSet<String> referencedClasses = ctBehaviorBodyAnalyzer.getReferencedClasses();
        SortedSet<String> referencedPackages = new TreeSet<>();

        referencedClasses.forEach(referencedClass -> referencedPackages.add(extractPackageName(referencedClass)));

        if (ctBehavior instanceof CtConstructor) {
            return new ConstructorInformation(ctBehavior.getLongName(), referencedPackages, referencedClasses, ctBehaviorBodyAnalyzer.getReferencedMethods());
        }
        return new MethodInformation(ctBehavior.getLongName(), referencedPackages, referencedClasses, ctBehaviorBodyAnalyzer.getReferencedMethods());
    }
}
