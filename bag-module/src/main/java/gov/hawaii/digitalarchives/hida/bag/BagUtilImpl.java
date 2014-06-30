package gov.hawaii.digitalarchives.hida.bag;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;
import gov.hawaii.digitalarchives.hida.core.util.ZipUtil;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.driver.CommandLineBagDriver;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.FailModeSupporting.FailMode;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Implementation of {@link BagUtil}. When writing to spaces, the code assumes
 * that the space will always honor the lease that was requested.
 * 
 * @author Calvin Wong
 */
@Component
public class BagUtilImpl implements BagUtil
{
    private Logger               log = null;
    /** Max amount of retries allowed. */
    private int                  maxRetries;
    /** Container for Bagit tool. */
    private CommandLineBagDriver driver;
    /** Factory for creating Bag objects. */
    private BagFactory           bagFactory;
    /** Contains options for completing bags. */
    private Completer            completer;
    /** Contains options for verifying bags */
    private ValidVerifierImpl    verifier;

    public BagUtilImpl ()
    {
        try
        {
            driver = new CommandLineBagDriver();
            bagFactory = new BagFactory();
            completer = new DefaultCompleter(bagFactory);
            CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
            // Checks for bagit.txt file
            completeVerifier.setMissingBagItTolerant(false);
            // Allow additional directories in bag root
            completeVerifier.setAdditionalDirectoriesInBagDirTolerant(true);
            // Ignore symbolic links
            completeVerifier.setIgnoreSymlinks(false);
            verifier = new ValidVerifierImpl(completeVerifier,
                    new ParallelManifestChecksumVerifier());
            verifier.setFailMode(FailMode.FAIL_SLOW);
        }
        catch (Exception e)
        {
            throw new HidaException("An error occurred while initializing Bagit", e);
        }
        this.maxRetries = 2;
    }

    @Override
    public void setMaxRetries (int maxRetries)
    {
        log.debug("Entering setMaxRetries(maxRetries={})", maxRetries);
        // Do nothing if maxRetries is 0 or less
        if (maxRetries > 0)
        {
            this.maxRetries = maxRetries;
        }
        log.debug("Exiting setMaxRetries()");
    }

    @Override
    public Path createBag (Path source, Path bag, boolean compress)
    {
        log.debug("Entering createBag(source={}, bag={}, compress={})", source, bag, compress);
        Path output = bag;
        try
        {
            // Create a bag in place.
            if (driver.execute(new String[] { "create",
                    bag.toAbsolutePath().toString() + "/" + source.getFileName(),
                    source.toAbsolutePath().toString() }) != 0)
            {
                String msg = "Could not create bag";
                log.error(msg);
                throw new HidaException(msg);
            }
        }
        // An error occurred while creating a bag.
        catch (Exception e)
        {
            String msg = "Exception was thrown from Bagit";
            log.error(msg, e);
            throw new HidaException(msg, e);
        }
        if (compress)
        {
            // Compress the file.
            output = ZipUtil.compress(bag);
        }
        // Exit the method
        log.debug("Exiting createBag(): {}", output);
        return output;
    }

    @Override
    public Path createBag (Path source, Path bag)
    {
        log.debug("Entering createBag(source={}, bag={})", source, bag);
        Path output = createBag(source, bag, false);
        log.debug("Exiting createBag(): {}", output);
        return output;
    }

    @Override
    public void makeComplete (Path bag)
    {
        log.debug("Entering makeComplete(bag={})", bag);
        bagFactory.createPreBag(bag.toFile()).makeBagInPlace(BagFactory.LATEST, true, false,
                completer);
        log.debug("Exiting makeComplete()");
    }

    @Override
    public boolean isValid (Path bag, List<String> messages)
    {
        log.debug("Entering isValid(bag={})", bag);
        Assert.notNull(bag);
        // Verify the bag
        try (Bag testBag = bagFactory.createBag(bag.toFile(), LoadOption.BY_MANIFESTS))
        {
            SimpleResult result = verifier.verify(testBag);
            if(messages != null) {
                messages.addAll(result.getMessages());
            }
            boolean success = result.isSuccess();
            log.debug("Exiting isValid(): {}", success);
            return success;
        }
        catch (RuntimeException e)
        {
            // Bagit failed to read bagit.txt from the bag.
            if(messages != null) {
                messages.add("bagit.txt was invalid");
            }
            log.debug("Exiting isValid(): false");
            return false;
        }
        catch (IOException e)
        {
            String msg = "Exception thrown while validating bag.";
            log.error(msg, e);
            throw new HidaIOException(msg, e);
        }
    }

