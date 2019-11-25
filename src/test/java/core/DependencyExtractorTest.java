package core;

import core.information.PackageInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private DependencyExtractor sut;
    private static String expectedResultText;

    @BeforeAll
    static void loadFiles() {
        try {
            expectedResultText = readFile("src/test/resources/txtfiles/expectedTextResultFromTestclass.txt", StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        sut = new DependencyExtractor();
    }

    @Test
    void analyseClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("src/test/resources/testclassfiles/Testclass.class");
        Collection<PackageInformation> analysedClasses = sut.analyseClasses(classes);
        StringBuilder resultText = new StringBuilder();
        analysedClasses.forEach(packageInformation -> resultText.append(DependencyListWriter.generateDeepList(packageInformation)));

        assertThat(resultText.toString()).isEqualTo(expectedResultText);

    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}