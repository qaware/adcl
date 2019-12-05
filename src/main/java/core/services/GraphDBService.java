package core.services;

import core.information.PackageInformation;
import core.repositories.BehaviorRepository;
import core.repositories.ClassRepository;
import core.repositories.PackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Service which imports the Data from the Neo4j-Database and maps it to POJOs.
 */
@Service
public class GraphDBService {
    private PackageRepository packageRepository;
    private ClassRepository classRepository;
    private BehaviorRepository behaviorRepository;

    /**
     * Instantiates a new GraphDBService.
     *
     * @param packageRepository  {@link PackageRepository}
     * @param classRepository    {@link ClassRepository}
     * @param behaviorRepository {@link BehaviorRepository}
     */
    public GraphDBService(PackageRepository packageRepository, ClassRepository classRepository,
                          BehaviorRepository behaviorRepository) {
        this.packageRepository = packageRepository;
        this.classRepository = classRepository;
        this.behaviorRepository = behaviorRepository;
    }

    /**
     * Saves all Nodes in the Database.
     *
     * @param packages Output of DependencyExtractor
     */
    @Transactional
    public void saveAllNodes(Collection<PackageInformation> packages) {
        packages.forEach(packageInformation -> {
            packageInformation.getClassInformations().forEach(classInformation -> {
                getBehaviorRepository().saveAll(classInformation.getBehaviorInformations());
                getClassRepository().save(classInformation);
            });
            getPackageRepository().save(packageInformation);
        });
    }

    public PackageRepository getPackageRepository() {
        return packageRepository;
    }

    public ClassRepository getClassRepository() {
        return classRepository;
    }

    public BehaviorRepository getBehaviorRepository() {
        return behaviorRepository;
    }


}
