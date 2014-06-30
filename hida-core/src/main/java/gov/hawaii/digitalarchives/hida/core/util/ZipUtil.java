package gov.hawaii.digitalarchives.hida.core.util;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;

/**
 * This is a utility class for handling the compression and expansion of
 * files.
 * 
 * @author Calvin Wong
 * @author Dongie Agnir
 */
public class ZipUtil
{
    private static final int BUFF_SIZE = 64 * 1024; // 64K buffer
    
    /** Checks whether or not a given file is zipped by looking at its extension.
     * This is a very rudimentary test and will not detected zipped files that
     * don't have the expected .zip extension.
     * 
     * @param path the {@link Path} to the file to check.
     * @return True if file is determined to be zipped, and false if file is
     *         determined not to be zipped, or if there was some other problem
     *         (file did not exist, path pointed to a directory, etc). */
    public static boolean isCompressed (Path path)
    {
        if (Files.isDirectory(path)) {
            return false;
        }
        return path.getFileName().toString().endsWith(".zip") ? true : false;
    }
    
    /**
     * Expands a zip file in place. Returns the path to the uncompressed file.
     * 
     * @param source The source file to uncompress.
     * @return The path to the uncompressed file.
     */
    public static Path expand (Path source)
    {
        Path output = source.getParent().resolve(
                FilenameUtils.getBaseName(source.getFileName().toString()));
        try (ZipFile zip = new ZipFile(source.toFile())) {
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                copyFromZip(zip, entries.nextElement(), output);
            }
        } catch (IOException e) {
            throw new HidaIOException("An exception was thrown while uncompressing", e);
        }
        return output;
    }

    /**
     * Compress the files to the destination.  The destination must be a file.
     * Does not replace existing files.  If destination is {@code null}, then
     * the name of the first file is used to name the new file.  It will be in
     * the same directory as the first file.  When adding files, the structure
     * leading up to that file is not preserved.  To preserve the directory
     * structure, it is recommended to build the structure that you want first
     * then send the root of that structure to the method.
     *
     * @param destination The destination to compress files to.
     * @param first The first file to compress.
     * @param rest The remaining files to compress.
     *
     * @return The path to the compressed file.
     */
    public static Path compress (Path destination, final Path first, final Path... rest) {
       if (destination == null) {
           Path firstParent = first.getParent();
           if (firstParent == null) {
               firstParent = Paths.get("");
           }

           destination = firstParent.resolve(FilenameUtils.getBaseName(first.getFileName()
                       .toString()) + ".zip");
       }

       try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(destination,
                       StandardOpenOption.CREATE_NEW), Charset.forName("UTF-8"))) {
           copyToZip(zipOut, first);
          for (Path file : rest) {
              copyToZip(zipOut, file);
          }
        } catch (IOException e) {
            throw new HidaIOException("Could not compress files to ZIP.", e);
        }

        return destination;
    }

    /**
     * Creates zip file in place.
     * 
     * @param file The file to compress.
     * @return The path to the compressed file.
     */
    public static Path compress (Path file)
    {
        return compress(null, file);
    }

        /**
     * Copies a file or directory to a destination. Destination must be a
     * directory.
     * 
     * @param file The file to be copied.
     * @param destination The destination directory for the file
     */
    public static void copyFile (final Path file, final Path destination)
    {
        try
        {
            // Check if the file is a directory
            if (!Files.isDirectory(file))
            {
                Files.createDirectories(destination);
                Files.copy(file, destination.resolve(file.getFileName().toString()));
            }
            else
            {
                // Walk the file tree and copy files to the destination
                Files.walkFileTree(file, new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult visitFile (Path f, BasicFileAttributes attrs)
                    {
                        try
                        {
                            Files.copy(f, destination.resolve(file.relativize(f).toString()));
                        }
                        catch (IOException e)
                        {
                            throw new HidaIOException("Exception was thrown while looking at " + f,
                                    e);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs)
                    {
                        try
                        {
                            Files.createDirectories(destination.resolve(file.relativize(dir)
                                    .toString()));
                        }
                        catch (IOException e)
                        {
                            throw new HidaIOException("Exception was thrown while looking at "
                                    + dir, e);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
        catch (IOException e)
        {
            throw new HidaIOException("Exception was throwing while compressing files", e);
        }
    }

    /**
     * Copy file(s) at the given {@code Path} to the Zip.  If this is a
     * directory, everything under the directory--including empty
     * directories--will be copied to the zip.
     *
     * @param zipOut The {@code ZipOutputStream} to write the files to.
     * @param path The path representing the file to copy into the Zip.
     *
     * @throws IOException Thrown if the given file could not be copied out to
     * the stream for any reason.
     */
    private static void copyToZip(final ZipOutputStream zipOut, final Path path) throws IOException
    {
        if (Files.isRegularFile(path)) {
            try (InputStream fileIn = Files.newInputStream(path)) {
                zipOut.putNextEntry(pathToZipEntry(path, false));
                copy(fileIn, zipOut);
                zipOut.closeEntry();
            }
        } else {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    try (InputStream fileIn = Files.newInputStream(file)) {
                        Path relative = path.relativize(file);
                        zipOut.putNextEntry(pathToZipEntry(relative, false));
                        copy(fileIn, zipOut);
                        zipOut.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                    Path relative = path.relativize(dir);
                    // Don't write an entry for empty Paths.
                    if (!relative.toString().equals("")) {
                        zipOut.putNextEntry(pathToZipEntry(relative, true));
                        zipOut.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Copy an entry from the Zip file out to the destination directory.
     *
     * @param zip The {@link ZipFile} to read the data from.
     * @param entry The {@link ZipEntry} that will the method will copy from
     * {@code zip} to {@code root}.
     * @param root The directory within which the contents of the Zip
     * will be written to.
     */
    private static void copyFromZip(final ZipFile zip, ZipEntry entry, final Path root)
        throws IOException {
        final InputStream entryIn = zip.getInputStream(entry);
        final Path filePath = root.resolve(entry.getName());
        if (entry.isDirectory()) {
            Files.createDirectories(filePath);
        } else {
            Files.createDirectories(filePath.getParent());
            try (OutputStream entryOut = Files.newOutputStream(filePath)) {
                copy(entryIn, entryOut);
            }
        }
    }

    /**
     * Copy the binary data from an {@link InputStream} out to the given
     * {@link OutputStream}.
     *
     * @param src The source {@code InputStream}.
     * @param dst The destination {@code OutputStream}.
     */
    private static void copy(InputStream src, OutputStream dst) throws IOException {
        byte buff[] = new byte[BUFF_SIZE];

        int read = 0;
        while ((read = src.read(buff)) > 0) {
            dst.write(buff, 0, read);
        }
    }

    /**
     * Utility method for creating suitable {@link ZipEntry}s from {@link
     * Path}s.
     *
     * @param p The {@code Path} from which the {@code ZipEntry}s name will be
     * created from.
     * @param isDirectory Flag to indicate whether the {@link Path} given
     * points to a directory.
     *
     * @return The {@code ZipEntry} representing the given {@code Path}.
     */
    private static ZipEntry pathToZipEntry(Path p, boolean isDirectory) {
        String separator = p.getFileSystem().getSeparator();
        String entryName = p.toString().replace(separator, "/");

        if (entryName.equals("")) {
            throw new HidaException("Cannot create ZipEntry for empty path.");
        }

        if (isDirectory && entryName.charAt(entryName.length() - 1) != '/') {
                entryName += "/";
        }

        return new ZipEntry(entryName);
    }
}
