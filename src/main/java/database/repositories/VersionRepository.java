package database.repositories;

import core.information.VersionInformation;
import org.springframework.data.neo4j.annotation.Depth;
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
    @Depth(value = -1)
    VersionInformation findVersionInformationByVersionName(String versionName);
}
