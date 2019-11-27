package core;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class TestClassCollector {
    private static final ClassCollector ALG = new ClassCollector("src/test/resources/algTest/out/test/PROJ_ADCL/test");
    private static final Logger LOGGER = LoggerFactory.getLogger(TestClassCollector.class);
    private static final String EXPECTED_CLASS_LIST = "src/test/resources/txtfiles/expectedClassList.txt";

    @Test
    void classesListPrint() {
        try {
            StringBuilder builder = new StringBuilder();
            ALG.generateFileList();
            ALG.getList().stream().map(File::getName).forEach(path -> builder.append(path).append(String.format("%n")));
            assertThat(builder.toString()).isEqualTo(DependencyExtractorTest.readFile(EXPECTED_CLASS_LIST, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOGGER.error(ex::getMessage);
        }
    }
}
