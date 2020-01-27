package core.information2;

/**
 * A Class Information with a Project Information as parent
 */
public class RootClassInformation extends ClassInformation<ProjectInformation> {
    public RootClassInformation(ProjectInformation parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}
