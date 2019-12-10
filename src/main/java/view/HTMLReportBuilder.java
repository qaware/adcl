package view;

import core.information.BehaviorInformation;
import core.information.ChangelogDependencyInformation;
import core.information.ClassInformation;
import core.information.PackageInformation;
import j2html.tags.ContainerTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtil;
import util.NameParserUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.span;
import static j2html.TagCreator.title;
import static j2html.TagCreator.ul;

class HTMLReportBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLReportBuilder.class);
    private static final String CSS_CLASS = "class";

    private HTMLReportBuilder() {
    }

    /**
     * Generates a HTMl report for the given collection of packageInformations at the stated destination
     *
     * @param packageInformations basis of the report
     * @param destinationPath     the location where the report is saved
     */
    static void createHTMLReport(Collection<PackageInformation> packageInformations, String destinationPath) {
        List<ContainerTag> packageItems = new ArrayList<>();
        packageInformations.stream().filter(PackageInformation::isInternalPackage).forEach(packageInformation -> packageItems.add(HTMLReportBuilder.createPackageItem(packageInformation)));
        writeHTMLFile(html(
                head(
                        title("ADCL-Report"),
                        rawHtml(readCSSFile())
                ),
                body(
                        div(
                                h3("Dependency changelog from " + LocalDate.now()),
                                ul(
                                        packageItems.toArray(new ContainerTag[0])
                                ).attr("id", "parentUL"),
                                rawHtml(readJSFile())
                        ).attr(CSS_CLASS, "content")
                )
                ).renderFormatted(),
                destinationPath
        );
    }

    /**
     * Extracts the needed Information from the PackageInformation and creates a HTML representation.
     *
     * @param packageInformation the packageInformation from which we extract the content from
     * @return a packageInformation represented in HTML code
     */
    private static ContainerTag createPackageItem(PackageInformation packageInformation) {
        List<ContainerTag> classItems = new ArrayList<>();
        packageInformation.getClassInformations().forEach(classInformation -> classItems.add(HTMLReportBuilder.createClassItem(classInformation)));
        return createLiWithNestedUl("Package", packageInformation.getPackageName(), classItems);
    }

    /**
     * Extracts the needed Information from the classInformation and creates a HTML representation.
     *
     * @param classInformation the classeInformation from which we extract the content from
     * @return a classInformation represented in HTML code
     */
    private static ContainerTag createClassItem(ClassInformation classInformation) {
        List<ContainerTag> behaviorItems = new ArrayList<>();
        classInformation.getBehaviorInformations().forEach(behaviorInformation -> {
            if (!behaviorInformation.getReferencedBehavior().isEmpty()) {
                behaviorItems.add(HTMLReportBuilder.createBehaviorItem(behaviorInformation));
            }
        });
        return createLiWithNestedUl(classInformation.isService() ? "Class[Service]" : "Class", NameParserUtil.extractSimpleClassNameFromCompleteClassName(classInformation.getClassName()), behaviorItems);
    }

    /**
     * Extracts the needed Information from the behaviorInformation and creates a HTML representation.
     *
     * @param behaviorInformation the behaviorInformation from which we extract the content from
     * @return a behaviorInformation represented in HTML code
     */
    private static ContainerTag createBehaviorItem(BehaviorInformation behaviorInformation) {
        List<ContainerTag> dependencyItems = new ArrayList<>();
        behaviorInformation.getReferencedBehavior().stream().filter(ref -> ref instanceof ChangelogDependencyInformation).map(ref -> (ChangelogDependencyInformation) ref)
                .forEach(dependencyInformation -> dependencyItems.add(HTMLReportBuilder.createDependencyItem(dependencyInformation.getName(), dependencyInformation.getChangeStatus())));
        return createLiWithNestedUl(behaviorInformation.isConstructor() ? "Constructor" : "Method", NameParserUtil.extractBehaviorName(behaviorInformation.getName()), dependencyItems);
    }

    /**
     * @param name         the name of the behavior that is a dependency.
     * @param changeStatus whenever this dependency has been added or deleted
     * @return the html element li containing the name of the dependency/behavior.
     */
    private static ContainerTag createDependencyItem(String name, ChangelogDependencyInformation.ChangeStatus changeStatus) {
        String cssClass = (changeStatus == ChangelogDependencyInformation.ChangeStatus.ADDED) ? "dependency-added" : "dependency-deleted";
        return li(
                span(name).attr(CSS_CLASS, cssClass)
        );
    }

    /**
     * A Helper method to generate HTML list item with nested unordered lists.
     *
     * @param category name of the category
     * @param name     name of the actual package, class or behavior being represented
     * @param items    the information contained by the item that is being represented
     * @return a html presentation for the given content
     */
    private static ContainerTag createLiWithNestedUl(String category, String name, List<ContainerTag> items) {
        return li(
                span(
                        span(category + ":").attr(CSS_CLASS, "bold"),
                        span(name)
                ).attr(CSS_CLASS, "arrow"),
                ul(items.toArray(new ContainerTag[0])).attr(CSS_CLASS, "nested")
        );
    }

    /**
     * Used to write the created HTML Code into a file.
     *
     * @param html            the html code
     * @param destinationPath the destination folder for the created file.
     */
    private static void writeHTMLFile(String html, String destinationPath) {
        try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(destinationPath + "/" + "changelog_" + LocalDate.now() + ".html"), StandardCharsets.UTF_8)) {
            fileWriter.write(html);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Reads the outsourced CSS code.
     *
     * @return the CSS code.
     */
    private static String readCSSFile() {
        try {
            return IOUtil.readResourceIntoString("view/treeviewCSS.html");
        } catch (IOException e) {
            LOGGER.error("CSS File not found");
        }
        return "";
    }

    /**
     * Reads the outsourced javascript code.
     *
     * @return the javascript code.
     */
    private static String readJSFile() {
        try {
            return IOUtil.readResourceIntoString("view/treeviewJS.html");
        } catch (IOException e) {
            LOGGER.error("javascript File not found");
        }
        return "";
    }
}
