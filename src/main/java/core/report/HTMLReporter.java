package core.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class HTMLReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLReporter.class);

    private HTMLReporter() {

    }

    public static void generate(String diffJson, Path outputPath) {
        LOGGER.info("to {}: {}", outputPath, diffJson);
    }
}
