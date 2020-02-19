package com.github.qaware.adcl.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

/**
 * Generates a local ADCL report in HTML.
 */
public class HTMLReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLReporter.class);

    private HTMLReporter() {

    }

    /**
     * Generates a local ADCL report in HTML on the specified output path.
     *
     * @param diff       the {@link DiffExtractor.Diff} containing the dependency changes.
     * @param outputPath the location there the generated report should be placed at.
     */
    public static void generateReport(DiffExtractor.Diff diff, Path outputPath) {
        InputStream is = HTMLReporter.class.getResourceAsStream("/com/github/qaware/adcl/report/adcl_template.html");

        StringBuilder report = new StringBuilder();

        try (Scanner scanner = new Scanner(Objects.requireNonNull(is))) {
            String nextLine;
            while (scanner.hasNext()) {
                nextLine = scanner.nextLine();
                if (nextLine.contains("<!--INSERT_DEPENDENCY_DATA_HERE-->")) {
                    report.append("var data_dependencies = ");
                    report.append(diff.changedDependencies);
                    report.append(";");

                } else if (nextLine.contains("<!--INSERT_POM_DATA_HERE-->")) {
                    report.append("var data_pom = ");
                    report.append(diff.changedPomDependencies);
                    report.append(";");
                } else if (nextLine.contains("<!--INSERT_PROJECT_AND_VERSION_HERE-->")) {
                    report.append("<h3>Project: ").append(diff.projectName).append("/").append(diff.projectVersion).append("</h3>").append(System.lineSeparator());
                } else {
                    report.append(nextLine).append(System.lineSeparator());
                }
            }
        }
        String fileName = diff.projectName.replace(":", "_") + "_" + diff.projectVersion + ".html";
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath.resolve("adcl_report_" + fileName))) {
            writer.write(report.toString());
        } catch (IOException e) {
            LOGGER.error("Unable to resolve report path: {}", outputPath);
        }
        LOGGER.info("to {}/{}", outputPath, fileName);
    }
}
