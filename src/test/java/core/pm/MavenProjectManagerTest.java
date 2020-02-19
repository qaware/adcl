package core.pm;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;
import util.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenProjectManagerTest {
    @Test
    void test() throws MavenInvocationException {
        Path basedir = Paths.get("src", "test", "resources", "pom");
        ProjectManager pm = new MavenProjectManager(basedir, basedir.resolve("pom.xml"));
        assertThat(pm.getProjectName()).isEqualTo("com-github-qaware:adcl");
        assertThat(pm.getProjectVersion()).isEqualTo("1.0-SNAPSHOT");
        assertThat(Utils.isSamePath(pm.getClassesOutput(), Paths.get("src", "test", "resources", "pom", "target", "classes"))).isTrue();
        assertThat(pm.getDependencies().size()).isEqualTo(13);
        assertThat(pm.getCompileDependencies().size()).isEqualTo(47);
    }
}
