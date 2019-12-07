package core;

import core.information.BehaviorInformation;
import core.information.ClassInformation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The  CtBehaviorBodyAnalyzer is used to extract information about with classes and methods are referenced inside a method/constructor body.
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
     * @throws NotFoundException      parameterTypes could not be found
     */
    void analyse(CtBehavior ctBehaviour) throws CannotCompileException, NotFoundException {
        ctBehaviour.instrument(this);
        Arrays.stream(ctBehaviour.getParameterTypes()).forEach(ctClass -> referencedClasses.add(dependencyPool.getOrCreateClassInformation(ctClass.getName())));

        if (ctBehaviour instanceof CtMethod) {
            CtMethod ctMethod = (CtMethod) ctBehaviour;
            String returnType = ctMethod.getReturnType().getName();
            if (!returnType.equals("void")) {
                referencedClasses.add(dependencyPool.getOrCreateClassInformation(returnType));
            }
        }
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
     * Parses the jvm style into a more readable format
     *
     * @param signature the jvm style signature
     * @return formatted signature
     */
    private String parseSignature(String signature) {
        return signature.replace("(L", "(").replace("/", ".").replace(";L", ",")
                .replace(";)Z", ")").replace(";)V", ")").replace(")V", ")");
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
