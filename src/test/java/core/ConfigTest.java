package core;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
    private static Properties defaultProps;

    @BeforeAll
    static void beforeAll() {
        defaultProps = System.getProperties();
    }

    @Test
    void testGetExistent() {
        configByArgs(
                Pair.of("a", "Hello World"),
                Pair.of("b", 3),
                Pair.of("c", true),
                Pair.of("d", "a,b,c"),
                Pair.of("e", ".")
        );

        assertTrue(Config.valuePresent("a"));

        assertEquals(Config.get("b", 0), 3);
        assertEquals(Config.get("b", (long) 0), 3);
        assertEquals(Config.get("b", (float) 0), 3);
        assertEquals(Config.get("b", (double) 0), 3);
        assertEquals(Config.get("b", (byte) 0), 3);
        assertEquals(Config.get("b", (short) 0), 3);

        assertTrue(Config.get("c", false));
        assertEquals(Config.get("b", '0'), '3');
        assertEquals(Config.get("a", null), "Hello World");
        assertThat(Config.getStringList("d", null)).containsExactly("a", "b", "c");
        assertThat(Config.getPath("e", null)).isEqualTo(Paths.get("."));
    }

    @Test
    void testGetWrongData() {
        configByArgs(
                Pair.of("b", "x"),
                Pair.of("c", 3),
                Pair.of("e", "???")
        );

        assertEquals(Config.get("b", 4), 4);
        assertEquals(Config.get("b", (long) 4), 4);
        assertEquals(Config.get("b", (float) 4), 4);
        assertEquals(Config.get("b", (double) 4), 4);
        assertEquals(Config.get("b", (byte) 4), 4);
        assertEquals(Config.get("b", (short) 4), 4);

        assertTrue(Config.get("c", true));
        assertEquals(Config.get("e", '4'), '4');
        assertNull(Config.getPath("e", null));
    }

    @Test
    void testGetNotExistent() {
        configByArgs();

        assertFalse(Config.valuePresent("a"));

        assertEquals(Config.get("b", 4), 4);
        assertEquals(Config.get("b", (long) 4), 4);
        assertEquals(Config.get("b", (float) 4), 4);
        assertEquals(Config.get("b", (double) 4), 4);
        assertEquals(Config.get("b", (byte) 4), 4);
        assertEquals(Config.get("b", (short) 4), 4);

        assertTrue(Config.get("c", true));
        assertEquals(Config.get("b", '4'), '4');
        assertEquals(Config.get("a", "X"), "X");
        assertNull(Config.getStringList("d", null));
        assertNull(Config.getPath("e", null));
    }

    @Test
    void testGetNoValue() {
        configByArgs(Pair.of("a", null));

        assertTrue(Config.valuePresent("a"));
        assertTrue(Config.get("a", false));
    }

    @Test
    void testInputArgs() {
        String input = "a=1 b= c d=\"3\" e=\"a b\"";
        Config.load(input.split(" "));

        assertEquals(Config.get("a", 0), 1);
        assertTrue(Config.get("b", false));
        assertTrue(Config.get("c", false));
        assertEquals(Config.get("d", 0), 3);
        assertEquals(Config.get("e", null), "a b");
    }

    @Test
    void testInputProperties() {
        System.setProperty("adcl.a", "1");
        System.setProperty("adcl.b", "");
        System.setProperty("adcl.d", "3");
        System.setProperty("adcl.e", "a b");
        Config.load(new String[0]);

        assertEquals(Config.get("a", 0), 1);
        assertTrue(Config.get("b", false));
        assertEquals(Config.get("d", 0), 3);
        assertEquals(Config.get("e", null), "a b");

        System.setProperties(defaultProps);
    }

    @Test
    void testInputFilePathGiven() throws IOException {
        Path path = Paths.get("myconf.properties");
        Files.write(path, Arrays.asList(
                "a=1",
                "b=",
                "c",
                "d=\"3\"",
                "e=\"a b\""
        ));

        Config.load(new String[]{"configPath=myconf.properties"});

        assertEquals(Config.get("a", 0), 1);
        assertTrue(Config.get("b", false));
        assertTrue(Config.get("c", false));
        assertEquals(Config.get("d", 0), 3);
        assertEquals(Config.get("e", null), "a b");

        Files.delete(path);
    }

    @Test
    void testInputFileDefaultPath() throws IOException {
        Path path = Paths.get("config.properties");
        Files.write(path, Arrays.asList(
                "a=1",
                "b=",
                "c",
                "d=\"3\"",
                "e=\"a b\""
        ));

        Config.load(new String[0]);

        assertEquals(Config.get("a", 0), 1);
        assertTrue(Config.get("b", false));
        assertTrue(Config.get("c", false));
        assertEquals(Config.get("d", 0), 3);
        assertEquals(Config.get("e", null), "a b");

        Files.delete(path);
    }

    @SafeVarargs
    private final void configByArgs(Pair<String, Object>... pairs) {
        String args = Arrays.stream(pairs).map(p -> {
            Object valRaw = p.getValue();
            String val = valRaw == null ? "" : String.valueOf(valRaw);
            if (val.contains(" ")) val = '"' + val + '"';
            return p.getKey() + '=' + val;
        }).collect(Collectors.joining(" "));
        Config.load(args.split(" "));
    }
}
