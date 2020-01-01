package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
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
     * @throws FileNotFoundException
     */
    public PomDependencyReader(String path) throws FileNotFoundException {
        this.path = path;
    }

    /**
     * Reads all dependencies from pom.xml file
     * @return returns an arraylist with dependencies
     */
    public List<Dependency> readDependency() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader(path));
            return model.getDependencies();
        }
        catch(Exception ex){
            LOGGER.info(ex.getMessage());
        }
        return new ArrayList<>();
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
