package core.information;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.context.annotation.Lazy;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * VersionInformation is a class for Versions/Commits, which have been analysed.
 */
@NodeEntity
public class VersionInformation {
    @Id
    @GeneratedValue
    private Long id;
    private String versionName;

    @Relationship(type = "IS_VERSION_OF")
    private Set<PackageInformation> packageInformations;

    @Lazy
    @Relationship(type = "FOLLOWED")
    private VersionInformation previousVersion = null;

    /**
     * Instantiates a new Version information without a previousVersion. Used primarily for the first Commit/Version.
     *
     * @param packageInformations the package informations
     * @param versionName         the version/commit name
     */
    public VersionInformation(Collection<PackageInformation> packageInformations, String versionName) {
        this.packageInformations = new TreeSet<>(packageInformations);
        this.versionName = versionName;
    }

    /**
     * Instantiates a new Version information.
     *
     * @param packageInformations the package informations
     * @param versionName         the version/commit name
     * @param previousVersion     the previous version
     */
    public VersionInformation(Collection<PackageInformation> packageInformations, String versionName, VersionInformation previousVersion) {
        this.packageInformations = new TreeSet<>(packageInformations);
        this.versionName = versionName;
        this.previousVersion = previousVersion;
    }

    /**
     * Is needed for Spring Data, should not be used otherwise.
     */
    private VersionInformation() {
    }

    /**
     * Gets version name.
     *
     * @return the version name
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * Gets the analysed packageInformation Set.
     *
     * @return the package informations
     */
    public Set<PackageInformation> getPackageInformations() {
        return packageInformations;
    }

    /**
     * Gets previous version.
     *
     * @return the previous version
     */
    public VersionInformation getPreviousVersion() {
        return previousVersion;
    }

    /**
     * Sets previous version.
     *
     * @param previousVersion the previous version
     */
    public void setPreviousVersion(VersionInformation previousVersion) {
        this.previousVersion = previousVersion;
    }
}
