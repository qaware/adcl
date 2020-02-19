package com.github.qaware.adcl;

import com.github.qaware.adcl.pm.Dependency;
import com.github.qaware.adcl.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

/**
 * A utility class to create class indices for class-to-project mappings
 * The index is a {@link Map} with the full class names as keys and the project they correspond to as values
 */
public class IndexBuilder {
    private IndexBuilder() {
    }

    /**
     * Creates an index based on a maven dependency with set system path
     *
     * @param dependency the maven dependency
     * @param appendTo   the index to potentially append to
     * @return the created index ({@code appendTo} if provided)
     * @throws IOException if dependency's system path is null or invalid (does not point to a valid jar file)
     */
    @NotNull
    public static Map<String, String> index(@NotNull Dependency dependency, @NotNull Path filePath, @Nullable Map<String, String> appendTo) throws IOException {
        return indexJar(filePath, dependency.getName(), appendTo);
    }

    /**
     * Creates an index based on a jar file
     *
     * @param jarFile     the jar file to index
     * @param projectName the project from which the jarFile originated
     * @param appendTo    the index to potentially append to
     * @return the created index ({@code appendTo} if provided)
     * @throws IOException if the jarFile is invalid (does not point to a valid jar file)
     */
    @NotNull
    public static Map<String, String> indexJar(@NotNull Path jarFile, @NotNull String projectName, @Nullable Map<String, String> appendTo) throws IOException {
        Map<String, String> index = appendTo == null ? new HashMap<>() : appendTo;

        try (ZipFile zipFile = new ZipFile(jarFile.toFile())) {
            zipFile.stream().forEach(e -> {
                String name = e.getName();
                if (!name.endsWith(".class")) return;
                index.put(name.substring(0, name.length() - 6).replace('/', '.'), projectName);
            });
        }

        return index;
    }

    /**
     * Creates an index based on a directory containing class files
     *
     * @param directory   the directory to index
     * @param projectName the project from which the class files originated
     * @param appendTo    the index to potentially append to
     * @return the created index ({@code appendTo} if provided)
     * @throws IOException if an I/O error is thrown when traversing the directory
     */
    @NotNull
    public static Map<String, String> indexDirectory(Path directory, String projectName, @Nullable Map<String, String> appendTo) throws IOException {
        Map<String, String> index = appendTo == null ? new HashMap<>() : appendTo;

        try (Stream<Path> walker = Files.walk(directory)) {
            walker.filter(Files::isRegularFile).map(p -> Utils.pathToPackage(directory.relativize(p)))
                    .filter(s -> s.endsWith(".class")).forEach(s -> index.put(s.substring(0, s.length() - 6), projectName));
        }

        return index;
    }
}
