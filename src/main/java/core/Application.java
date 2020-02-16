package core;

import com.fasterxml.jackson.core.JsonProcessingException;
import core.database.Neo4jService;
import core.depex.DependencyExtractor;
import core.information.ProjectInformation;
import core.information.RootInformation;
import core.information.VersionInformation;
import core.pm.ProjectManager;
import core.report.DiffExtractor;
import core.report.HTMLReporter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private static Configuration neo4jConfig;

    /**
     * Entry Point for fat jar
     *
     * @param args CLI args
     */
    public static void main(String[] args) {
        System.exit(launch(args));
    }

    /**
     * launches the application
     *
     * @param args CLI args
     * @return the exit code
     */
    public static int launch(String[] args) {
        ConfigurableApplicationContext ctx = null;
        try {
            LOGGER.info("Working Directory: {}", Paths.get(".").toAbsolutePath());

            ApplicationConfig appConfig = loadConfig(args);
            if (appConfig == null) return 1;

            ctx = launchSpring(appConfig);
            if (ctx == null) return 1;

            ExecutionData executionData = queryData(ctx, appConfig);

            if (executionData.runAnalysis && !analyse(appConfig, executionData.currentVersion)) return 1;

            generateReport(appConfig, executionData.currentVersion);

            if (!appConfig.localOnly) save(executionData.neo4jService);

            return 0;
        } catch (Exception e) {
            LOGGER.error("Application run failed!", e);
            return 1;
        } finally {
            if (ctx != null) ctx.close();
        }
    }

    @NotNull
    @Contract("_, _ -> new")
    private static ExecutionData queryData(@NotNull ConfigurableApplicationContext ctx, @NotNull ApplicationConfig appConfig) {
        LOGGER.info("Querying project data");
        Neo4jService neo4jService = ctx.getBean(Neo4jService.class);
        RootInformation root = neo4jService.getRoot();
        ProjectInformation project = (ProjectInformation) root.find(appConfig.projectName, null);
        if (project == null) {
            LOGGER.warn("Project {} not found. Creating new project", appConfig.projectName);
            project = new ProjectInformation(root, appConfig.projectName, true, "<unknown>");
        }
        VersionInformation currentVersion = project.getVersion(appConfig.currentVersionName);
        boolean runAnalysis = currentVersion == null;
        if (currentVersion == null) currentVersion = project.addVersion(appConfig.currentVersionName);
        LOGGER.info("Queried project data");
        return new ExecutionData(runAnalysis, currentVersion, neo4jService);
    }

    @Nullable
    private static ConfigurableApplicationContext launchSpring(ApplicationConfig appConfig) {
        LOGGER.info("Launching Spring");
        try {
            neo4jConfig = appConfig.neo4jConfig;
            ConfigurableApplicationContext ctx = SpringApplication.run(Application.class);
            LOGGER.info("Launched Spring");
            return ctx;
        } catch (Exception e) {
            if (Utils.hasCause(e, AuthenticationException.class)) {
                LOGGER.error("Could not authenticate to neo4j");
            } else if (Utils.hasCause(e, ServiceUnavailableException.class)) {
                LOGGER.error("Could not connect to neo4j, service unavailable");
            } else throw e;
            return null;
        }
    }

    private static boolean analyse(@NotNull ApplicationConfig appConfig, @NotNull VersionInformation currentVersion) {
        if (appConfig.projectManager != null) analysePomDependencies(appConfig.projectManager, currentVersion);
        return analyseCodeDependencies(appConfig, currentVersion);
    }

    private static void save(@NotNull Neo4jService neo4jService) {
        LOGGER.info("Saving collected data");
        neo4jService.saveRoot();
        LOGGER.info("Saved collected data");
    }

    private static void analysePomDependencies(@NotNull ProjectManager projectManager, VersionInformation currentVersion) {
        LOGGER.info("Analysing pom dependencies");
        PomDependencyExtractor.updatePomDependencies(projectManager, currentVersion);
        LOGGER.info("Analysed pom dependencies");
    }

    private static boolean analyseCodeDependencies(@NotNull ApplicationConfig appConfig, VersionInformation currentVersion) {
        LOGGER.info("Analysing code dependencies");
        try {
            new DependencyExtractor(appConfig.scanLocation, currentVersion, appConfig.projectManager).runAnalysis();
            LOGGER.info("Analysed code dependencies");
        } catch (IOException e) {
            LOGGER.error("Could not analyse current class structure", e);
            return false;
        }
        return true;
    }

    private static void generateReport(@NotNull ApplicationConfig appConfig, VersionInformation currentVersion) {
        LOGGER.info("Generating static report artifact");
        try {
            VersionInformation previousVersion = appConfig.previousVersionName == null ? currentVersion.previous() : currentVersion.getProject().getOrCreateVersion(appConfig.previousVersionName);
            HTMLReporter.generateReport(new DiffExtractor(previousVersion, currentVersion).generateDiff(true, true), appConfig.reportPath);
            LOGGER.info("Generated static report artifact");
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not generate static report", e);
        }
    }

    @Nullable
    private static ApplicationConfig loadConfig(String[] args) {
        LOGGER.info("Loading configuration");
        Config.load(args);
        try {
            ApplicationConfig appConfig = new ApplicationConfig();
            LOGGER.info("ADCL args: {}", appConfig);
            return appConfig;
        } catch (ApplicationConfig.ConfigurationException configurationException) {
            return null;
        }
    }

    /**
     * Configures data access to a Neo4j database
     *
     * @return Configuration for Spring Data Neo4j
     */
    @Profile("!test")
    @Bean
    public Configuration configuration() {
        return neo4jConfig;
    }

    private static class ExecutionData {
        public final boolean runAnalysis;
        public final VersionInformation currentVersion;
        private final Neo4jService neo4jService;

        private ExecutionData(boolean runAnalysis, VersionInformation currentVersion, Neo4jService neo4jService) {
            this.runAnalysis = runAnalysis;
            this.currentVersion = currentVersion;
            this.neo4jService = neo4jService;
        }
    }
}
