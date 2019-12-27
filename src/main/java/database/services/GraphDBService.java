package database.services;

import core.information.ChangelogInformation;
import core.information.PackageInformation;
import core.information.VersionInformation;
import database.repositories.*;
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
    private VersionRepository versionRepository;
    private ChangeLogRepository changeLogRepository;

    /**
     * Instantiates a new GraphDBService.
     *
     * @param packageRepository   {@link PackageRepository}
     * @param classRepository     {@link ClassRepository}
     * @param behaviorRepository  {@link BehaviorRepository}
     * @param versionRepository   the version repository
     * @param changeLogRepository the change log repository
     */
    public GraphDBService(PackageRepository packageRepository, ClassRepository classRepository,
                          BehaviorRepository behaviorRepository, VersionRepository versionRepository,
                          ChangeLogRepository changeLogRepository) {
        this.packageRepository = packageRepository;
        this.classRepository = classRepository;
        this.behaviorRepository = behaviorRepository;
        this.versionRepository = versionRepository;
        this.changeLogRepository = changeLogRepository;
    }

    /**
     * Saves all Nodes in the database.
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

    /**
     * Saves a changelog in the database.
     *
     * @param changelog the change log
     */
    @Transactional
    public void saveChangelog(ChangelogInformation changelog) {
        changeLogRepository.save(changelog);
    }

    /**
     * Saves a version in the database.
     *
     * @param versionInformation the version information
     */
    @Transactional
    public void saveVersion(VersionInformation versionInformation) {
        versionRepository.save(versionInformation);
    }

    /**
     * Gets package repository.
     *
     * @return the package repository
     */
    public PackageRepository getPackageRepository() {
        return packageRepository;
    }

    /**
     * Gets class repository.
     *
     * @return the class repository
     */
    public ClassRepository getClassRepository() {
        return classRepository;
    }

    /**
     * Gets behavior repository.
     *
     * @return the behavior repository
     */
    public BehaviorRepository getBehaviorRepository() {
        return behaviorRepository;
    }

    /**
     * Gets version repository.
     *
     * @return the version repository
     */
    public VersionRepository getVersionRepository() {
        return versionRepository;
    }

    /**
     * Gets change log repository.
     *
     * @return the change log repository
     */
    public ChangeLogRepository getChangeLogRepository() {
        return changeLogRepository;
    }
}
