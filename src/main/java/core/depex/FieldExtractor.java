package core.depex;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.function.Consumer;

public class FieldExtractor extends FieldVisitor {
    private final Consumer<String> addDependency;

    public FieldExtractor(Consumer<String> addDependency) {
        super(Opcodes.ASM7);
        this.addDependency = addDependency;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Utils.getTypesFromDescriptor(descriptor).forEach(addDependency);
        return new AnnotationExtractor(addDependency);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Utils.getTypesFromDescriptor(descriptor).forEach(addDependency);
        return new AnnotationExtractor(addDependency);
    }
}
