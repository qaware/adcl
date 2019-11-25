package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ClassInformationTest {

    private ClassInformation sut;

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
        sut = new ClassInformation("test.TestClass", referencedPackages, referencedClasses, constructorInformations, methodInformations);
    }

    @Test
    void getClassName() {
        assertThat(sut.getClassName()).isEqualTo("test.TestClass");
    }

    @Test
    void getReferencedPackages() {
        assertThat(sut.getReferencedPackages().first()).isEqualTo("testPackage2");
    }

    @Test
    void getReferencedClasses() {
        assertThat(sut.getReferencedClasses().first()).isEqualTo("test.TestClass");
    }

    @Test
    void getConstructorInformations() {
        assertThat(sut.getConstructorInformations().iterator().next().getConstructorSignature()).isEqualTo("TestClass");
    }

    @Test
    void getMethodInformations() {
        assertThat(sut.getMethodInformations().iterator().next().getMethodName()).isEqualTo("TestClass.test1");
    }

    @Test
    void getReferencedMethods() {
        assertThat(sut.getReferencedMethods().first()).isEqualTo("TestClass.test2");
    }
}