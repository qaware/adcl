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
        SortedSet<PackageInformation> packageDependencies = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> classDependencies = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<MethodInformation> methodDependencies = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST, false));
        sut = new MethodInformation(TEST_TEST_METHOD, packageDependencies, classDependencies, methodDependencies, false);
    }

    @Test
    void getMethodName() {
        assertThat(sut.getName()).isEqualTo(TEST_TEST_METHOD);
    }

    @Test
    void getPackageDependencies() {
        assertThat(sut.getPackageDependencies().first().getPackageName()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getClassDependencies() {
        assertThat(sut.getClassDependencies().first().getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getMethodDependencies() {
        assertThat(sut.getMethodDependencies().first().getName()).isEqualTo(TEST_CLASS_TEST);
    }
}