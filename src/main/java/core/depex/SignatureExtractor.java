package core.depex;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import util.CollectionWrapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ASM7;

public class SignatureExtractor extends SignatureVisitor implements CollectionWrapper<String> {
    private final Set<String> extracted = new HashSet<>();

    public SignatureExtractor(String signature) {
        super(ASM7);
        if (signature != null) new SignatureReader(signature).accept(this);
    }

    @Override
    public void visitClassType(String name) {
        extracted.add(name);
    }

    @Override
    public Collection<String> collectionToDisplay() {
        return extracted;
    }
}