    @Override
    public boolean isValid (Path bag)
    {
        return isValid(bag,null);
    }

    @Override
    public void moveBag (Path bag, Path destination) throws HidaIOException
    {
        log.debug("Entering moveBag(bag={}, destination={}", bag, destination);
        Assert.notNull(bag);
        Assert.notNull(destination);
        // Call the copyBag method to make a copy at the destination
        copyBag(bag, destination);
        // Delete the source bag.
        if (Files.exists(destination))
        {
            deleteBag_(bag);
        }
        // Bag move operation was successful.
        log.debug("Exiting moveBag()");
    }

    @Override
    public void copyBag (Path bag, Path destination) throws HidaIOException
    {
        log.debug("Entering copyBag(bag={}, destination={}", bag, destination);
        Assert.notNull(bag);
        Assert.notNull(destination);
        // Attempt copying until the max retries allowed
        Path output = destination;
        if (!Files.isDirectory(bag))
        {
            output = output.resolve(bag.getFileName());
        }
        int attempts = 0;
        do
        {
            attempts++;
            try
            {
                // Copy the file
                ZipUtil.copyFile(bag, destination);
                // Check if file exists at the destination
                if (!isValid(output))
                {
                    deleteBag_(output);
                }
                else
                {
                    log.debug("Exiting copyBag()");
                    return;
                }
            }
            catch (HidaIOException e)
            {
                log.error("Exception thrown while copying bag.", e);
                // Clean up destination
                deleteBag_(output);
            }
        }while (attempts < maxRetries);
        throw new HidaIOException("Could not copy bag to " + destination);
    }

    @Override
    public void addDataToBag (Path bag, String destinationPath, Path first, Path... rest)
    {
        log.debug("Entering addDataToBag(bag={},destinationPath={},first={},rest={})", bag,
                destinationPath, first, rest);
        addDataToBag(bag, destinationPath, true, first, rest);
        log.debug("Exiting addDataToBag()");
    }

    @Override
    public void addDataToBag (Path bag, String destinationPath, boolean updateManifest, Path first,
            Path... rest)
    {
        log.debug(
                "Entering addDataToBag(bag={},destinationPath={},updateManifest={},first={},rest={})",
                bag, destinationPath, updateManifest, first, rest);
        // Check if the bag is uncompressed
        if (Files.isDirectory(bag))
        {
            // Get the data path of the bag.
            Path destination = bag.resolve("data").resolve(destinationPath);
            // Add the files to the bag.
            ZipUtil.copyFile(first, destination);
            for (Path file : rest)
            {
                ZipUtil.copyFile(file, destination);
            }
        }
        else
        {
            // Get filesystem of the compressed bag
            try (FileSystem bagFs = FileSystems.newFileSystem(bag, null);
                    DirectoryStream<Path> ds = Files.newDirectoryStream(bagFs.getPath("/")))
            {
                // Get the data path of the bag.
                Path destination = ds.iterator().next().resolve("data").resolve(destinationPath);
                // Add the files to the bag
                ZipUtil.copyFile(first, destination);
                for (Path file : rest)
                {
                    ZipUtil.copyFile(file, destination);
                }
            }
            catch (IOException e)
            {
                String msg = "An IO exception was thrown while adding files to the bag.";
                log.error(msg, e);
                throw new HidaIOException(msg, e);
            }
        }
        if (updateManifest)
        {
            // Update the bag manifest
            updateAllManifests(bag);
        }
        log.debug("Exiting addDataToBag()");
    }

    @Override
    public void removeDataFromBag (Path bag, Path first, Path... rest)
    {
        log.debug("Entering removeDataFromBag(bag={},first={},rest={})", bag, first, rest);
        removeDataFromBag(bag, true, first, rest);
        log.debug("Exiting removeDataFromBag()");
    }

