package core;

import core.information.PackageInformation;

import java.io.File;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try (Scanner scan = new Scanner(System.in)) {
            String path = args[1];
            String destination = args[0];
            ClassCollector alg = new ClassCollector(path);
            alg.generateFileList();
            DependencyExtractor extractor = new DependencyExtractor();

            Collection<PackageInformation> packages = extractor.analyseClasses(alg.getList().stream()
                    .map(File::getAbsolutePath).collect(Collectors.toList()));

            DependencyListWriter.writeListToFile(packages, destination, "test");
        }
    }
}
