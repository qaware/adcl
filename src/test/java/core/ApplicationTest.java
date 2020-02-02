package core;

import core.database.Neo4jService;
import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.ogm.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(classes = {Application.class, ApplicationTest.TestConfig.class})
@ActiveProfiles("test")
@Transactional
public class ApplicationTest {
    @Autowired
    Neo4jService neo4jService;

    private static GraphDatabaseService dbService;

    @Test
    public void test() {
        String[] args = new String[]{
                "spring.data.neo4j.uri=bolt://localhost:7687",
                "spring.data.neo4j.username=neo4j",
                "spring.data.neo4j.password=test",
                "project.commit.current=test",
                "project.uri=src/test/resources/testclassfiles3/epro1"
        };
        assertThatCode(() -> assertThat(Application.launch(args)).isZero()).doesNotThrowAnyException();

        String[] args2 = Arrays.append(args, "project.commit.previous=test");
        args2[3] = "project.commit.current=test2";
        args2[4] = "project.uri=src/test/resources/testclassfiles3/epro2";

        assertThatCode(() -> assertThat(Application.launch(args2)).isZero()).doesNotThrowAnyException();

        //TODO re-implement (@1.0.17)
        /*VersionInformation before = neo4jService.getVersion("test");
        VersionInformation after = neo4jService.getVersion("test2");
        ChangelogInformation changelog = neo4jService.getChangeLogRepository().findAll().iterator().next();

        assertThat(before).isNotNull();
        assertThat(after).isNotNull();
        assertThat(changelog).isNotNull();
        assertThat(changelog.getChangelog()).isNotEmpty();
        assertThat(changelog.getAfter().getVersionName()).isEqualTo(after.getVersionName());
        assertThat(changelog.getBefore().getVersionName()).isEqualTo(before.getVersionName());*/
    }

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
