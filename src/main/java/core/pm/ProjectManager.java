package core.pm;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface ProjectManager {
    @NotNull
    String getProjectName();

    @NotNull
    String getProjectVersion();

    @NotNull
    Set<@NotNull Dependency> getDependencies();

    @NotNull
    Map<@NotNull Dependency, @NotNull Path> getCompileDependencies();

    @NotNull
    Path getClassesOutput();
}
