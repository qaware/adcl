package core.information;

import core.depex.DependencyExtractor;
import core.report.DiffExtractor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil.*;

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
                                        cbM = mi("method(java.util.function.Predicate)"),
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

        assertThat(new DiffExtractor(v1, v2).generateChangelist(false, false).stream().map(Object::toString)).containsExactlyInAnyOrder(
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

        assertThat(new DiffExtractor(v2, v3).generateChangelist(false, false).stream().map(Object::toString)).containsExactlyInAnyOrder(
                "proj.packageA.ClassA.newMethod()+>proj.packageA.ClassABase"
        );
    }

    @NotNull
    private VersionInformation runDepEx(@NotNull ProjectInformation project, String folderName, String versionName) throws IOException {
        VersionInformation result = project.addVersion(versionName);
        new DependencyExtractor(Paths.get("src", "test", "resources", "testclassfiles2", folderName, "target", "classes"), result, null).runAnalysis();
        return result;
    }

    //TODO all (@1.0.17)
}
