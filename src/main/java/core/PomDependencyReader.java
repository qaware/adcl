package core;

import core.information.Information;
import core.information.ProjectInformation;
import core.information.VersionInformation;
import core.pm.ProjectManager;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Reads all dependencies from the pom.xml
 */
public class PomDependencyReader { //TODO delete class

    public void updatePomDependencies(ProjectManager projectManager, @NotNull VersionInformation currentVersion) throws IOException, XmlPullParserException {
        ProjectInformation project = currentVersion.getProject();
        project.getPomDependenciesRaw().forEach(d -> d.setVersionAt(currentVersion, null));
        projectManager.getDependencies().forEach(d -> {
            ProjectInformation remote = (ProjectInformation) project.getRoot().findOrCreate(d.getName(), null, Information.Type.PROJECT);
            VersionInformation remoteVersion = remote.getOrCreateVersion(d.getVersion());
            project.addPomDependency(remoteVersion, currentVersion);
        });
    }
}
