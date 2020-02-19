package com.github.qaware.adcl;


import com.github.qaware.adcl.information.ProjectInformation;
import com.github.qaware.adcl.information.RootInformation;
import com.github.qaware.adcl.information.VersionInformation;
import com.github.qaware.adcl.pm.MavenProjectManager;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class PomDependencyExtractorTest {
    @Test
    void test() throws MavenInvocationException {
        RootInformation root = new RootInformation();
        ProjectInformation proj = new ProjectInformation(root, "proj", true, "v1");
        Path basedir = Paths.get("src", "test", "resources", "pom");
        PomDependencyExtractor.updatePomDependencies(new MavenProjectManager(basedir, basedir.resolve("pom.xml")), proj.getLatestVersion());

        assertThat(proj.getPomDependencies(proj.getLatestVersion()).stream().map(VersionInformation::toString)).containsExactlyInAnyOrder(
                "org-javassist:javassist@3.26.0-GA",
                "org-neo4j:neo4j-ogm-bolt-driver@3.2.2",
                "com-j2html:j2html@1.4.0",
                "com-google-guava:guava@28.1-jre",
                "org-slf4j:slf4j-simple@1.7.29",
                "org-jetbrains:annotations@17.0.0",
                "org-springframework-boot:spring-boot-starter-data-neo4j@2.2.1.RELEASE",
                "org-springframework-boot:spring-boot-starter-test@2.2.1.RELEASE",
                "org-apache-maven:maven-plugin-api@3.0.5",
                "org-neo4j:neo4j@3.5.13",
                "org-junit-jupiter:junit-jupiter@5.5.2",
                "org-assertj:assertj-core@3.14.0",
                "org-assertj:assertj-guava@3.2.1"
        );
    }
}
