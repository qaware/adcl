package core;

import core.information.MethodInformation;
import core.information.PackageInformation;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.NameParserUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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

        Arrays.stream(ctClass.getDeclaredBehaviors()).forEach(this::createMethodInformation);
    }

    /**
     * Extracts all methods and classes referenced by the method or constructor represented by the CtMethod.
     *
     * @param ctMethod from which we extract the referenced methods from.
     */
    private void createMethodInformation(CtBehavior ctMethod) {
        String methodName = ctMethod instanceof CtConstructor && ((CtConstructor) ctMethod).isConstructor() ? ctMethod.getLongName().replace("(", ".<init>(") : ctMethod.getLongName();
        MethodInformation methodInformation = dependencyPool.getOrCreateMethodInformation(methodName);

        CtMethodBodyAnalyzer ctMethodBodyAnalyzer = new CtMethodBodyAnalyzer();
        ctMethodBodyAnalyzer.analyse(ctMethod);
        methodInformation.setClassDependencies(ctMethodBodyAnalyzer.getClassDependencies());
        methodInformation.setMethodDependencies(ctMethodBodyAnalyzer.getMethodDependencies());

        SortedSet<PackageInformation> packageDependencies = new TreeSet<>();
        ctMethodBodyAnalyzer.getClassDependencies().forEach(classDependency -> packageDependencies.add(dependencyPool.getOrCreatePackageInformation(NameParserUtil.extractPackageName(classDependency.getClassName()))));
        methodInformation.setPackageDependencies(packageDependencies);
    }
}
