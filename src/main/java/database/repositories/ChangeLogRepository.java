package database.repositories;

import core.information.ChangelogInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve {@link ChangelogInformation} nodes that are stored in the Database.
 */
@Repository
public interface ChangeLogRepository extends Neo4jRepository<ChangelogInformation, Long> {

}
