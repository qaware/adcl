package core.information;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PackageInformationTest {
    private PackageInformation sut;
    private static ClassInformation classInformation;

    @BeforeAll
    private static void createClassInformation() {
        SortedSet<String> referencedPackages = new TreeSet<>();
        referencedPackages.add("testPackage2");
        SortedSet<String> referencedClasses = new TreeSet<>();
        referencedClasses.add("test.TestClass");
        SortedSet<String> referencedMethods = new TreeSet<>();
        referencedMethods.add("TestClass.test");
        SortedSet<ConstructorInformation> constructorInformations = new TreeSet<>();
        constructorInformations.add(new ConstructorInformation("TestClass", referencedPackages, referencedClasses, referencedMethods));
        SortedSet<MethodInformation> methodInformations = new TreeSet<>();
        methodInformations.add(new MethodInformation("TestClass.test", referencedPackages, referencedClasses, referencedMethods));
        classInformation = new ClassInformation("test.TestClass", referencedPackages, referencedClasses, constructorInformations, methodInformations);
    }

    @BeforeEach
    void setUp() {
        sut = new PackageInformation("testPackage");
        sut.addClassInformation(classInformation);
    }

    @Test
    void getPackageName() {
        assertThat(sut.getPackageName()).isEqualTo("testPackage");
    }

    @Test
    void getReferencedPackages() {
        assertThat(sut.getReferencedPackages().first()).isEqualTo("testPackage2");
    }

    @Test
    void getReferencedClasses() {
        sut.addClassInformation(classInformation);
        assertThat(sut.getReferencedClasses().first()).isEqualTo("test.TestClass");
    }

    @Test
    void getReferencedMethods() {
        assertThat(sut.getReferencedMethods().first()).isEqualTo("TestClass.test");
    }
}