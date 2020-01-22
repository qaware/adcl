package core.information2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static util.DataGenerationUtil2.*;

public class DatamodelRelationsTest {
    private RootInformation dm;

    @SuppressWarnings({"unused", "SpellCheckingInspection", "UnusedAssignment"})
    @BeforeEach
    void generateDataModel() {
        Ref<ProjectInformation, RootInformation> proj;
        Ref<PackageInformation<ProjectInformation>, ProjectInformation> pa, pb;
        Ref<ClassInformation<PackageInformation<?>>, PackageInformation<?>> ca, cabase, cb;
        Ref<ClassInformation<ProjectInformation>, ProjectInformation> cc, cca, cci, ce;
        Ref<Information<ClassInformation<?>>, ClassInformation<?>> caMa, caMb, caE, cabaseE, cbC, cbGia1, ccRca, ccC, ccaC, ccaGcc, cciC, cciRca, ceEm, caC, cbCC, cbM, cbL, cbGia2;

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
                                        cbGia2 = mi("getInstanceA(java.lang.String,int,packageA.ClassA[])")
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
                        ),
                        ce = cir("ExternalClass", false,
                                ceEm = mi("extMethod()")
                        )
                )
        );

        p(caMa, cbC);
        p(caMb, cb);
        p(cbCC, caC);
        p(cbGia1, pa);
        p(ccC, cca, ccaC);
    }

    @Test
    void testDependencies() {

    }
}
