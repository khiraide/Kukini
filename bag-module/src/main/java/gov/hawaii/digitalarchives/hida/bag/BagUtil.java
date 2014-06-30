package gov.hawaii.digitalarchives.hida.bag;

import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;

import java.nio.file.Path;
import java.util.List;

/**
 * Bag utility module. Provides a set of helper methods for
 * manipulating bag files.
 * 
 * This will be instantiated in a Spring context as a singleton-scoped bean so
 * the methods are intended to not be static.
 * 
 * NOTE: Updating the manifests of compressed bags is more expensive than
 * uncompressed bags.
 * 
 * @author Calvin Wong
 */
public interface BagUtil
{
    /**
     * Configures the bag module with the input max retries for copying and
     * moving bags. The default retries is 2. The max retries must be an int
     * and at least 0 or greater. Note that *re*tries is additional attempts
     * after the first attempt has failed.
     * 
     * @param maxRetries An int value of the number of times to retry bag
     *            operations before returning an error.
     */
    void setMaxRetries (int maxRetries);

    /**
     * Creates a new Bag structure from a directory of files. Generates a
     * manifest with fixity values. The source and bag path must be a
     * directory.
     * 
     * Bag path must be an empty directory.
     * 
     * If compressed is true, then the bag is compressed. Else the bag is not
     * compressed.
     * 
     * @param source The path to the directory to be bagged.
     * @param bag The destination path to create the bag at.
     * @param compress Whether to compress the bag or not.
     * @return The path to the bagged directory.
     */
    Path createBag (Path source, Path bag, boolean compress);

    /**
     * Convenience method for created a bag in place. Defaults to not
     * compressing the bag.
     * 
     * Bag path must be an empty directory.
     * 
     * @param source The path to the directory to be bagged.
     * @param bag The destination path to create the bag at.
     * @return The path to the bagged directory.
     */
    Path createBag (Path source, Path bag);

    /**
     * Given a partially created bag with a data folder and tag files, fills in
     * the remaining components to make it bag valid. If the manifests,
     * bag-info.txt, or bagit.txt are missing, then they are created.
     * 
     * @param bag The path to the partially created bag to complete.
     */
    void makeComplete (Path bag);

    /**
     * Checks that the manifest of the bag exists and is well formed. The bag’s
     * contents are checked against the bag’s manifest for the existence of all
     * of the files in the manifest, and that the checksum of each file matches
     * the checksum given in the manifest.
     * 
     * @param bag The filepath to the bag to be checked.
     * @return True if bag is well formed and contents match the manifest.
     *         Otherwise returns false.
     */
    boolean isValid (Path bag);

    /**
     * Checks that the manifest of the bag exists and is well formed. The bag’s
     * contents are checked against the bag’s manifest for the existence of all
     * of the files in the manifest, and that the checksum of each file matches
     * the checksum given in the manifest.
     * 
     * The list being passed in must be mutable as isValid will add string
     * messages to it.
     * 
     * @param bag The filepath to the bag to be checked.
     * @param messages Reference to list object to add messages to.
     * @return True if bag is well formed and contents match the manifest.
     *         Otherwise returns false.
     */
    boolean isValid (Path bag, List<String> messages);

    /**
     * Copies a Bag from one location to another location and fixity checks the
     * copy. If there is a fixity error it will retry the copy until it succeeds
     * or the retry count is exceeded. If the copy was successful, it deletes
     * the original.
     * 
     * Moves files by copying them to their destination and deleting the source
     * after confirming the fixity values of the copied bag.
     * 
     * @param bag The bag to be copied.
     * @param destination The location to copy the Bag to.
     * @throws HidaIOException
     */
    void moveBag (Path bag, Path destination) throws HidaIOException;

    /**
     * Copies a Bag from one location to another location with fixity checking
     * of the copy. If there is a fixity error it will retry the copy until it
     * succeeds or the retry count is exceeded.
     * 
     * The destination for uncompressed bags should be in an empty directory or
     * the input path should be the root of the bag.
     * 
     * @param bag The bag to be moved
     * @param destination The location to move the Bag to
     * @throws HidaIOException
     */
    void copyBag (Path bag, Path destination) throws HidaIOException;

