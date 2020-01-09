package core;

import core.information.ClassInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil.*;

class DependencyExtractorTest {
    private static final Path TESTCLASS_FOLDER = Paths.get("src", "test", "resources", "testclassfiles2");

    private DependencyExtractor depEx;
    private List<String> classFiles;

    @SuppressWarnings({"unused", "UnusedAssignment"})
    private static Set<PackageInformation> cmpData() {
        PackageInformation pa, pb, pd;
        ClassInformation ca, cabase, cb, cc, cca, cci, ce;
        MethodInformation caMa, caMb, caE, cabaseE, cbC, cbGia1, ccRca, ccC, ccaC, ccaGcc, cciC, cciRca, ceEm, caC, cbCC, cbM, cbL, cbGia2;

        Set<PackageInformation> result = version(
                pa = pi("packageA",
                        ca = ci("packageA.ClassA", false, true,
                                caC = mi("packageA.ClassA.<init>()"),
                                caMa = mi("packageA.ClassA.methodA()"),
                                caMb = mi("packageA.ClassA.methodB(packageB.ClassB)"),
                                caE = mi("packageA.ClassA.empty()")
                        ),
                        cabase = ci("packageA.ClassABase", false, true,
                                cabaseE = mi("packageA.ClassABase.empty()")
                        )
                ),
                pb = pi("packageB",
                        cb = ci("packageB.ClassB", true, true,
                                cbC = mi("packageB.ClassB.<init>()"),
                                cbCC = mi("packageB.ClassB.<clinit>()"),
                                cbGia1 = mi("packageB.ClassB.getInstanceA()"),
                                cbM = mi("packageB.ClassB.method(java.util.function.Predicate)"),
                                cbL = mi("packageB.ClassB.lambda$getInstanceA$0(java.lang.String)"),
                                cbGia2 = mi("packageB.ClassB.getInstanceA(java.lang.String,int,packageA.ClassA[])")
                        )
                ),
                pd = pi("default",
                        cc = ci("ClassC", false, true,
                                ccC = mi("ClassC.<init>()"),
                                ccRca = mi("ClassC.retrieveClassA()")
                        ),
                        cca = ci("ClassC$1", false, true,
                                ccaC = mi("ClassC$1.<init>(ClassC)"),
                                ccaGcc = mi("ClassC$1.getClassC()")
                        ),
                        cci = ci("ClassC$ClassCInner", false, true,
                                cciC = mi("ClassC$ClassCInner.<init>(ClassC)"),
                                cciRca = mi("ClassC$ClassCInner.retrieveClassA()")
                        ),
                        ce = ci("ExternalClass", false, false,
                                ceEm = mi("ExternalClass.extMethod()")
                        )
                )
        );

        p(caMa, pb, cb, cbC);
        p(caMb, pb, cb);
        //TODO analyse LDC OpCodes
        //p(cbC, ca);
        p(cbCC, pa, ca, cabase, caC);
        p(cbGia1, pa, ca, cabase);
        p(ccC, pd, cca, ccaC);
        p(ccaC, pd, cc);
        p(ccRca, pa, pb, ca, cbGia1, cb);
        p(ccaGcc, pd, cc, ccC);
        p(cciC, pd, cc);
        p(cciRca, pd, pa, ce, ca, ceEm);
        p(cbGia2, pa, pb, ca, cb, cbM);

        return result;
    }

    @BeforeEach
    void setUp() throws IOException {
        depEx = new DependencyExtractor();
        classFiles = Files.walk(TESTCLASS_FOLDER).filter(p -> !Files.isDirectory(p)).map(Path::toString).collect(Collectors.toList());
    }

    @Test
    void analyseClasses() {
        Set<PackageInformation> analysedClasses = depEx.analyseClasses(classFiles);

        assertThat(analysedClasses).isEqualTo(cmpData());
    }
}