package gov.hawaii.digitalarchives.hida.core.model.record;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a RecordLanguage model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class RecordLanguageDataOnDemand {

    /**
     * Helper class used to test the RecordLanguage model class methods.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of RecordLanguages to be used for testing purposes.
     */
    private List<RecordLanguage> data;

    
    
    /** 
     * Builds a transient RecordLanguage to be used for testing.
     * 
     * @param index  Used so that each field set by this RecordLanguage is
     *               unique.
     * @return A properly formed RecordLanguage to be used for testing.
     */
    public RecordLanguage getNewTransientRecordLanguage(int index) {
        String language = "language_" + index;
        DigitalRecord digitalRecord = new DigitalRecord();
        String digitalRecordId = "digitalRecordId_" + index;
        digitalRecord.setPrimaryId(digitalRecordId);
        RecordLanguage obj = new RecordLanguage(language, digitalRecord);
        return obj;
    }

    
    
    /**
     * Grab a specific RecordLanguage instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the RecordLanguage instance that you 
     *                  want to grab from the database.
     * @return       The RecordLanguage.
     */
    public RecordLanguage getSpecificRecordLanguage(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        RecordLanguage obj = data.get(index);
        Long id = obj.getPrimaryId();
        return RecordLanguage.findRecordLanguage(id);
    }

    
    
    /**
     * Gets a random RecordLanguage from the database.
     * 
     * @return A random RecordLanguage from the database.
     */
    public RecordLanguage getRandomRecordLanguage() {
        
        RecordLanguage obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getPrimaryId();
        return RecordLanguage.findRecordLanguage(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj RecordLanguage object.
     * @return Whether a RecordLanguage persistence context 
     *         has been modified.
     */
    public boolean modifyRecordLanguage(RecordLanguage obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link RecordLanguage}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link RecordLanguageIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  RecordLanguage classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of RecordLanguages that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = RecordLanguage.findRecordLanguageEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation " +
                    "for 'RecordLanguage' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<RecordLanguage>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            RecordLanguage obj = getNewTransientRecordLanguage(i);
            try {
                obj.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            obj.flush();
            data.add(obj);
        }
    }
}
