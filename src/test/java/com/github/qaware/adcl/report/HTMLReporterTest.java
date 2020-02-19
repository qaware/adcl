package com.github.qaware.adcl.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class HTMLReporterTest {
    private DiffExtractor.Diff diff;

    @BeforeEach
    void setUp() throws IOException {
        diff = new ObjectMapper().readValue(Files.newInputStream(Paths.get("src", "test", "resources", "report", "diff.json")), DiffExtractor.Diff.class);
    }

    @Test
    void generateReportTest() throws IOException {
        Path tmpDir = Files.createTempDirectory("adcl_HtmlReporterTest");
        HTMLReporter.generateReport(diff, tmpDir);

        assertThat(tmpDir.resolve("adcl_report_de-fhbingen_epro_1.1-SNAPSHOT.html")).hasSameContentAs(Paths.get("src", "test", "resources", "report", "adcl_report_expected.html"));
    }
}
