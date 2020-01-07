package core;


import core.information.ClassInformation;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

public class PomDependencyReaderTest {
    private static PomDependencyReader reader;
    private static String path="src/test/resources/Pom/pom.xml";
    @BeforeAll
    static void setup() throws FileNotFoundException {
        reader = new PomDependencyReader(path);
    }
    @Test
    void test() throws IOException, XmlPullParserException {
        MavenXpp3Reader in = new MavenXpp3Reader();
        Model model = in.read(new FileReader(path));
        List<Dependency> list = model.getDependencies();
        List<Dependency> list2 = reader.readDependency();
        for(int i=0; i<list.size(); i++){
            assertThat(list.get(i).toString().equals(list2.get(i).toString()));
        }
        reader.printListDependency();
    }

    @Test
    void testDataIntegration() throws FileNotFoundException {
        PomDependencyReader pdr=new PomDependencyReader(path);
        pdr.integrateInDataStructure();
        List<Dependency> testList=pdr.readDependency();
        ArrayList<ClassInformation> ts=new ArrayList<>();
        DependencyPool pool = new DependencyPool();
        for (Dependency dependency : pdr.readDependency()) {
           ts.add(pool.getOrCreateClassInformation(dependency.getGroupId() + dependency.getArtifactId()));
        }
        for(int i=0;i<testList.size();i++){
            assertThat(ts.get(i).getClassName().contains(testList.get(i).getGroupId()));
            assertThat(ts.get(i).getClassName().contains(testList.get(i).getArtifactId()));
        }
    }
    }
