package com.github.qaware.adcl.pm;

import com.github.qaware.adcl.util.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ProjectManager} for Maven
 */
public class MavenProjectManager implements ProjectManager {
    @NotNull
    private final Path pomFile;
    @NotNull
    private final String projectName;
    @NotNull
    private final String projectVersion;
    @NotNull
    private final Path classesOutput;
    @NotNull
    private final Path artifactOutput;
    @NotNull
    private final Map<Dependency, Path> compileDependencies;
    @NotNull
    private final Set<Dependency> dependencies;

    public MavenProjectManager(@NotNull Path basedir, @NotNull Path pomFile) throws MavenInvocationException {
        this.pomFile = pomFile;
        String[] vars = getVars("project.groupId", "project.artifactId", "project.version", "project.build.outputDirectory", "project.build.directory");
        projectName = vars[0].replace('.', '-') + ':' + vars[1];
        projectVersion = vars[2];
        classesOutput = basedir.resolve(vars[3]);
        artifactOutput = basedir.resolve(vars[4]);
        dependencies = getDependencies0();
        compileDependencies = getCompileDependencies0();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getProjectName() {
        return projectName;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getProjectVersion() {
        return projectVersion;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Path getClassesOutput() {
        return classesOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Path getArtifactOutput() {
        return artifactOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Set<Dependency> getDependencies() {
        return dependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<Dependency, Path> getCompileDependencies() {
        return compileDependencies;
    }

    @NotNull
    public Set<Dependency> getDependencies0() throws MavenInvocationException {
        String mvnResult = callMaven(null, null, "dependency:list", Pair.of("excludeTransitive", "true"));

        Matcher matcher = Pattern.compile("(?<group>\\S+?):(?<artifact>\\S+):.+?:(?<version>\\S+?):(?<scope>\\w+?)[\\x{1b}\\n\\s]").matcher(mvnResult);
        Set<Dependency> result = new HashSet<>();
        while (matcher.find()) {
            result.add(new Dependency(matcher.group("group") + ':' + matcher.group("artifact"), matcher.group("version"), matcher.group("scope")));
        }
        return result;
    }

    @NotNull
    public Map<Dependency, Path> getCompileDependencies0() throws MavenInvocationException {
        String mvnResult = callMaven(null, null, "dependency:list",
                Pair.of("outputAbsoluteArtifactFilename", "true"),
                Pair.of("includeScope", "compile")
        );

        Matcher matcher = Pattern.compile("(?<group>\\S+?):(?<artifact>\\S+):.+?:(?<version>\\S+?):compile:(?<path>.+?\\.jar)[\\x{1b}\\n\\s]").matcher(mvnResult);
        Map<Dependency, Path> result = new HashMap<>();
        while (matcher.find()) {
            Dependency dep = new Dependency(matcher.group("group") + ':' + matcher.group("artifact"), matcher.group("version"), "compile");
            result.put(dep, Paths.get(matcher.group("path"))); // absolute path assumed
        }
        return result;
    }

    /**
     * Executes a call to maven
     *
     * @param cliArgs additional arguments appended to the shell command
     * @param goals   the goals to activate, separated by spaces
     * @param options key-value pairs as passed options
     * @return the maven console output
     * @throws MavenInvocationException if the maven call itself fails itself or maven is not found
     */
    @NotNull
    @SafeVarargs
    private final String callMaven(@Nullable String interactiveInput, @Nullable String cliArgs, @NotNull String goals, @NotNull Pair<String, String>... options) throws MavenInvocationException {
        Pair<Integer, String> mvnResult = Utils.callMaven(pomFile, interactiveInput, cliArgs, goals, options);
        if (mvnResult.getKey() != 0)
            throw new MavenInvocationException("Maven invocation has exit code " + mvnResult.getKey());
        return mvnResult.getValue();
    }

    /**
     * @param vars the variables to evaluate
     * @return the resolved values in pom
     */
    @NotNull
    private String[] getVars(@NotNull String... vars) throws MavenInvocationException {
        String input = Stream.of(vars).map(var -> String.format("${%s}", var)).collect(Collectors.joining("\n", "", "\n0\n"));
        String output = callMaven(input, null, "help:evaluate");
        return Stream.of(output.split("\n")).filter(l -> !l.startsWith("[INFO]")).map(this::trimEndRollback).toArray(String[]::new);
    }

    @NotNull
    private String trimEndRollback(@NotNull String s) {
        return s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }
}
