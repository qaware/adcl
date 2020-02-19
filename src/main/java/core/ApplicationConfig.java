package core;

import core.pm.MavenProjectManager;
import core.pm.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.config.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class ApplicationConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * the base directory for project analysis. All further relative path configurations start from there
     */
    public final Path basedir = Config.getBasedir();

    /**
     * The name of the previous version of the project, if given. Does not load from database
     */
    @Nullable
    public final String previousVersionName = getPreviousVersionName();
    /**
     * The configuration for establishing a neo4j database connection
     */
    @NotNull
    public final Configuration neo4jConfig = getNeo4jConfig();
    /**
     * The selected project manager or null if none selected or none available
     */
    @Nullable
    public final ProjectManager projectManager = getProjectManager();

    /**
     * The location of the class files to analyse
     * Ensured that it is an existing directory with class files and correct package headers in it (random sample)
     */
    @NotNull
    public final Path scanLocation = getScanLocation();

    /**
     * The project name
     */
    @NotNull
    public final String projectName = getProjectName();

    /**
     * The name of the current (to be analyzed) version of the project
     */
    @NotNull
    public final String currentVersionName = getCurrentVersionName();

    /**
     * Whether to create only a diff artifact for local use
     */
    public final boolean localOnly = Config.get("local", false);

    /**
     * Where to place the static report. Existing directory ensured
     */
    public final Path reportPath = getReportPath();

    @SuppressWarnings("java:S1130" /* wrong, ConfigurationException can be thrown in field initialization */)
    ApplicationConfig() throws ConfigurationException {
    }

    // GETTERS ONLY

    private Path getReportPath() throws ConfigurationException {
        Path result = Config.getPath("report.path", null);
        if (result == null) {
            String raw = Config.get("report.path", null);
            if (raw == null) {
                result = projectManager == null ? basedir : projectManager.getArtifactOutput();
            } else {
                throw new ConfigurationException("report.path not valid. Is: {}", raw);
            }
        } else if (!Files.isDirectory(result)) {
            throw new ConfigurationException("report.path does not point to a directory. Is: {}", result);
        }
        return result;
    }

    @NotNull
    private static String readPackageOfClass(Path classPath) throws IOException {
        ClassNode classNode = new ClassNode();
        new ClassReader(Files.newInputStream(classPath)).accept(classNode, 0);
        int index = classNode.name.lastIndexOf('/');
        return (index < 0 ? "" : classNode.name.substring(0, index)).replace('/', '.');
    }

    @Nullable
    private Path getProjectPom() throws ConfigurationException {
        Path result = Config.getPath("project.pom", null);
        if (result == null) {
            String raw = Config.get("project.pom", null);
            if (raw == null) {
                result = basedir.resolve("pom.xml");
            } else {
                throw new ConfigurationException("project.pom not valid. Is: {}", raw);
            }
        }
        if (!Files.exists(result)) return null;
        if (!Files.isRegularFile(result))
            throw new ConfigurationException("project.uri does not point to a file. Is: {}", result);
        return result;
    }

    @Nullable
    private ProjectManager getProjectManager() throws ConfigurationException {
        Path pomPath = getProjectPom();
        if (pomPath != null) {
            LOGGER.info("Loading maven project data...");
            try {
                MavenProjectManager result = new MavenProjectManager(basedir, pomPath);
                LOGGER.info("Done");
                return result;
            } catch (Exception e /* generalized to catch RuntimeException like InvalidPathException */) {
                throw new ConfigurationException("Could not create maven project manager", e);
            }
        } else return null;
    }

    private void validateScanLocation(@NotNull Path scanLocation) throws ConfigurationException {
        try (Stream<Path> walker = Files.walk(scanLocation)) {
            Path classFile = walker.filter(p -> p.toString().endsWith(".class")).findAny().orElseThrow(() -> new ConfigurationException("project.uri contains no class files"));
            String actualPackageName = readPackageOfClass(classFile);
            String expectedPackageName = Utils.pathToPackage(scanLocation.relativize(classFile.getParent()));
            if (!actualPackageName.equals(expectedPackageName)) {
                throw new ConfigurationException("project.uri does not point to the root of the class files. Package entry in {} does not match. Expected: {}, Actual: {}", classFile, expectedPackageName, actualPackageName);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not verify project.uri as a valid directory", e);
        }
    }

    @NotNull
    private Path getScanLocation() throws ConfigurationException {
        Path result = Config.getPath("project.uri", null);
        if (result == null) {
            String raw = Config.get("project.uri", null);
            if (raw == null) {
                if (projectManager == null) throw new ConfigurationException("Option project.uri not specified");
                result = projectManager.getClassesOutput();
            } else {
                throw new ConfigurationException("project.uri not valid. Is: {}", raw);
            }
        } else if (!Files.isDirectory(result)) {
            throw new ConfigurationException("project.uri does not point to a directory. Is: {}", result);
        }
        validateScanLocation(result);
        return result;
    }

    @NotNull
    private String getProjectName() throws ConfigurationException {
        String result = Config.get("project.name", null);
        if (result == null) {
            if (projectManager == null) throw new ConfigurationException("Option project.name not specified");
            result = projectManager.getProjectName();
        }
        return result;
    }

    @NotNull
    private String getCurrentVersionName() throws ConfigurationException {
        String result = Config.get("project.commit.current", null);
        if (result == null) {
            result = Config.get("project.commit", null);
            if (result != null)
                LOGGER.warn("Option project.commit is deprecated and should not be used anymore. Use project.commit.current instead.");
        }
        if (result == null) {
            if (projectManager == null) throw new ConfigurationException("Option project.commit not specified");
            result = projectManager.getProjectVersion();
        }
        return result;
    }

    @Nullable
    private String getPreviousVersionName() {
        return Config.get("project.commit.previous", null);
    }

    @NotNull
    private Configuration getNeo4jConfig() throws ConfigurationException {
        String uri = Config.get("spring.data.neo4j.uri", null);
        if (uri == null) {
            uri = "bolt://127.0.0.1:7687";
            LOGGER.warn("spring.data.neo4j.uri not specified, using {} instead", uri);
        }
        String username = Config.get("spring.data.neo4j.username", null);
        if (username == null) {
            username = "neo4j";
            LOGGER.warn("spring.data.neo4j.username not specified, using {} instead", username);
        }
        String password = Config.get("spring.data.neo4j.password", null);
        if (password == null) throw new ConfigurationException("spring.data.neo4j.password not specified");

        Neo4jProperties properties = new Neo4jProperties();
        properties.setUri(uri);
        properties.setUsername(username);
        properties.setPassword(password);
        return properties.createConfiguration();
    }

    // OTHER

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationConfig.class.getSimpleName() + "[", "]")
                .add("previousVersionName='" + previousVersionName + "'")
                .add("neo4jConfig=" + neo4jConfig)
                .add("projectPom=" + projectManager)
                .add("scanLocation=" + scanLocation)
                .add("projectName='" + projectName + "'")
                .add("currentVersionName='" + currentVersionName + "'")
                .toString();
    }

    /**
     * Exception to be thrown if Application configuration is incomplete or invalid
     * Instantly logs any exception
     */
    static class ConfigurationException extends Exception {
        private ConfigurationException(String message, Object... format) {
            LOGGER.error(message, format);
        }
    }
}
