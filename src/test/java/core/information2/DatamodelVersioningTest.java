package core.information2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil2.*;

public class DatamodelVersioningTest {
    private RootInformation dm;
    private ProjectInformation proj;

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
        proj = dm.getProjects(null).iterator().next();
    }

    @Test
    void versionTraversalTest() {
        VersionInformation v1 = proj.getLatestVersion();
        VersionInformation v2 = proj.addVersion("2.0.0");
        assertThat(v1.previous()).isNull();
        assertThat(v1.next()).isEqualTo(v2);
        assertThat(v2.previous()).isEqualTo(v1);
        assertThat(v2.next()).isNull();
    }

    //TODO all
}
