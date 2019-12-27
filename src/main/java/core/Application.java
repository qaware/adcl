package core;

import core.information.ChangelogInformation;
import core.information.PackageInformation;
import core.information.VersionInformation;
import database.services.GraphDBService;
import org.neo4j.ogm.config.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableNeo4jRepositories("database.repositories")
@SpringBootConfiguration
@ComponentScan(basePackages = "database.*")
public class Application {
    public static void main(String[] args) throws IOException {
        Config.load(args);

        Path scanLocation = Config.getPath("project.uri", null);
        if (scanLocation == null) throw new IOException("project.uri is not properly defined in config.properties");

        ClassCollector alg = new ClassCollector(scanLocation.toString());
        alg.generateFileList();
        DependencyExtractor extractor = new DependencyExtractor();

        Collection<PackageInformation> packages = extractor.analyseClasses(alg.getList().stream()
                .map(File::getAbsolutePath).collect(Collectors.toList()));

        DependencyListWriter.writeListToFile(packages, Config.get("report.destination", "report"), "report");

        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        GraphDBService graphDBService = ctx.getBean(GraphDBService.class);
        VersionInformation current = new VersionInformation(packages, Config.get("project.commit", "commitNA"));
        graphDBService.saveVersion(current);

        DiffExtractor diffExtractor = new DiffExtractor(new ArrayList<>(), packages);

        graphDBService.saveChangelog(new ChangelogInformation(diffExtractor.getChanged(),
                new VersionInformation(new ArrayList<>(), "init"), current));

        ctx.close();

    }

    @Bean
    public Configuration configuration() {
        Neo4jProperties properties = new Neo4jProperties();
        properties.setUri(Config.get("spring.data.neo4j.uri", "bolt://127.0.0.1:7687"));
        properties.setUsername(Config.get("spring.data.neo4j.username", "neo4j"));
        properties.setPassword(Config.get("spring.data.neo4j.password", "neo4j"));
        return properties.createConfiguration();
    }

}
