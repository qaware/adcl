package core;

import core.information.ClassInformation;
import core.information.MethodInformation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CtMethodBodyAnalyzer is used to extract information about with classes and methods are referenced inside a method/constructor body.
 */
public class CtMethodBodyAnalyzer extends ExprEditor {

    private SortedSet<MethodInformation> methodDependencies;
    private SortedSet<ClassInformation> classDependencies;
    private DependencyPool dependencyPool;

    /**
     * Instantiates a new CtMethodBodyAnalyzer.
     */
    CtMethodBodyAnalyzer() {
        this.methodDependencies = new TreeSet<>(MethodInformation.MethodInformationComparator.getInstance());
        this.classDependencies = new TreeSet<>(ClassInformation.ClassInformationComparator.getInstance());
        this.dependencyPool = DependencyPool.getInstance();
    }

    /**
     * Analyses a {@link CtBehavior} and extracts parameter types, variable types, method-calls that happen inside a Method/Constructor body.
     *
     * @param ctMethods the ct method
     * @throws CannotCompileException if CtMethod body cannot be compiled
     */
    void analyse(CtBehavior ctMethods) throws CannotCompileException {
        ctMethods.instrument(this);
        addParameterTypesAsDependencies(ctMethods.getSignature());
    }

    /**
     * Checks whether a class is internal (JRE). A class is internal if it can be loaded and it's source is internal (aka null)
     *
     * @param className the class name to be checked
     */
    private static boolean isInternal(String className) {
        try {
            return isInternal(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks whether a class is internal (JRE). A class is internal if it can be loaded and it's source is internal (aka null)
     *
     * @param clazz the class to be checked
     */
    private static boolean isInternal(Class<?> clazz) {
        return clazz.getProtectionDomain().getCodeSource() == null;
    }

    /**
     * Parses the jvm style into a list of the contained types.
     *
     * @param signature the jvm style signature
     * @return list of types
     */
    private List<String> parseJVMSignatureIntoParameterTypeList(String signature) {
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
    private String parseJVMSignatureIntoMethodSignature(String signature) {
        return parseJVMSignatureIntoParameterTypeList(signature.substring(signature.indexOf('('), signature.lastIndexOf(')'))).toString().replace("[", "(").replace("]", ")").replace(" ", "");
    }

    @Override
    public void edit(MethodCall m) {
        String signature = parseJVMSignatureIntoMethodSignature(m.getSignature());
        addDependency(m.getClassName(), "." + m.getMethodName() + signature, false);
    }

    /**
     * Gets the referenced classes extracted by the analysis.
     *
     * @return the referenced classes
     */
    public SortedSet<ClassInformation> getClassDependencies() {
        return classDependencies;
    }

    /**
     * Gets referenced methods extracted by the analysis.
     *
     * @return the referenced methods
     */
    public SortedSet<MethodInformation> getMethodDependencies() {
        return methodDependencies;
    }

    @Override
    public void edit(NewExpr newExpr) {
        String signature = parseJVMSignatureIntoMethodSignature(newExpr.getSignature());
        addDependency(newExpr.getClassName(), signature, true);
    }

    /**
     * Adds the Parameter types to the references Classes.
     *
     * @param signature in JVM Type signature
     */
    private void addParameterTypesAsDependencies(String signature) {
        List<String> parameterTypes = parseJVMSignatureIntoParameterTypeList(signature);
        parameterTypes.forEach(this::addDependency);
    }

    /**
     * Add a new method dependency to the list of dependencies the method has. Dependencies to internal (JRE) methods are omitted
     *
     * @param toClass       the class the method dependency is pointing to
     * @param toMethod      the method the method dependency is pointing to
     * @param isConstructor whether the dependency represents a constructor call
     * @return whether a new method dependency got added
     */
    private boolean addDependency(String toClass, String toMethod, boolean isConstructor) {
        if (!addDependency(toClass)) return false;
        methodDependencies.add(dependencyPool.getOrCreateMethodInformation(toClass + toMethod, isConstructor));
        return true;
    }

    /**
     * Add a new class dependency to the list of dependencies the method has. Dependencies to internal (JRE) classes are omitted
     *
     * @param toClass the class the class dependency is pointing to
     * @return whether a new class dependency got added
     */
    private boolean addDependency(String toClass) {
        if (isInternal(toClass)) return false;
        classDependencies.add(dependencyPool.getOrCreateClassInformation(toClass));
        return true;
    }
}
