package core;

import core.information.VersionInformation;
import database.services.GraphDBService;
import org.neo4j.ogm.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.io.IOException;

/**
 * Application is a SpringBootApplication and the main Class for ADCL, which configures itself and handles everything from configuration loading to database accessing.
 */
@SpringBootApplication
@SpringBootConfiguration
@ComponentScan(basePackages = "database.*")
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

        VersionInformation currentVersion;
        try {
            currentVersion = new DependencyExtractor().analyseClasses(appConfig.scanLocation, appConfig.currentVersionName);
        } catch (IOException e) {
            LOGGER.error("Could not analyse current class structure", e);
            return 1;
        }

        //Starting Database Service
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class);
        GraphDBService graphDBService = ctx.getBean(GraphDBService.class);


        //Getting previous Commit
        VersionInformation previousVersion;
        if (appConfig.previousVersionName == null) {
            previousVersion = graphDBService.getVersionRepository().findLatestVersion();
        } else {
            previousVersion = graphDBService.getVersion(appConfig.previousVersionName);
            if (previousVersion == null) {
                LOGGER.error("Version {} does not exist in the database. Not creating diff", appConfig.previousVersionName);
            }
        }

        if (previousVersion != null) {
            currentVersion.setPreviousVersion(previousVersion);

            //Analyse differences between current and previous Commit
            DiffExtractor diffExtractor = new DiffExtractor(previousVersion, currentVersion);

            //Save the Analysis in the Database
            graphDBService.saveChangelog(diffExtractor.getChangelogInformation());
        }

        //Save the Version in the Database
        graphDBService.saveVersion(currentVersion);

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
