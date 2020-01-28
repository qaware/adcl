package core.information;

import core.Application;
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
import org.springframework.util.FileSystemUtils;
import util.DataGenerationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

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

    @Autowired
    ApplicationContext ctx;

    @BeforeAll
    static void setUpDatabase() {
        BoltConnector bolt = new BoltConnector("0");

        dbService = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File("neo4j"))
                .setConfig(bolt.type, "BOLT")
                .setConfig(bolt.enabled, "true")
                .setConfig(bolt.listen_address, "localhost:7687")
                .newGraphDatabase();
    }

    @AfterAll
    static void cleanup() throws IOException {
        dbService.shutdown();
        FileSystemUtils.deleteRecursively(new File("neo4j"));
        FileSystemUtils.deleteRecursively(new File("certificates"));
        Files.deleteIfExists(Paths.get("store_lock"));
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
    public void test() {
        neo4jService.overrideRoot(dm);
        RootInformation r = neo4jService.getRoot();
        assertThat(r).isSameAs(dm);

        SessionFactory sessionFactory = ctx.getBean(SessionFactory.class);
        Session session = sessionFactory.openSession();
        try (Transaction ignored = session.beginTransaction()) {
            Collection<Information> all = session.loadAll(Information.class);
            RootInformation loaded = session.loadAll(RootInformation.class).iterator().next();
            assertThat(loaded).isNotSameAs(dm);
            assertThat(loaded).isEqualTo(dm);
            assertThat(loaded.deepEquals(dm)).isTrue();
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Profile("test")
        @Bean
        public Configuration testConfig() {
            Neo4jProperties properties = new Neo4jProperties();
            properties.setUri("bolt://127.0.0.1:7687");
            properties.setUsername("neo4j");
            properties.setPassword("neo4j");
            return properties.createConfiguration();
        }
    }
}
