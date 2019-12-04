package util;

/**
 * Contains static methods that help extracting necessary information from conical class and method names
 */
public class StringNameUtil {
    /**
     * Should not be initialized
     */
    private StringNameUtil() {
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

    public static String cutOffParamaterList(String behaviorName) {
        int startOfParameterList = behaviorName.lastIndexOf('(');
        if (startOfParameterList < 0) {
            return behaviorName;
        }
        return behaviorName.substring(0, startOfParameterList);
    }
}
