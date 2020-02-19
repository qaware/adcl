package core.depex;

import core.information.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * A {@link MethodVisitor} that inserts a fitting method into the project
 */
class DepExMethodVisitor extends MethodVisitor {
    private final MethodInformation methodInfo;
    private final VersionInformation versionInfo;
    private final RootInformation root;
    private final ProjectInformation project;

    public DepExMethodVisitor(@NotNull ClassInformation<?> parent, @NotNull VersionInformation versionInfo, String name, String descriptor, String signature, String[] exceptions) {
        super(ASM7);
        this.methodInfo = (MethodInformation) parent.findOrCreate(name + Utils.convertDescriptor(descriptor), versionInfo, Information.Type.METHOD);
        this.versionInfo = versionInfo;
        this.project = versionInfo.getProject();
        this.root = project.getRoot();

        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        new SignatureExtractor(signature, this::addDependency);
        if (exceptions != null) for (String exception : exceptions) addDependency(exception);
    }

    /*
     * - call(DEP.class)
     * - Class<?> clazz = DEP.class
     */
    @Override
    public void visitLdcInsn(Object value) {
        Utils.analyseConstant(value, this::addDependency, this::addDependency);
    }

    /*
     * - @DEP call()
     */
    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    /*
     * - @DEP public void myMethod()
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    /*
     * - @DEP String var
     */
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    /*
     * - public void myMethod(@DEP param1)
     */
    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    /*
     * - @DEP Exception e in catch block
     */
    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    /*
     * All annotations in method signature except parameter annotations
     * - public @DEP1 RetType myMethod()
     * - public <DEP> void myMethod()
     * - public void myMethod() throws DEP
     */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    /*
     * lambda calls
     * - call parameter types
     * - called base method
     * - called implementation method
     */
    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, @NotNull Object... bootstrapMethodArguments) {
        Utils.analyseConstant(bootstrapMethodHandle, this::addDependency, this::addDependency);
        for (Object arg : bootstrapMethodArguments)
            Utils.analyseConstant(arg, this::addDependency, this::addDependency);
    }

    /*
     * - new DEP[][]
     */
    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
    }

    /*
     * - class reference in catch block
     */
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) { // catch or finally
        if (type != null) addDependency(type);
    }

    /*
     * - new DEP[]
     * - (DEP) o
     * - o instanceof DEP
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode != NEW) { // new handled with constructor call (MethodInsn)
            if (type.startsWith("[")) {
                Utils.getTypesFromDescriptor(type).forEach(this::addDependency);
            } else {
                addDependency(type);
            }
        }
    }

    /*
     * - DEP()
     */
    @Override
    public void visitMethodInsn(int opcode, @NotNull String owner, String name, String descriptor, boolean isInterface) { // method calls
        addDependency(owner.startsWith("[") ? Utils.getTypesFromDescriptor(owner).iterator().next() : owner, name + Utils.convertDescriptor(descriptor));
    }

    /*
     * - DEP x = ...
     */
    @Override
    public void visitLocalVariable(@NotNull String name, String descriptor, String signature, Label start, Label end, int index) {
        if (!name.matches("this(?:\\$\\d+)?")) { // this not needed
            Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
            new SignatureExtractor(signature, this::addDependency);
        }
    }

    /**
     * collective method for all annotation visits
     *
     * @param descriptor the annotation descriptor passed as parameter by ASM
     * @return an AnnotationVisitor that adds found class references as dependency
     */
    @NotNull
    @Contract("_ -> new")
    private AnnotationVisitor visitAnnotation(String descriptor) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        return new AnnotationExtractor(this::addDependency);
    }

    /**
     * Add a new method dependency to the list of dependencies the method has. Dependencies to internal (JRE) methods are omitted
     * The (new) resulting class will be marked external, but the dependencyExtractor will mark them internal afterwards
     *
     * @param toClass  the class the method dependency is pointing to
     * @param toMethod the method the method dependency is pointing to
     */
    private void addDependency(String toClass, String toMethod) {
        toClass = toClass.replace('/', '.');
        if (Utils.isJRE(toClass)) return;
        String path = project.resolveProjectByClassName(toClass) + '.' + toClass + '.' + toMethod;
        if (path.equals(methodInfo.getPath())) return;
        methodInfo.addMethodDependency((MethodInformation) root.findOrCreate(path, null, Information.Type.METHOD), versionInfo);
    }

    /**
     * Add a new class dependency to the list of dependencies the method has. Dependencies to internal (JRE) classes are omitted
     * The (new) resulting class will be marked external, but the dependencyExtractor will mark them internal afterwards
     *
     * @param toClass the class the class dependency is pointing to
     */
    private void addDependency(String toClass) {
        toClass = toClass.replace('/', '.');
        if (Utils.isJRE(toClass)) return;
        String path = project.resolveProjectByClassName(toClass) + '.' + toClass;
        if (path.equals(methodInfo.getParent().getPath())) return;
        methodInfo.addClassDependency((ClassInformation<?>) root.findOrCreate(path, null, Information.Type.CLASS), versionInfo);
    }
}