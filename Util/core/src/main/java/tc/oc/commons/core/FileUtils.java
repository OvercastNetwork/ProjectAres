package tc.oc.commons.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.io.Files;
import tc.oc.commons.core.util.ExceptionUtils;

import static com.google.common.base.Preconditions.checkArgument;

/** Source: http://www.crazysquirrel.com/computing/java/basics/java-file-and-directory-copying.jspx */
public class FileUtils {
    public static void copy(File source, File destination) throws IOException {
        copy(source, destination, false);
    }

    public static void copy(File source, File destination, boolean force) throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("Missing source " + source.getPath());
        }

        if (!force && destination.exists()) {
            throw new FileNotFoundException("Missing destination " + destination.getPath());
        }

        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private static void copyDirectory(File source, File destination) throws IOException {
        if (!destination.mkdirs()) {
            throw new IOException("Failed to create destination directories");
        }

        File[] files = source.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                copyDirectory(file, new File(destination, file.getName()));
            } else {
                copyFile(file, new File(destination, file.getName()));
            }
        }
    }

    private static void copyFile(File source, File destination) throws IOException {
        Files.copy(source, destination);
    }

    public static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        f.delete();
    }

    public static boolean isHidden(File file) {
        return file.isHidden() || file.getName().startsWith(".");
    }

    /**
     * You MUST call {@link Stream#close()} when you are finished with this stream!
     */
    public static Stream<Path> directoryStream(Path dir) throws IOException {
        final DirectoryStream<Path> dirStream = java.nio.file.Files.newDirectoryStream(dir);
        return StreamSupport.stream(dirStream.spliterator(), false)
                            .onClose(() -> ExceptionUtils.propagate(dirStream::close));
    }

    /**
     * Append the given extension string to the last element of the given path
     */
    public static Path appendExtension(Path path, String extension) {
        checkArgument(path.getNameCount() > 0);
        return path.getParent().resolve(path.getFileName().toString() + extension);
    }
}
