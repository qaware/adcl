package core;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.ogm.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

public class ApplicationConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);
    @Nullable
    public final String previousVersionName = getPreviousVersionName();
    @NotNull
    public final Configuration neo4jConfig = getNeo4jConfig();
    private Model localPom = getLocalPom();
    /**
     * An existing directory with .class-Files in it
     */
    @NotNull
    public final Path scanLocation = getScanLocation();
    @NotNull
    public final String projectName = getProjectName();
    @NotNull
    public final String currentVersionName = getCurrentVersionName();

    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationConfig.class.getSimpleName() + "[", "]")
                .add("previousVersionName='" + previousVersionName + "'")
                .add("neo4jConfig=" + neo4jConfig)
                .add("localPom=" + localPom)
                .add("scanLocation=" + scanLocation)
                .add("projectName='" + projectName + "'")
                .add("currentVersionName='" + currentVersionName + "'")
                .toString();
    }

    @SuppressWarnings("java:S1130" /* wrong, ConfigurationException can be thrown in field initialization */)
    ApplicationConfig() throws ConfigurationException {
    }

    private Model getLocalPom() throws ConfigurationException {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read(Files.newBufferedReader(Paths.get("pom.xml")));
        } catch (IOException | XmlPullParserException e) {
            throw new ConfigurationException("Could not open project pom.xml! Working directory has to be root of project to be analyzed", e);
        }
    }

    @NotNull
    private Path getScanLocation() throws ConfigurationException {
        Path result = Config.getPath("project.uri", null);
        if (result == null) {
            String raw = Config.get("project.uri", null);
            if (raw == null) {
                String output = localPom.getBuild().getOutputDirectory();
                if (output == null) output = "target/classes"; // as stated in jdoc of Build#getOutputDirectory()
                return Paths.get(output);
            } else {
                throw new ConfigurationException("project.uri not valid. Is: {}", raw);
            }
        } else if (!Files.isDirectory(result)) {
            throw new ConfigurationException("project.uri does not point to a directory. Is: {}", result);
        }
        return result;
    }

    @NotNull
    private String getProjectName() {
        return Config.get("project.name", localPom.getArtifactId());
    }

    @NotNull
    private String getCurrentVersionName() {
        String result = Config.get("project.commit.current", null);
        if (result == null) {
            result = Config.get("project.commit", null);
            if (result != null)
                LOGGER.warn("Option project.commit is deprecated and should not be used anymore. Use project.commit.current instead.");
        }
        if (result == null) {
            result = localPom.getVersion();
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

    static class ConfigurationException extends Exception {
        private ConfigurationException(String message, Object... format) {
            LOGGER.error(message, format);
        }

        private ConfigurationException(String message, Throwable ex) {
            LOGGER.error(message, ex);
        }
    }
}
