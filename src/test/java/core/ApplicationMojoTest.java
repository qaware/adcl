package core;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import java.io.File;

public class ApplicationMojoTest extends AbstractMojoTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    public void testMojoGoal() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/pom2/pom.xml");

        ApplicationMojo mojo = (ApplicationMojo) lookupMojo("start", testPom);

        assertNotNull(mojo);
    }
}
