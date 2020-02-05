package core.depex;

import org.objectweb.asm.AnnotationVisitor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 * An {@link AnnotationVisitor} that extracts all class references from an annotation and calls the given {@code dependencyConsumer} for each
 */
class AnnotationExtractor extends AnnotationVisitor {
    private final Consumer<String> classDepConsumer;
    private final BiConsumer<String, String> methodDepConsumer;

    public AnnotationExtractor(Consumer<String> classDepConsumer, BiConsumer<String, String> methodDepConsumer) {
        super(ASM7);
        this.classDepConsumer = classDepConsumer;
        this.methodDepConsumer = methodDepConsumer;
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        Utils.getTypesFromDescriptor(descriptor).forEach(classDepConsumer);
    }

    @Override
    public void visit(String name, Object value) {
        Utils.analyseConstant(value, classDepConsumer, methodDepConsumer);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        Utils.getTypesFromDescriptor(descriptor).forEach(classDepConsumer);
        return new AnnotationExtractor(classDepConsumer, methodDepConsumer);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationExtractor(classDepConsumer, methodDepConsumer);
    }
}