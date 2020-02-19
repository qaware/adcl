package com.github.qaware.adcl;

import com.github.qaware.adcl.util.MojoTestUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationMojoConfigurationTest {
    @Test
    public void integrationTest1() throws Exception {
        try (MojoTestUtil testUtil = new MojoTestUtil(Paths.get("pom.xml"))) {
            Pair<Integer, String> result = testUtil.runAdclOnPom(Paths.get("src", "test", "resources", "pom3", "pom.xml"));
            assertThat(result.getKey()).isNotZero();

            String configString = regexSubstring(result.getValue(), Pattern.compile("Configuration loaded: \\{(.+?)}"));
            assertThat(configString).isNotNull();

            Map<String, String> configs = parseProperties(configString);
            assertThat(configs.entrySet()).containsExactlyInAnyOrder(
                    Pair.of("spring.data.neo4j.uri", "bolt://localhost:7687"),
                    Pair.of("spring.data.neo4j.username", ""),
                    Pair.of("spring.data.neo4j.password", ""),
                    Pair.of("project.commit.current", "test"),
                    Pair.of("project.uri", "src/test/resources/test classfiles3/epro1")
            );
        }
    }

    @Test
    public void integrationTest2() throws Exception {
        try (MojoTestUtil testUtil = new MojoTestUtil(Paths.get("pom.xml"))) {
            Pair<Integer, String> result = testUtil.runAdclOnPom(Paths.get("src", "test", "resources", "pom4", "pom.xml"));
            assertThat(result.getKey()).isNotZero();

            String configString = regexSubstring(result.getValue(), Pattern.compile("Configuration loaded: \\{(.+?)}"));
            assertThat(configString).isNotNull();

            Map<String, String> configs = parseProperties(configString);
            assertThat(configs.entrySet()).containsExactlyInAnyOrder(
                    Pair.of("spring.data.neo4j.uri", "bolt://localhost:7687"),
                    Pair.of("spring.data.neo4j.username", "neo4j\""),
                    Pair.of("spring.data.neo4j.password", "test"),
                    Pair.of("project.commit.current", "test2"),
                    Pair.of("project.commit.previous", "test"),
                    Pair.of("project.uri", "src\\test\\resources\\testclassfiles3\\epro2")
            );
        }
    }

    @Nullable
    private String regexSubstring(String input, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) return null;
        if (matcher.groupCount() < 1) return null;
        return matcher.group(1);
    }

    @NotNull
    private Map<String, String> parseProperties(@NotNull String raw) {
        return Stream.of(raw.split(",")).map(String::trim).collect(Collectors.toMap(s -> s.substring(0, s.indexOf('=')), s -> s.substring(s.indexOf('=') + 1)));
    }
}