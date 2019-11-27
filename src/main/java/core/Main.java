package core;

import core.information.PackageInformation;

import java.io.File;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try (Scanner scan = new Scanner(System.in)) {
            System.out.println("Please enter the location of your project\n");
            String path = scan.nextLine();
            String destination = "C:/ADCLTest";
            Alg alg = new Alg(path);
            alg.generateFileList();
            DependencyExtractor extractor = new DependencyExtractor();
            Collection<PackageInformation> packages = extractor.analyseClasses(alg.getList().stream()
                    .map(File::getAbsolutePath).collect(Collectors.toList()));

            //System.out.println("Please enter the location of the output\n");
            //destination=scan.nextLine();

            DependencyListWriter.writeListToFile(packages, destination, "test");
        }
    }
}
