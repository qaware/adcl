package database.services;

import core.information.ChangelogInformation;
import core.information.VersionInformation;
import database.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service which imports the Data from the Neo4j-Database and maps it to POJOs.
 */
@Service
public class GraphDBService {
    private PackageRepository packageRepository;
    private ClassRepository classRepository;
    private MethodRepository methodRepository;
    private VersionRepository versionRepository;
    private ChangeLogRepository changeLogRepository;

    /**
     * Instantiates a new GraphDBService.
     *
     * @param packageRepository   {@link PackageRepository}
     * @param classRepository     {@link ClassRepository}
     * @param methodRepository  {@link MethodRepository}
     * @param versionRepository   the version repository
     * @param changeLogRepository the change log repository
     */
    public GraphDBService(PackageRepository packageRepository, ClassRepository classRepository,
                          MethodRepository methodRepository, VersionRepository versionRepository,
                          ChangeLogRepository changeLogRepository) {
        this.packageRepository = packageRepository;
        this.classRepository = classRepository;
        this.methodRepository = methodRepository;
        this.versionRepository = versionRepository;
        this.changeLogRepository = changeLogRepository;
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

    @Transactional
    public VersionInformation getVersion(String versionName) {
        return getVersionRepository().findVersionInformationByVersionName(versionName);
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
     * Gets method repository.
     *
     * @return the method repository
     */
    public MethodRepository getMethodRepository() {
        return methodRepository;
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
