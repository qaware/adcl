package core;

import core.information.BehaviorInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyListWriterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyListWriterTest.class);
    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST_2 = "TestClass.test2";
    private static final String TEST_CLASS = "TestClass";
    private static final String TEST_CLASS_TEST_1 = "TestClass.test1";
    private static final String TEST_PACKAGE = "testPackage";
    private static String expectedResultText;
    private DependencyExtractor dependencyExtractor;
    private PackageInformation packageInformation;
    private ClassInformation classInformation;

    @BeforeAll
    static void loadFiles() {
        try {
            expectedResultText = IOUtil.readFile("src/test/resources/txtfiles/expectedTextResultFromTestclass.txt", StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {

        SortedSet<PackageInformation> referencedPackages = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        referencedPackages.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> referencedClasses = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        referencedClasses.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<BehaviorInformation> referencedBehavior = new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance());
        referencedBehavior.add(new BehaviorInformation(TEST_CLASS_TEST_2, false));
        SortedSet<BehaviorInformation> behaviorInformations = new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance());
        behaviorInformations.add(new BehaviorInformation(TEST_CLASS, referencedPackages, referencedClasses, referencedBehavior, true));
        behaviorInformations.add(new BehaviorInformation(TEST_CLASS_TEST_1, referencedPackages, referencedClasses, referencedBehavior, false));

        classInformation = new ClassInformation(TEST_TEST_CLASS, behaviorInformations, false);
        packageInformation = new PackageInformation(TEST_PACKAGE);
        packageInformation.addClassInformation(classInformation);
        dependencyExtractor = new DependencyExtractor();
    }

    @Test
    void writeListToFile() {
        List<String> classes = new ArrayList<>();
        classes.add("src/test/resources/testclassfiles/Testclass.class");
        Collection<PackageInformation> analysedClasses = dependencyExtractor.analyseClasses(classes);
        DependencyListWriter.writeListToFile(analysedClasses, "src/test/resources/txtfiles", "writeListToFileResult");

        try {
            String generatedList = IOUtil.readFile("src/test/resources/txtfiles/writeListToFileResult.txt", StandardCharsets.UTF_8);
            assertThat(generatedList).isEqualTo(expectedResultText);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Test
    void generateDeepListPackage() {
        String resultText = DependencyListWriter.generateDeepList(packageInformation);
        assertThat(resultText).contains(TEST_PACKAGE);
        assertThat(resultText).contains(DependencyListWriter.generateDeepList(classInformation));
    }

    @Test
    void generateFlatListPackage() {
        String resultText = DependencyListWriter.generateFlatList(packageInformation);
        assertThat(resultText).contains(TEST_PACKAGE);
        assertThat(resultText).contains(classInformation.getClassName());
        classInformation.getReferencedBehavior().forEach(behaviorInformation -> assertThat(resultText).contains(behaviorInformation.getName()));
    }

    @Test
    void generateDeepListClass() {
        String resultText = DependencyListWriter.generateDeepList(classInformation);
        assertThat(resultText).contains(TEST_CLASS);
        assertThat(resultText).contains(TEST_CLASS_TEST_1);
        assertThat(resultText).contains(TEST_CLASS_TEST_2);
        assertThat(resultText).contains(TEST_PACKAGE_2);
    }

    @Test
    void generateFlatListClass() {
        String resultText = DependencyListWriter.generateFlatList(classInformation);
        assertThat(resultText).contains(TEST_CLASS);
        assertThat(resultText).contains(TEST_CLASS_TEST_2);
        assertThat(resultText).contains(TEST_PACKAGE_2);
    }
}