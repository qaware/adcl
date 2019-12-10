package util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /**
     * Reads a resource into a string.
     *
     * @param relativePathToResource the relative path to the resource
     * @return the resource as string
     * @throws IOException if the resource could not be found
     */
    public static String readResourceIntoString(String relativePathToResource) throws IOException {

        InputStream resourceFileAsStream = IOUtil.class.getClassLoader().getResourceAsStream(relativePathToResource);

        if (resourceFileAsStream == null) {
            throw new IOException("Resource at location: " + relativePathToResource + " not Found");
        }

        BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceFileAsStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int result = bufferedInputStream.read();
        while (result != -1) {
            byteArrayOutputStream.write((byte) result);
            result = bufferedInputStream.read();
        }
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
    }
}
