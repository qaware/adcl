package core.information2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil2.*;

public class DatamodelCompareTest {
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
    void compareVersions() {
        ProjectInformation p = dm.getProjects(null).iterator().next();
        VersionInformation v1 = p.getLatestVersion();
        VersionInformation v2 = p.addVersion("2.0.0");
        assertThat(v1.isBefore(v2)).isTrue();
        assertThat(v2.isAfter(v1)).isTrue();
    }

    //TODO (deep)compare two trees
    //TODO compare different information classes a) of same type b) different type
    //TODO for each node type for each property in node for test equal and not equal elements
}
