package core;


import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class IndexBuilder {
    private IndexBuilder() {
    }

    @NotNull
    public static Map<String, String> index(@NotNull Dependency dependency, @Nullable Map<String, String> appendTo) throws IOException {
        Map<String, String> index = appendTo == null ? new HashMap<>() : appendTo;
        String rawPath = dependency.getSystemPath();
        if (rawPath == null)
            throw new IllegalArgumentException("Cannot index dependency " + dependency + " as path is null");
        String project = dependency.getArtifactId();
        try (ZipFile zipFile = new ZipFile(Paths.get(rawPath).toFile())) {
            zipFile.stream().forEach(e -> {
                String name = e.getName();
                if (!name.endsWith(".class")) return;
                index.put(name.substring(0, name.length() - 6).replace('/', '.'), project);
            });
        }
        return index;
    }

    @NotNull
    public static Map<String, String> index(Path directory, String projectName, @Nullable Map<String, String> appendTo) throws IOException {
        Map<String, String> index = appendTo == null ? new HashMap<>() : appendTo;

        try (Stream<Path> walker = Files.walk(directory)) {
            walker.filter(Files::isRegularFile).map(p -> directory.relativize(p).toString().replace(p.getFileSystem().getSeparator(), "."))
                    .filter(s -> s.endsWith(".class")).forEach(s -> index.put(s.substring(0, s.length() - 6), projectName));
        }

        return index;
    }
}
