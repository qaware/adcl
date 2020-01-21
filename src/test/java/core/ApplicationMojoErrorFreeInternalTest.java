package core;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class ApplicationMojoErrorFreeInternalTest extends AbstractMojoTestCase {
    public void test() throws Exception {
        ApplicationMojo mojo = (ApplicationMojo) lookupMojo("start", "src/test/resources/pom3/pom.xml");
        ApplicationMojo mojo2 = (ApplicationMojo) lookupMojo("start", "src/test/resources/pom4/pom.xml");
        mojo.execute();
        mojo2.execute();
    }
}