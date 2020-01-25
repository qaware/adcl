package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Makes a list with all dependencies from the pom.xml
 */
public class PomDependencyReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomDependencyReader.class);
    private String path;
    /**
     * Init PomDependencyReader
     * @param path Path describes where the pom.xml file is
     */
    public PomDependencyReader(String path) {
        this.path = path;
    }

    /**
     * Reads all dependencies from pom.xml file
     *
     * @return returns a set with dependencies
     */
    public Set<Dependency> readDependency() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(path));
            return new HashSet<>(model.getDependencies());
        }
        catch(XmlPullParserException | IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
}
