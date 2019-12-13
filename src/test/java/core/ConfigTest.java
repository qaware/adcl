package core;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
    private static final Path confPath = Paths.get("myconf.properties");

    @BeforeAll
    static void initConfig() throws IOException {
        Files.write(confPath, Collections.singletonList("a=Hello World\nb=3\ne=."));
        Config.load(new String[]{"configPath=" + confPath, "c", "d=a,b,c"});
    }

    @AfterAll
    static void cleanup() throws IOException {
        Files.delete(confPath);
    }

    @Test
    void testImplicitTrue() {
        assertTrue(Config.get("c", false));
    }

    @Test
    void testNumbers() {
        assertEquals(Config.get("b", 0), 3);
        assertEquals(Config.get("b", (long) 0), 3);
        assertEquals(Config.get("b", (float) 0), 3);
        assertEquals(Config.get("b", (double) 0), 3);
        assertEquals(Config.get("b", (byte) 0), 3);
        assertEquals(Config.get("b", (short) 0), 3);
    }

    @Test
    void testOther() {
        assertEquals(Config.get("b", '0'), '3');
        assertEquals(Config.get("a", null), "Hello World");
        assertThat(Config.getStringList("d", null)).containsExactly("a", "b", "c");
        assertThat(Config.getPath("e", null)).isEqualTo(Paths.get("."));
    }

    @Test
    void testDefault() {
        assertThat(Config.get("x", "A")).isEqualTo("A");
        assertNull(Config.get(null, null));
        assertEquals(Config.get("a", 0), 0);
    }
}
