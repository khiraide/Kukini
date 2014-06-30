package gov.hawaii.digitalarchives.hida.bag;

import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class BagUtilImplTest extends AbstractTestNGSpringContextTests
{
    @Autowired
    private BagUtil bagUtil;
    private Path    testBag;      // Location of testbag.zip.
    private Path    invalidBag;   // Location of testbaginvalid.zip.
    private Path    outputPath;   // Output directory for test methods.
    private Path    testFilesPath;  // Location of testfiles directory
    private Path    testDirectory;  // Test directory
    private Path    testFileOne;  // Test file
    private Path    testFileTwo;  // Test file

    @BeforeMethod
    public void setup () throws IOException, URISyntaxException
    {
        Path root = Paths.get(this.getClass().getResource("/").toURI());
        testBag = Paths.get(this.getClass().getResource("/testbag.zip").toURI());
        testFilesPath = Paths.get(this.getClass().getResource("/testfiles").toURI());
        invalidBag = Paths.get(this.getClass().getResource("/testbaginvalid.zip").toURI());
        testDirectory = Files.createDirectories(root.resolve("test"));
        outputPath = Files.createDirectories(root.resolve("output"));
        testFileOne = Paths.get(this.getClass().getResource("/minuano.txt").toURI());
        testFileTwo = Paths.get(this.getClass().getResource("/desperado.txt").toURI());
    }

    @AfterMethod
    public void cleanup ()
    {
        FileUtil.deleteContents(testDirectory.toFile());
        FileUtil.deleteContents(outputPath.toFile());
    }

    /**
     * Tests to check if each bag invalidity is checked and caught.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void isValidTest () throws URISyntaxException, IOException
    {
        // Setup
        Path testBag = copy(this.testBag);
        Path testBagMissingFile = copy(Paths.get(this.getClass()
                .getResource("/testbagmissingfile.zip").toURI()));
        Path testBagWrongManifest = copy(Paths.get(this.getClass()
                .getResource("/testbagwrongmanifest.zip").toURI()));
        Path testBagWrongTags = copy(Paths.get(this.getClass().getResource("/testbagwrongtags.zip")
                .toURI()));
        Path invalidBag = copy(this.invalidBag);
        // Test valid bag
        Assert.assertTrue(bagUtil.isValid(testBag));
        // Test bag with missing file
        Assert.assertFalse(bagUtil.isValid(testBagMissingFile));
        // Test bag with wrong manifest
        Assert.assertFalse(bagUtil.isValid(testBagWrongManifest));
        // Test bag with wrong tags
        Assert.assertFalse(bagUtil.isValid(testBagWrongTags));
        // Test invalid bag
        Assert.assertFalse(bagUtil.isValid(invalidBag));
    }

    @Test
    public void isValidDeleteTest () throws IOException
    {
        // Test valid bag
        Path bag = copy(this.testBag);
        Assert.assertTrue(bagUtil.isValid(bag));
        Files.delete(bag);
        Assert.assertFalse(Files.exists(bag));
        // Test invalid bag
        Path invalidbag = copy(this.invalidBag);
        Assert.assertFalse(bagUtil.isValid(invalidbag));
        Files.delete(invalidbag);
    }
    
    @Test
    public void isValidMessages () throws IOException, URISyntaxException
    {
        // Test valid bag
        Path bag = copy(this.invalidBag);
        List<String> invalidBagList = new ArrayList<>();
        Assert.assertFalse(bagUtil.isValid(bag,invalidBagList));
        Assert.assertTrue("bagit.txt was invalid".equals(invalidBagList.get(0)));
        
        // Test missing file
        Path testBagMissingFile = copy(Paths.get(this.getClass()
                .getResource("/testbagmissingfile.zip").toURI()));
        List<String> missingFileList = new ArrayList<>();
        Assert.assertFalse(bagUtil.isValid(testBagMissingFile,missingFileList));
        Assert.assertTrue(missingFileList.get(0).contains("data/testfiles/monsoon.txt"));
    } 
    

    /**
     * Tests if moving a compressed bag is successful.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void moveCompressedBagTest () throws URISyntaxException, IOException
    {
        Path testbag = copy(Paths.get(this.getClass().getResource("/testbagmove.zip").toURI()));
        Path output = outputPath.resolve(testbag.getFileName());
        bagUtil.moveBag(testbag, outputPath);
        Assert.assertTrue(Files.exists(output));
        Assert.assertFalse(Files.exists(testbag));
        Assert.assertTrue(bagUtil.isValid(output));
    }

    /**
     * Tests if moving a uncompressed bag is successful.
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws HidaIOException
     */
    @Test
    public void moveUncompressedBagTest () throws URISyntaxException, HidaIOException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagmove").toURI()));
        Path output = outputPath.resolve(bag.getFileName());
        bagUtil.moveBag(bag, Files.createDirectory(output));
        Assert.assertTrue(Files.exists(output));
        Assert.assertFalse(Files.exists(bag));
        Assert.assertTrue(bagUtil.isValid(output));
    }

    /**
     * Tests if move bag method can recover when given the path to
     * itself.
     * 
     * @throws URISyntaxException
     */
    @Test(expectedExceptions = HidaIOException.class)
    public void failMoveBagTest () throws URISyntaxException
    {
        bagUtil.moveBag(testBag, testBag);
        Assert.assertTrue(Files.exists(testBag));
        Assert.assertTrue(bagUtil.isValid(testBag));
    }

    /**
     * Tests if move bag method can recover when given the path to
     * itself.
     * 
     * @throws URISyntaxException
     */
    @Test(expectedExceptions = HidaIOException.class)
    public void failMoveInvalidBagTest () throws URISyntaxException
    {
        bagUtil.moveBag(invalidBag, testDirectory.resolve(invalidBag));
        Assert.assertTrue(Files.exists(invalidBag));
        Assert.assertTrue(bagUtil.isValid(invalidBag));
    }

    /**
     * Tests if copy bag is successful.
     * 
     * @throws URISyntaxException
     */
    @Test
    public void copyBagTest () throws URISyntaxException
    {
        Path output = outputPath.resolve(testBag.getFileName());
        bagUtil.copyBag(testBag, outputPath);
        Assert.assertTrue(Files.exists(output));
        Assert.assertTrue(Files.exists(testBag));
        Assert.assertTrue(bagUtil.isValid(outputPath.resolve(testBag.getFileName())));
    }

    /**
     * Tests if copy bag method is able to recover when given the path to
     * itself.
     * 
     * @throws URISyntaxException
     */
    @Test(expectedExceptions = HidaIOException.class)
    public void failCopyBagTest () throws URISyntaxException
    {
        bagUtil.copyBag(testBag, testBag);
        Assert.assertTrue(Files.exists(testBag));
    }

    /**
     * Tests if bag creation is successful.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void createBagTestCompressed () throws URISyntaxException, IOException
    {
        Path output = testDirectory.resolve("compressedBag");
        Path outputzip = bagUtil.createBag(testFilesPath, output, true);
        Assert.assertTrue(Files.exists(outputzip));
        Assert.assertTrue(bagUtil.isValid(outputzip));
    }

    /**
     * Tests if bag creation is successful.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void createBagTestUncompressed () throws URISyntaxException, IOException
    {
        Path output = testDirectory.resolve("uncompressedBag");
        Path outputzip = bagUtil.createBag(testFilesPath, output);
        Assert.assertTrue(Files.exists(outputzip));
        Assert.assertTrue(bagUtil.isValid(outputzip.resolve("testfiles")));
    }

    /**
     * Tests if missing Bagit metadata is generated.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void makeCompleteBagTest () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagmakecomplete").toURI()));
        bagUtil.makeComplete(bag);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if the update bag was successful in adding a file. Fails if the
     * file was not found in the manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void updateManifestAddFileTest () throws URISyntaxException, IOException
    {
        // Update the manifest
        Path bag = copy(Paths.get(this.getClass().getResource("/testupdatebagaddfile").toURI()));
        bagUtil.updateAllManifests(bag);
        findManifestFile(bag, "manifest-md5.txt", "minuano.txt", true);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if the manifest file was updated after a file was removed. Fails
     * if the file is still in the manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void updateManifestRemoveFileTest () throws URISyntaxException, IOException
    {
        // Update the manifest
        Path bag = copy(Paths.get(this.getClass().getResource("/testupdatebagremovefile").toURI()));
        bagUtil.updateAllManifests(bag);
        findManifestFile(bag, "manifest-md5.txt", "mistral.txt", false);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a file was able to be added to the compressed bag. Fails if
     * the file is not found in the manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void addFileToCompressedBagTest () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagaddfile.zip").toURI()));
        Path file = copy(testFileOne);
        // Add a test file to the bag
        bagUtil.addDataToBag(bag, "", file);
        findManifestFile(bag, "manifest-md5.txt", testFileOne.getFileName().toString(), true);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a new directory is create in the compressed file.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void addFileToCompressedBagTestDestination () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagaddfile.zip").toURI()));
        Path file = copy(testFileTwo);
        // Add a test file to the bag
        bagUtil.addDataToBag(bag, "delaware", file);
        findManifestFile(bag, "manifest-md5.txt", testFileTwo.getFileName().toString(), true);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a file was able to be added to the uncompressed bag. Fails if
     * the file is not found in the manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void addFileToUncompressedBagTest () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testupdatebagaddfile").toURI()));
        Path file = copy(testFileTwo);
        // Add a test file to the bag
        bagUtil.addDataToBag(bag, "delaware", file);
        findManifestFile(bag, "manifest-md5.txt", testFileTwo.getFileName().toString(), true);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a file was removed from the compressed bag. Fails if the file
     * was found in the manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void removeFileFromCompressedBagTest () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagremovefile.zip").toURI()));
        Path file = Paths.get("testfiles/monsoon.txt");
        // Remove the file
        bagUtil.removeDataFromBag(bag, file);
        findManifestFile(bag, "manifest-md5.txt", "monsoon.txt", false);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a file was removed from the uncompressed bag. Fails if the
     * file was found in the manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void removeFileFromUnompressedBagTest () throws URISyntaxException, IOException
    {
        Path bag = Paths.get(this.getClass().getResource("/testupdatebagremovefile").toURI());
        Path file = Paths.get("testfiles/monsoon.txt");
        // Remove the file
        bagUtil.removeDataFromBag(bag, file);
        findManifestFile(bag, "manifest-md5.txt", "monsoon.txt", false);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a tag file was added. Fails if the file was not found in the
     * manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void addTagFileTest () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagaddmanifest.zip").toURI()));
        Path file = copy(testFileTwo);
        // Add the test manifest to the file
        bagUtil.addTagFilesToBag(bag, "testbag", file);
        findManifestFile(bag, "tagmanifest-md5.txt", testFileTwo.getFileName().toString(), true);
        Assert.assertTrue(bagUtil.isValid(bag));
    }

    /**
     * Tests if a tag file was removed. Fails if the file was not found in the
     * manifest.
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void removeTagFileTest () throws URISyntaxException, IOException
    {
        Path bag = copy(Paths.get(this.getClass().getResource("/testbagremovetag.zip").toURI()));
        Path file = Paths.get("/testbag/bagit.txt");
        // Add the test manifest to the file
        bagUtil.removeTagFilesFromBag(bag, file);
        findManifestFile(bag, "tagmanifest-md5.txt", "bagit.txt", false);
    }
    
    /**
     * Helper method for copying files to the test directory.
     * 
     * @param src
     * @param dest
     * @return
     * @throws IOException
     */
    private Path copy (final Path src) throws IOException
    {
        if (!Files.isDirectory(src))
        {
            return Files.copy(src, testDirectory.resolve(src.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        else
        {
            final Path output = Files.createDirectories(testDirectory.resolve(src.getFileName()));
            Files.walkFileTree(src, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs)
                        throws IOException
                {
                    Path targetdir = output.resolve(src.relativize(dir));
                    Files.copy(dir, targetdir, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile (Path file, BasicFileAttributes attrs)
                        throws IOException
                {
                    Files.copy(file, output.resolve(src.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });
            return output;
        }
    }

    /**
     * Helper method for finding the manifest files. Checks for files that have
     * been either added or removed. Fails if either conditions are not met.
     * 
     * @param bag The path to the bag.
     * @param manifestName The name of the manifest.
     * @param fileName The name of the file.
     * @param addingFile Whether a file is being added or removed.
     * @throws IOException
     */
    private void findManifestFile (Path bag, final String manifestName, final String fileName,
            final boolean addingFile) throws IOException
    {
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile (Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                if (addingFile)
                {
                    if (manifestName.equals(file.getFileName().toString()))
                    {
                        if (!findManifestFileEntry(fileName, file))
                        {
                            Assert.fail(fileName + " was not in the manifest.");
                        }
                        return FileVisitResult.TERMINATE;
                    }
                }
                else
                {
                    if (manifestName.equals(file.getFileName().toString()))
                    {
                        if (findManifestFileEntry(fileName, file))
                        {
                            Assert.fail(fileName + " was found in the manifest.");
                        }
                        return FileVisitResult.TERMINATE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        if (bag.toFile().isDirectory())
        {
            Files.walkFileTree(bag, visitor);
        }
        else
        {
            try (FileSystem fs = FileSystems.newFileSystem(bag, null))
            {
                final Path root = fs.getPath("/");
                Files.walkFileTree(root, visitor);
            }
        }
    }

    /**
     * Helper method for checking the manifest file for an entry.
     * 
     * @param fileName The name of the file.
     * @param manifest The location of the manifest.
     * @return True if file has been found, otherwise false is returned.
     * @throws IOException
     */
    private boolean findManifestFileEntry (String fileName, Path manifest) throws IOException
    {
        try (Scanner scanner = new Scanner(manifest))
        {
            while (scanner.hasNext())
            {
                if (scanner.nextLine().contains(fileName))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
