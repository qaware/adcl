package core;

import core.information.*;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The DependencyExtractor can extract static dependencies from class files such as referenced packages, classes, methods.
 */
public class DependencyExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyExtractor.class);
    private final Path scanLocation;
    private final ProjectInformation project;
    private final VersionInformation version;

    private final ClassPool classPool = new ClassPool(true);

    public DependencyExtractor(Path scanLocation, ProjectInformation project, VersionInformation version) {
        this.scanLocation = scanLocation;
        this.project = project;
        this.version = version;
    }

    /**
     * Analyse classes collection.
     *
     * @throws IOException              if scanLocation is invalid or project pom exists but is invalid
     * @throws MavenInvocationException if maven fails to read dependencies of project pom
     */
    public void runAnalysis() throws IOException, MavenInvocationException {
        LOGGER.info("Updating indices...");
        project.updateIndices(scanLocation);
        LOGGER.info("Updated");
        project.getDirectChildren(version).forEach(c -> c.setExists(version, false));

        LOGGER.info("Analysing project classes...");
        analyseClasses();
        LOGGER.info("Done");
    }

    private void analyseClasses() throws IOException {
        try (Stream<Path> classes = Files.walk(scanLocation)) {
            classes.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".class")).forEach(p -> {
                try {
                    analyseClass(p);
                } catch (IOException e) {
                    LOGGER.error("Could not analyse class file {}", p);
                }
            });
        }
    }

    /**
     * Creates ClassInformation for a Class.
     *
     * @param classPath Path to class file
     * @throws IOException if class not contained in ClassPool and could not be loaded through the given path.
     */
    private void analyseClass(Path classPath) throws IOException {
        CtClass ctClass = classPool.makeClassIfNew(Files.newInputStream(classPath));
        ClassInformation<?> clInfo = (ClassInformation<?>) project.findOrCreate(ctClass.getName(), version, Information.Type.CLASS);
        clInfo.setIsService(ctClass.hasAnnotation("org.springframework.stereotype.Service"));
        Arrays.stream(ctClass.getDeclaredBehaviors()).forEach(this::analyseMethod);
    }

    /**
     * Extracts all methods and classes referenced by the method or constructor represented by the CtMethod.
     *
     * @param ctMethod from which we extract the referenced methods from.
     */
    private void analyseMethod(CtBehavior ctMethod) {
        String methodName = ctMethod instanceof CtConstructor && ((CtConstructor) ctMethod).isConstructor() ? ctMethod.getLongName().replace("(", ".<init>(") : ctMethod.getLongName();
        MethodInformation m = (MethodInformation) project.findOrCreate(methodName, version, Information.Type.METHOD);
        new CtMethodBodyAnalyzer(m, version).analyse(ctMethod);
    }
}
