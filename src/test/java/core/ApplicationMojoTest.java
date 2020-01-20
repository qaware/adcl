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

public class ApplicationMojoTest extends AbstractMojoTestCase {
    public void testMojoGoal() throws Exception {
        Exception e = runMojo(lookupMojo("start", Paths.get("src", "test", "resources", "pom2", "pom.xml")));
        assertNotNull(e);
        assertEquals(e.getCause().getMessage(), "project.uri is not properly defined in config.properties");
    }

    public Mojo lookupMojo(String goal, Path configPath) throws Exception {
        PlexusConfiguration configuration = null;
        if (configPath != null) {
            try {
                Xpp3Dom thisProject = Xpp3DomBuilder.build(ReaderFactory.newXmlReader(new File("pom.xml")));
                configuration = extractPluginConfiguration(thisProject.getChild("artifactId").getValue(), Xpp3DomBuilder.build(ReaderFactory.newXmlReader(new File("src/test/resources/pom2/pom.xml"))));
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
