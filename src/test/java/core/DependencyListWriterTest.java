package core;

import core.information.MethodInformation;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        SortedSet<PackageInformation> packageDependencies = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> classDependencies = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<MethodInformation> methodDependencies = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST_2, false));
        SortedSet<MethodInformation> methodInformations = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodInformations.add(new MethodInformation(TEST_CLASS, packageDependencies, classDependencies, methodDependencies, true));
        methodInformations.add(new MethodInformation(TEST_CLASS_TEST_1, packageDependencies, classDependencies, methodDependencies, false));

        classInformation = new ClassInformation(TEST_TEST_CLASS, methodInformations, false);
        packageInformation = new PackageInformation(TEST_PACKAGE);
        packageInformation.addClassInformation(classInformation);
        dependencyExtractor = new DependencyExtractor();
    }

    @Test
    void writeListToFile() throws IOException {
        List<String> classes = new ArrayList<>();
        Path path = Files.createTempDirectory("DependencyWriterTest");

        classes.add("src/test/resources/testclassfiles/Testclass.class");
        Collection<PackageInformation> analysedClasses = dependencyExtractor.analyseClasses(classes);

        DependencyListWriter.writeListToFile(analysedClasses, path.toString(), "writeListToFileResult");
        String generatedList = IOUtil.readFile(Paths.get(path.toString(), "writeListToFileResult.txt").toString(), StandardCharsets.UTF_8);

        assertThat(generatedList).isEqualTo(expectedResultText);
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
        classInformation.getMethodDependencies().forEach(methodInformation -> assertThat(resultText).contains(methodInformation.getName()));
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