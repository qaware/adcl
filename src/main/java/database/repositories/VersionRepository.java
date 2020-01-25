package database.repositories;

import core.information.VersionInformation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve {@link VersionInformation} nodes that are stored in the Database.
 */
@Repository
public interface VersionRepository extends Neo4jRepository<VersionInformation, Long> {

    /**
     * @param versionName name of the package that is searched
     * @return a database stored {@link VersionInformation} with the name of versionName
     */
    VersionInformation findVersionInformationByVersionName(String versionName);

    @Query("match (n:VersionInformation) where not (n)<-[:FOLLOWED]-() return n.versionName")
    String findLatestVersion();
}
