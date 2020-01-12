package core;


import core.information.ChangelogInformation;
import core.information.PackageInformation;
import core.information.VersionInformation;
import database.services.GraphDBService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
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
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableNeo4jRepositories("database.repositories")
@SpringBootConfiguration
@ComponentScan(basePackages = "database.*")
@Mojo(name = "start")
public class ApplicationMojo extends AbstractMojo {
    private static final String COMMIT_NA = "COMMIT_NA";
    @Bean
    public Configuration configuration() {
        Neo4jProperties properties = new Neo4jProperties();
        properties.setUri(Config.get("spring.data.neo4j.uri", "bolt://127.0.0.1:7687"));
        properties.setUsername(Config.get("spring.data.neo4j.username", "neo4j"));
        properties.setPassword(Config.get("spring.data.neo4j.password", "neo4j"));
        return properties.createConfiguration();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //Loading config
        getLog().info("Start, Welcome");
        getLog().info("Start your Configuration here");
        Scanner scanner = new Scanner(System.in);
        String t = scanner.nextLine();
        String args2[] = t.split(" ");
        Config.load(args2);
        //Analysing current version
        Path scanLocation = Config.getPath("project.uri", null);
        if (scanLocation == null) try {
            throw new IOException("project.uri is not properly defined in config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClassCollector alg = new ClassCollector(scanLocation.toString());
        alg.generateFileList();
        DependencyExtractor extractor = new DependencyExtractor();

        Collection<PackageInformation> packages = extractor.analyseClasses(alg.getList().stream()
                .map(File::getAbsolutePath).collect(Collectors.toList()));

        //Writing Analyse to a File
        DependencyListWriter.writeListToFile(packages, Config.get("report.destination", "report"), "report");

        //Starting Database Service
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args2);
        GraphDBService graphDBService = ctx.getBean(GraphDBService.class);

        //Getting previous Commit
        VersionInformation previous;
        String previousCommitName = Config.get("project.commit.previous", COMMIT_NA);
        VersionInformation current = new VersionInformation(packages, Config.get("project.commit", COMMIT_NA));

        if (!previousCommitName.equals(COMMIT_NA)) {
            previous = graphDBService.getVersion(previousCommitName);
            if (previous == null)
                throw new NoSuchElementException("Commit " + previousCommitName + " does not exist in the database");

            current.setPreviousVersion(previous);

            //Analyse differences between current and previous Commit
            DiffExtractor diffExtractor = new DiffExtractor(previous.getPackageInformations(), packages);

            //Save the Analysis in the Database
            graphDBService.saveChangelog(new ChangelogInformation(diffExtractor.getChangelist(), previous, current));
        }

        //Save the Version in the Database
        graphDBService.saveVersion(current);

        ctx.close();
    }
}