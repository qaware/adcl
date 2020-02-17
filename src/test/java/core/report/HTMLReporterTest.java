package core.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class HTMLReporterTest {
    private DiffExtractor.Diff diff;
    private File expected;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        diff = new ObjectMapper().readValue(Objects.requireNonNull(classloader.getResourceAsStream("./report/diff.json")), DiffExtractor.Diff.class);
        expected = new File(Objects.requireNonNull(classloader.getResource("./report/adcl_report_de-fhbingen_epro_1.1-SNAPSHOT.html")).toURI());

    }

    @Test
    void generateReportTest() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        HTMLReporter.generateReport(diff, temporaryFolder.getRoot().toPath());

        File resultFile = new File(temporaryFolder.getRoot().toPath().resolve("adcl_report_de-fhbingen_epro_1.1-SNAPSHOT.html").toString());
        assertThat(FileUtils.readLines(resultFile, "utf-8"))
                .containsExactlyInAnyOrder(FileUtils.readLines(expected, "utf-8").toArray(new String[0]));

    }
}
