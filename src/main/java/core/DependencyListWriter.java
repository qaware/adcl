package core;

import core.information.ClassInformation;
import core.information.ConstructorInformation;
import core.information.MethodInformation;
import core.information.PackageInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * The DependencyListWriter formats the extracted information and writes them into a file.
 */
public class DependencyListWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyListWriter.class);
    private static String formatString15 = "%n %15s %s";
    private static String formatString20 = "%n %20s %s";
    private static String formatString30 = "%n %30s %s";
    private static String formatString35 = "%n %35s %s";
    private static String arrowDownRight = "↪";
    private static String refMethods = "Referenced methods: ";
    private static String refClasses = "Referenced classes: ";
    private static String refPackages = "Referenced packages: ";

    /**
     * Prevent initialisation.
     */
    private DependencyListWriter() {

    }

    /**
     * Write a list containing all PackageInformation received and expands every {@link ClassInformation} contained.
     *
     * @param packageInformations the package informations
     * @param destinationPath     the destination path
     * @param fileName            the file name
     */
    public static void writeListToFile(Collection<PackageInformation> packageInformations, String destinationPath, String fileName) {

        try (FileWriter fileWriter = new FileWriter(destinationPath + "/" + fileName + ".txt")) {
            fileWriter.write(generateDeepList(packageInformations));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Generates  a deep list expanding all {@link PackageInformation}.
     *
     * @param packageInformations contains multiple package information
     * @return all information in form of a list
     */
    public static String generateDeepList(Collection<PackageInformation> packageInformations) {
        StringBuilder deepList = new StringBuilder();
        packageInformations.forEach(packageInformation -> deepList.append(generateDeepList(packageInformation)));
        return deepList.toString();
    }

    /**
     * Generates  a deep list expanding all {@link ClassInformation}.
     *
     * @param packageInformation the package information
     * @return all information in form of a list
     */
    public static String generateDeepList(PackageInformation packageInformation) {
        StringBuilder deepList = new StringBuilder();
        deepList.append(String.format("Package: %s ", packageInformation.getPackageName()));
        packageInformation.getClassInformations().forEach(classInformation -> deepList.append(String.format("%n %5s %s", arrowDownRight, generateDeepList(classInformation))));
        return deepList.toString();
    }

    /**
     * Generates a flat list giving a short overview about all packages,classes,methods referenced in this package.
     *
     * @param packageInformation the package information
     * @return a list containing a short overview about all packages,classes,methods referenced in this package
     */
    public static String generateFlatList(PackageInformation packageInformation) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Package: ").append(packageInformation.getPackageName());
        stringBuilder.append(String.format(formatString15, arrowDownRight, refPackages));
        packageInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString20, arrowDownRight, packAge)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refClasses));
        packageInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString20, arrowDownRight, clazz)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refMethods));
        packageInformation.getReferencedMethods().forEach(method -> stringBuilder.append(String.format(formatString20, arrowDownRight, method)));

        return stringBuilder.toString();
    }

    /**
     * Generates a deep list expanding all {@link ConstructorInformation} and {@link MethodInformation}.
     *
     * @param classInformation the class information
     * @return all information in form of a list
     */
    public static String generateDeepList(ClassInformation classInformation) {
        StringBuilder stringBuilder = new StringBuilder();
        if (classInformation.isService()) {
            stringBuilder.append("Class[Service]: ").append(classInformation.getClassName());
        } else {
            stringBuilder.append("Class: ").append(classInformation.getClassName());
        }
        stringBuilder.append(String.format(formatString15, arrowDownRight, refPackages));
        classInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString20, arrowDownRight, packAge)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refClasses));
        classInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString20, arrowDownRight, clazz)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, "Constructors: "));
        classInformation.getConstructorInformations().forEach(constructorInformation -> stringBuilder.append(generateFlatList(constructorInformation)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, "Methods: "));
        classInformation.getMethodInformations().forEach(methodInformation -> stringBuilder.append(generateFlatList(methodInformation)));

        return stringBuilder.toString();
    }

    /**
     * Generates a flat list giving a short overview about all packages,classes,methods referenced in this class.
     *
     * @param classInformation the class information
     * @return a list containing a short overview about all packages,classes,methods referenced in this class
     */
    public static String generateFlatList(ClassInformation classInformation) {
        StringBuilder stringBuilder = new StringBuilder();
        if (classInformation.isService()) {
            stringBuilder.append("Class[Service]: ").append(classInformation.getClassName());
        } else {
            stringBuilder.append("Class: ").append(classInformation.getClassName());
        }
        stringBuilder.append(String.format(formatString15, arrowDownRight, refPackages));
        classInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString20, arrowDownRight, packAge)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refClasses));
        classInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString20, arrowDownRight, clazz)));
        stringBuilder.append(String.format(formatString15, arrowDownRight, "Referenced Method: "));
        classInformation.getReferencedMethods().forEach(method -> stringBuilder.append(String.format(formatString20, arrowDownRight, method)));

        return stringBuilder.toString();
    }

    /**
     * Generates a list giving a short overview about all packages,classes,methods referenced in this method.
     *
     * @param methodInformation the method information
     * @return a list containing a short overview about all packages,classes,methods referenced in this method
     */
    public static String generateFlatList(MethodInformation methodInformation) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%n%25sMethod: %s", arrowDownRight, methodInformation.getMethodName()));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refPackages));
        methodInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString35, arrowDownRight, packAge)));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refClasses));
        methodInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString35, arrowDownRight, clazz)));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refMethods));
        methodInformation.getReferencedMethods().forEach(method -> stringBuilder.append(String.format(formatString35, arrowDownRight, method)));
        return stringBuilder.toString();
    }

    /**
     * Generates a list giving a short overview about all packages,classes,methods referenced in this constructor.
     *
     * @param constructorInformation the constructor information
     * @return a list containing a short overview about all packages,classes,methods referenced in this constructor
     */
    public static String generateFlatList(ConstructorInformation constructorInformation) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%n%25sConstructor: %s", arrowDownRight, constructorInformation.getConstructorSignature()));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refPackages));
        constructorInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString35, arrowDownRight, packAge)));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refClasses));
        constructorInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString35, arrowDownRight, clazz)));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refMethods));
        constructorInformation.getReferencedMethods().forEach(method -> stringBuilder.append(String.format(formatString35, arrowDownRight, method)));

        return stringBuilder.toString();
    }
}
