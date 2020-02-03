package core.depex;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM7;

public class AnnotationExtractor extends AnnotationVisitor {

    private final Consumer<String> dependencyConsumer;

    public AnnotationExtractor(Consumer<String> dependencyConsumer) {
        super(ASM7);
        this.dependencyConsumer = dependencyConsumer;
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        Utils.getTypesFromDescriptor(descriptor).forEach(dependencyConsumer);
    }

    @Override
    public void visit(String name, Object value) {
        if (value instanceof Type)
            Utils.getTypesFromDescriptor(((Type) value).getDescriptor()).forEach(dependencyConsumer);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        Utils.getTypesFromDescriptor(descriptor).forEach(dependencyConsumer);
        return new AnnotationExtractor(dependencyConsumer);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationExtractor(dependencyConsumer);
    }
}