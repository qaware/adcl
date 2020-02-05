package core.depex;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 * An {@link SignatureVisitor} that extracts all class references from the given (generics) signature and calls the given {@code dependencyConsumer} for each
 */
class SignatureExtractor extends SignatureVisitor {
    private final Consumer<String> addDependency;

    public SignatureExtractor(String signature, Consumer<String> addDependency) {
        super(ASM7);
        this.addDependency = addDependency;
        if (signature != null) new SignatureReader(signature).accept(this);
    }

    /*
     * add any class reference in the generic signature as a dependency
     */
    @Override
    public void visitClassType(String name) {
        addDependency.accept(name);
    }
}