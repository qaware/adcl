package core;


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
import java.util.List;

public class PomDependencyReaderTest {
    private static PomDependencyReader reader;
    @BeforeAll
    static void setup() throws FileNotFoundException {
        reader = new PomDependencyReader("src/test/resources/Pom/pom.xml");
    }
    @Test
    void test() throws IOException, XmlPullParserException {
        MavenXpp3Reader in = new MavenXpp3Reader();
        Model model = in.read(new FileReader("src/test/resources/Pom/pom.xml"));
        List<Dependency> list = model.getDependencies();
        List<Dependency> list2 = reader.readDependency();
        for(int i=0; i<list.size(); i++){
            assertThat(list.get(i).toString().equals(list2.get(i).toString()));
        }
        reader.printListDependency();
    }
}
