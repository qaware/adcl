package core.depex;

import core.information.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.NEW;

public class DepExMethodVisitor extends MethodVisitor {
    private final MethodInformation methodInfo;
    private final VersionInformation versionInfo;
    private final RootInformation root;
    private final ProjectInformation project;

    public DepExMethodVisitor(MethodInformation methodInfo, @NotNull VersionInformation versionInfo, String descriptor, String signature, String[] exceptions) {
        super(ASM7);
        this.methodInfo = methodInfo;
        this.versionInfo = versionInfo;
        this.project = versionInfo.getProject();
        this.root = project.getRoot();

        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        new SignatureExtractor(signature).forEach(this::addDependency);
        if (exceptions != null) for (String exception : exceptions) addDependency(exception);
    }

    @Override
    public void visitLdcInsn(Object value) { // X.class
        if (value instanceof Type)
            Utils.getTypesFromDescriptor(((Type) value).getDescriptor()).forEach(this::addDependency);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return visitAnnotation(descriptor);
    }

    @NotNull
    @Contract("_ -> new")
    private AnnotationVisitor visitAnnotation(String descriptor) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        return new AnnotationExtractor(this::addDependency);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, @NotNull Object... bootstrapMethodArguments) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        for (Object arg : bootstrapMethodArguments)
            if (arg instanceof Type)
                Utils.getTypesFromDescriptor(((Type) arg).getDescriptor()).forEach(this::addDependency);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) { // catch or finally
        if (type != null) addDependency(type);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) { // new String[], (String) o, instanceof String
        if (opcode != NEW) addDependency(type); //new handled with constructor call (MethodInsn)
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) { // method calls
        addDependency(owner, name + Utils.convertSignature(descriptor));
    }

    @Override
    public void visitLocalVariable(@NotNull String name, String descriptor, String signature, Label start, Label end, int index) {
        if (!name.matches("this(?:\\$\\d+)?")) { //this not needed
            Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
            new SignatureExtractor(signature).forEach(this::addDependency);
        }
    }

    /**
     * Add a new method dependency to the list of dependencies the method has. Dependencies to internal (JRE) methods are omitted
     * The (new) resulting class will be marked external, but the dependencyExtractor will mark them internal afterwards
     *
     * @param toClass  the class the method dependency is pointing to
     * @param toMethod the method the method dependency is pointing to
     * @return whether a new method dependency got added
     */
    boolean addDependency(String toClass, String toMethod) {
        toClass = toClass.replace('/', '.');
        if (Utils.isJRE(toClass)) return false;
        methodInfo.addMethodDependency((MethodInformation) root.findOrCreate(project.resolveProjectByClassName(toClass) + '.' + toClass + '.' + toMethod, null, Information.Type.METHOD), versionInfo);
        return true;
    }

    /**
     * Add a new class dependency to the list of dependencies the method has. Dependencies to internal (JRE) classes are omitted
     * The (new) resulting class will be marked external, but the dependencyExtractor will mark them internal afterwards
     *
     * @param toClass the class the class dependency is pointing to
     * @return whether a new class dependency got added
     */
    boolean addDependency(String toClass) {
        toClass = toClass.replace('/', '.');
        if (Utils.isJRE(toClass)) return false;
        methodInfo.addClassDependency((ClassInformation<?>) root.findOrCreate(project.resolveProjectByClassName(toClass) + '.' + toClass, null, Information.Type.CLASS), versionInfo);
        return true;
    }
}