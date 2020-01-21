package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil.*;

class ClassInformationTest {
    PackageInformation p1, pd;
    ClassInformation c1, c2;
    MethodInformation test1, test2, testc;

    @BeforeEach
    void setUp() {

        version(
                p1 = pi("p1",
                        c1 = ci("p1.c1", false, true,
                                test1 = mi("c1.test1()")
                        )
                ),
                pd = pi("default",
                        c2 = ci("c2", false, true,
                                testc = mi("c2.<init>()"),
                                test2 = mi("c2.test2()")
                        )
                )
        );
        p(test2, p1, c1, test1);
    }

    @Test
    void getClassName() {
        assertThat(c2.getClassName()).isEqualTo("c2");
    }

    @Test
    void getPackageDependencies() {
        assertThat(c2.getPackageDependencies().iterator().next()).isEqualTo(p1);
    }

    @Test
    void getClassDependencies() {
        assertThat(c2.getClassDependencies().iterator().next()).isEqualTo(c1);
    }

    @Test
    void getConstructorInformations() {
        c2.getMethodInformations().forEach(methodInformation -> {
            if (methodInformation.isConstructor()) assertThat(methodInformation).isEqualTo(testc);
        });
    }

    @Test
    void getMethodInformations() {
        c2.getMethodInformations().forEach(methodInformation -> {
            if (!methodInformation.isConstructor()) assertThat(methodInformation).isEqualTo(test2);
        });
    }

    @Test
    void getMethodDependencies() {
        assertThat(c2.getMethodDependencies().iterator().next()).isEqualTo(test1);
    }

    @Test
    void getPackageName(){
       assertThat(c1.getPackageName()).isEqualTo("p1");
    }

    @Test
    void getPackageInformation(){
        PackageInformation p1=new PackageInformation("one");
        PackageInformation p2=new PackageInformation("two");
        PackageInformation p3=new PackageInformation("three");
        List<PackageInformation> setPi = new ArrayList<>();
        setPi.add(p1);
        setPi.add(p2);
        setPi.add(p3);
        ClassInformation ci=new ClassInformation("whatever");
        p2.addClassInformation(ci);
        assertThat(ci.getPackageInformation(setPi)).isEqualTo(p2);
    }

    @Test
    public void getConstructorInformation(){
        Set<MethodInformation> testList=c2.getMethodInformations();
        ArrayList<MethodInformation> removeList=new ArrayList<>();
        for (MethodInformation mi : testList) {
            if (!mi.isConstructor()) {
                removeList.add(mi);
            }
        }
        for (MethodInformation mi:removeList){
            testList.remove(mi);
        }
        assertThat(c2.getConstructorInformation()).isEqualTo(testList);
    }
}