    @Override
    public void removeDataFromBag (Path bag, boolean updateManifest, Path first, Path... rest)
    {
        log.debug("Entering removeDataFromBag(bag={},updateManifest={},first={},rest={})", bag,
                updateManifest, first, rest);
        // Check if the bag is uncompressed.
        if (Files.isDirectory(bag))
        {
            Path dataPath = bag.resolve("data");
            // Delete the file from the bag
            deleteFile_(dataPath.resolve(first));
            for (Path file : rest)
            {
                // Delete the files from the bag
                deleteFile_(dataPath.resolve(file));
            }
        }
        else
        {
            // Get filesystem of the compressed bag
            try (FileSystem bagFs = FileSystems.newFileSystem(bag, null);
                    DirectoryStream<Path> ds = Files.newDirectoryStream(bagFs.getPath("/")))
            {
                Path dataPath = ds.iterator().next().resolve("data");
                // Delete the file from the bag
                deleteFile_(dataPath.resolve(first.toString()));
                for (Path file : rest)
                {
                    // Delete the files from the bag
                    deleteFile_(dataPath.resolve(file.toString()));
                }
            }
            catch (IOException e)
            {
                String msg = "An IO exception was thrown while removing files from the bag.";
                log.error(msg, e);
                throw new HidaIOException(msg, e);
            }
        }
        if (updateManifest)
        {
            // Update the bag manifest
            updateAllManifests(bag);
        }
        log.debug("Exiting removeDataFromBag()");
    }

    @Override
    public void addTagFilesToBag (Path bag, String destinationPath, Path first, Path... rest)
    {
        log.debug("Entering addTagFilesToBag(bag={},destinationPath={},first={},rest={})", bag,
                destinationPath, first, rest);
        addTagFilesToBag(bag, destinationPath, true, first, rest);
        log.debug("Exiting addTagFilesToBag():");
    }

    @Override
    public void addTagFilesToBag (Path bag, String destinationPath, boolean updateManifest,
            Path first, Path... rest)
    {
        log.debug(
                "Entering addTagFilesToBag(bag={},destinationPath={},updateManifest={},first={},rest={})",
                bag, destinationPath, updateManifest, first, rest);
        // Check if the bag is uncompressed.
        if (Files.isDirectory(bag))
        {
            Path destination = bag.resolve(destinationPath);
            // Add the files to the bag
            ZipUtil.copyFile(first, destination);
            for (Path file : rest)
            {
                ZipUtil.copyFile(file, destination);
            }
        }
        else
        {
            // Get the file system of the compressed bag
            try (FileSystem bagFs = FileSystems.newFileSystem(bag, null))
            {
                Path destination = bagFs.getPath("/").resolve(destinationPath);
                // Add the files to the bag
                ZipUtil.copyFile(first, destination);
                for (Path file : rest)
                {
                    ZipUtil.copyFile(file, destination);
                }
            }
            catch (IOException e)
            {
                String msg = "An IO exception was thrown while adding the manifest file to the bag.";
                log.error(msg, e);
                throw new HidaIOException(msg, e);
            }
        }
        if (updateManifest)
        {
            updateTagManifests(bag);
        }
        log.debug("Exiting addTagFilesToBag():");
    }

    @Override
    public void removeTagFilesFromBag (Path bag, Path first, Path... rest)
    {
        log.debug("Entering removeTagFilesToBag(bag={},first={},rest={})", bag, first, rest);
        removeTagFilesFromBag(bag, true, first, rest);
        log.debug("Exiting removeTagFilesToBag()");
    }

    @Override
    public void removeTagFilesFromBag (Path bag, boolean updateManifest, Path first, Path... rest)
    {
        log.debug("Entering removeTagFilesToBag(bag={},updateManifest={},first={},rest={})", bag,
                updateManifest, first, rest);
        if (Files.isDirectory(bag))
        {
            // Delete the tag file from the bag
            deleteFile_(bag.resolve(first));
            for (Path file : rest)
            {
                // Delete the tag files from the bag
                deleteFile_(bag.resolve(file));
            }
        }
        // Bag is uncompressed
        else
        {
            // Get the file system of the compressed bag
            try (FileSystem bagFs = FileSystems.newFileSystem(bag, null))
            {
                Path root = bagFs.getPath("/");
                // Delete the tag file from the bag
                deleteFile_(root.resolve(first.toString()));
                for (Path file : rest)
                {
                    // Delete the tag files from the bag
                    deleteFile_(root.resolve(file.toString()));
                }
            }
            catch (IOException e)
            {
                String msg = "An IO exception was thrown while removing tag files from the bag.";
                log.error(msg, e);
                throw new HidaIOException(msg, e);
            }
        }
        if (updateManifest)
        {
            updateTagManifests(bag);
        }
        log.debug("Exiting removeTagFilesToBag()");
    }

