package core.information2;

public class RootClassInformation extends ClassInformation<ProjectInformation> {
    //TODO maybe warn for usage of such classes as they are not allowed in JDK > 8
    public RootClassInformation(ProjectInformation parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}
