package core;


import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PomDependencyReaderTest {
    private static PomDependencyReader reader;

    @BeforeAll
    static void setup() {
        reader = new PomDependencyReader("src/test/resources/pom/pom.xml");
    }

    @Test
    void test() {
        Set<String> list = reader.readDependency().stream().map(Object::toString).collect(Collectors.toSet());
        Set<String> newList = Stream.of(
                dependency("javassist", "org.javassist", "3.26.0-GA"),
                dependency("neo4j-ogm-bolt-driver", "org.neo4j", "3.2.2"),
                dependency("j2html", "com.j2html", "1.4.0"),
                dependency("guava", "com.google.guava", "28.1-jre"),
                dependency("slf4j-simple", "org.slf4j", "1.7.29"),
                dependency("annotations", "org.jetbrains", "17.0.0"),
                dependency("spring-boot-starter-data-neo4j", "org.springframework.boot", null),
                dependency("spring-boot-starter-test", "org.springframework.boot", null),
                dependency("maven-plugin-api", "org.apache.maven", "3.0.5"),
                dependency("neo4j", "org.neo4j", "3.5.13"),
                dependency("junit-jupiter", "org.junit.jupiter", "5.5.2"),
                dependency("assertj-core", "org.assertj", "3.14.0"),
                dependency("assertj-guava", "org.assertj", "3.2.1")
        ).map(Object::toString).collect(Collectors.toSet());

        assertThat(list).containsExactlyInAnyOrderElementsOf(newList);
    }

    private Dependency dependency(String artifact, String group, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(group);
        dependency.setArtifactId(artifact);
        dependency.setVersion(version);
        dependency.setType("jar");
        return dependency;
    }
}
