package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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
     * @return returns an arraylist with dependencies
     */
    public Set<Dependency> readDependency() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(path));
            Set<Dependency> dependencies = new HashSet<Dependency>();
            dependencies.addAll(model.getDependencies());
            return dependencies;
        }
        catch(XmlPullParserException | IOException ex) {
            LOGGER.info(ex.getMessage());
        }
        return null;
    }

    /**
     * Prints an arraylist out
     */
    public void printListDependency(){
        try {
            readDependency().forEach(dependency -> LOGGER.info(dependency.toString()));
        }
        catch(Exception ex){
            LOGGER.info(ex.getMessage());
        }
    }
}