    @Override
    public void updateAllManifests (Path bag)
    {
        log.debug("Entering updateAllManifest(bag={}", bag);
        int returnValue = 0;
        // Check if the bag is compressed
        if (bag.toString().endsWith(".zip"))
        {
            // Update the bag manifest
            try
            {
                Path unzippedBag = ZipUtil.expand(bag);
                Files.delete(bag);
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(unzippedBag))
                {
                    returnValue = driver.execute(new String[] { "update",
                            ds.iterator().next().toString() });
                }
                ZipUtil.compress(unzippedBag);
                FileUtil.deleteContents(unzippedBag.toFile());
                Files.delete(unzippedBag);
            }
            catch (Exception e)
            {
                String msg = "An exception was thrown from Bagit.";
                log.error(msg, e);
                throw new HidaException(msg, e);
            }
        }
        // Bag is uncompressed
        else
        {
            // Update the bag manifest
            try
            {
                returnValue = driver.execute(new String[] { "update",
                        bag.toAbsolutePath().toString() });
            }
            catch (Exception e)
            {
                String msg = "An exception was thrown from Bagit.";
                log.error(msg, e);
                throw new HidaException(msg, e);
            }
        }
        if (returnValue != 0)
        {
            String msg = "Bagit was not able to update the manifests.";
            log.error(msg);
            throw new HidaException(msg);
        }
        log.debug("Exiting updateAllManifest()");
    }

    @Override
    public void updateDataManifests (Path bag)
    {
        log.debug("Entering updataDataManifests(bag={})", bag);
        updateAllManifests(bag);
        log.debug("Exiting updataDataManifests()");
    }

    @Override
    public void updateTagManifests (Path bag)
    {
        log.debug("Entering updateTagManifests(bag={})", bag);
        int returnValue = 0;
        // Check if the bag is compressed
        if (bag.toString().endsWith(".zip"))
        {
            // Update the bag manifest
            try
            {
                // Uncompress the bag
                Path unzippedBag = ZipUtil.expand(bag);
                // Delete the old bag
                Files.delete(bag);
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(unzippedBag))
                {
                    returnValue = driver.execute(new String[] { "updatetagmanifests",
                            ds.iterator().next().toString() });
                }
                ZipUtil.compress(unzippedBag);
                // Clean up unzipped bag
                FileUtil.deleteContents(unzippedBag.toFile());
                Files.deleteIfExists(unzippedBag);
            }
            catch (Exception e)
            {
                String msg = "An exception was thrown from Bagit.";
                log.error(msg, e);
                throw new HidaException(msg, e);
            }
        }
        // Bag is uncompressed
        else
        {
            // Update the bag manifest
            try
            {
                returnValue = driver.execute(new String[] { "updatetagmanifests",
                        bag.toAbsolutePath().toString() });
            }
            catch (Exception e)
            {
                String msg = "An exception was thrown from Bagit.";
                log.error(msg, e);
                throw new HidaException(msg, e);
            }
        }
        if (returnValue != 0)
        {
            String msg = "Bagit was not able to update the manifests.";
            log.error(msg);
            throw new HidaException(msg);
        }
        log.debug("Exiting updataTagManifests()");
    }

    /**
     * Helper method for deleting the bag at the input path. If files were not
     * deleted successfully, they will have to be manually removed by a user.
     * 
     * @param bag Bag to be cleaned up.
     */
    private void deleteBag_ (Path bag)
    {
        log.trace("Entering deleteBag_(bag={})", bag);
        // Check if bag exists
        if (Files.exists(bag))
        {
            try
            {
                FileUtils.forceDelete(bag.toFile());
            }
            // An error occurred while deleting the bag.
            catch (IOException e)
            {
                String msg = "Exception was thrown while deleting " + bag;
                log.error(msg, e);
                throw new HidaIOException(msg, e);
            }
        }
        log.trace("Exiting deleteBag_()");
    }

    /**
     * Helper method for deleting a file from a bag. Does not work on deleting
     * non-empty directories.
     * 
     * @param file File to delete
     */
    private void deleteFile_ (Path file)
    {
        log.trace("Entering deleteFiles_(file={})", file);
        try
        {
            Files.deleteIfExists(file);
        }
        catch (IOException e)
        {
            String msg = "An IO exception was thrown while removing the file from the bag: " + file;
            log.error(msg, e);
            throw new HidaIOException(msg, e);
        }
        log.trace("Exiting deleteFiles_()");
    }

    /**
     * Sets the logger for this class.
     * 
     * @param log The logger for this class.
     */
    @AutowiredLogger
    public void setLog (Logger log)
    {
        this.log = log;
    }
}
