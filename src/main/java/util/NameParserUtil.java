package util;

/**
 * Contains static methods that help extracting necessary information from conical class and method names
 */
public class NameParserUtil {
    /**
     * Should not be initialized
     */
    private NameParserUtil() {
    }

    /**
     * Extracts the package name from a complete class name.
     *
     * @param completeClassName a complete class name
     * @return package name
     */
    public static String extractPackageName(String completeClassName) {
        int startOfClassName = completeClassName.lastIndexOf('.');
        //in case of default
        if (startOfClassName == -1) {
            return "default";
        }
        return completeClassName.substring(0, startOfClassName);
    }

    /**
     * Extracts the class name from a complete method name.
     *
     * @param completeMethodName a complete method name
     * @return class name
     */
    public static String extractClassName(String completeMethodName) {
        int startOfMethodName = completeMethodName.lastIndexOf('.');
        return completeMethodName.substring(0, startOfMethodName);
    }

    /**
     * Extracts the the simple method name from a complete method name.
     *
     * @param completeMethodName a complete method name
     * @return simple method name
     */
    public static String extractMethodName(String completeMethodName) {
        int startOfMethodName = cutOffParamaterList(completeMethodName).lastIndexOf('.');
        return completeMethodName.substring(startOfMethodName + 1);
    }

    /**
     * Extracts the simple class name from a complete class name.
     *
     * @param completeClassName a complete class name
     * @return a simple class name without package name
     */
    public static String extractSimpleClassNameFromCompleteClassName(String completeClassName) {
        int startOfSimpleClassName = completeClassName.lastIndexOf('.');
        return completeClassName.substring(startOfSimpleClassName + 1);
    }

    /**
     * Cuts of the the parameter list of a complete method signature
     *
     * @param methodSignature the complete
     * @return the complete method name without the parameter list
     */
    public static String cutOffParamaterList(String methodSignature) {
        int startOfParameterList = methodSignature.lastIndexOf('(');
        if (startOfParameterList < 0) {
            return methodSignature;
        }
        return methodSignature.substring(0, startOfParameterList);
    }
}
