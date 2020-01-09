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
 * ChangelogInformation is a Class for managing the position of a changelog between two commits/versions.
 */
@NodeEntity
public class ChangelogInformation {
    @Id
    @GeneratedValue
    private Long id;

    @Relationship(type = "CHANGELOG")
    private Set<PackageInformation> changelog;

    @Lazy
    @Relationship(type = "BEFORE")
    private VersionInformation before;

    @Lazy
    @Relationship(type = "AFTER")
    private VersionInformation after;


    /**
     * Instantiates a new Changelog information.
     *
     * @param changelog the changelog
     * @param before    the before
     * @param after     the after
     */
    public ChangelogInformation(Collection<PackageInformation> changelog,
                                VersionInformation before, VersionInformation after) {
        this.changelog = new TreeSet<>(changelog);
        this.before = before;
        this.after = after;
    }

    /**
     * Gets changelog.
     *
     * @return the changelog
     */
    public Set<PackageInformation> getChangelog() {
        return changelog;
    }

    /**
     * Gets the Version before commit.
     *
     * @return the before
     */
    public VersionInformation getBefore() {
        return before;
    }

    /**
     * Gets the Version after commit.
     *
     * @return the after
     */
    public VersionInformation getAfter() {
        return after;
    }
}
