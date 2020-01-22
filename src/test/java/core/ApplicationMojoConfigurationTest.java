package core;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.ConfigurationException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationMojoConfigurationTest extends AbstractMojoTestCase {

    public void test() throws Exception {
        ApplicationMojo mojo = (ApplicationMojo) lookupMojo("start", "src/test/resources/pom3/pom.xml");
        ApplicationMojo mojo2 = (ApplicationMojo) lookupMojo("start", "src/test/resources/pom4/pom.xml");

        try {
            mojo.execute();
        } catch (Exception ignored) {
        }
        assertEquals(Config.get("spring.data.neo4j.uri", "NA"), "bolt://localhost:7687");
        assertEquals(Config.get("spring.data.neo4j.username", "NA"), "");
        assertEquals(Config.get("spring.data.neo4j.password", "NA"), "");
        assertEquals(Config.get("project.commit.current", "NA"), "test");
        assertEquals(Config.get("project.uri", "NA"), "src/test/resources/test classfiles3/epro1");

        try {
            mojo2.execute();
        } catch (Exception ignored) {
        }
        assertEquals(Config.get("spring.data.neo4j.uri", "NA"), "bolt://localhost:7687");
        assertEquals(Config.get("spring.data.neo4j.username", "NA"), "neo4j\"");
        assertEquals(Config.get("spring.data.neo4j.password", "NA"), "test");
        assertEquals(Config.get("project.commit.current", "NA"), "test2");
        assertEquals(Config.get("project.commit.previous", "NA"), "test");
        assertEquals(Config.get("project.uri", "NA"), "src\\test\\resources\\testclassfiles3\\epro2");
    }

    public void testMojo() throws Exception {
        Exception e = runMojo(lookupMojo("start", Paths.get("src", "test", "resources", "pom2", "pom.xml")));
        assertNotNull(e);
        assertNotNull(e.getCause());
        assertEquals(e.getCause().getMessage(), "project.uri is not properly defined in config.properties");
    }

    public Mojo lookupMojo(String goal, Path configPath) throws Exception {
        PlexusConfiguration configuration = null;
        if (configPath != null) {
            try {
                Xpp3Dom thisProject = Xpp3DomBuilder.build(ReaderFactory.newXmlReader(new File("pom.xml")));
                configuration = extractPluginConfiguration(thisProject.getChild("artifactId").getValue(), Xpp3DomBuilder.build(ReaderFactory.newXmlReader(configPath.toFile())));
            } catch (ConfigurationException ignored) {

            }
        }
        return lookupMojo(goal, configuration);
    }

    public Mojo lookupMojo(String goal, PlexusConfiguration configuration) throws Exception {
        Xpp3Dom thisProject = Xpp3DomBuilder.build(ReaderFactory.newXmlReader(new File("pom.xml")));
        return lookupMojo(thisProject.getChild("groupId").getValue(), thisProject.getChild("artifactId").getValue(), thisProject.getChild("version").getValue(), goal, configuration);
    }

    @Nullable
    public Exception runMojo(@NotNull Mojo mojo) {
        try {
            mojo.execute();
            return null;
        } catch (MojoExecutionException | MojoFailureException ex) {
            return ex;
        }
    }
}