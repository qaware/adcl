package database.repositories;

import core.information.BehaviorInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve BehaviorInformation nodes that are stored in the Database.
 */
@Repository
public interface BehaviorRepository extends Neo4jRepository<BehaviorInformation, Long> {

    /**
     * @param name name of the Method/Constructor that is searched
     * @return a database stored {@link BehaviorInformation} with the name of name
     */
    BehaviorInformation findByName(String name);
}
