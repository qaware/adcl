package database.repositories;

import core.information.MethodInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve MethodInformation nodes that are stored in the Database.
 */
@Repository
public interface MethodRepository extends Neo4jRepository<MethodInformation, Long> {

    /**
     * @param name name of the Method/Constructor that is searched
     * @return a database stored {@link MethodInformation} with the name of name
     */
    MethodInformation findByName(String name);
}
