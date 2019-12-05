package database.services;

import core.Application;
import core.DependencyExtractor;
import core.information.BehaviorInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@Transactional
@ActiveProfiles(profiles = "test")
public class GraphDBServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphDBServiceTest.class);
    private static final String SRC_TEST_RESOURCES_TESTCLASSFILES = "src/test/resources/testclassfiles/Testclass.class";

    private static Collection<PackageInformation> packages;

    @Autowired
    GraphDBService graphDBService;

    @BeforeAll
    static void loadPackages() {
        packages = new DependencyExtractor().analyseClasses(Collections.singletonList(SRC_TEST_RESOURCES_TESTCLASSFILES));
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

        BehaviorInformation testBehavior = testClass.getBehaviorInformations().first();
        assertThat(testBehavior).isEqualTo(graphDBService.getBehaviorRepository().findByName(testBehavior.getName()));

        graphDBService.getPackageRepository().deleteAll(packages);
    }
}
