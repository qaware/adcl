package core.depex;

import org.objectweb.asm.AnnotationVisitor;

import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 * An {@link AnnotationVisitor} that extracts all class references from an annotation and calls the given {@code dependencyConsumer} for each
 */
class AnnotationExtractor extends AnnotationVisitor {
    private final Consumer<String> classDepConsumer;

    public AnnotationExtractor(Consumer<String> classDepConsumer) {
        super(ASM7);
        this.classDepConsumer = classDepConsumer;
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        Utils.getTypesFromDescriptor(descriptor).forEach(classDepConsumer);
    }

    @Override
    public void visit(String name, Object value) {
        Utils.analyseConstant(value, classDepConsumer, (a, b) -> {
            throw new UnsupportedOperationException("Annotations cannot have method dependencies");
        });
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        Utils.getTypesFromDescriptor(descriptor).forEach(classDepConsumer);
        return new AnnotationExtractor(classDepConsumer);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationExtractor(classDepConsumer);
    }
}