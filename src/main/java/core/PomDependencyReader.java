package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Makes a list with all dependencies from the pom.xml
 */
public class PomDependencyReader {
    private Path pomPath;

    /**
     * Init PomDependencyReader
     *
     * @param pomPath Path describes where the pom.xml file is
     */
    public PomDependencyReader(Path pomPath) {
        this.pomPath = pomPath;
    }

    /**
     * Reads all dependencies from pom.xml file
     *
     * @return returns a set with dependencies
     */
    public Set<Dependency> readDependencies() throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(Files.newBufferedReader(pomPath));
        return new HashSet<>(model.getDependencies());
    }

    public Set<Dependency> readAllCompilationRelevantDependencies() throws MavenInvocationException, IOException {
        Path outputPath = Paths.get("dependencies.txt");
        Files.deleteIfExists(outputPath);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomPath.toFile());
        request.setGoals(Collections.singletonList("dependency:list"));
        Properties properties = new Properties();
        properties.setProperty("outputAbsoluteArtifactFilename", "true");
        properties.setProperty("outputFile", "dependencies.txt");
        properties.setProperty("appendOutput", "true");
        properties.setProperty("includeScope", "compile");
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        Path mvnPath = Utils.searchInPath("mvn");
        if (mvnPath != null) invoker.setMavenHome(mvnPath.getParent().getParent().toFile());
        invoker.setOutputHandler(null);

        try {
            InvocationResult mvnResult = invoker.execute(request);
            if (mvnResult.getExitCode() != 0)
                throw new MavenInvocationException("mvn call failed", mvnResult.getExecutionException());

            Matcher matcher = Pattern.compile("(?<group>\\S+?):(?<artifact>\\S+):.+?:(?<version>\\S+?):compile:(?<path>.+?)\\x{1b}").matcher(new String(Files.readAllBytes(outputPath)));
            Set<Dependency> result = new HashSet<>();
            while (matcher.find()) {
                Dependency dep = new Dependency();
                dep.setGroupId(matcher.group("group"));
                dep.setArtifactId(matcher.group("artifact"));
                dep.setVersion(matcher.group("version"));
                dep.setScope("compile");
                dep.setSystemPath(matcher.group("path"));
                result.add(dep);
            }
            return result;
        } finally {
            Files.deleteIfExists(outputPath);
        }
    }
}
