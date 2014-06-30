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
 * Helper class for the integration testing of a ManifestVolume.
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class ManifestVolumeDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();
    
    /**
     * Collection of ManifestVolumes to be used for testing purposes.
     */
    private List<ManifestVolume> data;

    /**
     * Builds a transient {@link ManifestVolume} to be used for testing.
     * 
     * @return A properly formed {@link ManifestVolume} to be used for testing.
     * @param index Used so that each field set by this {@link ManifestVolume}
     *              is unique.
     */
    public ManifestVolume getNewTransientManifestVolume(int index) {
        String name = "name_" + index;
        ManifestDrive manifestDrive = new ManifestDrive();
        ManifestVolume obj = new ManifestVolume(name,
                new ArrayList<ManifestDirectory>(), manifestDrive);
        return obj;
    }

    /**
     * Grab a specific ManifestVolume instance from the database according to
     * its index in the collection.
     * 
     * @param index Index of the ManifestVolume instance that you want to grab
     *              from the database.
     * @return The ManifestVolume.
     */
    public ManifestVolume getSpecificManifestVolume(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));
        ManifestVolume volume = data.get(index);
        long id = volume.getPrimaryId();
        return ManifestVolume.findManifestVolume(id);
    }

    /**
     * Gets a random ManifestVolume from the database.
     * 
     * @return A random ManifestVolume from the database.
     */
    public ManifestVolume getRandomManifestVolume() {
        ManifestVolume volume = data.get(rnd.nextInt(data.size()));
        long id = volume.getPrimaryId();
        return ManifestVolume.findManifestVolume(id);
    }

    /**
     * Used to test merge and flush methods.
     * 
     * @param ManifestVolume
     * @return Whether a ManifestVolume persistence context has been modified.
     */
    // TODO Still need to do this... should be good enough for this story for
    // now though.
    public boolean modifyManifestVolume(ManifestVolume volume) {
        return false;
    }

    /**
     * Persists an initial amount of {@link ManifestVolume}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link ManifestVolumeIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  ManifestVolume classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of ManifestVolumes that
     *                            you want persisted into the database. 
     */
    public void init(int databaseSize) {
        data = ManifestVolume.findManifestVolumeEntries(0, databaseSize);
        if (data == null) {
            throw new IllegalStateException(
                    "Find entries implementation for 'ManifestVolume'"
                            + " illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ManifestVolume>();
        for (int i = 0; i < databaseSize; i++) {
            ManifestVolume volume = getNewTransientManifestVolume(i);
            try {
                volume.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            volume.flush();
            data.add(volume);
        }
    }
}