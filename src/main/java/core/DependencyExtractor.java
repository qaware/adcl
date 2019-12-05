package core;


import core.information.BehaviorInformation;
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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.StringNameUtil;

/**
 * The DependencyExtractor can extract static dependencies from class files such as referenced packages, classes, methods.
 */
public class DependencyExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyExtractor.class);

    private ClassPool classPool;
    private DependencyPool dependencyPool;

    /**
     * Instantiates a new DependencyExtractor.
     */
    public DependencyExtractor() {
        this.classPool = new ClassPool(true);
        this.dependencyPool = DependencyPool.getInstance();
        dependencyPool.resetDataStorage();
    }

    /**
     * Analyse classes collection.
     *
     * @param classFiles all list of class files that should be analysed for dependencies.
     * @return the collection
     */
    public Collection<PackageInformation> analyseClasses(List<String> classFiles) {
        classFiles.forEach(classFilename -> {
            try {
                createClassInformation(classFilename);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
        return dependencyPool.retrievePackageInformation();
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
     * @throws IOException if class not contained in ClassPool and could not be loaded through the given path.
     */
    private void createClassInformation(String className) throws IOException {
        CtClass ctClass = getOrCreateCtClass(className);
        boolean isService = ctClass.hasAnnotation("org.springframework.stereotype.Service");
        dependencyPool.getOrCreateClassInformation(ctClass.getName(), isService, true);
        createBehaviorInformations(ctClass);
    }

    /**
     * Extracts CtBehaviors from the CtClass and creates for a BehaviorInformation.
     *
     * @param ctClass CtClass from which CtBehaviors are taken from.
     */
    private void createBehaviorInformations(CtClass ctClass) {

        CtBehavior[] ctBehaviors = ctClass.getDeclaredBehaviors();
        Arrays.stream(ctBehaviors).forEach(ctBehavior -> {
            try {
                createBehaviorInformation(ctBehavior);
            } catch (CannotCompileException | NotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    /**
     * Extracts all methods and classes referenced by the method or constructor represented by the CtBehavior.
     *
     * @param ctBehavior from which we extract the referenced methods from.
     * @throws CannotCompileException CtBehavior body cannot be compiled
     * @throws NotFoundException      if parameter types not set in CtBehavior
     */
    private void createBehaviorInformation(CtBehavior ctBehavior) throws CannotCompileException, NotFoundException {
        CtBehaviorBodyAnalyzer ctBehaviorBodyAnalyzer = new CtBehaviorBodyAnalyzer();
        ctBehaviorBodyAnalyzer.analyse(ctBehavior);
        BehaviorInformation behaviorInformation;
        if (ctBehavior instanceof CtConstructor) {
            behaviorInformation = dependencyPool.getOrCreateBehaviorInformation(ctBehavior.getLongName(), true);
        } else {
            behaviorInformation = dependencyPool.getOrCreateBehaviorInformation(ctBehavior.getLongName(), false);
        }

        behaviorInformation.setReferencedClasses(ctBehaviorBodyAnalyzer.getReferencedClasses());
        behaviorInformation.setReferencedBehavior(ctBehaviorBodyAnalyzer.getReferencedBehavior());

        SortedSet<PackageInformation> referencedPackages = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        ctBehaviorBodyAnalyzer.getReferencedClasses().forEach(referencedClass -> referencedPackages.add(dependencyPool.getOrCreatePackageInformation(StringNameUtil.extractPackageName(referencedClass.getClassName()))));
        behaviorInformation.setReferencedPackages(referencedPackages);
    }
}
