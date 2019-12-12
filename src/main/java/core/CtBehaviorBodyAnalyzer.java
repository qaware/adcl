package core;

import core.information.BehaviorInformation;
import core.information.ClassInformation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CtBehaviorBodyAnalyzer is used to extract information about with classes and methods are referenced inside a method/constructor body.
 */
public class CtBehaviorBodyAnalyzer extends ExprEditor {

    private SortedSet<BehaviorInformation> referencedBehavior;
    private SortedSet<ClassInformation> referencedClasses;
    private DependencyPool dependencyPool;

    /**
     * Instantiates a new CtBehaviorBodyAnalyzer.
     */
    CtBehaviorBodyAnalyzer() {
        this.referencedBehavior = new TreeSet<>(BehaviorInformation.BehaviorInformationComparator.getInstance());
        this.referencedClasses = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        this.dependencyPool = DependencyPool.getInstance();
    }

    /**
     * Analyses a {@link CtBehavior} and extracts parameter types, variable types, method-calls that happen inside a Method/Constructor body.
     *
     * @param ctBehaviour the ct behaviour
     * @throws CannotCompileException if CtBehavior body cannot be compiled
     */
    void analyse(CtBehavior ctBehaviour) throws CannotCompileException {
        ctBehaviour.instrument(this);
        addParameterTypesAsDependencies(ctBehaviour.getSignature());
    }

    @Override
    public void edit(MethodCall m) {
        String signature = parseSignature(m.getSignature());
        referencedBehavior.add(dependencyPool.getOrCreateBehaviorInformation(m.getClassName() + "." + m.getMethodName() + signature, false));
        referencedClasses.add(dependencyPool.getOrCreateClassInformation(m.getClassName()));
    }

    @Override
    public void edit(NewExpr newExpr) {
        String signature = parseSignature(newExpr.getSignature());
        referencedBehavior.add(dependencyPool.getOrCreateBehaviorInformation(newExpr.getClassName() + signature, true));
        referencedClasses.add(dependencyPool.getOrCreateClassInformation(newExpr.getClassName()));
    }

    /**
     * Parses the jvm style into a list of the contained types.
     *
     * @param signature the jvm style signature
     * @return list of types
     */
    private List<String> parseSignatureIntoParameterTypes(String signature) {
        String[] split = signature.replace("/", ".").replace("(", "").replace(")", "").split(";");

        Pattern pattern = Pattern.compile("[a-z]");
        List<String> formatted = new ArrayList<>();
        Arrays.stream(split).forEach(s -> {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                formatted.add(s.substring(matcher.start()));
            }
        });
        return formatted;
    }

    /**
     * Parses a jvm style signature into a Method parameter list.
     *
     * @param signature the jvm style signature
     * @return types in style of (Type,Type,Type,...)
     */
    private String parseSignature(String signature) {
        return parseSignatureIntoParameterTypes(signature.substring(signature.indexOf('('), signature.lastIndexOf(')'))).toString().replace("[", "(").replace("]", ")").replace(" ", "");
    }

    /**
     * Adds the Parameter types to the references Classes.
     *
     * @param signature in JVM Type signature
     */
    private void addParameterTypesAsDependencies(String signature) {
        List<String> parameterTypes = parseSignatureIntoParameterTypes(signature);
        parameterTypes.forEach(parameterType -> referencedClasses.add(dependencyPool.getOrCreateClassInformation(parameterType)));
    }

    /**
     * Gets the referenced classes extracted by the analysis.
     *
     * @return the referenced classes
     */
    public SortedSet<ClassInformation> getReferencedClasses() {
        return referencedClasses;
    }

    /**
     * Gets referenced methods extracted by the analysis.
     *
     * @return the referenced methods
     */
    public SortedSet<BehaviorInformation> getReferencedBehavior() {
        return referencedBehavior;
    }
}
