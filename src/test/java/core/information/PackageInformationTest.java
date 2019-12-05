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

        SortedSet<PackageInformation> referencedPackages = new TreeSet<>(PackageInformation.PackageInformationComparator.getInstance());
        referencedPackages.add(new PackageInformation(TEST_PACKAGE_2));
        SortedSet<ClassInformation> referencedClasses = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        referencedClasses.add(new ClassInformation(TEST_TEST_CLASS));
        SortedSet<BehaviorInformation> referencedBehavior = new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance());
        referencedBehavior.add(new BehaviorInformation(TEST_CLASS_TEST, false));
        SortedSet<BehaviorInformation> behaviorInformations = new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance());
        behaviorInformations.add(new BehaviorInformation(TEST_CLASS, referencedPackages, referencedClasses, referencedBehavior, true));
        behaviorInformations.add(new BehaviorInformation(TEST_CLASS_TEST, referencedPackages, referencedClasses, referencedBehavior, false));

        classInformation = new ClassInformation(TEST_TEST_CLASS, behaviorInformations, false);
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
    void getReferencedPackages() {
        assertThat(sut.getReferencedPackages().first().getPackageName()).isEqualTo(TEST_PACKAGE_2);
    }

    @Test
    void getReferencedClasses() {
        sut.addClassInformation(classInformation);
        assertThat(sut.getReferencedClasses().first().getClassName()).isEqualTo(TEST_TEST_CLASS);
    }

    @Test
    void getReferencedBehavior() {
        assertThat(sut.getReferencedBehaviors().first().getName()).isEqualTo(TEST_CLASS_TEST);
    }
}