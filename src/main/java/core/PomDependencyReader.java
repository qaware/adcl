package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
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
 * Reads all dependencies from the pom.xml
 */
public class PomDependencyReader {
    @NotNull
    private final Path pomPath;

    /**
     * Init PomDependencyReader
     *
     * @param pomPath Path describes where the pom.xml file is
     */
    public PomDependencyReader(@NotNull Path pomPath) {
        this.pomPath = pomPath;
    }

    /**
     * Reads all dependencies from pom.xml file
     *
     * @return returns a set with dependencies
     * @throws IOException            if local pom exists but can't be read
     * @throws XmlPullParserException if local pom exists but is invalid
     */
    public Set<Dependency> readDependencies() throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(Files.newBufferedReader(pomPath));
        return new HashSet<>(model.getDependencies());
    }

    /**
     * @return all compilation-relevant dependencies including transitive dependencies. Use only for personal read access!
     * @throws MavenInvocationException if {@code mvn dependency:list} fails or mvn is not found on the system
     * @throws IOException              if {@code ./dependencies.txt} cannot be deleted
     * @apiNote Returned dependencies only have set groupId, artifactId, version, scope and systemPath. Even if scope is
     * compile the system path is given (contrary to the definition of {@link Dependency#getSystemPath()}).
     * @implSpec takes a while (dependent on internet connection and project size) as maven has to download the dependencies
     * <br>uses {@code ./dependencies.txt} for temporary output storage
     */
    public Set<Dependency> readAllCompilationRelevantDependencies() throws MavenInvocationException, IOException {
        Path outputPath = Paths.get("dependencies.txt");
        Files.deleteIfExists(outputPath);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setBatchMode(true);
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
