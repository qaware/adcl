package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;

class ClassInformationTest {

    private static final String TEST_PACKAGE_2 = "testPackage2";
    private static final String TEST_TEST_CLASS = "test.TestClass";
    private static final String TEST_CLASS_TEST_2 = "TestClass.test2";
    private static final String TEST_CLASS = "TestClass";
    private static final String TEST_CLASS_TEST_1 = "TestClass.test1";
    private ClassInformation sut;

    @BeforeEach
    void setUp() {
        SortedSet<PackageInformation> packageDependencies = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        packageDependencies.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> classDependencies = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        classDependencies.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<MethodInformation> methodDependencies = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodDependencies.add(new MethodInformation(TEST_CLASS_TEST_2, false));
        SortedSet<MethodInformation> methodInformations = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        methodInformations.add(new MethodInformation(TEST_CLASS, packageDependencies, classDependencies, methodDependencies, true));
        methodInformations.add(new MethodInformation(TEST_CLASS_TEST_1, packageDependencies, classDependencies, methodDependencies, false));

        sut = new ClassInformation(TEST_TEST_CLASS, methodInformations, false);
    }

    @Test
    void getClassName() {
        assertThat(sut.getClassName()).isEqualTo(TEST_TEST_CLASS);
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
    void getConstructorInformations() {
        sut.getMethodInformations().forEach(methodInformation -> {
            if (methodInformation.isConstructor()) {
                assertThat(methodInformation.getName()).isEqualTo(TEST_CLASS);
            }
        });
    }

    @Test
    void getMethodInformations() {
        sut.getMethodInformations().forEach(methodInformation -> {
            if (!methodInformation.isConstructor()) {
                assertThat(methodInformation.getName()).isEqualTo(TEST_CLASS_TEST_1);
            }
        });
    }

    @Test
    void getMethodDependencies() {
        assertThat(sut.getMethodDependencies().first().getName()).isEqualTo(TEST_CLASS_TEST_2);
    }
}