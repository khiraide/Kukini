package gov.hawaii.digitalarchives.hida.core.model.accession;

import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a ManifestFile.
 * 
 * @author Keone Hiraide
 */
@Configurable
@Component
public class ManifestFileDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of ManifestFiles to be used for testing purposes.
     */
    private List<ManifestFile> data;
    
    /**
     * Builds a transient {@link ManifestFile} to be used for testing.
     * 
     * @return A properly formed {@link ManifestFile} to be used for testing.
     * @param index Used so that some fields set by this {@link ManifestFile} are
     *              unique.
     */
    public ManifestFile getNewTransientManifestFile(int index) {
        Date createdDate = new GregorianCalendar(Calendar.getInstance()
                .get(Calendar.YEAR), Calendar.getInstance()
                .get(Calendar.MONTH), Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH), Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY), Calendar.getInstance()
                .get(Calendar.MINUTE), Calendar.getInstance()
                .get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue())
                .getTime();
        Long size = new Integer(index).longValue(); 
        ManifestFile obj = new ManifestFile("name_" + index, createdDate, size,
                new ManifestDirectory());
        return obj;
    }

    /**
     * Grab a specific ManifestFile instance from the database according to its
     * index in the collection.
     * 
     * @param index Index of the ManifestFile instance that you want to grab 
     *              from the database.
     * @return The ManifestFile.
     */
    public ManifestFile getSpecificManifestFile(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));
        ManifestFile file = data.get(index);
        long id = file.getPrimaryId();
        return ManifestFile.findManifestFile(id);
    }

    /**
     * Gets a random ManifestFile from the database.
     * 
     * @return A random ManifestFile from the database.
     */
    public ManifestFile getRandomManifestFile() {
        ManifestFile file = data.get(rnd.nextInt(data.size()));
        long id = file.getPrimaryId();
        return ManifestFile.findManifestFile(id);
    }

    /**
     * Used to test merge and flush methods.
     * 
     * @param  file The ManifestFile in which you want check for
     *              modifications.
     * @return true if the ManifestFile passed in has been modified, or 
     *         false if it has not been modified.
     */
    // TODO Still need to do this... should be good enough for this story for
    // now though.
    public boolean modifyManifestFile(ManifestFile file) {
        return false;
    }

    /**
     * Persists an initial amount of {@link ManifestFile}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link ManifestFileIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  ManifestFile classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of ManifestFiles that
     *                            you want persisted into the database. 
     */
    public void init(int databaseSize) {
        data = ManifestFile.findManifestFileEntries(0, databaseSize);
        if (data == null) {
            throw new IllegalStateException(
                    "Find entries implementation for 'ManifestFile' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ManifestFile>();
        for (int i = 0; i < databaseSize; i++) {
            ManifestFile file = getNewTransientManifestFile(i);
            try {
                file.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            file.flush();
            data.add(file);
        }
    }
}
