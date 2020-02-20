package com.github.qaware.adcl.information;

import com.github.qaware.adcl.PomDependencyExtractor;
import com.github.qaware.adcl.depex.DependencyExtractor;
import com.github.qaware.adcl.pm.MavenProjectManager;
import com.github.qaware.adcl.report.DiffExtractor;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static com.github.qaware.adcl.util.DataGenerationUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DatamodelVersioningTest {
    Ref<ProjectInformation, RootInformation> proj;
    Ref<PackageInformation<ProjectInformation>, ProjectInformation> pa, pb;
    Ref<ClassInformation<PackageInformation<?>>, PackageInformation<?>> ca, cabase, cb;
    Ref<ClassInformation<ProjectInformation>, ProjectInformation> cc, ce, cca, cci;
    Ref<MethodInformation, ClassInformation<?>> caMa, caMb, caE, cabaseE, cbC, cbGia1, ccRca, ccC, ccaC, ccaGcc, cciC, cciRca, ceEm, caC, cbCC, cbM, cbL, cbGia2;
    private RootInformation dm;

    @BeforeEach
    void generateDataModel() {
        dm = root(
                proj = project("proj", true, "v1.0.0",
                        pa = pir("packageA",
                                ca = cio("ClassA", false,
                                        caC = mi("<init>()"),
                                        caMa = mi("methodA()"),
                                        caMb = mi("methodB(packageB.ClassB)"),
                                        caE = mi("empty()")
                                ),
                                cabase = cio("ClassABase", false,
                                        cabaseE = mi("empty()")
                                )
                        ),
                        pb = pir("packageB",
                                cb = cio("ClassB", true,
                                        cbC = mi("<init>()"),
                                        cbCC = mi("<clinit>()"),
                                        cbGia1 = mi("getInstanceA()"),
                                        cbM = mi("method(java.com.github.qaware.adcl.util.function.Predicate)"),
                                        cbL = mi("lambda$getInstanceA$0(java.lang.String)"),
                                        cbGia2 = mi("getInstanceA(java.lang.String, int, packageA.ClassA[])")
                                ),
                                pis("emptyPackage")
                        ),
                        cc = cir("ClassC", false,
                                ccC = mi("<init>()"),
                                ccRca = mi("retrieveClassA()")
                        ),
                        cca = cir("ClassC$1", false,
                                ccaC = mi("<init>(ClassC)"),
                                ccaGcc = mi("getClassC()")
                        ),
                        cci = cir("ClassC$ClassCInner", false,
                                cciC = mi("<init>(ClassC)"),
                                cciRca = mi("retrieveClassA()")
                        ),
                        ce = cir("ExternalClass", false,
                                ceEm = mi("extMethod()")
                        )
                )
        );

        p(cbM, proj);
        p(cbGia1, pa);
        p(caMb, cb);
        p(ccC, cca, ccaC);
        p(caMa, cbC);
        p(cbCC, caC);
    }

    @Test
    void versionTraversalTest() {
        VersionInformation v1 = proj.getStored().getLatestVersion();
        VersionInformation v2 = proj.getStored().addVersion("2.0.0");
        assertThat(v1.previous()).isNull();
        assertThat(v1.next()).isEqualTo(v2);
        assertThat(v2.previous()).isEqualTo(v1);
        assertThat(v2.next()).isNull();
    }

    @Test
    void test() throws IOException {
        RootInformation root = new RootInformation();
        ProjectInformation project = new ProjectInformation(root, "proj", true, "<unknown>");
        VersionInformation v1 = runDepEx(project, "testproject", "0.0.1");
        VersionInformation v2 = runDepEx(project, "testproject2", "0.0.2");
        VersionInformation v3 = runDepEx(project, "testproject3", "0.0.3");

        assertThat(new DiffExtractor(v1, v2).generateDependencyDiff(false, false).stream().map(Object::toString)).containsExactlyInAnyOrder(
                "proj.packageB.ClassB->null.org.springframework.stereotype.Service",
                "proj.packageA.MyAnnotation.notNullRef()->null.org.jetbrains.annotations.NotNull",
                "proj.packageA.ClassA->proj.packageA.MyAnnotation",
                "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->proj.packageA.ClassA.$$$reportNull$$$0(int)",
                "proj.packageA.ClassA->null.org.jetbrains.annotations.Nullable",
                "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->null.org.springframework.context.NoSuchMessageException",
                "proj.packageA.ClassA->null.org.jetbrains.annotations.NotNull",
                "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->null.org.jetbrains.annotations.NotNull",
                "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->proj.packageA.ClassA.methodA()"
        );

        assertThat(new DiffExtractor(v2, v3).generateDependencyDiff(false, false).stream().map(Object::toString)).containsExactlyInAnyOrder(
                "proj.packageA.ClassA.newMethod()+>proj.packageA.ClassABase"
        );
    }

    @NotNull
    private VersionInformation runDepEx(@NotNull ProjectInformation project, String folderName, String versionName) throws IOException {
        VersionInformation result = project.addVersion(versionName);
        new DependencyExtractor(Paths.get("src", "test", "resources", "testclassfiles2", folderName, "target", "classes"), result, null).runAnalysis();
        return result;
    }

    @Test
    void pomDependencyTest() throws MavenInvocationException {
        RootInformation root = new RootInformation();
        ProjectInformation project = new ProjectInformation(root, "proj", true, "<unknown>");
        VersionInformation v1 = runPomAnalysis(project, "testproject", "0.0.1");
        VersionInformation v2 = runPomAnalysis(project, "testproject2", "0.0.2");
        VersionInformation v3 = runPomAnalysis(project, "testproject3", "0.0.3");

        assertThat(new DiffExtractor(v1, v2).generatePomDiff().stream().map(Object::toString)).containsExactlyInAnyOrder(
                "-> null@org-springframework:spring-context",
                "-> 18.0.0@org-jetbrains:annotations"
        );
        assertThat(new DiffExtractor(v2, v3).generatePomDiff().stream().map(Object::toString)).containsExactlyInAnyOrder(
                "-> 5.2.1.RELEASE@org-springframework:spring-context",
                "-> null@org-jetbrains:annotations"
        );
    }

    @NotNull
    private VersionInformation runPomAnalysis(@NotNull ProjectInformation project, String folderName, String versionName) throws MavenInvocationException {
        VersionInformation result = project.addVersion(versionName);
        Path basedir = Paths.get("src", "test", "resources", "testclassfiles2", folderName);
        PomDependencyExtractor.updatePomDependencies(new MavenProjectManager(basedir, basedir.resolve("pom.xml")), result);
        return result;
    }
    @Test
    void versionTest(){
        VersionInformation v1 = proj.getStored().addVersion("2.0.0");
        VersionInformation vx = null;
        proj.getStored().addClassDependency(ce.getStored(),v1);
        assertThat(proj.getStored().getLatestVersion().getName().equals(v1.getName())).isTrue();

        Set<ClassInformation<?>> classInformation=proj.getStored().getClassDependencies(v1);
        for (ClassInformation<?> information : classInformation) {
            if (information.getName().equals(ce.getStored().getName())) {
                classInformation.remove(information);
                vx = proj.getStored().addVersion("3.0.0");
            }
        }
        assert (vx != null);
        assertThat(proj.getStored().getLatestVersion().getName().equals(vx.getName())).isTrue();

        proj.getStored().addMethodDependency(new MethodInformation(cc.getStored(),"what()"),vx);
        Set<MethodInformation> mInformation=proj.getStored().getMethodDependencies(vx);
        for (MethodInformation del : mInformation) {
            if (del.getParent().getName().equals(cc.getStored().getName())) {
                mInformation.remove(del);
                vx = proj.getStored().addVersion("4.0.0");
            }
        }

        assertThat(proj.getStored().getLatestVersion().getName().equals(vx.getName())).isTrue();

        proj.getStored().addPackageDependency(pa.getStored(),vx);
        Set<PackageInformation<?>> pInformation=proj.getStored().getPackageDependencies(vx);
        for (PackageInformation<?> packageInformation : pInformation) {
            if (packageInformation.getName().equals(pa.getStored().getName())) {
                pInformation.remove(packageInformation);
                vx = proj.getStored().addVersion("5.0.0");
            }
        }

        assertThat(proj.getStored().getLatestVersion().getName().equals(vx.getName())).isTrue();

        ProjectInformation px=new ProjectInformation(dm,"testproj",true,vx.getName());
        proj.getStored().addProjectDependency(px,vx);
        Set<ProjectInformation> paInformation=proj.getStored().getProjectDependencies(vx);
        for (ProjectInformation del : paInformation) {
            if (del.getName().equals(px.getName())) {
                paInformation.remove(del);
                vx = proj.getStored().addVersion("6.0.0");
            }
        }
        assertThat(proj.getStored().getLatestVersion().getName().equals(vx.getName())).isTrue();
    }
}
