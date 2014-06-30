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
 * Helper class for the integration testing of a ManifestDrive.
 * 
 * @author Keone Hiraide
 */
@Configurable
@Component
public class ManifestDriveDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of ManifestDrives to be used for testing purposes.
     */
    private List<ManifestDrive> data;
    
    /**
     * Builds a transient {@link ManifestDrive} to be used for testing.
     * 
     * @return A properly formed {@link ManifestDrive} to be used for testing.
     * @param index Used so that each field set by this {@link ManifestDrive} 
     *              is unique.
     */
    public ManifestDrive getNewTransientManifestDrive(int index) {
        String serialNumber = "serialNumber_" + index;
        Accession accession = new Accession("http://www.example.com/" + index);
        ManifestDrive obj = new ManifestDrive(serialNumber, new ArrayList<ManifestVolume>(),
                accession);
        return obj;
    }

    /**
     * Grab a specific ManifestDrive instance from the database according to its
     * index in the collection.
     * 
     * @param index Index of the ManifestDrive instance that you want to grab 
     *              from the database.
     * @return The ManifestDrive.
     */
    public ManifestDrive getSpecificManifestDrive(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));
        ManifestDrive manifestDrive = data.get(index);
        long id = manifestDrive.getPrimaryId();
        return ManifestDrive.findManifestDrive(id);
    }

    /**
     * Gets a random ManifestDrive from the database.
     * 
     * @return A random ManifestDrive from the database.
     */
    public ManifestDrive getRandomManifestDrive() {
        ManifestDrive manifestDrive = data.get(rnd.nextInt(data.size()));
        long id = manifestDrive.getPrimaryId();
        return ManifestDrive.findManifestDrive(id);
    }

    /**
     * Used to test merge and flush methods.
     * 
     * @param  manifestDrive The ManifestDrive in which you want check for
     *                       modifications.
     * @return true if the ManifestDrive passed in has been modified, or 
     *         false if it has not been modified.
     */
    // TODO Still need to do this... should be good enough for this story for
    // now though.
    public boolean modifyManifestDrive(ManifestDrive manifestDrive) {
        return false;
    }

    /**
     * Persists an initial amount of {@link ManifestDrive}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link ManifestDriveIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  ManifestDrive classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of ManifestDrives that
     *                            you want persisted into the database. 
     */
    public void init(int databaseSize) {
        data = ManifestDrive.findManifestDriveEntries(0, databaseSize);
        if (data == null) {
            throw new IllegalStateException(
                    "Find entries implementation for 'ManifestDrive'"
                            + " illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ManifestDrive>();
        for (int i = 0; i < databaseSize; i++) {
            ManifestDrive manifestDrive = getNewTransientManifestDrive(i);
            try {
                manifestDrive.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            manifestDrive.flush();
            data.add(manifestDrive);
        }
    }
}
