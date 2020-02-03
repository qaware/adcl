package core.depex;

import core.information.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

public class DepExClassVisitor extends ClassVisitor {
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

    @Override
    public void visit(int version, int access, @NotNull String name, String signature, String superName, @NotNull String[] interfaces) {
        classInfo = (ClassInformation<?>) versionInfo.getProject().findOrCreate(name.replace('/', '.'), versionInfo, Information.Type.CLASS);
        new SignatureExtractor(signature).forEach(this::addDependency);
        addDependency(superName);
        for (String i : interfaces) addDependency(i);
    }

    @Override
    public AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) { // @
        if (descriptor.equals("Lorg/springframework/stereotype/Service;")) classInfo.setIsService(true);
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        return new AnnotationExtractor(this::addDependency);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        Utils.getTypesFromDescriptor(descriptor).forEach(this::addDependency);
        new SignatureExtractor(signature).forEach(this::addDependency);
        return new FieldExtractor(this::addDependency);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodInformation method = (MethodInformation) classInfo.findOrCreate(name + Utils.convertSignature(descriptor), versionInfo, Information.Type.METHOD);
        return new DepExMethodVisitor(method, versionInfo, descriptor, signature, exceptions);
    }

    /**
     * Add a new class dependency to the list of dependencies the method has. Dependencies to internal (JRE) classes are omitted
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
}
