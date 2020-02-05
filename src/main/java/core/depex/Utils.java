package core.depex;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Utility Methods to work with ASM or for general dependency analysis
 */
class Utils {
    private static final String PRIMITIVES_SHORT = "ZBCSIJFD";

    private Utils() {

    }

    /**
     * A one-method-extractor for constants
     * A constant might be a string literal, an integer constant, etc...
     * but also a Type constant (-> ClassDependency)
     * or a Method Handle (-> MethodDependency)
     *
     * @param value        the constant to analyse
     * @param addClassDep  consumer for found class dependencies
     * @param addMethodDep consumer for found method dependencies
     */
    public static void analyseConstant(Object value, Consumer<String> addClassDep, BiConsumer<String, String> addMethodDep) {
        if (value instanceof Type) Utils.getTypesFromDescriptor(((Type) value).getDescriptor()).forEach(addClassDep);
        else if (value instanceof Handle) {
            Handle handle = (Handle) value;
            addMethodDep.accept(handle.getOwner(), handle.getName() + convertDescriptor(handle.getDesc()));
        }
    }

    /**
     * Gets all types denoted by prefix 'L' and suffix ';' that can be found in the methods bytecode descriptor
     *
     * @param descriptor a java bytecode method descriptor (e.g. {@code getTypesFromDescriptor(Ljava/lang/String;)Ljava/util/Set;})
     * @return a set of full class names (com.example.MyClass) that were found
     */
    @NotNull
    public static Set<String> getTypesFromDescriptor(@NotNull String descriptor) {
        Set<String> result = new HashSet<>();
        int pos = 0;
        while (true) {
            int lpos = descriptor.indexOf('L', pos);
            if (lpos == -1) break;
            int spos = descriptor.indexOf(';', lpos);
            result.add(descriptor.substring(lpos + 1, spos).replace('/', '.'));
            pos = spos;
        }
        return result;
    }

    /**
     * Converts a bytecode descriptor to a java source descriptor (only parameter lists, starting with '(' and ending with ')')
     *
     * @param descriptor the java bytecode method descriptor's parameter list in its parentheses (e.g. {@code (Ljava/lang/String;)})
     * @return the parameter list in java source form, also with parentheses (e.g. {@code (java.lang.String)})
     */
    @NotNull
    public static String convertDescriptor(@NotNull String descriptor) {
        StringBuilder sb = new StringBuilder("(");

        int pos = 1;
        boolean array = false;

        while (pos < descriptor.length()) {
            switch (descriptor.charAt(pos)) {
                case ')':
                    pos = descriptor.length();
                    if (sb.length() > 1) sb.setLength(sb.length() - 2); // remove last comma
                    break;
                case 'Z':
                    sb.append("boolean");
                    break;
                case 'B':
                    sb.append("byte");
                    break;
                case 'C':
                    sb.append("char");
                    break;
                case 'S':
                    sb.append("short");
                    break;
                case 'I':
                    sb.append("int");
                    break;
                case 'J':
                    sb.append("long");
                    break;
                case 'F':
                    sb.append("float");
                    break;
                case 'D':
                    sb.append("double");
                    break;
                case '[':
                    array = true;
                    pos++;
                    continue;
                case 'L':
                    int sPos = descriptor.indexOf(';', pos);
                    sb.append(descriptor, pos + 1, sPos);
                    pos = sPos;
                    break;
                default:
                    throw new IllegalStateException("Invalid character " + descriptor.charAt(pos) + " in method descriptor");
            }
            pos++;
            if (array) {
                sb.append("[]");
                array = false;
            }
            if (pos < descriptor.length()) sb.append(", ");
        }

        sb.append(')');
        return sb.toString().replace('/', '.');
    }

    /**
     * @param className the class name to be checked
     * @return whether a class is internal (JRE). A class is internal if it can be loaded and it's source is internal (aka null)
     */
    public static boolean isJRE(String className) {
        className = className.replace("[", "").replace("]", "");
        try {
            return (className.length() == 1 && PRIMITIVES_SHORT.indexOf(className.charAt(0)) >= 0) || isJRE(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * @param clazz the class to be checked
     * @return whether a class is internal (JRE). A class is internal if it can be loaded and it's source is internal (aka null)
     */
    public static boolean isJRE(@NotNull Class<?> clazz) {
        return clazz.getProtectionDomain().getCodeSource() == null;
    }

    /**
     * @param num opcode
     * @return the name of that opcode
     * @see Opcodes
     */
    public static String getOpCode(int num) {
        return Arrays.stream(Opcodes.class.getFields()).filter(f -> {
            try {
                return f.getInt(null) == num;
            } catch (Exception e) {
                return false;
            }
        }).findAny().map(Field::getName).orElse(null);
    }
}
