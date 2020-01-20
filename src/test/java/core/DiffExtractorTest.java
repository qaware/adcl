package core;

import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DiffExtractorTest {

    private static Collection<PackageInformation> packageOld;
    private static Collection<PackageInformation> packageNew;

    @BeforeAll
    static void beforeAll() {
        packageOld = new ArrayList<>();
        packageNew = new ArrayList<>();

        //create packages
        PackageInformation packageOldOne = new PackageInformation("packageone");
        PackageInformation packageNewTwo = new PackageInformation("packageone");

        //add packages
        packageOld.add(packageOldOne);
        packageNew.add(packageNewTwo);

        //create classes owned by the packages above
        ClassInformation classOne = new ClassInformation("packageone.ClassOne");
        ClassInformation classTwo = new ClassInformation("packageone.ClassTwo");
        ClassInformation classThree = new ClassInformation("packageone.ClassThree");

        ClassInformation classCopyOne = new ClassInformation("packageone.ClassOne");

        //add Classes to packages
        packageOldOne.addClassInformation(classOne);
        packageOldOne.addClassInformation(classTwo);

        packageNewTwo.addClassInformation(classThree);
        packageNewTwo.addClassInformation(classCopyOne);

        //create methods owned by the classes above
        MethodInformation classOneMethodOne = new MethodInformation("packageone.ClassOne.methodOne()", false);
        MethodInformation classTwoMethodTwo = new MethodInformation("packageone.ClassTwo.methodTwo()", false);
        MethodInformation classThreeMethodThree = new MethodInformation("packageone.ClassThree.methodThree()", false);

        MethodInformation classCopyOneMethodCopyOne = new MethodInformation("packageone.ClassOne.methodOne()", false);
        MethodInformation classCopyOneMethodFour = new MethodInformation("packageone.ClassOne.methodFour()", false);

        //add methods to classes
        classOne.addMethodInformation(classOneMethodOne);
        classTwo.addMethodInformation(classTwoMethodTwo);
        classThree.addMethodInformation(classThreeMethodThree);

        classCopyOne.addMethodInformation(classCopyOneMethodCopyOne);
        classCopyOne.addMethodInformation(classCopyOneMethodFour);

        //create changelogitems
        ChangelogDependencyInformation depedencyOne = new ChangelogDependencyInformation("sample.Class.method1(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.ADDED);
        ChangelogDependencyInformation depedencyTwo = new ChangelogDependencyInformation("sample.Class.method2(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.DELETED);
        ChangelogDependencyInformation depedencyThree = new ChangelogDependencyInformation("sample.Class.method3(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.ADDED);
        ChangelogDependencyInformation depedencyFour = new ChangelogDependencyInformation("sample.Class.method4(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.DELETED);

        //put dependency items into sets and add them to methods
        Set<MethodInformation> dsetOne = new TreeSet<>();
        dsetOne.add(depedencyOne);
        classOneMethodOne.setMethodDependencies(dsetOne);

        Set<MethodInformation> dsetTwo = new TreeSet<>();
        dsetTwo.add(depedencyTwo);
        classTwoMethodTwo.setMethodDependencies(dsetTwo);

        Set<MethodInformation> dsetThree = new TreeSet<>();
        dsetThree.add(depedencyThree);
        classThreeMethodThree.setMethodDependencies(dsetThree);

        Set<MethodInformation> dsetFour = new TreeSet<>();
        dsetFour.add(depedencyFour);
        classCopyOneMethodCopyOne.setMethodDependencies(dsetFour);

    }

    @Test
    void getChanged() {
        DiffExtractor diffExtractor = new DiffExtractor(packageOld, packageNew);
        ArrayList<PackageInformation> change = new ArrayList<>(diffExtractor.getChangelist());
        Collections.sort(change);

        assertThat(change).isNotEmpty();
        assertThat(change.get(0).getPackageName()).isEqualTo("packageone");
        assertThat(change.get(0).getClassInformations().stream().map(ClassInformation::getClassName)).contains("packageone.ClassOne", "packageone.ClassTwo", "packageone.ClassThree");
        assertThat(change.get(0).getClassInformations().iterator().next()
                .getMethodInformations().iterator().next()
                .getMethodDependencies().iterator().next()).isInstanceOf(ChangelogDependencyInformation.class)
                .hasFieldOrPropertyWithValue("changeStatus", ChangelogDependencyInformation.ChangeStatus.DELETED);
    }

    @Test
    void analyseSame() {
        DiffExtractor diffExtractor = new DiffExtractor(packageOld, packageOld);

        assertThat(diffExtractor.getChangelist()).isEmpty();
    }


    @Test
    void reverseChangelog() {
        DiffExtractor diffExtractor = new DiffExtractor(packageOld, packageNew);
        DiffExtractor reverseExtractor = new DiffExtractor(packageNew, packageOld);

        ArrayList<PackageInformation> changelog = new ArrayList<>(diffExtractor.getChangelist());
        ArrayList<PackageInformation> reverseChangelog = new ArrayList<>(reverseExtractor.getChangelist());

        assertThat(changelog).isNotEmpty();
        assertThat(changelog.get(0).getPackageName()).isEqualTo("packageone");
        assertThat(changelog.get(0).getClassInformations().stream().map(ClassInformation::getClassName)).contains("packageone.ClassOne", "packageone.ClassTwo", "packageone.ClassThree");
        assertThat(changelog.get(0).getClassInformations().iterator().next()
                .getMethodInformations().iterator().next()
                .getMethodDependencies().iterator().next()).isInstanceOf(ChangelogDependencyInformation.class)
                .hasFieldOrPropertyWithValue("changeStatus", ChangelogDependencyInformation.ChangeStatus.DELETED);

        assertThat(reverseChangelog).isNotEmpty();
        assertThat(reverseChangelog.get(0).getPackageName()).isEqualTo("packageone");
        assertThat(reverseChangelog.get(0).getClassInformations().stream().map(ClassInformation::getClassName)).contains("packageone.ClassOne", "packageone.ClassTwo", "packageone.ClassThree");
        assertThat(reverseChangelog.get(0).getClassInformations().iterator().next()
                .getMethodInformations().iterator().next()
                .getMethodDependencies().iterator().next()).isInstanceOf(ChangelogDependencyInformation.class);


        for (MethodInformation rc : reverseChangelog.get(0).getClassInformations().iterator().next()
                .getMethodInformations().iterator().next()
                .getMethodDependencies()) {

            ChangelogDependencyInformation c = (ChangelogDependencyInformation) changelog.get(0).getClassInformations().iterator().next()
                    .getMethodInformations().iterator().next()
                    .getMethodDependencies().stream().filter(methodInformation -> methodInformation.getName().equals(rc.getName())).findFirst().orElse(null);

            assertThat(c).isNotNull();
            assertThat(rc).hasFieldOrPropertyWithValue("changeStatus", c.getChangeStatus() == ChangelogDependencyInformation.ChangeStatus.ADDED ?
                    ChangelogDependencyInformation.ChangeStatus.DELETED : ChangelogDependencyInformation.ChangeStatus.ADDED);
        }
    }
}