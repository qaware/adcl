package core.information;

/**
 * A Class Information with a Project Information as parent
 */
public class RootClassInformation extends ClassInformation<ProjectInformation> {
    @SuppressWarnings("unused")
    private RootClassInformation() {
        super();
    }

    public RootClassInformation(ProjectInformation parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}