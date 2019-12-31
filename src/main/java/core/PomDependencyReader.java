package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class PomDependencyReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomDependencyReader.class);
    MavenXpp3Reader reader;
    Model model;
    InputStream in;
    public PomDependencyReader(String path) throws FileNotFoundException {
        reader = new MavenXpp3Reader();
        model = null;
        in = new FileInputStream(path);
    }
    public List<Dependency> readDependency() {
        try {
            model = reader.read(in);
            return model.getDependencies();
        }
        catch(Exception ex){
            LOGGER.info(ex.getMessage());
        }
        return new ArrayList<>();
    }
    public void printListDependency(){
        try {
            readDependency().forEach(dependency -> LOGGER.info(dependency.toString()));
        }
        catch(Exception ex){
            LOGGER.info(ex.getMessage());
        }
    }
}
