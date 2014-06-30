package gov.hawaii.digitalarchives.hida.core.model.accession;

import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a ManifestDirectory.
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class ManifestDirectoryDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of ManifestDirectory instances to be used for testing
     * purposes.
     */
    private List<ManifestDirectory> data;
    
    /**
     * Builds a transient {@link ManifestDirectory} to be used for testing.
     * 
     * @return A properly formed {@link ManifestDirectory} to be used for
     *         testing.
     * @param index Used so that each field set by this 
     *        {@link ManifestDirectory} is unique.
     */
    public ManifestDirectory getNewTransientManifestDirectory(int index) {
        List<ManifestFile> fileEntries = new ArrayList<ManifestFile>();
        ManifestVolume volume = new ManifestVolume("name_" + index,
                new ArrayList<ManifestDirectory>(), new ManifestDrive());
        ManifestDirectory obj = new ManifestDirectory("path", fileEntries, volume);
        return obj;
    }

    /**
     * Grab a specific ManifestDirectory instance from the database according 
     * to its index in the collection.
     * 
     * @param index Index of the ManifestDirectory instance that you want to 
     *              grab from the database.
     * @return The ManifestDirectory.
     */
    public ManifestDirectory getSpecificManifestDirectory(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));
        ManifestDirectory directory = data.get(index);
        long id = directory.getPrimaryId();
        return ManifestDirectory.findManifestDirectory(id);
    }

    /**
     * Gets a random ManifestDirectory from the database.
     * 
     * @return A random ManifestDirectory from the database.
     */
    public ManifestDirectory getRandomManifestDirectory() {
        ManifestDirectory directory = data.get(rnd.nextInt(data.size()));
        long id = directory.getPrimaryId();
        return ManifestDirectory.findManifestDirectory(id);
    }

    /**
     * Used to test merge and flush methods.
     * 
     * @param  directory The ManifestDirectory in which you want check for
     *                   modifications.
     * @return true if the ManifestDirectory passed in has been modified, or 
     *         false if it has not been modified.
     */
    // TODO Still need to do this... should be good enough for this story for
    // now though.
    public boolean modifyManifestDirectory(ManifestDirectory directory) {
        return false;
    }

    /**
     * Persists an initial amount of {@link ManifestDirectory}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link ManifestDirectoryIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  ManifestDirectory classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of ManifestDirectories that
     *                            you want persisted into the database. 
     */
    public void init(int databaseSize) {
        data = ManifestDirectory.findManifestDirectoryEntries(0, databaseSize);
        if (data == null) {
            throw new IllegalStateException(
                    "Find entries implementation for 'ManifestDirectory'"
                            + " illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ManifestDirectory>();
        for (int i = 0; i < databaseSize; i++) {
            ManifestDirectory directory = getNewTransientManifestDirectory(i);
            try {
                directory.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            directory.flush();
            data.add(directory);
        }
    }
}
