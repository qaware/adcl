package util;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    /**
     * Casts a Set of type T to type U, dropping not castable entries
     *
     * @param set     the original set
     * @param toClass the corresponding class for type parameter {@code <U>}
     * @param <T>     the old type
     * @param <U>     the new type
     * @return a new set of type U
     */
    @NotNull
    public static <T, U> Set<U> cast(@NotNull Set<T> set, @NotNull Class<U> toClass) {
        return set.stream().filter(toClass::isInstance).map(toClass::cast).collect(Collectors.toSet());
    }

    /**
     * Tries to resolve a CLI command to the binary that provides it
     *
     * @param cmd the command to be resolved
     * @return the binary that provides the command, or null if no such binary found
     */
    @Nullable
    public static Path searchInPath(String cmd) {
        try {
            String raw = new BufferedReader(new InputStreamReader(new ProcessBuilder(SystemUtils.IS_OS_WINDOWS ? "where" : "which", cmd).start().getInputStream())).readLine();
            if (raw == null) return null;
            return Paths.get(raw);
        } catch (IOException | InvalidPathException e) {
            LOGGER.error("Exception while searching for {} in PATH", cmd, e);
            return null;
        }
    }

    /**
     * @param thr   the exception
     * @param cause the Throwable type to search for
     * @return whether the exception has given cause type in its (transitive) causes
     */
    public static boolean hasCause(Throwable thr, @NotNull Class<? extends Throwable> cause) {
        if (cause.isInstance(thr)) return true;
        else if (thr.getCause() != null) return hasCause(thr.getCause(), cause);
        else return false;
    }

    /**
     * @param path a (relative) path in a java structure
     * @return the corresponding package name if the path would represent a java structure
     */
    @NotNull
    public static String pathToPackage(@NotNull Path path) {
        return path.toString().replace(path.getFileSystem().getSeparator(), ".");
    }

    /**
     * Resolves a Map with String keys and either Boolean or other such Maps as values to a {@code Map<String, Boolean>}, concatenating the keys to one "super" key
     *
     * @param <T>       the value type to be expected in the map
     * @param valueType the corresponding class for type parameter {@code <T>}
     * @param prefix    the prefix for the final super keys
     * @param map       the map to resolve
     * @return the map with "super" keys
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> resolveNestedMaps(@NotNull Class<T> valueType, @Nullable String prefix, @NotNull Map<String, Object> map) {
        Map<String, T> result = new HashMap<>();
        map.forEach((k, v) -> {
            String key = prefix == null ? k : (prefix + '.' + k);
            if (valueType.isInstance(v)) result.put(key, (T) v);
            else if (v instanceof Map) resolveNestedMaps(valueType, key, (Map<String, Object>) v);
            else
                throw new IllegalStateException("versionInfoInternal contains invalid element of type " + (v == null ? Void.class : v.getClass()));
        });
        return result;
    }

    /**
     * @param list the list to stream
     * @param <T>  the element type
     * @return a stream of T traversing the list's elements in reverse order
     */
    public static <T> Stream<T> reverseStream(@NotNull List<T> list) {
        int size = list.size();
        return IntStream.rangeClosed(0, size).mapToObj(i -> list.get(size - i));
    }

    /**
     * A vararg variant for {@link Stream#concat(Stream, Stream)}
     *
     * @param streams the streams to concat. Order determines resulting stream element order
     * @param <T>     element type
     * @return the concatenated stream
     * @see Stream#concat(Stream, Stream)
     */
    @SafeVarargs
    public static <T> Stream<T> concatStreams(@NotNull Stream<? extends T>... streams) {
        Stream<T> result = Stream.empty();
        for (Stream<? extends T> stream : streams) result = Stream.concat(result, stream);
        return result;
    }

    /**
     * Executes a call to maven
     *
     * @param pomPath          the path to the maven project pom you want to work with
     * @param interactiveInput the input for interactive mode or null for batch mode
     * @param cliArgs          additional arguments appended to the shell command
     * @param goals            the goals to activate, separated by spaces
     * @param options          key-value pairs as passed options
     * @return the maven console output
     * @throws MavenInvocationException if the maven call itself fails itself or maven is not found
     */
    @NotNull
    @SafeVarargs
    public static Pair<Integer, String> callMaven(@NotNull Path pomPath, @Nullable String interactiveInput, @Nullable String cliArgs, @NotNull String goals, @NotNull Pair<String, String>... options) throws MavenInvocationException {
        Properties properties = new Properties();
        for (Pair<String, String> option : options) properties.setProperty(option.getKey(), option.getValue());

        Path mvnPath = Utils.searchInPath("mvn");
        StringWriter sw = new StringWriter();

        InvocationResult mvnResult = new DefaultInvoker()
                .setMavenHome(mvnPath == null ? null : mvnPath.getParent().getParent().toFile())
                .setOutputHandler(new PrintWriter(sw)::println)
                .execute(new DefaultInvocationRequest()
                        .setPomFile(pomPath.toFile())
                        .setGoals(Arrays.asList(goals.split(" ")))
                        .setBatchMode(interactiveInput == null)
                        .setInputStream(interactiveInput == null ? null : new ByteArrayInputStream(interactiveInput.getBytes()))
                        .setProperties(properties)
                        .setBuilder(cliArgs));
        return Pair.of(mvnResult.getExitCode(), sw.toString());
    }

    /**
     * @param pom a maven pom.xml
     * @param var the variable to evaluate
     * @return the resolved value in pom
     */
    @Nullable
    public static String getMavenVar(@NotNull Path pom, String var) {
        try {
            Pair<Integer, String> result = callMaven(pom, null, "help:evaluate", "-q", Pair.of("expression", var), Pair.of("forceStdout", "true"));
            return result.getKey() != 0 ? null : result.getValue();
        } catch (MavenInvocationException e) {
            return null;
        }
    }
}
