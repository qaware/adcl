package core.database;

public interface Purgeable {
    /**
     * For use by {@link Neo4jService only}
     * deletes the id of self and outgoing relations so the objects get stored in db as new objects
     */
    void purgeIds();
}
