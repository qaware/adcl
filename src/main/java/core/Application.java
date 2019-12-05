package core;

import core.information.PackageInformation;
import core.services.GraphDBService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableNeo4jRepositories("core.repositories")
@SpringBootConfiguration
public class Application {

    public static void main(String[] args) {
        String path = args[1];
        String destination = args[0];
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
    }
}
