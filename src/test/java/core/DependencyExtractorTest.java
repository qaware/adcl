package core;

import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DependencyExtractorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyExtractorTest.class);
    private static final String SRC_TEST_RESOURCES_TESTCLASSFILES = "src/test/resources/testclassfiles/";
    private static final String SRC_TEST_RESOURCES_TXTFILES_EXPECTED = "src/test/resources/txtfiles/expectedTextResultFromTestclass.txt";
    private DependencyExtractor sut;
    private static String expectedResultText;

    @BeforeAll
    static void loadFiles() {
        try {
            expectedResultText = readFile(SRC_TEST_RESOURCES_TXTFILES_EXPECTED, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        sut = new DependencyExtractor();
    }

    @Test
    void analyseClasses() {
        List<String> classes = new ArrayList<>();
        classes.add(SRC_TEST_RESOURCES_TESTCLASSFILES + "Testclass.class");
        Collection<PackageInformation> analysedClasses = sut.analyseClasses(classes);
        StringBuilder resultText = new StringBuilder();
        analysedClasses.forEach(packageInformation -> resultText.append(DependencyListWriter.generateDeepList(packageInformation)));

        assertThat(resultText.toString()).isEqualTo(expectedResultText);

    }

    @Test
    void analyseClassesWithService() {
        List<String> classes = new ArrayList<>();
        classes.add(SRC_TEST_RESOURCES_TESTCLASSFILES + "TestService.class");
        Collection<PackageInformation> analysedClasses = sut.analyseClasses(classes);
        assertThat(analysedClasses.iterator().next().getClassInformations().first().isService()).isTrue();
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}