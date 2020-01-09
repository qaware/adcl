package core.information;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class PackageInformationTest {
    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST = "TestClass.test";
    private static final String TEST_CLASS = "TestClass";
    private static final String TEST_PACKAGE = "testPackage";
    private PackageInformation sut;
    private static ClassInformation classInformation;

    @BeforeAll
    private static void createClassInformation() {

        Set<PackageInformation> packageDependencies = new TreeSet<>();
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        Set<ClassInformation> classDependencies = new TreeSet<>();
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        Set<MethodInformation> methodDependencies = new TreeSet<>();
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST, false));
        Set<MethodInformation> methodInformations = new TreeSet<>();
        methodInformations.add(new MethodInformation(TEST_CLASS, packageDependencies, classDependencies, methodDependencies, true));
        methodInformations.add(new MethodInformation(TEST_CLASS_TEST, packageDependencies, classDependencies, methodDependencies, false));

        classInformation = new ClassInformation(TEST_TEST_CLASS, methodInformations, false);
    }

    @BeforeEach
    void setUp() {
        sut = new PackageInformation(TEST_PACKAGE);
        sut.addClassInformation(classInformation);
    }

    @Test
    void getPackageName() {
        assertThat(sut.getPackageName()).isEqualTo(TEST_PACKAGE);
    }

    @Test
    void getPackageDependencies() {
        assertThat(sut.getPackageDependencies().iterator().next().getPackageName()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getClassDependencies() {
        sut.addClassInformation(classInformation);
        assertThat(sut.getClassDependencies().iterator().next().getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getMethodDependencies() {
        assertThat(sut.getMethodDependencies().iterator().next().getName()).isEqualTo(TEST_CLASS_TEST);
    }
}