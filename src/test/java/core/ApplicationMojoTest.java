package core;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import util.Utils;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationMojoTest {
    @Test
    public void testMojo() throws Exception {
        Pair<Integer, String> result = Utils.callMaven(Paths.get("src", "test", "resources", "pom2", "pom.xml"), null, null, "adcl:start");
        assertThat(result.getKey()).isNotZero();
        assertThat(result.getValue()).contains("spring.data.neo4j.password not specified");
    }
}
