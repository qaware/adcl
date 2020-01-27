package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static util.DataGenerationUtil.*;

public class DatamodelNodesTest {
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
                                        cbGia2 = mi("getInstanceA(java.lang.String,int,packageA.ClassA[])")
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
    void testRootInformation() {
        assertThat(dm.getModelVersion()).isEqualTo(2);
        assertThat(dm.getProjects(null)).doesNotContainNull().hasSize(1);
        assertThat(dm.getType()).isEqualTo(Information.Type.ROOT);
        assertThat(dm.getParent()).isSameAs(dm);
        assertThat(dm.getParent(RootInformation.class)).isNull();
        assertThat(dm.getPath()).isEmpty();
        assertThat(dm.exists(new VersionInformation("", dm.getProjects(null).iterator().next()))).isTrue();
        assertThatThrownBy(() -> dm.getProject()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testProjectInformation() {
        ProjectInformation p = (ProjectInformation) dm.find("proj", null);
        assertThat(p).isNotNull();
        assertThat(p.isInternal()).isTrue();
        VersionInformation v = p.addVersion("2.0.0");
        assertThat(v.getName()).isEqualTo("2.0.0");
        assertThat(v.getProject()).isSameAs(p);
        assertThat(p.getVersions()).hasSize(2);
        assertThat(p.getLatestVersion()).isEqualTo(v);
        assertThat(p.getType()).isEqualTo(Information.Type.PROJECT);
        assertThat(p.getPath()).isEqualTo("proj");
        assertThat(p.getName()).isEqualTo("proj");
    }

    @Test
    void testMethodInformation() {
        MethodInformation mi1 = (MethodInformation) dm.find("proj.ClassC.<init>()", null);
        assertThat(mi1).isNotNull();
        assertThat(mi1.isConstructor()).isTrue();
        MethodInformation mi2 = (MethodInformation) dm.find("proj.ClassC.retrieveClassA()", null);
        assertThat(mi2).isNotNull();
        assertThat(mi2.isConstructor()).isFalse();
        MethodInformation mi3 = (MethodInformation) dm.find("proj.packageB.ClassB.<clinit>()", null);
        assertThat(mi3).isNotNull();
        assertThat(mi3.isConstructor()).isTrue();
        assertThat(mi3.getType()).isEqualTo(Information.Type.METHOD);
        assertThat(mi3.getPath()).isEqualTo("proj.packageB.ClassB.<clinit>()");
        assertThat(mi3.getName()).isEqualTo("<clinit>()");
    }

    @Test
    void testClassInformation() {
        ClassInformation<?> c1 = (ClassInformation<?>) dm.find("proj.ClassC", null);
        assertThat(c1).isNotNull();
        assertThat(c1.getType()).isEqualTo(Information.Type.CLASS);
        assertThat(c1.getPath()).isEqualTo("proj.ClassC");
        assertThat(c1.getName()).isEqualTo("ClassC");
        assertThat(c1.isService()).isFalse();
        ClassInformation<?> c2 = (ClassInformation<?>) dm.find("proj.packageB.ClassB", null);
        assertThat(c2).isNotNull();
        assertThat(c2.isService()).isTrue();
    }

    @SuppressWarnings("ConstantConditions" /* null checking by list */)
    @Test
    void testInformation() {
        Information<?> p = dm.find("proj", null);
        Information<?> cc = dm.find("proj.ClassC", null);
        MethodInformation ccinit = (MethodInformation) dm.find("proj.ClassC.<init>()", null);
        MethodInformation ccrca = (MethodInformation) dm.find("proj.ClassC.retrieveClassA()", null);
        Information<?> cca = dm.find("proj.ClassC$1", null);
        Information<?> cci = dm.find("proj.ClassC$ClassCInner", null);

        assertThat(Arrays.asList(p, cc, ccinit, ccrca, cca, cci)).doesNotContainNull();

        assertThat(cc.getProject()).isEqualTo(p);
        assertThat(cc.getParent()).isEqualTo(p);
        assertThat(cc.getDirectChildren(null)).containsExactlyInAnyOrder(ccinit, ccrca);
        assertThat(cc.getRoot()).isEqualTo(dm);
        assertThat(cc.find(MethodInformation.class, null)).containsExactlyInAnyOrder(ccinit, ccrca);
        assertThat(cc.findAll(MethodInformation.class, null).stream().map(Information::getName))
                .containsExactlyInAnyOrder("<init>()", "retrieveClassA()");
        assertThat(cc.getAllChildren(null)).hasSize(2);
        assertThat(cci.getParent(MethodInformation.class)).isNull();
        assertThat((PackageInformation<?>) cci.getParent(PackageInformation.class)).isNull();
        assertThat(cci.getParent(ProjectInformation.class)).isEqualTo(p);
        assertThat(cci.getParent(RootInformation.class)).isEqualTo(dm);
    }

    @Test
    void versionTest() {
        assertThat(dm.getProjects(null).iterator().next().getLatestVersion().toString()).isEqualTo("proj@v1.0.0");
    }
}
