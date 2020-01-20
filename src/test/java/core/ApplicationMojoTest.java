package core;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class ApplicationMojoTest extends AbstractMojoTestCase {
    public void testMojoGoal() throws Exception {
        File testPom = new File(getBasedir(), "/src/test/resources/pom2/pom.xml");

        Exception e = null;
        try {
            ApplicationMojo mojo = (ApplicationMojo) lookupMojo("start", testPom);
            mojo.execute();
        } catch (MojoExecutionException ex) {
            e = ex;
        }
        assertNotNull(e);
        assertEquals(e.getCause().getMessage(), "project.uri is not properly defined in config.properties");
    }
}
