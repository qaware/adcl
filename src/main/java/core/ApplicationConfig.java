package core;

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
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class ApplicationConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

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
     * The location of the project's pom.xml, existence ensured, else null
     */
    @Nullable
    public final Path projectPom = getProjectPom();

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

    @SuppressWarnings("java:S1130" /* wrong, ConfigurationException can be thrown in field initialization */)
    ApplicationConfig() throws ConfigurationException {
    }

    // GETTERS ONLY

    @Nullable
    private Path getProjectPom() throws ConfigurationException {
        Path result = Config.getPath("project.pom", null);
        if (result == null) {
            String raw = Config.get("project.pom", null);
            if (raw == null) {
                result = Paths.get("pom.xml");
            } else {
                throw new ConfigurationException("project.pom not valid. Is: {}", raw);
            }
        }
        if (!Files.exists(result)) return null;
        if (!Files.isRegularFile(result))
            throw new ConfigurationException("project.uri does not point to a file. Is: {}", result);
        return result;
    }

    @NotNull
    private Path getScanLocation() throws ConfigurationException {
        Path result = Config.getPath("project.uri", null);
        if (result == null) {
            String raw = Config.get("project.uri", null);
            if (raw == null) {
                result = tryGetScanLocationByPom();
            } else {
                throw new ConfigurationException("project.uri not valid. Is: {}", raw);
            }
        } else if (!Files.isDirectory(result)) {
            throw new ConfigurationException("project.uri does not point to a directory. Is: {}", result);
        }
        validateScanLocation(result);
        return result;
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
    private String readPackageOfClass(Path classPath) throws IOException {
        ClassNode classNode = new ClassNode();
        new ClassReader(Files.newInputStream(classPath)).accept(classNode, 0);
        int index = classNode.name.lastIndexOf('/');
        return (index < 0 ? "" : classNode.name.substring(0, index)).replace('/', '.');
    }

    @NotNull
    private Path tryGetScanLocationByPom() throws ConfigurationException {
        if (projectPom == null) throw new ConfigurationException("Option project.uri not specified");
        String output = Utils.getMavenVar(projectPom, "project.build.outputDirectory");
        if (output == null) output = "target/classes";
        return Paths.get(output);
    }

    @NotNull
    private String getProjectName() throws ConfigurationException {
        String result = Config.get("project.name", null);
        if (result == null && projectPom != null) result = Utils.getMavenVar(projectPom, "project.artifactId");
        if (result == null) throw new ConfigurationException("Option project.name not specified");
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
        if (result == null && projectPom != null) {
            result = Utils.getMavenVar(projectPom, "project.version");
        }
        if (result == null) throw new ConfigurationException("Option project.commit not specified");
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
                .add("projectPom=" + projectPom)
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
