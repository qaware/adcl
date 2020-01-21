package core;

import core.information.MethodInformation;
import core.information.PackageInformation;
import core.information.VersionInformation;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.NameParserUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The DependencyExtractor can extract static dependencies from class files such as referenced packages, classes, methods.
 */
public class DependencyExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyExtractor.class);
    private static final String COMMIT_NA = "COMMIT_NA";

    private ClassPool classPool;
    private DependencyPool dependencyPool = new DependencyPool();

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
     * @return the current VersionInformation
     */
    public VersionInformation analyseClasses(List<String> classFiles) {
        classFiles.forEach(classFilename -> {
            try {
                createClassInformation(classFilename);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });

        String currentName = COMMIT_NA;
        if (Config.valuePresent("project.commit.current")) {
            currentName = Config.get("project.commit.current", COMMIT_NA);
        } else if (Config.valuePresent("project.commit")) {
            LOGGER.warn("Option project.commit is deprecated and should not be used anymore. Use project.commit.current instead.");
            currentName = Config.get("project.commit", COMMIT_NA);
        }

        if (currentName.equals(COMMIT_NA))
            throw new NullPointerException("Commit name is missing, you have to specify it in project.commit.current");

        return new VersionInformation(dependencyPool.retrievePackageInformation(), currentName);
    }

    /**
     * Gets CtClass from the ClassPool, if the Class is not contained in the ClassPool it gets created and added to the ClassPool.
     *
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
        MethodInformation methodInformation = dependencyPool.getOrCreateMethodInformation(methodName, true);

        CtMethodBodyAnalyzer ctMethodBodyAnalyzer = new CtMethodBodyAnalyzer(dependencyPool);
        ctMethodBodyAnalyzer.analyse(ctMethod);
        methodInformation.setClassDependencies(ctMethodBodyAnalyzer.getClassDependencies());
        methodInformation.setMethodDependencies(ctMethodBodyAnalyzer.getMethodDependencies());

        Set<PackageInformation> packageDependencies = new TreeSet<>();
        ctMethodBodyAnalyzer.getClassDependencies().forEach(classDependency -> packageDependencies.add(dependencyPool.getOrCreatePackageInformation(NameParserUtil.extractPackageName(classDependency.getClassName()))));
        methodInformation.setPackageDependencies(packageDependencies);
    }
}
