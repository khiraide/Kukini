package gov.hawaii.digitalarchives.hida.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests for the {@link ZipUtil} class.
 * 
 * @author Calvin Wong
 * @author Micah Takabayshi
 * @author Dongie Agnir
 * */
public class ZipTest
{
    
    private final Path TEMP_DIR = Paths.get("zipTestDir");
    
    Path     testZipExpand, testZipCompress, outputFolder, ppXml, rtpXml, testRecord;

    String[] files = new String[] { "monsoon.txt", "sundowner.txt", "mistral.txt" };

    /** Copy all test files into a temporary directory for easy cleanup.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @BeforeMethod
    public void setup () throws URISyntaxException, IOException
    {
        Files.createDirectories(TEMP_DIR);
        
        testZipExpand = Paths.get(this.getClass().getResource("/testZipExpand.zip").toURI());
        testZipExpand = Files.copy(testZipExpand, TEMP_DIR.resolve(testZipExpand.getFileName()));
        
        testZipCompress = Paths.get(this.getClass().getResource("/testZipCompress").toURI());
        Path testZipDestPath = TEMP_DIR.resolve(testZipCompress.getFileName());
        FileUtils.copyDirectory(testZipCompress.toFile(), testZipDestPath.toFile());
        testZipCompress = testZipDestPath;
        
        ppXml = Paths.get(this.getClass().getResource("/PP_XML.xml").toURI());
        ppXml = Files.copy(ppXml, TEMP_DIR.resolve(ppXml.getFileName()));
        
        rtpXml = Paths.get(this.getClass().getResource("/RTP_XML.xml").toURI());
        rtpXml = Files.copy(rtpXml, TEMP_DIR.resolve(rtpXml.getFileName()));
        
        testRecord = Paths.get(this.getClass().getResource("/testrecord.txt").toURI());
        testRecord = Files.copy(testRecord, TEMP_DIR.resolve(testRecord.getFileName()));
        
        outputFolder = Files.createDirectories(TEMP_DIR.resolve("output"));
    }
    
    /** Deletes temp files after each test.
     * 
     * @throws IOException */
    @AfterMethod
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(TEMP_DIR.toFile());
    }
    
    /** Tests for {@link ZipUtil#isCompressed}. */
    @Test
    public void testIsCompressed() {
        Assert.assertTrue(ZipUtil.isCompressed(testZipExpand));
        Assert.assertFalse(ZipUtil.isCompressed(testZipCompress));
        Assert.assertFalse(ZipUtil.isCompressed(testRecord));
        Assert.assertFalse(ZipUtil.isCompressed(Paths.get("SomeBogusPath")));
    }

    /**
     * Tests if unzip bag is successful.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testExpand () throws URISyntaxException, IOException
    {
        Path output = ZipUtil.expand(testZipExpand);
        Assert.assertTrue(Files.exists(output));
        for (String file : files)
        {
            Assert.assertTrue(findFile(output, Paths.get(file)));
        }
    }

    /**
     * Test compress method for single file.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void compressTestFile () throws IOException, URISyntaxException
    {
        Path output = ZipUtil.compress(outputFolder.resolve("testZipFile.zip"), this.testRecord);
        Assert.assertTrue(Files.exists(output));
        try (FileSystem fs = FileSystems.newFileSystem(output, null))
        {
            Assert.assertTrue(findFile(fs.getPath("/"), Paths.get("testrecord.txt")));
        }
    }

    /**
     * Test compress method for directories.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void compressTestDirectory () throws IOException
    {
        Path output = ZipUtil.compress(outputFolder.resolve("testZipCompress.zip"),
        this.testZipCompress);
        Assert.assertTrue(Files.exists(output));
        try (FileSystem fs = FileSystems.newFileSystem(output, null))
        {
            for (String file : files)
            {
                Assert.assertTrue(findFile(fs.getPath("/"), Paths.get(file)));
            }
        }
    }

    /**
     * Test creating directories in compressed files.
     * 
     * @throws IOException
     */
    @Test
    public void addDirectoryToCompressedFileTest () throws IOException
    {
        try (FileSystem fs = FileSystems.newFileSystem(testZipExpand, null))
        {
            ZipUtil.copyFile(this.testRecord, fs.getPath("/some/test/directory"));
            Assert.assertTrue(Files.exists(fs.getPath("/some/test/directory/testrecord.txt")));
        }
    }

    /**
     * Regression test for HIDA-298.
     * <p>
     * On some machines with limited memory, {@link ZipUtil} is unable to write
     * large files.  The bug is also connected with the inability to write
     * files larger than 2GB into the ZIP (regardless of amount of host
     * memory.)  See issue in JIRA for complete details.
     * <p>
     * Note: This test requires at least 4GB of free disk space!
     */
    //@Test
    public void testCompressLargeFile() throws IOException {
        Path testDir = TEMP_DIR.resolve("TestCompressLargeFile");
        Files.createDirectory(testDir);
        Path largeFile = testDir.resolve("2gb.dat");

        try (OutputStream out = Files.newOutputStream(largeFile)) {
            // Integer.MAX_VALUE is 2^31 - 1, or 2GB - 1
            writeRandomData(out, Integer.MAX_VALUE);
        } catch (IOException e) {
            fail("Test failed to generate the test file.  Not enough disk space?");
        }

        try {
            // The file we created is 2GB in size.  If the implementation were
            // causing the entire file to be cached in memory, this would fail
            // because it exceeds the JVM's maximum array size
            // (Integer.MAX_VALUE - 5).
            ZipUtil.compress(testDir);
        } catch (OutOfMemoryError e) {
            fail("Ran out of memory while attempting to write 2GB file into the ZIP.");
        }
    }

    /**
     * Test that empty directories are still created within the Zip rather than
     * being ignored.
     */
    @Test
    public void testCompressWithEmptyDirectories() throws IOException {
        Path testDir = TEMP_DIR.resolve("testCompressWithEmptyDirectories");
        Path emptyDir = testDir.resolve("empty");
        Files.createDirectories(emptyDir);

        Path compressed = ZipUtil.compress(testDir);

        try (FileSystem fs = FileSystems.newFileSystem(compressed, null)) {
            Path empty = fs.getPath("empty");
            Assert.assertTrue(Files.exists(empty));
        }
    }


    private boolean findFile (Path filesystem, final Path file) throws IOException
    {
        final Map<String, Boolean> map = new HashMap<>();
        Files.walkFileTree(filesystem, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile (Path f, BasicFileAttributes attrs)
            {
                if (file.toString().contains(f.getFileName().toString()))
                {
                    map.put("return", Boolean.TRUE);
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return map.get("return");
    }

    /**
     * Utility function to write a random stream of data with the given size.
     * This is useful for writing data out to a {@link ZipOutputStream} because
     * it essentially defeats the compression.  This allows for easier
     * verification that data isn't being cached in memory (e.g. for HIDA-298).
     *
     * @param out The {@code OutputStream} to write the random data to.
     * @param size The size of the data in bytes.
     *
     * @return The number of bytes written.
     */
    private int writeRandomData(OutputStream out, int size) throws IOException {
        final int chunkSize = 64 * 1024;
        byte buff[] = new byte[chunkSize];
        Random rng = new Random();

        int bytesWritten = 0;
        int bytesLeft = size;
        while (bytesLeft != 0) {
            rng.nextBytes(buff);

            int bytesToWrite = Math.min(bytesLeft, chunkSize);
            out.write(buff, 0, bytesToWrite);
            bytesWritten += bytesToWrite;

            bytesLeft = Math.max(0, bytesLeft - bytesToWrite);
        }

        return bytesWritten;
    }
}