    /**
     * Adds files to the bag at the path in the bag directory and updates the
     * manifest. The files will be added relative to the data directory. The
     * destination path is relative from the data directory. Updates the
     * directory by default.
     * 
     * @param bag The bag to add the files to.
     * @param destinationPath The path relative to the bag root.
     * @param first The file to add to the bag.
     * @param rest The files to add to the bag.
     */
    void addDataToBag (Path bag, String destinationPath, Path first, Path... rest);

    /**
     * Adds files to the bag at the path in the bag directory. If updateManifest
     * is set to false, the manifest does not get updated, else the manifest
     * gets updated when the files are finished being added. The files will be
     * added relative to the data directory. The destination path is relative
     * from the data directory.
     * 
     * @param bag The bag to add the files to.
     * @param destinationPath The path relative to the bag root.
     * @param updateManifest Determine whether or not the manifest gets updated.
     * @param first The file to add to the bag.
     * @param rest The files to add to the bag.
     */
    void addDataToBag (Path bag, String destinationPath, boolean updateManifest, Path first,
            Path... rest);

    /**
     * Removes files from the bag at the path in the bag directory and updates
     * the manifest. The files being removed are located relative from the data
     * directory.
     * 
     * The same as removeDataFromBag(bag,true,files).
     * 
     * This method does not perform any file walking to look for the file to
     * delete.
     * 
     * @param bag The bag to remove the files from.
     * @param first The file to remove from the bag.
     * @param rest The files to remove from the bag.
     */
    void removeDataFromBag (Path bag, Path first, Path... rest);

    /**
     * Removes files from the bag at the path in the bag directory. If
     * updateManifest is set to false, the manifest does not get updated, else
     * the manifest gets updated when the files are finished being removed.The
     * files being removed are relative from the data directory.
     * 
     * This method does not perform any file walking to look for the file to
     * delete.
     * 
     * @param bag The bag to remove the files from.
     * @param updateManifest Determine whether or not the manifest gets updated.
     * @param first The file to remove from the bag.
     * @param rest The files to remove from the bag.
     */
    void removeDataFromBag (Path bag, boolean updateManifest, Path first, Path... rest);

    /**
     * Add new tag files to the bag. The tag files are added relative to the bag
     * root. Updates the directory by default.
     * 
     * @param bag The bag to add the tag files to.
     * @param destinationPath The destination directory in the bag relative to
     *            the root
     * @param first The tag file to add to the bag.
     * @param rest The tag files to add to the bag.
     */
    void addTagFilesToBag (Path bag, String destinationPath, Path first, Path... rest);

    /**
     * Add new tag files to the bag. The tag files are added relative to the bag
     * root.
     * 
     * @param bag The bag to add the tag files to.
     * @param destinationPath The destination directory in the bag relative to
     *            the root
     * @param updateManifest Determine whether the tag manifest gets updated.
     * @param first The tag file to add to the bag.
     * @param rest The tag files to add to the bag.
     */
    void addTagFilesToBag (Path bag, String destinationPath, boolean updateManifest, Path first,
            Path... rest);

    /**
     * Remove a tag file from the bag. The tag files being removed are in
     * directories relative to the bag root.
     * 
     * This method does not perform any file walking to look for the file to
     * delete.
     * 
     * @param bag The bag to remove the tag files from.
     * @param updateManifest Determine whether the tag manifest gets updated.
     * @param first The tag file to remove from the bag.
     * @param rest TThe tags file to remove from the bag.
     */
    void removeTagFilesFromBag (Path bag, boolean updateManifest, Path first, Path... rest);

    /**
     * Remove a tag file from the bag. The tag files being removed are in
     * directories relative to the bag root. Defaults to updating the manifest.
     * 
     * This method does not perform any file walking to look for the file to
     * delete.
     * 
     * @param bag The bag to remove the tag files from.
     * @param first The tag file to remove from the bag.
     * @param rest TThe tags file to remove from the bag.
     */
    void removeTagFilesFromBag (Path bag, Path first, Path... rest);

    /**
     * Updates all the manifests of the bag.
     * 
     * @param bag The bag to update the manifests for.
     */
    void updateAllManifests (Path bag);

    /**
     * Update the data manifests of the bag.
     * 
     * @param bag The bag to update the manifests for.
     */
    void updateDataManifests (Path bag);

    /**
     * Update the tag manifests of the bag.
     * 
     * @param bag The bag to update the manifests for.
     */
    void updateTagManifests (Path bag);
}
