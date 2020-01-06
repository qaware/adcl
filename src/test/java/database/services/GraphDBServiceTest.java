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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@Transactional
@ActiveProfiles(profiles = "test")
public class GraphDBServiceTest {
    private static final String SRC_TEST_RESOURCES_TESTCLASSFILES = "src/test/resources/testclassfiles/Testclass.class";

    private static Collection<PackageInformation> packages;
    private static GraphDatabaseService dbService;

    @Autowired
    GraphDBService graphDBService;

    @BeforeAll
    static void setup() {
        BoltConnector bolt = new BoltConnector("0");

        dbService = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File("neo4j"))
                .setConfig(bolt.type, "BOLT")
                .setConfig(bolt.enabled, "true")
                .setConfig(bolt.listen_address, "localhost:7687")
                .newGraphDatabase();

        packages = new DependencyExtractor().analyseClasses(Collections.singletonList(SRC_TEST_RESOURCES_TESTCLASSFILES));
    }

    @AfterAll
    static void cleanup() throws IOException {
        dbService.shutdown();
        FileSystemUtils.deleteRecursively(new File("neo4j"));
        FileSystemUtils.deleteRecursively(new File("certificates"));
        Files.deleteIfExists(Paths.get("store_lock"));
    }

    @Test
    void saveAllNodesTest() {

        graphDBService.saveAllNodes(packages);

        PackageInformation testPackage = graphDBService.getPackageRepository().findByPackageName("testclasses");
        assertThat(testPackage).isNotNull();
        assertThat(testPackage).isInstanceOf(PackageInformation.class);
        assertThat(testPackage).isEqualTo(packages.stream()
                .filter(packageInformation -> packageInformation.getPackageName().equals("testclasses"))
                .findFirst().orElse(null));

        ClassInformation testClass = testPackage.getClassInformations().first();
        assertThat(testClass).isEqualTo(graphDBService.getClassRepository().findByClassName(testClass.getClassName()));

        MethodInformation testMethod = testClass.getMethodInformations().first();
        assertThat(testMethod).isEqualTo(graphDBService.getMethodRepository().findByName(testMethod.getName()));

        graphDBService.getPackageRepository().deleteAll(packages);
    }

    @Test
    void saveChangelogTest() {
        Collection<PackageInformation> changes = new DiffExtractor(packages, new ArrayList<>()).getChangelist();
        ChangelogInformation changelogInformation = new ChangelogInformation(changes, null, null);
        graphDBService.saveChangelog(changelogInformation);

        changelogInformation = graphDBService.getChangeLogRepository().findAll().iterator().next();
        assertThat(changelogInformation).isNotNull();
        assertThat(changelogInformation.getChangelog()).isNotEmpty();

        PackageInformation testPackage = graphDBService.getPackageRepository().findByPackageName("testclasses");
        assertThat(testPackage).isNotNull();
        assertThat(testPackage).isInstanceOf(PackageInformation.class);

        ClassInformation testClass = testPackage.getClassInformations().first();
        assertThat(testClass).isEqualTo(graphDBService.getClassRepository().findByClassName(testClass.getClassName()));

        MethodInformation testMethod = testClass.getMethodInformations().first();
        assertThat(testMethod).isEqualTo(graphDBService.getMethodRepository().findByName(testMethod.getName()));

        assertThat(testMethod.getMethodDependencies()).hasOnlyElementsOfType(ChangelogDependencyInformation.class);

        graphDBService.getChangeLogRepository().delete(changelogInformation);
    }

    @Test
    void saveVersionTest() {
        graphDBService.saveVersion(new VersionInformation(packages, "test"));
        VersionInformation version = graphDBService.getVersionRepository().findVersionInformationByVersionName("test");
        assertThat(version).isNotNull();

        PackageInformation testPackage = version.getPackageInformations().stream()
                .filter(packageInformation -> packageInformation.getPackageName().equals("testclasses")).findFirst().orElse(null);
        assertThat(testPackage).isNotNull();
        assertThat(testPackage).isInstanceOf(PackageInformation.class);
        assertThat(testPackage).isEqualTo(packages.stream()
                .filter(packageInformation -> packageInformation.getPackageName().equals("testclasses"))
                .findFirst().orElse(null));

        ClassInformation testClass = testPackage.getClassInformations().first();
        assertThat(testClass).isEqualTo(graphDBService.getClassRepository().findByClassName(testClass.getClassName()));

        MethodInformation testMethod = testClass.getMethodInformations().first();
        assertThat(testMethod).isEqualTo(graphDBService.getMethodRepository().findByName(testMethod.getName()));

        graphDBService.getVersionRepository().delete(version);
    }
}
