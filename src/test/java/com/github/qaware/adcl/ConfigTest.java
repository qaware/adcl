package com.github.qaware.adcl;

import com.github.qaware.adcl.util.Utils;
import com.github.qaware.adcl.util.log.CapturedOutputLogInspector;
import com.github.qaware.adcl.util.log.LogInspector;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class ConfigTest {

    @Test
    void testGetExistent() {
        configByArgs(
                Pair.of("a", "Hello World"),
                Pair.of("b", 3),
                Pair.of("c", true),
                Pair.of("d", "a,b,c"),
                Pair.of("e", "."),
                Pair.of("f", "FaLsE")
        );

        assertThat(Config.valuePresent("a")).isTrue();

        assertThat(Config.get("b", 0)).isEqualTo(3);
        assertThat(Config.get("b", (long) 0)).isEqualTo(3);
        assertThat(Config.get("b", (float) 0)).isEqualTo(3);
        assertThat(Config.get("b", (double) 0)).isEqualTo(3);
        assertThat(Config.get("b", (byte) 0)).isEqualTo((byte) 3);
        assertThat(Config.get("b", (short) 0)).isEqualTo((short) 3);

        assertThat(Config.get("c", false)).isTrue();
        assertThat(Config.get("f", true)).isFalse();
        assertThat(Config.get("b", '0')).isEqualTo('3');
        assertThat(Config.get("a", null)).isEqualTo("Hello World");
        assertThat(Config.getStringList("d", null)).containsExactly("a", "b", "c");
        assertThat(Utils.isSamePath(Config.getPath("e", null), Paths.get("."))).isTrue();
    }

    @Test
    void testGetWrongData() {
        configByArgs(
                Pair.of("b", "x"),
                Pair.of("c", 3),
                Pair.of("d", "xxx"),
                Pair.of("e", "\0")
        );

        assertThat(Config.get("b", 4)).isEqualTo(4);
        assertThat(Config.get("b", (long) 4)).isEqualTo(4);
        assertThat(Config.get("b", (float) 4)).isEqualTo(4);
        assertThat(Config.get("b", (double) 4)).isEqualTo(4);
        assertThat(Config.get("b", (byte) 4)).isEqualTo((byte) 4);
        assertThat(Config.get("b", (short) 4)).isEqualTo((short) 4);

        assertThat(Config.get("c", true)).isTrue();
        assertThat(Config.get("d", '4')).isEqualTo('4');
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
        String input = "a=1 b= c d=\"3\" e=\"a b\" f==\" g=\"";
        Config.load(input.split(" "));

        assertThat(Config.get("a", 0)).isEqualTo(1);
        assertThat(Config.get("b", false)).isTrue();
        assertThat(Config.get("c", false)).isTrue();
        assertThat(Config.get("d", 0)).isEqualTo(3);
        assertThat(Config.get("e", null)).isEqualTo("a b");
        assertThat(Config.get("f", null)).isEqualTo("=\"");
        assertThat(Config.get("g", null)).isEqualTo("\"");
    }

    @Test
    void testInputProperties() {
        Properties propertiesBackup = new Properties(System.getProperties());

        try {
            System.setProperty("adcl.a", "1");
            System.setProperty("adcl.b", "");
            System.setProperty("adcl.d", "3");
            System.setProperty("adcl.e", "a b");
            System.getProperties().put("adcl.x", new Object());
            System.getProperties().put(new Object(), "x");
            Config.load();

            assertThat(Config.get("a", 0)).isEqualTo(1);
            assertThat(Config.get("b", false)).isTrue();
            assertThat(Config.get("d", 0)).isEqualTo(3);
            assertThat(Config.get("e", null)).isEqualTo("a b");
        } finally {
            System.setProperties(propertiesBackup);
        }
    }

    @Test
    void testInputFilePathGiven() throws IOException {
        Path path = Paths.get("myconf.properties");
        try {
            Files.write(path, Arrays.asList(
                    "a=1",
                    "b=",
                    "c",
                    "d=\"3\"",
                    "e=\"a b\"",
                    "f==\"",
                    "g=\""
            ));

            Config.load("configPath=myconf.properties");

            assertThat(Config.get("a", 0)).isEqualTo(1);
            assertThat(Config.get("b", false)).isTrue();
            assertThat(Config.get("c", false)).isTrue();
            assertThat(Config.get("d", 0)).isEqualTo(3);
            assertThat(Config.get("e", null)).isEqualTo("a b");
            assertThat(Config.get("f", null)).isEqualTo("=\"");
            assertThat(Config.get("g", null)).isEqualTo("\"");
        } finally {
            Utils.delete(path);
        }
    }

    @Test
    void testInputFileDefaultPath() throws IOException {
        Path path = Paths.get("config.properties");
        try {
            Files.write(path, Arrays.asList(
                    "a=1",
                    "b=",
                    "c",
                    "d=\"3\"",
                    "e=\"a b\"",
                    "f==\"",
                    "g=\""
            ));

            Config.load();

            assertThat(Config.get("a", 0)).isEqualTo(1);
            assertThat(Config.get("b", false)).isTrue();
            assertThat(Config.get("c", false)).isTrue();
            assertThat(Config.get("d", 0)).isEqualTo(3);
            assertThat(Config.get("e", null)).isEqualTo("a b");
            assertThat(Config.get("f", null)).isEqualTo("=\"");
            assertThat(Config.get("g", null)).isEqualTo("\"");
        } finally {
            Utils.delete(path);
        }
    }

    @Test
    void testInputPathFailing(@NotNull CapturedOutput output) throws IOException {
        LogInspector log = new CapturedOutputLogInspector(output);
        Path path = Paths.get("config.properties");

        try {
            Utils.delete(path);
            Config.load("configPath=config.properties");
            assertThat(log.getNewErr()).contains("configPath points to a non-existent file");
            Config.load("configPath=\0");
            assertThat(log.getNewErr()).contains("configPath is present but invalid");
            Files.createDirectory(path);
            Config.load(); // load default config while config is folder
            assertThat(log.getNewOut()).contains("Configuration loaded");
            Config.load("configPath=config.properties");
            assertThat(log.getNewErr()).contains("configPath points to a directory");
        } finally {
            Utils.delete(path);
        }
    }

    @Test
    void testOther() {
        Config.load();
        assertThat(Config.get(null, 5)).isEqualTo(5);
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
