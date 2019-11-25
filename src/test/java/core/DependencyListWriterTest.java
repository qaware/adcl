package core;

import core.information.ClassInformation;
import core.information.ConstructorInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DependencyListWriterTest {

    private static String expectedResultText;
    private DependencyExtractor dependencyExtractor;
    private PackageInformation packageInformation;
    private ClassInformation classInformation;

    @BeforeAll
    static void loadFiles() {
        try {
            expectedResultText = readFile("src/test/resources/txtfiles/expectedTextResultFromTestclass.txt", StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        SortedSet<String> referencedPackages = new TreeSet<>();
        referencedPackages.add("testPackage2");
        SortedSet<String> referencedClasses = new TreeSet<>();
        referencedClasses.add("test.TestClass");
        SortedSet<String> referencedMethods = new TreeSet<>();
        referencedMethods.add("TestClass.test2");
        SortedSet<ConstructorInformation> constructorInformations = new TreeSet<>();
        constructorInformations.add(new ConstructorInformation("TestClass", referencedPackages, referencedClasses, referencedMethods));
        SortedSet<MethodInformation> methodInformations = new TreeSet<>();
        methodInformations.add(new MethodInformation("TestClass.test1", referencedPackages, referencedClasses, referencedMethods));
        classInformation = new ClassInformation("test.TestClass", referencedPackages, referencedClasses, constructorInformations, methodInformations);
        packageInformation = new PackageInformation("testPackage");
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
            String generatedList = readFile("src/test/resources/txtfiles/writeListToFileResult.txt", StandardCharsets.UTF_8);
            assertThat(generatedList).isEqualTo(expectedResultText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void generateDeepListPackage() {
        String resultText = DependencyListWriter.generateDeepList(packageInformation);
        assertThat(resultText).contains("testPackage");
        assertThat(resultText).contains(DependencyListWriter.generateDeepList(classInformation));
    }

    @Test
    void generateFlatListPackage() {
        String resultText = DependencyListWriter.generateFlatList(packageInformation);
        assertThat(resultText).contains("testPackage");
        assertThat(resultText).contains(classInformation.getClassName());
        classInformation.getReferencedMethods().forEach(refMethod -> assertThat(resultText).contains(refMethod));
    }

    @Test
    void generateDeepListClass() {
        String resultText = DependencyListWriter.generateDeepList(classInformation);
        assertThat(resultText).contains("TestClass");
        assertThat(resultText).contains("TestClass.test1");
        assertThat(resultText).contains("TestClass.test2");
        assertThat(resultText).contains("testPackage2");
    }

    @Test
    void generateFlatListClass() {
        String resultText = DependencyListWriter.generateFlatList(classInformation);
        assertThat(resultText).contains("TestClass");
        assertThat(resultText).contains("TestClass.test2");
        assertThat(resultText).contains("testPackage2");
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}