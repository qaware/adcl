package core;

import core.information2.Neo4jService;
import core.information2.ProjectInformation;
import core.information2.RootInformation;
import core.information2.VersionInformation;
import org.neo4j.ogm.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.io.IOException;

/**
 * Application is a SpringBootApplication and the main Class for ADCL, which configures itself and handles everything from configuration loading to database accessing.
 */
@SpringBootApplication
@SpringBootConfiguration
@EnableNeo4jRepositories("database.repositories")
@EntityScan("core.information")
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static ApplicationConfig appConfig;

    public static void main(String[] args) {
        System.exit(launch(args));
    }

    public static int launch(String[] args) {
        //Loading config
        Config.load(args);

        try {
            appConfig = new ApplicationConfig();
        } catch (ApplicationConfig.ConfigurationException configurationException) {
            return 1;
        }

        //Starting Database Service
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class);
        Neo4jService neo4jService = ctx.getBean(Neo4jService.class);

        RootInformation root = neo4jService.getRoot();
        ProjectInformation project = (ProjectInformation) root.find(appConfig.projectName, null);
        VersionInformation currentVersion;
        if (project == null) {
            LOGGER.warn("Project {} not found. Creating new project", appConfig.projectName);
            project = new ProjectInformation(root, appConfig.projectName, true, appConfig.currentVersionName);
            currentVersion = project.getLatestVersion();
        } else {
            currentVersion = project.addVersion(appConfig.projectName);
        }

        try {
            new DependencyExtractor(appConfig.scanLocation, project, currentVersion).analyseClasses();
        } catch (IOException e) {
            LOGGER.error("Could not analyse current class structure", e);
            return 1;
        }

        //Save the Version in the Database
        neo4jService.saveRoot();

        ctx.close();
        return 0;
    }

    /**
     * Configures data access to a Neo4j database
     *
     * @return Configuration for Spring Data Neo4j
     */
    @Profile("!test")
    @Bean
    public Configuration configuration() {
        return appConfig.neo4jConfig;
    }
}
