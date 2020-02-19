package com.github.qaware.adcl.depex;

import com.github.qaware.adcl.information.VersionInformation;
import com.github.qaware.adcl.pm.ProjectManager;
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
    private final ProjectManager projectManager;

    /**
     * Instantiate a new Extractor. Does nothing except field init. Start analysis with {@link DependencyExtractor#runAnalysis()}
     *
     * @param scanLocation   the root directory where the class files are located.
     * @param version        the version the to-be-analysed class files correspond to
     * @param projectManager the location of the project pom.xml
     */
    public DependencyExtractor(@NotNull Path scanLocation, @NotNull VersionInformation version, @Nullable ProjectManager projectManager) {
        this.scanLocation = scanLocation;
        this.version = version;
        this.projectManager = projectManager;
    }

    /**
     * Analyse classes given by scanLocation. Inserts the results into given project tree, but might also create new entries on root level
     *
     * @throws IOException if scanLocation is invalid or project pom exists but is invalid
     */
    public void runAnalysis() throws IOException {
        LOGGER.info("Updating indices...");
        version.getProject().updateIndices(scanLocation, projectManager);
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
