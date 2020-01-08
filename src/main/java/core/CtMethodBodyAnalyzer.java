package core;

import core.information.ClassInformation;
import core.information.MethodInformation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The CtMethodBodyAnalyzer is used to extract information about with classes and methods are referenced inside a method/constructor body.
 */
public class CtMethodBodyAnalyzer extends ExprEditor {
    private static final Logger logger = LoggerFactory.getLogger(CtMethodBodyAnalyzer.class);
    private static final Set<String> PRIMITIVES = new HashSet<>(Arrays.asList("boolean", "byte", "short", "char", "int", "long", "float", "double"));

    private SortedSet<MethodInformation> methodDependencies;
    private SortedSet<ClassInformation> classDependencies;
    private DependencyPool dependencyPool;

    /**
     * Instantiates a new CtMethodBodyAnalyzer.
     */
    CtMethodBodyAnalyzer() {
        this.methodDependencies = new TreeSet<>();
        this.classDependencies = new TreeSet<>();
        this.dependencyPool = DependencyPool.getInstance();
    }

    /**
     * Checks whether a class is internal (JRE). A class is internal if it can be loaded and it's source is internal (aka null)
     *
     * @param className the class name to be checked
     */
    private static boolean isInternal(String className) {
        className = className.replace("[", "").replace("]", "");
        try {
            return PRIMITIVES.contains(className) || isInternal(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets all types denoted by prefix 'L' and suffix ';' that can be found in the methods bytecode signature
     *
     * @return a set of full class names (com.example.MyClass) that were found
     */
    private static Set<String> getTypesFromSignature(String signature) {
        Set<String> result = new HashSet<>();
        int pos = 0;
        while (true) {
            int lpos = signature.indexOf('L', pos);
            if (lpos == -1) break;
            int spos = signature.indexOf(';', lpos);
            result.add(signature.substring(lpos + 1, spos).replace('/', '.'));
            pos = spos;
        }
        return result;
    }

    /**
     * Converts a bytecode signature to a java source signature (only parameter lists, starting with '(' and ending with ')')
     */
    private static String convertSignature(String signature) {
        StringBuilder sb = new StringBuilder("(");

        int pos = 1;
        boolean array = false;

        while (pos < signature.length()) {
            switch (signature.charAt(pos)) {
                case ')':
                    pos = signature.length();
                    if (sb.length() > 1) sb.setLength(sb.length() - 2);
                    break;
                case 'Z':
                    sb.append("boolean");
                    break;
                case 'B':
                    sb.append("byte");
                    break;
                case 'C':
                    sb.append("char");
                    break;
                case 'S':
                    sb.append("short");
                    break;
                case 'I':
                    sb.append("int");
                    break;
                case 'J':
                    sb.append("long");
                    break;
                case 'F':
                    sb.append("float");
                    break;
                case 'D':
                    sb.append("double");
                    break;
                case '[':
                    array = true;
                    pos++;
                    continue;
                case 'L':
                    int sPos = signature.indexOf(';', pos);
                    sb.append(signature, pos + 1, sPos);
                    pos = sPos;
                    break;
            }
            pos++;
            if (array) {
                sb.append("[]");
                array = false;
            }
            if (pos < signature.length()) sb.append(", ");
        }

        sb.append(')');
        return sb.toString().replace('/', '.');
    }

    /**
     * Analyses a {@link CtBehavior} and extracts parameter types, variable types, method-calls that happen inside a Method/Constructor body.
     *
     * @param ctMethod the ct method
     */
    void analyse(CtBehavior ctMethod) {
        getTypesFromSignature(ctMethod.getSignature()).forEach(this::addDependency);
        try {
            ctMethod.instrument(this);
        } catch (CannotCompileException e) {
            logger.warn("Got CannotCompileException without intention to compile", e);
        }
    }

    @Override
    public void edit(MethodCall m) {
        addDependency(m.getClassName(), m.getMethodName() + convertSignature(m.getSignature()), false);
    }

    @Override
    public void edit(NewExpr newExpr) {
        addDependency(newExpr.getClassName(), "<init>" + convertSignature(newExpr.getSignature()), true);
    }

    @Override
    public void edit(Cast c) {
        try {
            addDependency(c.getType().getName());
        } catch (NotFoundException e) {
            //TODO needs to be analyzed
            logger.warn("Could not process class cast in " + c.where().getLongName() + " as the casted type is not initialized yet");
        }
    }

    @Override
    public void edit(NewArray a) {
        try {
            addDependency(a.getComponentType().getName());
        } catch (NotFoundException e) {
            //TODO needs to be analyzed
            logger.warn("Could not process newarr in " + a.where().getLongName() + " as the casted type is not initialized yet");
        }
    }

    @Override
    public void edit(FieldAccess f) {
        getTypesFromSignature(f.getSignature()).forEach(this::addDependency);
    }

    /**
     * Checks whether a class is internal (JRE). A class is internal if it can be loaded and it's source is internal (aka null)
     *
     * @param clazz the class to be checked
     */
    private static boolean isInternal(Class<?> clazz) {
        return clazz.getProtectionDomain().getCodeSource() == null;
    }

    @Override
    public void edit(Instanceof i) {
        try {
            addDependency(i.getType().getName());
        } catch (NotFoundException e) {
            //TODO needs to be analyzed
            logger.warn("Could not process instanceof in " + i.where().getLongName() + " as the casted type is not initialized yet");
        }
    }

    @Override
    public void edit(Handler h) throws CannotCompileException {
        try {
            addDependency(h.getType().getName());
        } catch (NotFoundException e) {
            //TODO needs to be analyzed
            logger.warn("Could not process catch type in " + h.where().getLongName() + " as the casted type is not initialized yet");
        }
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
        methodDependencies.add(dependencyPool.getOrCreateMethodInformation(toClass + '.' + toMethod));
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
