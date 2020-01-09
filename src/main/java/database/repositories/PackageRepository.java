package database.repositories;

import core.information.PackageInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve PackageInformation nodes that are stored in the Database.
 */
@Repository
public interface PackageRepository extends Neo4jRepository<PackageInformation, Long> {

}
