package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ClassInformationTest {

    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST_2 = "TestClass.test2";
    private static final String TEST_CLASS = "TestClass";
    private static final String TEST_CLASS_TEST_1 = "TestClass.test1";
    private ClassInformation sut;

    @BeforeEach
    void setUp() {
        SortedSet<String> referencedPackages = new TreeSet<>();
        referencedPackages.add(TEST_PACKAGE_2);
        SortedSet<String> referencedClasses = new TreeSet<>();
        referencedClasses.add(TEST_TEST_CLASS);
        SortedSet<String> referencedMethods = new TreeSet<>();
        referencedMethods.add(TEST_CLASS_TEST_2);
        SortedSet<ConstructorInformation> constructorInformations = new TreeSet<>();
        constructorInformations.add(new ConstructorInformation(TEST_CLASS, referencedPackages, referencedClasses, referencedMethods));
        SortedSet<MethodInformation> methodInformations = new TreeSet<>();
        methodInformations.add(new MethodInformation(TEST_CLASS_TEST_1, referencedPackages, referencedClasses, referencedMethods));
        sut = new ClassInformation(TEST_TEST_CLASS, referencedPackages, referencedClasses, constructorInformations, methodInformations);
    }

    @Test
    void getClassName() {
        assertThat(sut.getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getReferencedPackages() {
        assertThat(sut.getReferencedPackages().first()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getReferencedClasses() {
        assertThat(sut.getReferencedClasses().first()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getConstructorInformations() {
        assertThat(sut.getConstructorInformations().iterator().next().getConstructorSignature()).isEqualTo(TEST_CLASS);
    }

    @Test
    void getMethodInformations() {
        assertThat(sut.getMethodInformations().iterator().next().getMethodName()).isEqualTo(TEST_CLASS_TEST_1);
    }

    @Test
    void getReferencedMethods() {
        assertThat(sut.getReferencedMethods().first()).isEqualTo(TEST_CLASS_TEST_2);
    }
}