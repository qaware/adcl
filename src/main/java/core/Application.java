package core;

import core.information.PackageInformation;
import database.services.GraphDBService;
import org.neo4j.ogm.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableNeo4jRepositories("database.repositories")
@SpringBootConfiguration
@ComponentScan(basePackages = "database.*")
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static String uri;
    private static String username;
    private static String password;

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));

            String destination = properties.getProperty("report.destination");
            String path = properties.getProperty("project.uri");
            uri = properties.getProperty("spring.data.neo4j.uri");
            username = properties.getProperty("spring.data.neo4j.username");
            password = properties.getProperty("spring.data.neo4j.password");

            ClassCollector alg = new ClassCollector(path);
            alg.generateFileList();
            DependencyExtractor extractor = new DependencyExtractor();

            Collection<PackageInformation> packages = extractor.analyseClasses(alg.getList().stream()
                    .map(File::getAbsolutePath).collect(Collectors.toList()));

            DependencyListWriter.writeListToFile(packages, destination, "test");

            ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
            GraphDBService graphDBService = ctx.getBean(GraphDBService.class);
            graphDBService.saveAllNodes(packages);
            graphDBService.getPackageRepository().findAll();
            ctx.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Bean
    public Configuration configuration() {
        Neo4jProperties properties = new Neo4jProperties();
        properties.setUri(uri);
        properties.setPassword(password);
        properties.setUsername(username);
        return properties.createConfiguration();
    }

}
