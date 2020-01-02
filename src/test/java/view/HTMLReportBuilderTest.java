package view;

import core.information.MethodInformation;
import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
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
        MethodInformation classOneMethodOne = new MethodInformation("packageone.ClassOne.methodOne()", false);
        MethodInformation classTwoMethodTwo = new MethodInformation("packageone.ClassTwo.methodTwo()", false);
        MethodInformation classThreeMethodThree = new MethodInformation("packagetwo.ClassThree.methodThree()", false);
        MethodInformation classFourMethodFour = new MethodInformation("packagetwo.ClassFour.methodFour()", false);

        //add methods to classes
        classOne.addMethodInformation(classOneMethodOne);
        classTwo.addMethodInformation(classTwoMethodTwo);
        classThree.addMethodInformation(classThreeMethodThree);
        classFour.addMethodInformation(classFourMethodFour);

        //create changelogitems
        ChangelogDependencyInformation depedencyOne = new ChangelogDependencyInformation("sample.Class.method1(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.ADDED);
        ChangelogDependencyInformation depedencyTwo = new ChangelogDependencyInformation("sample.Class.method2(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.DELETED);
        ChangelogDependencyInformation depedencyThree = new ChangelogDependencyInformation("sample.Class.method3(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.ADDED);
        ChangelogDependencyInformation depedencyFour = new ChangelogDependencyInformation("sample.Class.method4(java.lang.String)", false, ChangelogDependencyInformation.ChangeStatus.DELETED);

        //put dependency items into sets and add them methods
        SortedSet<MethodInformation> dsetOne = new TreeSet<>();
        dsetOne.add(depedencyOne);
        classOneMethodOne.setReferencedMethods(dsetOne);

        SortedSet<MethodInformation> dsetTwo = new TreeSet<>();
        dsetTwo.add(depedencyTwo);
        classTwoMethodTwo.setReferencedMethods(dsetTwo);

        SortedSet<MethodInformation> dsetThree = new TreeSet<>();
        dsetThree.add(depedencyThree);
        classThreeMethodThree.setReferencedMethods(dsetThree);

        SortedSet<MethodInformation> dsetFour = new TreeSet<>();
        dsetFour.add(depedencyFour);
        classFourMethodFour.setReferencedMethods(dsetFour);

    }

    @Test
    void createHTMLReport() throws IOException {
        Path path = Files.createTempDirectory("html");
        HTMLReportBuilder.createHTMLReport(packages, path.toString());
        File resultFile = Objects.requireNonNull(new File(path.toString()).listFiles())[0];

        String result = IOUtil.readFile(resultFile.getPath()).replaceFirst("Dependency changelog from .+", "Dependency changelog from");
        String expected = IOUtil.readFile("src/test/resources/html/changelog_expected.html");

        assertThat(result).isEqualToIgnoringWhitespace(expected);
    }
}