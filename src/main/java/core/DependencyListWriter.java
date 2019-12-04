package core;

import core.information.BehaviorInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
    private static String formatStringMethod = "%n%25sMethod: %s";
    private static String formatStringConstructor = "%n%25sConstructor: %s";

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

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destinationPath + "/" + fileName + ".txt"), StandardCharsets.UTF_8)) {
            writer.write(generateDeepList(packageInformations));
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
        packageInformations.forEach(packageInformation -> deepList.append(String.format("%s %n", generateDeepList(packageInformation))));
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
        packageInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString20, arrowDownRight, packAge.getPackageName())));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refClasses));
        packageInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString20, arrowDownRight, clazz.getClassName())));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refMethods));
        packageInformation.getReferencedBehaviors().forEach(behavior -> stringBuilder.append(String.format(formatString20, arrowDownRight, behavior.getName())));

        return stringBuilder.toString();
    }

    /**
     * Generates a deep list expanding all {@link BehaviorInformation}.
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
        classInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString20, arrowDownRight, packAge.getPackageName())));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refClasses));
        classInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString20, arrowDownRight, clazz.getClassName())));
        stringBuilder.append(String.format(formatString15, arrowDownRight, "Constructors: "));
        classInformation.getBehaviorInformations().forEach(behaviorInformation -> {
            if (behaviorInformation.isConstructor()) {
                stringBuilder.append(generateFlatList(behaviorInformation, formatStringConstructor));
            }
        });
        stringBuilder.append(String.format(formatString15, arrowDownRight, "Methods: "));
        classInformation.getBehaviorInformations().forEach(behaviorInformation -> {
            if (!behaviorInformation.isConstructor()) {
                stringBuilder.append(generateFlatList(behaviorInformation, formatStringMethod));
            }
        });

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
        classInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString20, arrowDownRight, packAge.getPackageName())));
        stringBuilder.append(String.format(formatString15, arrowDownRight, refClasses));
        classInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString20, arrowDownRight, clazz.getClassName())));
        stringBuilder.append(String.format(formatString15, arrowDownRight, "Referenced Method: "));
        classInformation.getReferencedBehavior().forEach(behavior -> stringBuilder.append(String.format(formatString20, arrowDownRight, behavior.getName())));

        return stringBuilder.toString();
    }

    /**
     * Generates a list giving a short overview about all packages,classes,methods referenced in this behavior.
     *
     * @param behaviorInformation the behavior information
     * @return a list containing a short overview about all packages,classes,methods referenced in this method
     */
    public static String generateFlatList(BehaviorInformation behaviorInformation, String formatString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(formatString, arrowDownRight, behaviorInformation.getName()));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refPackages));
        behaviorInformation.getReferencedPackages().forEach(packAge -> stringBuilder.append(String.format(formatString35, arrowDownRight, packAge.getPackageName())));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refClasses));
        behaviorInformation.getReferencedClasses().forEach(clazz -> stringBuilder.append(String.format(formatString35, arrowDownRight, clazz.getClassName())));
        stringBuilder.append(String.format(formatString30, arrowDownRight, refMethods));
        behaviorInformation.getReferencedBehavior().forEach(behavior -> stringBuilder.append(String.format(formatString35, arrowDownRight, behavior.getName())));
        return stringBuilder.toString();
    }
}
