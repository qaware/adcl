package core;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class PomDependencyReader {
    MavenXpp3Reader reader;
    Model model;
    InputStream in;
    public PomDependencyReader() throws FileNotFoundException {
        super();
        reader = new MavenXpp3Reader();
        model = null;
        in = new FileInputStream("pom.xml");
    }
    public List<Dependency> readDependency() throws IOException {
        InputStream in = new FileInputStream("pom.xml");
        List<Dependency> list = model.getDependencies();
        return list;
    }
    public void close() throws IOException {
        in.close();
    }
    public void printListDependency(){

    }
}
