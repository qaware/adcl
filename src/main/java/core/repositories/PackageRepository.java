package core.repositories;

import core.information.PackageInformation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to retrieve PackageInformation nodes that are stored in the Database.
 */
@Repository
public interface PackageRepository extends Neo4jRepository<PackageInformation, Long> {

    /**
     * @param packageName name of the package that is searched
     * @return a database stored {@link PackageInformation} with the name of packageName
     */
    PackageInformation findByPackageName(String packageName);

}
