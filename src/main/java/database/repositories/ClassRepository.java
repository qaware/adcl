package database.repositories;

import core.information.ClassInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve ClassInformation nodes that are stored in the Database.
 */
@Repository
public interface ClassRepository extends Neo4jRepository<ClassInformation, Long> {

}
