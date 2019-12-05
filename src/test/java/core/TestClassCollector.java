package core;


import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TestClassCollector {
    private static final ClassCollector ALG = new ClassCollector("src/test/resources/sampleproject");
    private static final Logger LOGGER = LoggerFactory.getLogger(TestClassCollector.class);
    private static final String EXPECTED_CLASS_LIST = "src/test/resources/txtfiles/expectedClassList.txt";

    @Test
    void classesListPrint() {
        try {
            StringBuilder builder = new StringBuilder();
            ALG.generateFileList();
            ALG.getList().stream().map(File::getName).sorted().forEach(path -> builder.append(path).append(String.format("%n")));
            assertThat(builder.toString()).isEqualTo(TestUtil.readFile(EXPECTED_CLASS_LIST, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOGGER.error(ex::getMessage);
        }
    }
}
