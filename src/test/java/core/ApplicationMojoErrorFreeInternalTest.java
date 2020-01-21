package core;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class ApplicationMojoErrorFreeInternalTest extends AbstractMojoTestCase {

    public void test() throws Exception {
        ApplicationMojo mojo = (ApplicationMojo) lookupMojo("start", "src/test/resources/pom3/pom.xml");
        ApplicationMojo mojo2 = (ApplicationMojo) lookupMojo("start", "src/test/resources/pom4/pom.xml");

        try {
            mojo.execute();
        } catch (Exception ignored) {
        }
        assertEquals(Config.get("spring.data.neo4j.uri", "NA"), "bolt://localhost:7687");
        assertEquals(Config.get("spring.data.neo4j.username", "NA"), "neo4j");
        assertEquals(Config.get("spring.data.neo4j.password", "NA"), "test");
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
}