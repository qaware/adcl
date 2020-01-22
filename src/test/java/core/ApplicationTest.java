package core;

import core.information.ChangelogInformation;
import core.information.VersionInformation;
import database.services.GraphDBService;
import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@Transactional
public class ApplicationTest {
    private static GraphDatabaseService dbService;

    @Autowired
    GraphDBService graphDBService;

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

    @Test
    public void test() throws Exception {

        String[] args = new String[]{
                "spring.data.neo4j.uri=bolt://localhost:7687",
                "spring.data.neo4j.username=neo4j",
                "spring.data.neo4j.password=test",
                "project.commit.current=test",
                "project.uri=src/test/resources/testclassfiles3/epro1"
        };
        Application.main(args);

        args = Arrays.append(args, "project.commit.previous=test");
        args[3] = "project.commit.current=test2";
        args[4] = "project.uri=src/test/resources/testclassfiles3/epro2";

        Application.main(args);

        VersionInformation before = graphDBService.getVersion("test");
        VersionInformation after = graphDBService.getVersion("test2");
        ChangelogInformation changelog = graphDBService.getChangeLogRepository().findAll().iterator().next();

        assertThat(before).isNotNull();
        assertThat(after).isNotNull();
        assertThat(changelog).isNotNull();
        assertThat(changelog.getChangelog()).isNotEmpty();
        assertThat(changelog.getAfter().getVersionName()).isEqualTo(after.getVersionName());
        assertThat(changelog.getBefore().getVersionName()).isEqualTo(before.getVersionName());
    }
}
