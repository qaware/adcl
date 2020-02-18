package core.database;

import core.Application;
import core.PomDependencyExtractor;
import core.depex.DependencyExtractor;
import core.information.*;
import core.pm.MavenProjectManager;
import core.report.DiffExtractor;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import util.DataGenerationUtil;
import util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static util.DataGenerationUtil.*;

@SpringBootTest(classes = {Application.class, Neo4jServiceTest.TestConfig.class})
@ActiveProfiles("test")
public class Neo4jServiceTest {
    private static GraphDatabaseService dbService;
    DataGenerationUtil.Ref<ProjectInformation, RootInformation> proj;
    DataGenerationUtil.Ref<PackageInformation<ProjectInformation>, ProjectInformation> pa, pb;
    DataGenerationUtil.Ref<ClassInformation<PackageInformation<?>>, PackageInformation<?>> ca, cabase, cb;
    DataGenerationUtil.Ref<ClassInformation<ProjectInformation>, ProjectInformation> cc, ce, cca, cci;
    DataGenerationUtil.Ref<MethodInformation, ClassInformation<?>> caMa, caMb, caE, cabaseE, cbC, cbGia1, ccRca, ccC, ccaC, ccaGcc, cciC, cciRca, ceEm, caC, cbCC, cbM, cbL, cbGia2;
    @Autowired
    Neo4jService neo4jService;
    private RootInformation dm;
    private static RootInformation pomRoot = new RootInformation();
    private static RootInformation depExRoot = new RootInformation();

    @Autowired
    ApplicationContext ctx;

    @BeforeAll
    static void setUpDatabase() throws MavenInvocationException, IOException {
        try {
            BoltConnector bolt = new BoltConnector("0");
            dbService = new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder(new File("neo4j"))
                    .setConfig(bolt.type, "BOLT")
                    .setConfig(bolt.enabled, "true")
                    .setConfig(bolt.listen_address, "localhost:7687")
                    .newGraphDatabase();
        } catch (Exception ignored) {

        }

        ProjectInformation depExProj = new ProjectInformation(depExRoot, "proj", true, "<unknown>");
        runDepEx(depExProj, "testproject", "0.0.1");
        runDepEx(depExProj, "testproject2", "0.0.2");
        runDepEx(depExProj, "testproject3", "0.0.3");

        ProjectInformation pomProj = new ProjectInformation(pomRoot, "proj", true, "<unknown>");
        runPomAnalysis(pomProj, "testproject", "0.0.1");
        runPomAnalysis(pomProj, "testproject2", "0.0.2");
        runPomAnalysis(pomProj, "testproject3", "0.0.3");
    }

    @AfterAll
    static void cleanup() throws IOException {
        if (dbService != null) dbService.shutdown();
        Utils.delete(Paths.get("neo4j"));
        Utils.delete(Paths.get("certificates"));
        Utils.delete(Paths.get("store_lock"));
    }

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
    public void nodesTest() {
        SessionFactory sessionFactory = ctx.getBean(SessionFactory.class);
        Session newSession = sessionFactory.openSession();
        newSession.purgeDatabase();

        neo4jService.overrideRoot(dm);
        RootInformation r = neo4jService.getRoot();
        assertThat(r).isSameAs(dm);

        try (Transaction ignored = newSession.beginTransaction()) {
            newSession.loadAll(Information.class);
            RootInformation loaded = newSession.loadAll(RootInformation.class).iterator().next();
            assertThat(loaded).isNotSameAs(dm);
            assertThat(loaded).isEqualTo(dm);
            assertThat(loaded.deepEquals(dm)).isTrue();
        }
    }

    @NotNull
    private static VersionInformation runDepEx(@NotNull ProjectInformation project, String folderName, String versionName) throws IOException {
        VersionInformation result = project.addVersion(versionName);
        new DependencyExtractor(Paths.get("src", "test", "resources", "testclassfiles2", folderName, "target", "classes"), result, null).runAnalysis();
        return result;
    }

    @NotNull
    private static VersionInformation runPomAnalysis(@NotNull ProjectInformation project, String folderName, String versionName) throws MavenInvocationException {
        VersionInformation result = project.addVersion(versionName);
        Path basedir = Paths.get("src", "test", "resources", "testclassfiles2", folderName);
        PomDependencyExtractor.updatePomDependencies(new MavenProjectManager(basedir, basedir.resolve("pom.xml")), result);
        return result;
    }

