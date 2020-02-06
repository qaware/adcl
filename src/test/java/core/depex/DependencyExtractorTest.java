package core.depex;

import core.information.*;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil.*;

class DependencyExtractorTest {
    private static final Path TESTCLASS_FOLDER = Paths.get("src", "test", "resources", "testclassfiles2");

    Ref<ProjectInformation, RootInformation> proj;
    Ref<PackageInformation<ProjectInformation>, ProjectInformation> pa, pb;
    Ref<ClassInformation<PackageInformation<?>>, PackageInformation<?>> ca, cabase, cb, service, jn, jnn, cmya, ex;
    Ref<ClassInformation<ProjectInformation>, ProjectInformation> cc, ce, cca, cci;
    Ref<MethodInformation, ClassInformation<?>> caMa, caMb, caE, cabaseE, caJnn, caMc, cmyaCr, cmyaNnr;
    Ref<MethodInformation, ClassInformation<?>> cbC, cbGia1, cbCC, cbM, cbLgia, cbGia2, cbLt, cbT;
    Ref<MethodInformation, ClassInformation<?>> ccRca, ccC, ccaC, ccaGcc, cciC, cciRca, ceEm, caC;
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
                                        caMc = mi("methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)"),
                                        caE = mi("empty()"),
                                        caJnn = mi("$$$reportNull$$$0(int)")
                                ),
                                cabase = cio("ClassABase", false,
                                        cabaseE = mi("empty()")
                                ),
                                cmya = cio("MyAnnotation", false,
                                        cmyaCr = mi("classRefs()"),
                                        cmyaNnr = mi("notNullRef()"),
                                        mi("type()")
                                )
                        ),
                        pb = pir("packageB",
                                cb = cio("ClassB", true,
                                        cbC = mi("<init>()"),
                                        cbCC = mi("<clinit>()"),
                                        cbGia1 = mi("getInstanceA()"),
                                        cbM = mi("method(java.util.function.Predicate)"),
                                        cbLgia = mi("lambda$getInstanceA$1(java.lang.String)"),
                                        cbGia2 = mi("getInstanceA(java.lang.String, int, packageA.ClassA[])"),
                                        cbT = mi("test()"),
                                        cbLt = mi("lambda$test$0(java.lang.Object, java.lang.Object)")
                                )
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
                        )
                ),
                project("null", false, "<unknown>",
                        ce = cir("ExternalClass", false,
                                ceEm = mi("extMethod()")
                        )
                ),
                project("spring-context", false, "<unknown>", pir("org", pis("springframework",
                        pis("stereotype", service = cio("Service", false)),
                        pis("context", ex = cio("NoSuchMessageException", false))
                ))),
                project("annotations", false, "<unknown>", pir("org", pis("jetbrains", pis("annotations",
                        jnn = cio("NotNull", false),
                        jn = cio("Nullable", false)
                ))))
        );

        p(ca, cabase, jn, jnn, cmya);
        p(caMa, cb, cbC);
        p(caMb, cb);
        p(caMc, jnn, caJnn, caMa, ex);
        p(cmyaNnr, jnn);
        p(cb, cabase, service);
        p(cbC, ca);
        p(cbCC, caC);
        p(cbGia1, ca);
        p(cbGia2, ca, cbM, cbLgia);
        p(cbT, cbLt);
        p(cc, ce);
        p(cca, cc);
        p(ccaC, cc);
        p(ccaGcc, cc, ccC);
        p(ccC, ccaC);
        p(cci, cc);
        p(cciC, cc, ccC);
        p(cciRca, ca, ceEm);
        p(ccRca, ca, cbGia1);
    }

    @Test
    void analyseClasses() throws IOException, MavenInvocationException {
        RootInformation root = new RootInformation();
        ProjectInformation proj = new ProjectInformation(root, "proj", true, "v1.0.0");

        new DependencyExtractor(TESTCLASS_FOLDER, proj.getLatestVersion()).runAnalysis();
        assertThat(root.deepEquals(dm)).overridingErrorMessage("Not deep equal!\nExpected:\n%s\n\nActual:\n%s", dm, root).isTrue();
    }
}