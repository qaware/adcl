package com.github.qaware.adcl;

import com.github.qaware.adcl.util.MojoTestUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class ApplicationMojoTest {
    @ExtendWith(OutputCaptureExtension.class)
    @Test
    public void executionTest(@NotNull CapturedOutput output) {
        ApplicationMojo mojo = new ApplicationMojo();
        assertThatCode(mojo::execute).isInstanceOf(Exception.class);
        assertThat(output.getErr()).contains("spring.data.neo4j.password not specified");
    }

    @Test
    public void integrationTest() throws Exception {
        try (MojoTestUtil testUtil = new MojoTestUtil(Paths.get("pom.xml"))) {
            Pair<Integer, String> result = testUtil.runAdclOnPom(Paths.get("src", "test", "resources", "pom2", "pom.xml"));
            assertThat(result.getKey()).isNotZero();
            assertThat(result.getValue()).contains("spring.data.neo4j.password not specified");
        }
    }
}
