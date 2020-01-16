package core;


import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PomDependencyReaderTest {
    private static PomDependencyReader reader;
    @BeforeAll
    static void setup() {
        reader = new PomDependencyReader("src/test/resources/pom/pom.xml");
    }
    @Test
    void test() throws IOException, XmlPullParserException {
        MavenXpp3Reader in = new MavenXpp3Reader();
        Model model = in.read(new FileReader("src/test/resources/pom/pom.xml"));
        List<Dependency> list = model.getDependencies();
        Set<Dependency> list2 = reader.readDependency();
        List<Dependency> newList=new ArrayList<>();
        Dependency a=new Dependency();
        Dependency b=new Dependency();
        Dependency c=new Dependency();
        Dependency d=new Dependency();
        Dependency e=new Dependency();
        Dependency f=new Dependency();
        Dependency g=new Dependency();
        Dependency h=new Dependency();
        Dependency n=new Dependency();
        Dependency j=new Dependency();
        Dependency k=new Dependency();
        Dependency l=new Dependency();
        Dependency m=new Dependency();
        a.setArtifactId("javassist");
        a.setGroupId("org.javassist");
        a.setVersion("3.26.0-GA");
        a.setType("jar");
        newList.add(a);
        b.setArtifactId("neo4j-ogm-bolt-driver");
        b.setGroupId("org.neo4j");
        b.setVersion("3.2.2");
        b.setType("jar");
        newList.add(b);
        c.setArtifactId("j2html");
        c.setGroupId("com.j2html");
        c.setVersion("1.4.0");
        c.setType("jar");
        newList.add(c);
        d.setArtifactId("guava");
        d.setGroupId("com.google.guava");
        d.setVersion("28.1-jre");
        d.setType("jar");
        newList.add(d);
        e.setArtifactId("slf4j-simple");
        e.setGroupId("org.slf4j");
        e.setVersion("1.7.29");
        e.setType("jar");
        newList.add(e);
        f.setArtifactId("annotations");
        f.setGroupId("org.jetbrains");
        f.setVersion("17.0.0");
        f.setType("jar");
        newList.add(f);
        g.setArtifactId("spring-boot-starter-data-neo4j");
        g.setGroupId("org.springframework.boot");
        g.setVersion(null);
        g.setType("jar");
        newList.add(g);
        h.setArtifactId("spring-boot-starter-test");
        h.setGroupId("org.springframework.boot");
        h.setVersion(null);
        h.setType("jar");
        newList.add(h);
        n.setArtifactId("maven-plugin-api");
        n.setGroupId("org.apache.maven");
        n.setVersion("3.0.5");
        n.setType("jar");
        newList.add(n);
        j.setArtifactId("neo4j");
        j.setGroupId("org.neo4j");
        j.setVersion("3.5.13");
        j.setType("jar");
        newList.add(j);
        k.setArtifactId("junit-jupiter");
        k.setGroupId("org.junit.jupiter");
        k.setVersion("5.5.2");
        k.setType("jar");
        newList.add(k);
        l.setArtifactId("assertj-core");
        l.setGroupId("org.assertj");
        l.setVersion("3.14.0");
        l.setType("jar");
        newList.add(l);
        m.setArtifactId("assertj-guava");
        m.setGroupId("org.assertj");
        m.setVersion("3.2.1");
        m.setType("jar");
        newList.add(m);
        for(int i=0; i<list.size(); i++){
            //assertThat(newList.containsAll(list2));
            assertThat(newList.toString()).isEqualTo(list.toString());
        }
        reader.printListDependency();
    }
}
