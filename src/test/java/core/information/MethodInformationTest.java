package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;

class MethodInformationTest {
    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST = "TestClass.test";
    private static final String TEST_TEST_METHOD = "Test.testMethod";
    private MethodInformation sut;

    @BeforeEach
    void setUp() {
        SortedSet<PackageInformation> referencedPackages = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        referencedPackages.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> referencedClasses = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        referencedClasses.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<MethodInformation> referencedMethods = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        referencedMethods.add(new MethodInformation(TEST_CLASS_TEST, false));
        sut = new MethodInformation(TEST_TEST_METHOD, referencedPackages, referencedClasses, referencedMethods, false);
    }

    @Test
    void getMethodName() {
        assertThat(sut.getName()).isEqualTo(TEST_TEST_METHOD);
    }

    @Test
    void getReferencedPackages() {
        assertThat(sut.getReferencedPackages().first().getPackageName()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getReferencedClasses() {
        assertThat(sut.getReferencedClasses().first().getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getReferencedMethods() {
        assertThat(sut.getReferencedMethods().first().getName()).isEqualTo(TEST_CLASS_TEST);
    }
}