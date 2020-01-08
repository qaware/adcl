package core.information;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertThat(c2.getPackageDependencies().first()).isEqualTo(p1);
    }

    @Test
    void getClassDependencies() {
        assertThat(c2.getClassDependencies().first()).isEqualTo(c1);
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
        assertThat(c2.getMethodDependencies().first()).isEqualTo(test1);
    }
}