package com.github.qaware.adcl.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A general package node
 *
 * @param <P> the parent type
 */
@NodeEntity
public abstract class PackageInformation<P extends Information<?>> extends Information<P> {
    /**
     * Neo4j init
     */
    PackageInformation() {
        super();
    }

    /**
     * Creates a new package information and registers itself in parent
     *
     * @param parent the parent node ({@link RootInformation} or {@link PackageInformation})
     * @param name   the package name (only own name, no dots allowed)
     * @see Information#createChild(Type, String)
     * @see RootPackageInformation#RootPackageInformation(ProjectInformation, String)
     * @see SubPackageInformation#SubPackageInformation(PackageInformation, String)
     */
    PackageInformation(@NotNull P parent, @NotNull String name) {
        super(parent, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Type getType() {
        return Type.PACKAGE;
    }
}
