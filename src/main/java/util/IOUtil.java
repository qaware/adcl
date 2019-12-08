package util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class that contains methods commonly used in tests.
 */
public class IOUtil {

    /**
     * Should not be initialized.
     */
    private IOUtil() {
    }

    /**
     * Read the file on the given path and returns it as a string.
     * Uses Charset UTF-8
     *
     * @param path the path to the file
     * @return the files content as string
     * @throws IOException throwm if the path is invalid
     */
    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Read the file on the given path and returns it as a string.
     *
     * @param path     the path to the file
     * @param encoding the encoding of the file
     * @return the files content as string
     * @throws IOException throwm if the path is invalid
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
