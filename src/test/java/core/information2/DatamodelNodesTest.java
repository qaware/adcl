package core.information2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static util.DataGenerationUtil2.*;

public class DatamodelNodesTest {
    private RootInformation dm;

    @BeforeEach
    void generateDataModel() {
        dm = root(
                project("proj", true, "v1.0.0",
                        pir("packageA",
                                cio("ClassA", false,
                                        mi("<init>()"),
                                        mi("methodA()"),
                                        mi("methodB(packageB.ClassB)"),
                                        mi("empty()")
                                ),
                                cio("ClassABase", false,
                                        mi("empty()")
                                )
                        ),
                        pir("packageB",
                                cio("ClassB", true,
                                        mi("<init>()"),
                                        mi("<clinit>()"),
                                        mi("getInstanceA()"),
                                        mi("method(java.util.function.Predicate)"),
                                        mi("lambda$getInstanceA$0(java.lang.String)"),
                                        mi("getInstanceA(java.lang.String,int,packageA.ClassA[])")
                                ),
                                pis("emptyPackage")
                        ),
                        cir("ClassC", false,
                                mi("<init>()"),
                                mi("retrieveClassA()"),
                                cii("1", false,
                                        mi("<init>(ClassC)"),
                                        mi("getClassC()")
                                ),
                                cii("ClassCInner", false,
                                        mi("<init>(ClassC)"),
                                        mi("retrieveClassA()")
                                )
                        ),

                        cir("ExternalClass", false,
                                mi("extMethod()")
                        )
                )
        );
    }

    @Test
    void testRootInformation() {
        assertThat(dm.getModelVersion()).isEqualTo(2);
        assertThat(dm.getProjects(null)).doesNotContainNull().hasSize(1);
        assertThat(dm.getType()).isEqualTo(Information.Type.ROOT);
        assertThat(dm.getParent()).isSameAs(dm);
        assertThat(dm.getPath()).isEmpty();
        assertThat(dm.exists(new VersionInformation("", dm.getProjects(null).iterator().next()))).isTrue();
        assertThatThrownBy(() -> dm.getProject()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testProjectInformation() {
        ProjectInformation p = (ProjectInformation) dm.findByPath("proj", null);
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
        MethodInformation mi1 = (MethodInformation) dm.findByPath("proj.ClassC.<init>()", null);
        assertThat(mi1).isNotNull();
        assertThat(mi1.isConstructor()).isTrue();
        MethodInformation mi2 = (MethodInformation) dm.findByPath("proj.ClassC.retrieveClassA()", null);
        assertThat(mi2).isNotNull();
        assertThat(mi2.isConstructor()).isFalse();
        MethodInformation mi3 = (MethodInformation) dm.findByPath("proj.packageB.ClassB.<clinit>()", null);
        assertThat(mi3).isNotNull();
        assertThat(mi3.isConstructor()).isTrue();
        assertThat(mi3.getType()).isEqualTo(Information.Type.METHOD);
        assertThat(mi3.getPath()).isEqualTo("proj.packageB.ClassB.<clinit>()");
        assertThat(mi3.getName()).isEqualTo("<clinit>()");
    }

    @Test
    void testClassInformation() {
        ClassInformation<?> c1 = (ClassInformation<?>) dm.findByPath("proj.ClassC", null);
        assertThat(c1).isNotNull();
        assertThat(c1.getType()).isEqualTo(Information.Type.CLASS);
        assertThat(c1.getPath()).isEqualTo("proj.ClassC");
        assertThat(c1.getName()).isEqualTo("ClassC");
        assertThat(c1.isService()).isFalse();
        ClassInformation<?> c2 = (ClassInformation<?>) dm.findByPath("proj.packageB.ClassB", null);
        assertThat(c2).isNotNull();
        assertThat(c2.isService()).isTrue();
    }

    @SuppressWarnings("ConstantConditions" /* null checking by list */)
    @Test
    void testInformation() {
        Information<?> p = dm.findByPath("proj", null);
        Information<?> cc = dm.findByPath("proj.ClassC", null);
        MethodInformation ccinit = (MethodInformation) dm.findByPath("proj.ClassC.<init>()", null);
        MethodInformation ccrca = (MethodInformation) dm.findByPath("proj.ClassC.retrieveClassA()", null);
        Information<?> cca = dm.findByPath("proj.ClassC$1", null);
        Information<?> cci = dm.findByPath("proj.ClassC$ClassCInner", null);

        assertThat(Arrays.asList(p, cc, ccinit, ccrca, cca, cci)).doesNotContainNull();
        assertThat(cc.getProject()).isEqualTo(p);
        assertThat(cc.getParent()).isEqualTo(p);
        assertThat(cc.getDirectChildren(null)).containsExactlyInAnyOrder(ccinit, ccrca, cca, cci);
        assertThat(cc.getRoot()).isEqualTo(dm);
        assertThat(cc.find(MethodInformation.class, null)).containsExactlyInAnyOrder(ccinit, ccrca);
        assertThat(cc.findAll(MethodInformation.class, null).stream().map(Information::getName))
                .containsExactlyInAnyOrder("<init>()", "retrieveClassA()", "<init>(ClassC)", "getClassC()", "<init>(ClassC)", "retrieveClassA()");
        assertThat(cc.getAllChildren(null)).hasSize(8);
    }

    @Test
    void versionTest() {
        assertThat(dm.getProjects(null).iterator().next().getLatestVersion().toString()).isEqualTo("proj@v1.0.0");
    }
}
