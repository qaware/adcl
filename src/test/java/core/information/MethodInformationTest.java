package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MethodInformationTest {
    private MethodInformation sut;

    @BeforeEach
    void setUp() {
        SortedSet<String> referencedPackages = new TreeSet<>();
        referencedPackages.add("testPackage2");
        SortedSet<String> referencedClasses = new TreeSet<>();
        referencedClasses.add("test.TestClass");
        SortedSet<String> referencedMethods = new TreeSet<>();
        referencedMethods.add("TestClass.test");
        sut = new MethodInformation("Test.testMethod", referencedPackages, referencedClasses, referencedMethods);
    }

    @Test
    void getMethodName() {
        assertThat(sut.getMethodName()).isEqualTo("Test.testMethod");
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
    void getReferencedMethods() {
        assertThat(sut.getReferencedMethods().first()).isEqualTo("TestClass.test");
    }
}