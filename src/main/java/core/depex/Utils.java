package core.depex;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class Utils {
    private static final String PRIMITIVES_SHORT = "ZBCSIJFD";

    private Utils() {

    }

    /**
     * Gets all types denoted by prefix 'L' and suffix ';' that can be found in the methods bytecode signature
     *
     * @param signature a java bytecode method signature (e.g. {@code getTypesFromSignature(Ljava/lang/String;)Ljava/util/Set;})
     * @return a set of full class names (com.example.MyClass) that were found
     */
    @NotNull
    public static Set<String> getTypesFromDescriptor(@NotNull String signature) {
        Set<String> result = new HashSet<>();
        int pos = 0;
        while (true) {
            int lpos = signature.indexOf('L', pos);
            if (lpos == -1) break;
            int spos = signature.indexOf(';', lpos);
            result.add(signature.substring(lpos + 1, spos).replace('/', '.'));
            pos = spos;
        }
        return result;
    }

    /**
     * Converts a bytecode signature to a java source signature (only parameter lists, starting with '(' and ending with ')')
     *
     * @param signature the java bytecode method signature's parameter list in its parentheses (e.g. {@code (Ljava/lang/String;)})
     * @return the parameter list in java source form, also with parentheses (e.g. {@code (java.lang.String)})
     */
    @NotNull
    public static String convertSignature(@NotNull String signature) {
        StringBuilder sb = new StringBuilder("(");

        int pos = 1;
        boolean array = false;

        while (pos < signature.length()) {
            switch (signature.charAt(pos)) {
                case ')':
                    pos = signature.length();
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
                    int sPos = signature.indexOf(';', pos);
                    sb.append(signature, pos + 1, sPos);
                    pos = sPos;
                    break;
                default:
                    throw new IllegalStateException("Invalid character " + signature.charAt(pos) + " in method signature");
            }
            pos++;
            if (array) {
                sb.append("[]");
                array = false;
            }
            if (pos < signature.length()) sb.append(", ");
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
