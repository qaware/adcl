package core;


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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.NameParserUtil;

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
     * @param classFile path to the classfile
     * @return the corresponding CTClass
     * @throws IOException then the class file on the given is not found
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
        createMethodInformations(ctClass);
    }

    /**
     * Extracts CtMethods from the CtClass and creates for a MethodInformation.
     *
     * @param ctClass CtClass from which CtMethods are taken from.
     */
    private void createMethodInformations(CtClass ctClass) {

        CtBehavior[] ctMethods = ctClass.getDeclaredBehaviors();
        Arrays.stream(ctMethods).forEach(ctBehavior -> {
            try {
                createMethodInformation(ctBehavior);
            } catch (CannotCompileException | NotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    /**
     * Extracts all methods and classes referenced by the method or constructor represented by the CtMethod.
     *
     * @param ctMethods from which we extract the referenced methods from.
     * @throws CannotCompileException CtMethod body cannot be compiled
     * @throws NotFoundException      if parameter types not set in CtMethods
     */
    private void createMethodInformation(CtBehavior ctMethods) throws CannotCompileException, NotFoundException {
        CtMethodBodyAnalyzer ctMethodBodyAnalyzer = new CtMethodBodyAnalyzer();
        ctMethodBodyAnalyzer.analyse(ctMethods);
        MethodInformation methodInformation;
        if (ctMethods instanceof CtConstructor) {
            methodInformation = dependencyPool.getOrCreateMethodInformation(ctMethods.getLongName(), true);
        } else {
            methodInformation = dependencyPool.getOrCreateMethodInformation(ctMethods.getLongName(), false);
        }

        methodInformation.setClassDependencies(ctMethodBodyAnalyzer.getClassDependencies());
        methodInformation.setMethodDependencies(ctMethodBodyAnalyzer.getMethodDependencies());

        SortedSet<PackageInformation> packageDependencies = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        ctMethodBodyAnalyzer.getClassDependencies().forEach(classDependency -> packageDependencies.add(dependencyPool.getOrCreatePackageInformation(NameParserUtil.extractPackageName(classDependency.getClassName()))));
        methodInformation.setPackageDependencies(packageDependencies);
    }
}