    @Test
    public void versionsTest() {
        SessionFactory sessionFactory = ctx.getBean(SessionFactory.class);
        Session newSession = sessionFactory.openSession();
        newSession.purgeDatabase();

        neo4jService.overrideRoot(depExRoot);

        try (Transaction ignored = newSession.beginTransaction()) {
            newSession.loadAll(Information.class);
            RootInformation loaded = newSession.loadAll(RootInformation.class).iterator().next();

            ProjectInformation proj = (ProjectInformation) loaded.find("proj", null);
            assertThat(proj).isNotNull();
            VersionInformation v1 = proj.getVersion("0.0.1");
            assertThat(v1).isNotNull();
            VersionInformation v2 = proj.getVersion("0.0.2");
            assertThat(v2).isNotNull();
            VersionInformation v3 = proj.getVersion("0.0.3");
            assertThat(v3).isNotNull();

            assertThat(new DiffExtractor(v1, v2).generateDependencyDiff(false, false).stream().map(Object::toString)).containsExactlyInAnyOrder(
                    "proj.packageB.ClassB->null.org.springframework.stereotype.Service",
                    "proj.packageA.MyAnnotation.notNullRef()->null.org.jetbrains.annotations.NotNull",
                    "proj.packageA.ClassA->proj.packageA.MyAnnotation",
                    "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->proj.packageA.ClassA.$$$reportNull$$$0(int)",
                    "proj.packageA.ClassA->null.org.jetbrains.annotations.Nullable",
                    "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->null.org.springframework.context.NoSuchMessageException",
                    "proj.packageA.ClassA->null.org.jetbrains.annotations.NotNull",
                    "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->null.org.jetbrains.annotations.NotNull",
                    "proj.packageA.ClassA.methodC(boolean, byte, char, short, int, long, float, double, java.lang.String)->proj.packageA.ClassA.methodA()"
            );

            assertThat(new DiffExtractor(v2, v3).generateDependencyDiff(false, false).stream().map(Object::toString)).containsExactlyInAnyOrder(
                    "proj.packageA.ClassA.newMethod()+>proj.packageA.ClassABase"
            );
        }
    }

    @Test
    void pomDependencyWriteTest() {
        SessionFactory sessionFactory = ctx.getBean(SessionFactory.class);
        Session newSession = sessionFactory.openSession();
        newSession.purgeDatabase();
        neo4jService.overrideRoot(pomRoot);

        try (Transaction ignored = newSession.beginTransaction()) {
            newSession.loadAll(Information.class);
            newSession.loadAll(ProjectInformation.class);
            RootInformation loaded = newSession.loadAll(RootInformation.class).iterator().next();

            ProjectInformation proj = (ProjectInformation) loaded.find("proj", null);
            assertThat(proj).isNotNull();
            VersionInformation v1 = proj.getVersion("0.0.1");
            assertThat(v1).isNotNull();
            VersionInformation v2 = proj.getVersion("0.0.2");
            assertThat(v2).isNotNull();
            VersionInformation v3 = proj.getVersion("0.0.3");
            assertThat(v3).isNotNull();

            assertThat(new DiffExtractor(v1, v2).generatePomDiff().stream().map(Object::toString)).containsExactlyInAnyOrder(
                    "-> null@org-springframework:spring-context",
                    "-> 18.0.0@org-jetbrains:annotations"
            );
            assertThat(new DiffExtractor(v2, v3).generatePomDiff().stream().map(Object::toString)).containsExactlyInAnyOrder(
                    "-> 5.2.1.RELEASE@org-springframework:spring-context",
                    "-> null@org-jetbrains:annotations"
            );
        }
    }

    @Test
    void pomDependencyReadTest() {
        SessionFactory sessionFactory = ctx.getBean(SessionFactory.class);
        Session newSession = sessionFactory.openSession();
        newSession.purgeDatabase();
        neo4jService.overrideRoot(pomRoot);
        neo4jService.loadRoot();

        ProjectInformation proj = (ProjectInformation) neo4jService.getRoot().find("proj", null);
        assertThat(proj).isNotNull();
        VersionInformation v1 = proj.getVersion("0.0.1");
        assertThat(v1).isNotNull();
        VersionInformation v2 = proj.getVersion("0.0.2");
        assertThat(v2).isNotNull();
        VersionInformation v3 = proj.getVersion("0.0.3");
        assertThat(v3).isNotNull();

        assertThat(new DiffExtractor(v1, v2).generatePomDiff().stream().map(Object::toString)).containsExactlyInAnyOrder(
                "-> null@org-springframework:spring-context",
                "-> 18.0.0@org-jetbrains:annotations"
        );
        assertThat(new DiffExtractor(v2, v3).generatePomDiff().stream().map(Object::toString)).containsExactlyInAnyOrder(
                "-> 5.2.1.RELEASE@org-springframework:spring-context",
                "-> null@org-jetbrains:annotations"
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Profile("test")
        @Bean
        public Configuration testConfig() {
            Neo4jProperties properties = new Neo4jProperties();
            properties.setUri("bolt://127.0.0.1:7687");
            properties.setUsername("neo4j");
            properties.setPassword("test");
            return properties.createConfiguration();
        }
    }
}
