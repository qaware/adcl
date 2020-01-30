package core;

import core.information.ProjectInformation;
import core.information.RootInformation;
import core.information.VersionInformation;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil.Ref;
import static util.DataGenerationUtil.cio;
import static util.DataGenerationUtil.cir;
import static util.DataGenerationUtil.mi;
import static util.DataGenerationUtil.pir;
import static util.DataGenerationUtil.pis;
import static util.DataGenerationUtil.project;
import static util.DataGenerationUtil.root;

public class PomDependencyReaderTest {
    private static PomDependencyReader reader;
    private static Ref<ProjectInformation, RootInformation> projRef;

    @BeforeAll
    static void setup() {
        reader = new PomDependencyReader(Paths.get("src", "test", "resources", "pom", "pom.xml"));
        root(
                projRef = project("proj", true, "v1.0.0",
                        pir("packageA",
                                cio("ClassA", false,
                                        mi("<init>()"),
                                        mi("methodA()"),
                                        mi("methodB(packageB.ClassB)"),
                                        mi("empty()")
                                ),
                                cio("ClassABase", false,
                                        mi("empty()")
                                )
                        ),
                        pir("packageB",
                                cio("ClassB", true,
                                        mi("<init>()"),
                                        mi("<clinit>()"),
                                        mi("getInstanceA()"),
                                        mi("method(java.util.function.Predicate)"),
                                        mi("lambda$getInstanceA$0(java.lang.String)"),
                                        mi("getInstanceA(java.lang.String,int,packageA.ClassA[])")
                                ),
                                pis("emptyPackage")
                        ),
                        cir("ClassC", false,
                                mi("<init>()"),
                                mi("retrieveClassA()")
                        ),
                        cir("ExternalClass", false,
                                mi("extMethod()")
                        )
                )
        );
    }

    @Test
    void test() throws IOException, XmlPullParserException {
        Set<String> list = reader.readDependencies().stream().map(Object::toString).collect(Collectors.toSet());
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
        ProjectInformation proj = projRef.getStored();
        VersionInformation at = proj.getLatestVersion();
        reader.integrateInDataModell(proj, at);
        reader.readDependencies().forEach(x -> Assert.assertNotNull(proj.getPomDependencies(at).stream().
                filter(y -> y.getName().equals(x.getArtifactId()))));
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
