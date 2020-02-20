package com.github.qaware.adcl.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;

import static com.github.qaware.adcl.util.DataGenerationUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DatamodelCompareTest {
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
    void compareVersions() {
        ProjectInformation p = dm.getProjects(null).iterator().next();
        VersionInformation v1 = p.getLatestVersion();
        VersionInformation v2 = p.addVersion("2.0.0");
        assertThat(v1.isBefore(v2)).isTrue();
        assertThat(v2.isAfter(v1)).isTrue();
    }

    @Test
    void compareTypes() {
        assertThat(Information.Type.ROOT.isSuper(Information.Type.CLASS)).isTrue();
        assertThat(Information.Type.CLASS.isSub(Information.Type.ROOT)).isTrue();
    }

    @Test
    void compareTreeWithProperties(){
        assertThat(dm.getType().equals(Information.Type.ROOT)).isTrue();
        assertThat(dm.getType().equals(Information.Type.CLASS)).isFalse();
        assertThat(dm.getType().equals(Information.Type.PROJECT)).isFalse();
        assertThat(dm.getType().equals(Information.Type.PACKAGE)).isFalse();
        assertThat(dm.getType().equals(Information.Type.METHOD)).isFalse();

        ProjectInformation p1 = dm.getProjects(null).iterator().next();
        ProjectInformation p2 = dm.getProjects(null).iterator().next();
        ProjectInformation p3 = new ProjectInformation(root(),"newNode",true,"v1.0.1");
        assertThat(p1.directChildren.equals(p2.directChildren)).isTrue();
        assertThat(p1.directChildren.equals(p3.directChildren)).isFalse();
        assertThat(dm.equals(p1.getParent())).isTrue();
        assertThat(p1.getType().equals(Information.Type.PROJECT)).isTrue();
        assertThat(p1.getType().equals(Information.Type.ROOT)).isFalse();
        assertThat(p1.getType().equals(Information.Type.CLASS)).isFalse();
        assertThat(p1.getType().equals(Information.Type.PACKAGE)).isFalse();
        assertThat(p1.getType().equals(Information.Type.METHOD)).isFalse();
        assertThat(p1.getLatestVersion().equals(p2.getLatestVersion())).isTrue();
        assertThat(p1.getName().equals(p2.getName())).isTrue();
        assertThat(p1.getPath().equals(p2.getPath())).isTrue();
        assertThat(p1.getType().equals(Information.Type.ROOT)).isFalse();
        assertThat(p1.getPath().equals(p3.getPath())).isFalse();
        assertThat(p1.getName().equals(p3.getName())).isFalse();
        assertThat(p1.getLatestVersion().equals(p3.getLatestVersion())).isFalse();

        Set<VersionInformation> v1=p1.getPomDependencies(p1.getLatestVersion());
        Set<VersionInformation> v2=p2.getPomDependencies(p2.getLatestVersion());
        assertThat(v1.equals(v2)).isTrue();
        v2.add(new VersionInformation("test",p2));
        assertThat(v1.equals(v2)).isFalse();
        v1.forEach(versionInformation -> assertThat(v2.contains(versionInformation)));

        Set<PackageInformation<?>> pi1 =p1.getPackageDependencies(p1.getLatestVersion());
        Set<PackageInformation<?>> pi2 =p2.getPackageDependencies(p2.getLatestVersion());
        pi1.forEach(packageInformation -> assertThat(pi2.contains(packageInformation)));
        Iterator pa1=pi1.iterator();
        Iterator pa2=pi2.iterator();
        while(pa1.hasNext()){
            PackageInformation packageInformation1=(PackageInformation) pa1.next();
            PackageInformation packageInformation2=(PackageInformation) pa2.next();
            assertThat(packageInformation1.getParent().equals(packageInformation2.getParent())).isTrue();
            assertThat(packageInformation1.directChildren.equals(packageInformation2.directChildren)).isTrue();
            assertThat(packageInformation1.getName().equals(packageInformation2.getName())).isTrue();
            assertThat(packageInformation1.getType().equals(Information.Type.ROOT)).isFalse();
            assertThat(packageInformation1.getType().equals(Information.Type.CLASS)).isFalse();
            assertThat(packageInformation1.getType().equals(Information.Type.PROJECT)).isFalse();
            assertThat(packageInformation1.getType().equals(Information.Type.PACKAGE)).isTrue();
            assertThat(packageInformation1.getType().equals(Information.Type.METHOD)).isFalse();
        }

        Set<ClassInformation<?>> ci1 =p1.getClassDependencies(p1.getLatestVersion());
        Set<ClassInformation<?>> ci2 =p2.getClassDependencies(p2.getLatestVersion());
        ci1.forEach(classInformation -> assertThat(ci2.contains(classInformation)));
        Iterator c1=ci1.iterator();
        Iterator c2=ci2.iterator();
        while(c1.hasNext()){
            OuterClassInformation classInformation1=(OuterClassInformation) c1.next();
            OuterClassInformation classInformation2=(OuterClassInformation) c2.next();
            assertThat(classInformation1.getParent().equals(classInformation2.getParent())).isTrue();
            assertThat(classInformation1.directChildren.equals(classInformation2.directChildren)).isTrue();
            assertThat(classInformation1.getName().equals(classInformation2.getName())).isTrue();
            assertThat(classInformation1.getType().equals(Information.Type.ROOT)).isFalse();
            assertThat(classInformation1.getType().equals(Information.Type.CLASS)).isTrue();
            assertThat(classInformation1.getType().equals(Information.Type.PROJECT)).isFalse();
            assertThat(classInformation1.getType().equals(Information.Type.PACKAGE)).isFalse();
            assertThat(classInformation1.getType().equals(Information.Type.METHOD)).isFalse();
        }

        Set<MethodInformation> mi1 =p1.getMethodDependencies(p1.getLatestVersion());
        Set<MethodInformation> mi2 =p2.getMethodDependencies(p2.getLatestVersion());
        mi1.forEach(methodInformation -> assertThat(mi2.contains(methodInformation)));
        Iterator m1=mi1.iterator();
        Iterator m2=mi2.iterator();
        while(m1.hasNext()){
            MethodInformation methodInformation1=(MethodInformation) m1.next();
            MethodInformation methodInformation2=(MethodInformation) m2.next();
            assertThat(methodInformation1.getParent().equals(methodInformation2.getParent())).isTrue();
            assertThat(methodInformation1.getName().equals(methodInformation2.getName())).isTrue();
            assertThat(methodInformation1.getType().equals(Information.Type.ROOT)).isFalse();
            assertThat(methodInformation1.getType().equals(Information.Type.CLASS)).isFalse();
            assertThat(methodInformation1.getType().equals(Information.Type.PROJECT)).isFalse();
            assertThat(methodInformation1.getType().equals(Information.Type.PACKAGE)).isFalse();
            assertThat(methodInformation1.getType().equals(Information.Type.METHOD)).isTrue();
        }
    }

    @Test
    public void testPomDependencies(){
        ProjectInformation p2 = dm.getProjects(null).iterator().next();
        Set<VersionInformation> v1=proj.getStored().getPomDependencies(proj.getStored().getLatestVersion());
        Set<VersionInformation> v2=p2.getPomDependencies(p2.getLatestVersion());
        assertThat(v1.equals(v2)).isTrue();
        v1.add(new VersionInformation("test",proj.getStored()));
        v2.add(new VersionInformation("test",p2));
        Iterator iv1=v1.iterator();
        Iterator iv2=v2.iterator();
        assertThat(iv2.hasNext()).isTrue();
        assertThat(iv1.equals(iv2)).isFalse();
        while(iv1.hasNext()){
            VersionInformation versionInformation1=(VersionInformation)iv1.next();
            VersionInformation versionInformation2=(VersionInformation)iv2.next();
            assertThat(versionInformation1.getName().equals(versionInformation2.getName())).isTrue();
        }
        v2.add(new VersionInformation("test2",p2));
        assertThat(v1.equals(v2)).isFalse();
        v1.forEach(versionInformation -> assertThat(v2.contains(versionInformation)));
    }

    @Test
    void compareInformationTypes(){
        ProjectInformation p1 = dm.getProjects(null).iterator().next();
        ProjectInformation p2 = dm.getProjects(null).iterator().next();
        assertThat(proj.getStored().compareTo(ca.getStored())).isNotEqualTo(0);
        assertThat(ca.getStored().compareTo(cb.getStored())).isNotEqualTo(0);
        assertThat(p1.compareTo(p2)).isZero();
        Set<Information<?>> oci=p1.getDirectChildren(p1.getLatestVersion());
        Set<Information<?>> oci2=p2.getDirectChildren(p2.getLatestVersion());
        assertThat(oci.iterator().next().compareTo(oci2.iterator().next())).isZero();
    }
}