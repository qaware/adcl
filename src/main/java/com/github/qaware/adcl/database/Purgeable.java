package com.github.qaware.adcl.database;

/**
 * This interface is for OGM entities to be able to reset their {@link org.neo4j.ogm.annotation.Id} field
 * This is a temporary solution and has connection to B1.0.14
 */
public interface Purgeable {
    /**
     * For use by {@link Neo4jService} only
     * deletes the id of self and outgoing relations so the objects get stored in db as new objects
     */
    void purgeIds();
}
