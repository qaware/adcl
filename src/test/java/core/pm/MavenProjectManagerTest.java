package core.pm;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenProjectManagerTest {
    @Test
    void test() throws MavenInvocationException {
        ProjectManager pm = new MavenProjectManager(Paths.get("src", "test", "resources", "pom", "pom.xml"));
        assertThat(pm.getProjectName()).isEqualTo("com-github-qaware:adcl");
        assertThat(pm.getProjectVersion()).isEqualTo("1.0-SNAPSHOT");
        assertThat(pm.getClassesOutput()).isEqualTo(Paths.get("src", "test", "resources", "pom", "target", "classes").toAbsolutePath());
        assertThat(pm.getDependencies().size()).isEqualTo(13);
        assertThat(pm.getCompileDependencies().size()).isEqualTo(47);
    }
}
