package com.github.qaware.adcl.depex;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.function.Consumer;

/**
 * An {@link FieldVisitor} that extracts all class references from a field and calls the given {@code dependencyConsumer} for each
 */
class FieldExtractor extends FieldVisitor {
    private final Consumer<String> addDependency;

    public FieldExtractor(Consumer<String> addDependency) {
        super(Opcodes.ASM7);
        this.addDependency = addDependency;
    }

    /*
     * - @DEP Field myField (annotates the field itself)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Utils.getTypesFromDescriptor(descriptor).forEach(addDependency);
        return new AnnotationExtractor(addDependency);
    }

    /*
     * - @DEP Field myField (annotates the field type)
     * - Field<@DEP T> myField
     */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Utils.getTypesFromDescriptor(descriptor).forEach(addDependency);
        return new AnnotationExtractor(addDependency);
    }
}
