package view;

import core.information.BehaviorInformation;
import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class HTMLReportBuilderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLReportBuilderTest.class);

    private static Collection<PackageInformation> packages;

    @BeforeAll
    static void beforeAll() {
        packages = new ArrayList<>();
        //create packages
        PackageInformation packageOne = new PackageInformation("packageone");
        packageOne.setInternalPackage(true);
        PackageInformation packageTwo = new PackageInformation("packagetwo");
        packageTwo.setInternalPackage(true);

        //add packages
        packages.add(packageOne);
        packages.add(packageTwo);

        //create classes owned by the packages above
        ClassInformation classOne = new ClassInformation("packageone.ClassOne");
        ClassInformation classTwo = new ClassInformation("packageone.ClassTwo");
        ClassInformation classThree = new ClassInformation("packagetwo.ClassThree");
        ClassInformation classFour = new ClassInformation("packagetwo.ClassFour");

        //add Classes to packages
        packageOne.addClassInformation(classOne);
        packageOne.addClassInformation(classTwo);
        packageTwo.addClassInformation(classThree);
        packageTwo.addClassInformation(classFour);

        //create methods owned by the classes above
        BehaviorInformation classOneMethodOne = new BehaviorInformation("packageone.ClassOne.methodOne()", false);
        BehaviorInformation classTwoMethodTwo = new BehaviorInformation("packageone.ClassTwo.methodTwo()", false);
        BehaviorInformation classThreeMethodThree = new BehaviorInformation("packagetwo.ClassThree.methodThree()", false);
        BehaviorInformation classFourMethodFour = new BehaviorInformation("packagetwo.ClassFour.methodFour()", false);

        //add methods to classes
        classOne.addBehaviorInformation(classOneMethodOne);
        classTwo.addBehaviorInformation(classTwoMethodTwo);
        classThree.addBehaviorInformation(classThreeMethodThree);
        classFour.addBehaviorInformation(classFourMethodFour);

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

    }

    @Test
    void createHTMLReport() {
        try {
            Path path = Files.createTempDirectory("html");
            HTMLReportBuilder.createHTMLReport(packages, path.toString() + "/");
            String result = IOUtil.readFile(path.toString() + "/" + "changelog_" + java.time.LocalDate.now() + ".html").replace(java.time.LocalDate.now().toString(), "");
            String expected = IOUtil.readFile("src/test/resources/html/" + "changelog_expected.html");
            assertThat(result).isEqualToIgnoringWhitespace(expected);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}