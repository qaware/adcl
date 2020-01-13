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

        assertThat(Config.valuePresent("a")).isTrue();

        assertThat(Config.get("b", 0)).isEqualTo(3);
        assertThat(Config.get("b", (long) 0)).isEqualTo(3);
        assertThat(Config.get("b", (float) 0)).isEqualTo(3);
        assertThat(Config.get("b", (double) 0)).isEqualTo(3);
        assertThat(Config.get("b", (byte) 0)).isEqualTo((byte) 3);
        assertThat(Config.get("b", (short) 0)).isEqualTo((short) 3);

        assertThat(Config.get("c", false)).isTrue();
        assertThat(Config.get("b", '0')).isEqualTo('3');
        assertThat(Config.get("a", null)).isEqualTo("Hello World");
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

        assertThat(Config.get("b", 4)).isEqualTo(4);
        assertThat(Config.get("b", (long) 4)).isEqualTo(4);
        assertThat(Config.get("b", (float) 4)).isEqualTo(4);
        assertThat(Config.get("b", (double) 4)).isEqualTo(4);
        assertThat(Config.get("b", (byte) 4)).isEqualTo((byte) 4);
        assertThat(Config.get("b", (short) 4)).isEqualTo((short) 4);

        assertThat(Config.get("c", true)).isTrue();
        assertThat(Config.get("e", '4')).isEqualTo('4');
        assertThat(Config.getPath("e", null)).isNull();
    }

    @Test
    void testGetNotExistent() {
        configByArgs();

        assertThat(Config.valuePresent("a")).isFalse();

        assertThat(Config.get("b", 4)).isEqualTo(4);
        assertThat(Config.get("b", (long) 4)).isEqualTo(4);
        assertThat(Config.get("b", (float) 4)).isEqualTo(4);
        assertThat(Config.get("b", (double) 4)).isEqualTo(4);
        assertThat(Config.get("b", (byte) 4)).isEqualTo((byte) 4);
        assertThat(Config.get("b", (short) 4)).isEqualTo((short) 4);

        assertThat(Config.get("c", true)).isTrue();
        assertThat(Config.get("b", '4')).isEqualTo('4');
        assertThat(Config.get("a", "X")).isEqualTo("X");
        assertThat(Config.getStringList("d", null)).isNull();
        assertThat(Config.getPath("e", null)).isNull();
    }

    @Test
    void testGetNoValue() {
        configByArgs(Pair.of("a", null));

        assertThat(Config.valuePresent("a")).isTrue();
        assertThat(Config.get("a", false)).isTrue();
    }

    @Test
    void testInputArgs() {
        String input = "a=1 b= c d=\"3\" e=\"a b\"";
        Config.load(input.split(" "));

        assertThat(Config.get("a", 0)).isEqualTo(1);
        assertThat(Config.get("b", false)).isTrue();
        assertThat(Config.get("c", false)).isTrue();
        assertThat(Config.get("d", 0)).isEqualTo(3);
        assertThat(Config.get("e", null)).isEqualTo("a b");
    }

    @Test
    void testInputProperties() {
        System.setProperty("adcl.a", "1");
        System.setProperty("adcl.b", "");
        System.setProperty("adcl.d", "3");
        System.setProperty("adcl.e", "a b");
        Config.load(new String[0]);

        assertThat(Config.get("a", 0)).isEqualTo(1);
        assertThat(Config.get("b", false)).isTrue();
        assertThat(Config.get("d", 0)).isEqualTo(3);
        assertThat(Config.get("e", null)).isEqualTo("a b");

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

        assertThat(Config.get("a", 0)).isEqualTo(1);
        assertThat(Config.get("b", false)).isTrue();
        assertThat(Config.get("c", false)).isTrue();
        assertThat(Config.get("d", 0)).isEqualTo(3);
        assertThat(Config.get("e", null)).isEqualTo("a b");

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

        assertThat(Config.get("a", 0)).isEqualTo(1);
        assertThat(Config.get("b", false)).isTrue();
        assertThat(Config.get("c", false)).isTrue();
        assertThat(Config.get("d", 0)).isEqualTo(3);
        assertThat(Config.get("e", null)).isEqualTo("a b");

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
