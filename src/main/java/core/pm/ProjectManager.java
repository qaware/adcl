package core.pm;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Defines an interface to a project management tool like Maven or Gradle
 */
public interface ProjectManager {
    /**
     * @return the unique project name. A dependency reference to that project should be able to reconstruct it.
     */
    @NotNull
    String getProjectName();

    /**
     * @return the current version of the project
     */
    @NotNull
    String getProjectVersion();

    /**
     * @return all direct (non-transitive) dependencies defined by project configuration
     */
    @NotNull
    Set<@NotNull Dependency> getDependencies();

    /**
     * @return all (including transitive) compile-time dependencies of the project. These are dependencies which classes can be referred to in the project's code.
     * Additionally this method has to provide a path to an existing jar of that dependency as value (downloading from remote repository if needed)
     */
    @NotNull
    Map<@NotNull Dependency, @NotNull Path> getCompileDependencies();

    /**
     * @return the folder location of the output classes
     */
    @NotNull
    Path getClassesOutput();
}
