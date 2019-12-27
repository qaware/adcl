package core;

import core.information.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DiffExtractorTest {

    private static DiffExtractor diffExtractor;
    private static Collection<PackageInformation> packageOld;
    private static Collection<PackageInformation> packageNew;

    @BeforeAll
    static void beforeAll() {
        packageOld = new ArrayList<>();
        packageNew = new ArrayList<>();
        //create packages
        PackageInformation packageOldOne = new PackageInformation("packageone");
        //packageOldOne.setInternalPackage(true);
        PackageInformation packageNewTwo = new PackageInformation("packageone");
        //packageNewTwo.setInternalPackage(true);

        //add packages
        packageOld.add(packageOldOne);
        packageNew.add(packageNewTwo);

        //create classes owned by the packages above
        ClassInformation classOne = new ClassInformation("packageone.ClassOne");
        ClassInformation classTwo = new ClassInformation("packageone.ClassTwo");
        ClassInformation classThree = new ClassInformation("packageone.ClassThree");
        ClassInformation classFour = new ClassInformation("packageone.ClassOne");

        //add Classes to packages
        packageOldOne.addClassInformation(classOne);
        packageOldOne.addClassInformation(classTwo);
        packageNewTwo.addClassInformation(classThree);
        packageNewTwo.addClassInformation(classFour);

        //create methods owned by the classes above
        BehaviorInformation classOneMethodOne = new BehaviorInformation("packageone.ClassOne.methodOne()", false);
        BehaviorInformation classTwoMethodTwo = new BehaviorInformation("packageone.ClassTwo.methodTwo()", false);
        BehaviorInformation classThreeMethodThree = new BehaviorInformation("packageone.ClassThree.methodThree()", false);
        BehaviorInformation classFourMethodFour = new BehaviorInformation("packageone.ClassOne.methodOne()", false);
        BehaviorInformation classFourMethodFifth = new BehaviorInformation("packageone.ClassOne.methodFour()", false);

        //add methods to classes
        classOne.addBehaviorInformation(classOneMethodOne);
        classTwo.addBehaviorInformation(classTwoMethodTwo);
        classThree.addBehaviorInformation(classThreeMethodThree);
        classFour.addBehaviorInformation(classFourMethodFour);
        classFour.addBehaviorInformation(classFourMethodFifth);

        //create changelogitems
        ChangelogDependencyInformation depedencyOne = new ChangelogDependencyInformation("sample.Class.method1(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.ADDED);
        ChangelogDependencyInformation depedencyTwo = new ChangelogDependencyInformation("sample.Class.method2(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.DELETED);
        ChangelogDependencyInformation depedencyThree = new ChangelogDependencyInformation("sample.Class.method3(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.ADDED);
        ChangelogDependencyInformation depedencyFour = new ChangelogDependencyInformation("sample.Class.method4(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.DELETED);

        //put dependency items into sets and add them methods
        SortedSet<BehaviorInformation> dsetOne = new TreeSet<>();
        dsetOne.add(depedencyOne);
        classOneMethodOne.setReferencedBehavior(dsetOne);

        SortedSet<BehaviorInformation> dsetTwo = new TreeSet<>();
        dsetTwo.add(depedencyTwo);
        classTwoMethodTwo.setReferencedBehavior(dsetTwo);

        SortedSet<BehaviorInformation> dsetThree = new TreeSet<>();
        dsetThree.add(depedencyThree);
        classThreeMethodThree.setReferencedBehavior(dsetThree);

        SortedSet<BehaviorInformation> dsetFour = new TreeSet<>();
        dsetFour.add(depedencyFour);
        classFourMethodFour.setReferencedBehavior(dsetFour);

        diffExtractor = new DiffExtractor(Collections.singletonList(packageOldOne), Collections.singletonList(packageNewTwo));

    }

    @Test
    void getChanged() {
        ClassInformation classOne = new ClassInformation("packageone.ClassOne");
        ClassInformation classTwo = new ClassInformation("packageone.ClassTwo");
        ClassInformation classThree = new ClassInformation("packageone.ClassThree");

        ArrayList<PackageInformation> change = new ArrayList<>(diffExtractor.getChanged());
        Collections.sort(change);

        assertThat(change).isNotEmpty();
        assertThat(change.get(0).getPackageName()).isEqualTo("packageone");
        assertThat(change.get(0).getClassInformations().contains(classOne)).isTrue();
        assertThat(change.get(0).getClassInformations().contains(classTwo)).isTrue();
        assertThat(change.get(0).getClassInformations().contains(classThree)).isTrue();
        assertThat(change.get(0).getClassInformations().first()
                .getBehaviorInformations().first()
                .getReferencedBehavior().first()).isInstanceOf(ChangelogDependencyInformation.class)
                .hasFieldOrPropertyWithValue("changeStatus", ChangelogDependencyInformation.ChangeStatus.DELETED);
    }
}