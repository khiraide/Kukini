package gov.hawaii.digitalarchives.hida.core.model.record;
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
 * Helper class for the integration testing of an DigitalRecord. 
 * 
 * @author Keone Hiraide
 */
@Configurable
@Component
public class DigitalRecordDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of DigitalRecords to be used for testing purposes.
     */
    private List<DigitalRecord> data;

    
    
    /** 
     * Builds a transient DigitalRecord to be used for testing.
     * 
     * @return A properly formed DigitalRecord to be used for testing.
     */
    public DigitalRecord getNewTransientDigitalRecord(int index) {
        DigitalRecord obj = new DigitalRecord();
        setPrimaryId(obj, index);
        setAccessionId(obj, index);
        setAgencyRecordId(obj, index);
        setCreatedDate(obj);
        setCreator(obj, index);
        setPreserver(obj, index);
        setTitle(obj, index);
        return obj;
    }

    
    
    /**
     * Sets a unique {@link DigitalRecordId} into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link DigitalRecord} field
     *               set by this {@link DigitalRecord} is unique.
     */
    public void setPrimaryId(DigitalRecord obj, int index) {
        String digitalRecordId = "http://www.example.com/" + index;
        obj.setPrimaryId(digitalRecordId);
    }
    
    
    
    /**
     * Sets a unique {@link AccessionId} into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link DigitalRecord} field
     *               set by this {@link DigitalRecord} is unique.
     */
    public void setAccessionId(DigitalRecord obj, int index) {
        String accessionId = "http://www.example.com/" + index;
        obj.setAccessionId(accessionId);
    }

    
    
    /**
     * Sets a unique agencyRecordId of type String into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link DigitalRecord} field
     *               set by this {@link DigitalRecord} is unique.
     */
    public void setAgencyRecordId(DigitalRecord obj, int index) {
        String agencyRecordId = "agencyRecordId_" + index;
        obj.setAgencyRecordId(agencyRecordId);
    }

    
    
    /**
     * Sets a unique instance of a createdDate of type {@link java.util.Date} into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance whose fields we want to
     *             set for testing purposes.
     */
    public void setCreatedDate(DigitalRecord obj) {
        Date createdDate = new GregorianCalendar(Calendar.getInstance()
                .get(Calendar.YEAR), Calendar.getInstance()
                .get(Calendar.MONTH), Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH), Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY), Calendar.getInstance()
                .get(Calendar.MINUTE), Calendar.getInstance()
                .get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue())
                .getTime();
        obj.setCreatedDate(createdDate);
    }

    
    
    /**
     * Sets a unique creator of type {@link Agent} into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link DigitalRecord} field
     *               set by this {@link DigitalRecord} is unique.
     */
    public void setCreator(DigitalRecord obj, int index) {
        Agent creator = null;
        obj.setCreator(creator);
    }

    
    
    /**
     * Sets a unique preserver of type {@link Agent} into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link DigitalRecord} field
     *               set by this {@link DigitalRecord} is unique.
     */
    public void setPreserver(DigitalRecord obj, int index) {
        Agent preserver = null;
        obj.setPreserver(preserver);
    }

    
    
    /**
     * Sets a unique title of type String into this 
     * {@link DigitalRecord}.
     * 
     * @param obj  The {@link DigitalRecord} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link DigitalRecord} field
     *               set by this {@link DigitalRecord} is unique.
     */
    public void setTitle(DigitalRecord obj, int index) {
        String title = "title_" + index;
        obj.setTitle(title);
    }

    
    
    /**
     * Grab a specific DigitalRecord instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the DigitalRecord instance that you 
     *                  want to grab from the database.
     * @return       The DigitalRecord.
     */
    public DigitalRecord getSpecificDigitalRecord(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        DigitalRecord obj = data.get(index);
        String id = obj.getPrimaryId().toString();
        return DigitalRecord.findDigitalRecord(id);
    }

    
    
    /**
     * Gets a random DigitalRecord from the database.
     * 
     * @return A random DigitalRecord from the database.
     */
    public DigitalRecord getRandomDigitalRecord() {
        DigitalRecord obj = data.get(rnd.nextInt(data.size()));
        String id = obj.getPrimaryId().toString();
        return DigitalRecord.findDigitalRecord(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj DigitalRecord object.
     * @return Whether a DigitalRecord persistence context 
     *         has been modified.
     */
    //TODO Still need to do this... should be good enough for this story for now though.
    public boolean modifyDigitalRecord(DigitalRecord obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link DigitalRecord}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link DigitalRecordIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  DigitalRecord classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of DigitalRecorda that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = DigitalRecord.findDigitalRecordEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for" 
                    + " 'DigitalRecord' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<DigitalRecord>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            DigitalRecord obj = getNewTransientDigitalRecord(i);
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
