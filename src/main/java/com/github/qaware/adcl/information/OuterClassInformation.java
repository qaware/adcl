package com.github.qaware.adcl.information;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Class node with a Package node as parent
 */
@NodeEntity
public class OuterClassInformation extends ClassInformation<PackageInformation<?>> {
    /**
     *Needed for neo4j initialization
     */
    @SuppressWarnings("unused")
    private OuterClassInformation() {
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
    public OuterClassInformation(PackageInformation<?> parent, String name, boolean isService) {
        super(parent, name, isService);
    }
}
