package core;

import core.information.Information;
import core.information.ProjectInformation;
import core.information.VersionInformation;
import core.pm.ProjectManager;
import org.jetbrains.annotations.NotNull;

public class PomDependencyExtractor {
    private PomDependencyExtractor() {

    }

    /**
     * Integrates the dependencies listed by the projectManager into the Project at given version
     *
     * @param projectManager the ProjectManager whose dependencies to read from
     * @param currentVersion the project and version to add the dependencies to
     */
    public static void updatePomDependencies(@NotNull ProjectManager projectManager, @NotNull VersionInformation currentVersion) {
        ProjectInformation project = currentVersion.getProject();
        project.getPomDependenciesRaw().forEach(d -> d.setVersionAt(currentVersion, null));
        projectManager.getDependencies().forEach(d -> {
            ProjectInformation remote = (ProjectInformation) project.getRoot().findOrCreate(d.getName(), null, Information.Type.PROJECT);
            VersionInformation remoteVersion = remote.getOrCreateVersion(d.getVersion());
            project.addPomDependency(remoteVersion, currentVersion);
        });
    }
}
