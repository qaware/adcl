package database.services;

import core.information.PackageInformation;
import database.repositories.MethodRepository;
import database.repositories.ClassRepository;
import database.repositories.PackageRepository;
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
    private MethodRepository methodRepository;

    /**
     * Instantiates a new GraphDBService.
     *
     * @param packageRepository  {@link PackageRepository}
     * @param classRepository    {@link ClassRepository}
     * @param methodRepository {@link MethodRepository}
     */
    public GraphDBService(PackageRepository packageRepository, ClassRepository classRepository,
                          MethodRepository methodRepository) {
        this.packageRepository = packageRepository;
        this.classRepository = classRepository;
        this.methodRepository = methodRepository;
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
                getMethodRepository().saveAll(classInformation.getMethodInformations());
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

    public MethodRepository getMethodRepository() {
        return methodRepository;
    }


}
