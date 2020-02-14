package core;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import util.MojoTestUtil;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationMojoTest {
    @Test
    public void testMojo() throws Exception {
        try (MojoTestUtil testUtil = new MojoTestUtil(Paths.get("pom.xml"), Paths.get("localRepo"))) {
            Pair<Integer, String> result = testUtil.runAdclOnPom(Paths.get("src", "test", "resources", "pom2", "pom.xml"));
            assertThat(result.getKey()).isNotZero();
            assertThat(result.getValue()).contains("spring.data.neo4j.password not specified");
        }
    }
}
