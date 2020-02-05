package core.depex;

import core.information.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 * A {@link ClassVisitor} that inserts a fitting class into the project. Also initiates method analysis
 */
class DepExClassVisitor extends ClassVisitor {
    private final VersionInformation versionInfo;
    private final RootInformation root;
    private final ProjectInformation project;
    private ClassInformation<?> classInfo;

    public DepExClassVisitor(@NotNull VersionInformation versionInfo) {
        super(ASM7);
        this.versionInfo = versionInfo;
        this.project = versionInfo.getProject();
        this.root = project.getRoot();
    }

    /*
     * Class Entry point, create ClassInformation
     * - extends DEP
     * - implements DEP2, DEP3
     * - class X<T extends DEP4>
     */
    @Override
    public void visit(int version, int access, @NotNull String name, String signature, String superName, @NotNull String[] interfaces) {
        classInfo = (ClassInformation<?>) versionInfo.getProject().findOrCreate(name.replace('/', '.'), versionInfo, Information.Type.CLASS);
        new SignatureExtractor(signature, this::addDependency);
        addDependency(superName);
        for (String i : interfaces) addDependency(i);
    }

    /*
     * - @DEP class X
     */
    @Override
    public AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) {
        if (descriptor.equals("Lorg/springframework/stereotype/Service;")) classInfo.setIsService(true);
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        return new AnnotationExtractor(this::addDependency, this::addDependency);
    }

    /*
     * - public DEP field = (initialization dependencies go to method <clinit>)
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        new SignatureExtractor(signature, this::addDependency);
        return new FieldExtractor(this::addDependency, this::addDependency);
    }

    /*
     * Initiates Method analysis
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new DepExMethodVisitor(classInfo, versionInfo, name, descriptor, signature, exceptions);
    }

    /**
     * Add a new class dependency to the list of dependencies the class has. Dependencies to internal (JRE) classes are omitted
     * The (new) resulting class will be marked external, but the dependencyExtractor will mark them internal afterwards
     *
     * @param toClass the class the class dependency is pointing to
     * @return whether a new class dependency got added
     */
    private boolean addDependency(String toClass) {
        toClass = toClass.replace('/', '.');
        if (Utils.isJRE(toClass)) return false;
        classInfo.addClassDependency((ClassInformation<?>) root.findOrCreate(project.resolveProjectByClassName(toClass) + '.' + toClass, null, Information.Type.CLASS), versionInfo);
        return true;
    }

    /**
     * Add a new method dependency to the list of dependencies the class has. Dependencies to internal (JRE) methods are omitted
     * The (new) resulting class will be marked external, but the dependencyExtractor will mark them internal afterwards
     *
     * @param toClass  the class the method dependency is pointing to
     * @param toMethod the method the method dependency is pointing to
     * @return whether a new method dependency got added
     */
    boolean addDependency(String toClass, String toMethod) {
        toClass = toClass.replace('/', '.');
        if (Utils.isJRE(toClass)) return false;
        classInfo.addMethodDependency((MethodInformation) root.findOrCreate(project.resolveProjectByClassName(toClass) + '.' + toClass + '.' + toMethod, null, Information.Type.METHOD), versionInfo);
        return true;
    }
}
