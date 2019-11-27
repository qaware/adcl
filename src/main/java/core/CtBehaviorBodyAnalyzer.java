package core;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The  CtBehaviorBodyAnalyzer is used to extract information about with classes and methods are referenced inside a method/constructor body.
 */
public class CtBehaviorBodyAnalyzer extends ExprEditor {
    private SortedSet<String> referencedMethods;
    private SortedSet<String> referencedClasses;

    /**
     * Instantiates a new CtBehaviorBodyAnalyzer.
     */
    CtBehaviorBodyAnalyzer() {
        this.referencedMethods = new TreeSet<>();
        this.referencedClasses = new TreeSet<>();
    }

    /**
     * Analyses a {@link CtBehavior} and extracts parameter types, variable types, method-calls that happen inside a Method/Constructor body.
     *
     * @param ctBehaviour the ct behaviour
     * @throws CannotCompileException if CtBehavior body cannot be compiled
     * @throws NotFoundException      parameterTypes could not be found
     */
    void analyse(CtBehavior ctBehaviour) throws CannotCompileException, NotFoundException {
        ctBehaviour.instrument(this);
        Arrays.stream(ctBehaviour.getParameterTypes()).forEach(ctClass -> referencedClasses.add(ctClass.getName()));
        if (ctBehaviour instanceof CtMethod) {
            CtMethod ctMethod = (CtMethod) ctBehaviour;
            String returnType = ctMethod.getReturnType().getName();
            if (!returnType.equals("void")) {
                referencedClasses.add(returnType);
            }
        }
    }

    @Override
    public void edit(MethodCall m) {
        referencedMethods.add(m.getClassName() + "." + m.getMethodName());
        referencedClasses.add(m.getClassName());
    }

    /**
     * Gets the referenced classes extracted by the analysis.
     *
     * @return the referenced classes
     */
    public SortedSet<String> getReferencedClasses() {
        return referencedClasses;
    }

    /**
     * Gets referenced methods extracted by the analysis.
     *
     * @return the referenced methods
     */
    public SortedSet<String> getReferencedMethods() {
        return referencedMethods;
    }
}
