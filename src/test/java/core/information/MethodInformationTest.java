package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MethodInformationTest {
    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST = "TestClass.test";
    private static final String TEST_TEST_METHOD = "Test.testMethod";
    private MethodInformation sut;

    @BeforeEach
    void setUp() {
        SortedSet<String> referencedPackages = new TreeSet<>();
        referencedPackages.add(TEST_PACKAGE_2);
        SortedSet<String> referencedClasses = new TreeSet<>();
        referencedClasses.add(TEST_TEST_CLASS);
        SortedSet<String> referencedMethods = new TreeSet<>();
        referencedMethods.add(TEST_CLASS_TEST);
        sut = new MethodInformation(TEST_TEST_METHOD, referencedPackages, referencedClasses, referencedMethods);
    }

    @Test
    void getMethodName() {
        assertThat(sut.getMethodName()).isEqualTo(TEST_TEST_METHOD);
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
    void getReferencedMethods() {
        assertThat(sut.getReferencedMethods().first()).isEqualTo(TEST_CLASS_TEST);
    }
}