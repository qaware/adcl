package core.depex;

import core.information.VersionInformation;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * The DependencyExtractor can extract static dependencies from class files such as referenced packages, classes, methods.
 */
public class DependencyExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyExtractor.class);
    @NotNull
    private final Path scanLocation;
    @NotNull
    private final VersionInformation version;
    @Nullable
    private final Path projectPom;

    /**
     * Instantiate a new Extractor. Does nothing except field init. Start analysis with {@link DependencyExtractor#runAnalysis()}
     *
     * @param scanLocation the root directory where the class files are located.
     * @param version      the version the to-be-analysed class files correspond to
     * @param projectPom   the location of the project pom.xml
     */
    public DependencyExtractor(@NotNull Path scanLocation, @NotNull VersionInformation version, @Nullable Path projectPom) {
        this.scanLocation = scanLocation;
        this.version = version;
        this.projectPom = projectPom;
    }

    /**
     * Analyse classes given by scanLocation. Inserts the results into given project tree, but might also create new entries on root level
     *
     * @throws IOException              if scanLocation is invalid or project pom exists but is invalid
     * @throws MavenInvocationException if maven fails to read dependencies of project pom
     */
    public void runAnalysis() throws IOException, MavenInvocationException {
        LOGGER.info("Updating indices...");
        version.getProject().updateIndices(scanLocation, projectPom);
        LOGGER.info("Updated");
        version.getProject().getDirectChildren(version).forEach(c -> c.setExists(version, false));

        LOGGER.info("Analysing project classes...");
        analyseClasses();
        LOGGER.info("Done");
    }

    /**
     * Runs the class analysis after indices are updated and next version got prepared in {@link DependencyExtractor#runAnalysis()}
     *
     * @throws IOException if scanLocation is invalid
     */
    private void analyseClasses() throws IOException {
        try (Stream<Path> classes = Files.walk(scanLocation)) {
            classes.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".class")).forEach(p -> {
                try {
                    new ClassReader(Files.newInputStream(p)).accept(new DepExClassVisitor(version), 0);
                } catch (IOException e) {
                    LOGGER.error("Could not analyse class file {}", p);
                }
            });
        }
    }
}
