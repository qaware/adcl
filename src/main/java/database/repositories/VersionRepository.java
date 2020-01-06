package database.repositories;

import core.information.VersionInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve {@link VersionInformation} nodes that are stored in the Database.
 */
@Repository
public interface VersionRepository extends Neo4jRepository<VersionInformation, Long> {
    VersionInformation findVersionInformationByVersionName(String versionName);
}
