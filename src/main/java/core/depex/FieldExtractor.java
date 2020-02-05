package core.depex;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An {@link FieldVisitor} that extracts all class references from a field and calls the given {@code dependencyConsumer} for each
 */
class FieldExtractor extends FieldVisitor {
    private final Consumer<String> addDependency;
    private final BiConsumer<String, String> methodDepConsumer;

    public FieldExtractor(Consumer<String> addDependency, BiConsumer<String, String> methodDepConsumer) {
        super(Opcodes.ASM7);
        this.addDependency = addDependency;
        this.methodDepConsumer = methodDepConsumer;
    }

    /*
     * - @DEP Field myField
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Utils.getTypesFromDescriptor(descriptor).forEach(addDependency);
        return new AnnotationExtractor(addDependency, methodDepConsumer);
    }

    //TODO where is the difference?
    /*
     * - @DEP Field myField
     */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Utils.getTypesFromDescriptor(descriptor).forEach(addDependency);
        return new AnnotationExtractor(addDependency, methodDepConsumer);
    }
}
