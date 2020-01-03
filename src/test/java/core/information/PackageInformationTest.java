package core.information;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;

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

        SortedSet<PackageInformation> packageDependencies = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> classDependencies = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<MethodInformation> methodDependencies = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST, false));
        SortedSet<MethodInformation> methodInformations = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
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
        assertThat(sut.getPackageDependencies().first().getPackageName()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getClassDependencies() {
        sut.addClassInformation(classInformation);
        assertThat(sut.getClassDependencies().first().getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getMethodDependencies() {
        assertThat(sut.getMethodDependencies().first().getName()).isEqualTo(TEST_CLASS_TEST);
    }
}