package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class MethodInformationTest {
    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST = "TestClass.test";
    private static final String TEST_TEST_METHOD = "Test.testMethod";
    private MethodInformation sut;

    @BeforeEach
    void setUp() {
        Set<PackageInformation> packageDependencies = new TreeSet<>();
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        Set<ClassInformation> classDependencies = new TreeSet<>();
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        Set<MethodInformation> methodDependencies = new TreeSet<>();
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST, false));
        sut = new MethodInformation(TEST_TEST_METHOD, packageDependencies, classDependencies, methodDependencies, false);
    }

    @Test
    void getMethodName() {
        assertThat(sut.getName()).isEqualTo(TEST_TEST_METHOD);
    }

    @Test
    void getPackageDependencies() {
        assertThat(sut.getPackageDependencies().iterator().next().getPackageName()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getClassDependencies() {
        assertThat(sut.getClassDependencies().iterator().next().getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getMethodDependencies() {
        assertThat(sut.getMethodDependencies().iterator().next().getName()).isEqualTo(TEST_CLASS_TEST);
    }
}