package core;

import core.database.Neo4jService;
import core.depex.DependencyExtractor;
import core.information.ProjectInformation;
import core.information.RootInformation;
import core.information.VersionInformation;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
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
import util.Utils;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Application is a SpringBootApplication and the main Class for ADCL, which configures itself and handles everything from configuration loading to database accessing.
 */
@SpringBootApplication
@SpringBootConfiguration
@EnableNeo4jRepositories(basePackageClasses = Neo4jService.InformationRepository.class, considerNestedRepositories = true)
@EntityScan(basePackageClasses = RootInformation.class)
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static ApplicationConfig appConfig;

    /**
     * Entry Point for fat jar
     *
     * @param args CLI args
     */
    public static void main(String[] args) {
        try {
            System.exit(launch(args));
        } catch (Exception e) {
            LOGGER.error("Application run failed!", e);
            System.exit(1);
        }
    }

    /**
     * launches the application
     *
     * @param args CLI args
     * @return the exit code
     * @throws Exception potentially any exception, just to enforce catching so application stops in a controlled manner
     */
    @SuppressWarnings({"RedundantThrows", "java:S112"} /* enforce caller to catch any potential exception */)
    public static int launch(String[] args) throws Exception {
        LOGGER.info("Working Directory: {}", Paths.get(".").toAbsolutePath());
        //Loading config
        Config.load(args);

        try {
            appConfig = new ApplicationConfig();
        } catch (ApplicationConfig.ConfigurationException configurationException) {
            return 1;
        }
        LOGGER.info("ADCL args: {}", appConfig);

        LOGGER.info("Launching Spring");
        ConfigurableApplicationContext ctx;
        try {
            ctx = SpringApplication.run(Application.class);
        } catch (Exception e) {
            if (Utils.hasCause(e, AuthenticationException.class)) {
                LOGGER.error("Could not authenticate to neo4j");
            } else if (Utils.hasCause(e, ServiceUnavailableException.class)) {
                LOGGER.error("Could not connect to neo4j, service unavailable");
            } else throw e;
            return 1;
        }
        LOGGER.info("Querying project data");
        Neo4jService neo4jService = ctx.getBean(Neo4jService.class);
        RootInformation root = neo4jService.getRoot();
        ProjectInformation project = (ProjectInformation) root.find(appConfig.projectName, null);
        VersionInformation currentVersion;
        if (project == null) {
            LOGGER.warn("Project {} not found. Creating new project", appConfig.projectName);
            project = new ProjectInformation(root, appConfig.projectName, true, appConfig.currentVersionName);
            currentVersion = project.getLatestVersion();
        } else {
            currentVersion = project.addVersion(appConfig.currentVersionName);
        }

        if (appConfig.projectPom != null) {
            LOGGER.info("Analysing pom dependencies");
            try {
                new PomDependencyReader(appConfig.projectPom).updatePomDependencies(currentVersion);
            } catch (IOException | XmlPullParserException e) {
                LOGGER.error("Could not analyse current pom dependencies", e);
                return 1;
            }
        }

        LOGGER.info("Analysing code dependencies");
        try {
            new DependencyExtractor(appConfig.scanLocation, currentVersion, appConfig.projectPom).runAnalysis();
        } catch (IOException | MavenInvocationException e) {
            LOGGER.error("Could not analyse current class structure", e);
            return 1;
        }

        LOGGER.info("Saving collected data");
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
