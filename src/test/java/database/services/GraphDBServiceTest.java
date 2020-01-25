package database.services;

import core.Application;
import core.DependencyExtractor;
import core.DiffExtractor;
import core.information.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@Transactional
@ActiveProfiles(profiles = "test")
public class GraphDBServiceTest {
    private static final Path TESTCLASS_FOLDER = Paths.get("src", "test", "resources", "testclassfiles2");

    private static VersionInformation version;
    private static GraphDatabaseService dbService;

    @Autowired
    GraphDBService graphDBService;

    @BeforeAll
    static void setup() throws IOException {
        BoltConnector bolt = new BoltConnector("0");

        dbService = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File("neo4j"))
                .setConfig(bolt.type, "BOLT")
                .setConfig(bolt.enabled, "true")
                .setConfig(bolt.listen_address, "localhost:7687")
                .newGraphDatabase();

        version = new DependencyExtractor().analyseClasses(TESTCLASS_FOLDER, "test");
    }

    @AfterAll
    static void cleanup() throws IOException {
        dbService.shutdown();
        FileSystemUtils.deleteRecursively(new File("neo4j"));
        FileSystemUtils.deleteRecursively(new File("certificates"));
        Files.deleteIfExists(Paths.get("store_lock"));
    }

    @Test
    void saveChangelogTest() {
        Collection<PackageInformation> changes = new DiffExtractor(version, new VersionInformation(new ArrayList<>(), "Empty"))
                .getChangelogInformation().getChangelog();
        ChangelogInformation changelogInformation = new ChangelogInformation(changes, null, null);
        graphDBService.saveChangelog(changelogInformation);

        changelogInformation = graphDBService.getChangeLogRepository().findAll().iterator().next();
        assertThat(changelogInformation).isNotNull();
        assertThat(changelogInformation.getChangelog()).isNotEmpty();

        PackageInformation testPackage = changelogInformation.getChangelog().stream()
                .filter(packageInformation -> packageInformation.getPackageName().equals("packageB")).findFirst().orElse(null);
        assertThat(testPackage).isNotNull();
        assertThat(testPackage).isInstanceOf(PackageInformation.class);

        ClassInformation testClass = testPackage.getClassInformations().iterator().next();
        assertThat(testClass.getClassName()).isEqualTo("packageB.ClassB");

        MethodInformation testMethod = testClass.getMethodInformations().iterator().next();
        assertThat(testMethod.getName()).isEqualTo("packageB.ClassB.getInstanceA(java.lang.String,int,packageA.ClassA[])");

        assertThat(testMethod.getMethodDependencies()).hasOnlyElementsOfType(ChangelogDependencyInformation.class);

        graphDBService.getChangeLogRepository().delete(changelogInformation);
    }

    @Test
    void saveVersionTest() {
        graphDBService.saveVersion(version);
        VersionInformation version = graphDBService.getVersionRepository().findVersionInformationByVersionName("test");
        assertThat(version).isNotNull();

        PackageInformation testPackage = version.getPackageInformations().stream()
                .filter(packageInformation -> packageInformation.getPackageName().equals("packageA")).findFirst().orElse(null);
        assertThat(testPackage).isNotNull();
        assertThat(testPackage).isInstanceOf(PackageInformation.class);
        assertThat(testPackage).isEqualTo(version.getPackageInformations().stream()
                .filter(packageInformation -> packageInformation.getPackageName().equals("packageA"))
                .findFirst().orElse(null));

        assertThat(testPackage.getClassInformations().stream().map(ClassInformation::getClassName)).contains("packageA.ClassA");

        ClassInformation testClass = testPackage.getClassInformations().stream().filter(c -> c.getClassName().equals("packageA.ClassA")).findAny().orElse(null);
        assertThat(testClass).isNotNull();
        assertThat(testClass.getMethodInformations().stream().map(MethodInformation::getName)).contains("packageA.ClassA.empty()");

        graphDBService.getVersionRepository().delete(version);
    }

    @Test
    void analyseSame() {
        VersionInformation eproVersion = null;
        try {
            eproVersion = new DependencyExtractor().analyseClasses(Paths.get("src", "test", "resources", "testclassfiles3", "epro1"), "epro");
        } catch (IOException ignored) {
        }
        assert eproVersion != null;
        graphDBService.saveVersion(eproVersion);
        DiffExtractor diffExtractor = new DiffExtractor(eproVersion, graphDBService.getVersion("epro"));

        assertThat(diffExtractor.getChangelogInformation().getChangelog()).isEmpty();
    }
}
