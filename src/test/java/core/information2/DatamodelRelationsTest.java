package core.information2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil2.*;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class DatamodelRelationsTest {
    private RootInformation dm;

    Ref<ProjectInformation, RootInformation> proj;
    Ref<PackageInformation<ProjectInformation>, ProjectInformation> pa, pb;
    Ref<ClassInformation<PackageInformation<?>>, PackageInformation<?>> ca, cabase, cb;
    Ref<ClassInformation<ProjectInformation>, ProjectInformation> cc, ce;
    Ref<ClassInformation<ClassInformation<?>>, ClassInformation<?>> cca, cci;
    Ref<MethodInformation, ClassInformation<?>> caMa, caMb, caE, cabaseE, cbC, cbGia1, ccRca, ccC, ccaC, ccaGcc, cciC, cciRca, ceEm, caC, cbCC, cbM, cbL, cbGia2;

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
                                        cbGia2 = mi("getInstanceA(java.lang.String,int,packageA.ClassA[])")
                                ),
                                pis("emptyPackage")
                        ),
                        cc = cir("ClassC", false,
                                ccC = mi("<init>()"),
                                ccRca = mi("retrieveClassA()"),
                                cca = cii("1", false,
                                        ccaC = mi("<init>(ClassC)"),
                                        ccaGcc = mi("getClassC()")
                                ),
                                cci = cii("ClassCInner", false,
                                        cciC = mi("<init>(ClassC)"),
                                        cciRca = mi("retrieveClassA()")
                                )
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
    void testDependencies() {
        assertThat(dm.getAllProjectDependencies(null, true)).containsExactlyInAnyOrder(proj.getStored());
        assertThat(dm.getAllProjectDependencies(null, false)).isEmpty();
        assertThat(dm.getAllPackageDependencies(null, true)).containsExactlyInAnyOrder(pa.getStored());
        assertThat(dm.getAllClassDependencies(null, true)).containsExactlyInAnyOrder(cb.getStored(), cca.getStored());
        assertThat(dm.getAllMethodDependencies(null, true)).containsExactlyInAnyOrder(ccaC.getStored(), cbC.getStored(), caC.getStored());
        assertThat(cbGia1.getStored().getPackageDependencies(null)).containsExactlyInAnyOrder(pa.getStored());
        assertThat(caMb.getStored().getClassDependencies(null)).containsExactlyInAnyOrder(cb.getStored());
        assertThat(ccC.getStored().getMethodDependencies(null)).containsExactlyInAnyOrder(ccaC.getStored());
        assertThat(cbCC.getStored().getProjectDependencies(null)).isEmpty();
        assertThat(dm.getAllClassDependenciesAggregated(null, true)).containsExactlyInAnyOrder(ca.getStored(), cb.getStored(), cca.getStored());
        assertThat(dm.getAllPackageDependenciesAggregated(null, true)).containsExactlyInAnyOrder(pa.getStored(), pb.getStored());
        assertThat(dm.getAllProjectDependenciesAggregated(null, true)).containsExactlyInAnyOrder(proj.getStored());
    }
    //TODO pom dependencies
}