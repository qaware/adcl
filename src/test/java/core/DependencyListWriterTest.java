package core;

import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import core.information.VersionInformation;
import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Deprecated, will no longer be maintained")
@Deprecated
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

        Set<PackageInformation> packageDependencies = new TreeSet<>();
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        Set<ClassInformation> classDependencies = new TreeSet<>();
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        Set<MethodInformation> methodDependencies = new TreeSet<>();
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST_2, false));
        Set<MethodInformation> methodInformations = new TreeSet<>();
        methodInformations.add(new MethodInformation(TEST_CLASS, packageDependencies, classDependencies, methodDependencies, true));
        methodInformations.add(new MethodInformation(TEST_CLASS_TEST_1, packageDependencies, classDependencies, methodDependencies, false));

        classInformation = new ClassInformation(TEST_TEST_CLASS, methodInformations, false);
        packageInformation = new PackageInformation(TEST_PACKAGE);
        packageInformation.addClassInformation(classInformation);
        dependencyExtractor = new DependencyExtractor();

        Config.load(Arrays.append(new String[0], "project.commit.current=test"));
    }

    @Test
    void writeListToFile() throws IOException {
        List<String> classes = new ArrayList<>();
        Path path = Files.createTempDirectory("DependencyWriterTest");

        classes.add("src/test/resources/testclassfiles/Testclass.class");
        VersionInformation analysedClasses = dependencyExtractor.analyseClasses(classes);

        DependencyListWriter.writeListToFile(analysedClasses.getPackageInformations(), path.toString(), "writeListToFileResult");
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