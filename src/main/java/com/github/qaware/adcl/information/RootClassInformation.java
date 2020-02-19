package com.github.qaware.adcl.information;

/**
 * A Class node with a Project node as parent
 */
public class RootClassInformation extends ClassInformation<ProjectInformation> {
    /**
     *Needed for neo4j initialization
     */
    @SuppressWarnings("unused")
    private RootClassInformation() {
        super();
    }

    /**
     * Creates a new class information and registers itself in parent
     *
     * @param parent    the parent node
     * @param name      the class name (simple name only)
     * @param isService whether the class has the {@link org.springframework.stereotype.Service} annotation
     * @see Information#createChild(Type, String)
     */
    public RootClassInformation(ProjectInformation parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}
