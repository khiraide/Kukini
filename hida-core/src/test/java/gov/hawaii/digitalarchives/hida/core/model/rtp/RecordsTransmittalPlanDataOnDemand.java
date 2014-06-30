package gov.hawaii.digitalarchives.hida.core.model.rtp;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;


/**
 * Helper class for the integration testing of a RecordsTransmittalPlan. 
 * 
 * @author Keone Hiraide
 */
@Configurable
@Component
public class RecordsTransmittalPlanDataOnDemand {

    /**
     * Collection of RecordsTransmittalPlans to be used for testing purposes.
     */
    private List<RecordsTransmittalPlan> recordsTransmittalPlans;

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /** 
     * Builds a RecordsTransmittalPlan to be used for testing.
     * 
     * @return A properly formed RecordsTransmittalPlan to be used for testing.
     */
    public RecordsTransmittalPlan getNewTransientRecordsTransmittalPlan(int index) {
        RecordsTransmittalPlan recordsTransmittalPlan = new RecordsTransmittalPlan();
        setPrimaryId(recordsTransmittalPlan, index);
        setParserId(recordsTransmittalPlan, index);
        return recordsTransmittalPlan;
    }

    
    
    /**
     * Gets a random RecordsTransmittalPlan from the database.
     * 
     * @return A random RecordsTransmittalPlan from the database.
     */
    public RecordsTransmittalPlan getRandomRecordsTransmittalPlan() {
        RecordsTransmittalPlan recordsTransmittalPlan = 
                recordsTransmittalPlans.get(rnd.nextInt(recordsTransmittalPlans.size()));
        String id = recordsTransmittalPlan.getPrimaryId();
        return RecordsTransmittalPlan.findRecordsTransmittalPlan(id.toString());
    }
    
    /**
     * @param  The RecordsTransmittalPlan instance
     *         whose fields we want to set for testing
     *         purposes.
     * @param  index Used so that each field set by this RecordsTransmittalPlan
     *         is unique.
     */
    public void setParserId(RecordsTransmittalPlan recordsTransmittalPlan, int index) {
        String parserId = "http://www.example.com/" + index;
        recordsTransmittalPlan.setParserId(parserId);
    }
    
    /**
     * Sets a RecordsTransmittalPlan with a unique primaryId.
     * 
     * @param recordsTransmittalPlan The RecordsTransmittalPlan instance
     *                               whose fields we want to set for testing
     *                               purposes.
     * @param index Used so that each field set by this RecordsTransmittalPlan
     *        is unique.
     */
    public void setPrimaryId(RecordsTransmittalPlan recordsTransmittalPlan, int index) {
        String primaryId = "http://www.example.com/" + index;
        recordsTransmittalPlan.setPrimaryId(primaryId);
    }

    /**
     * Grab a specific RecordsTransmittalPlan instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the RecordsTransmittalPlan instance that you 
     *                  want to grab from the database.
     * @return       The RecordsTransmittalPlan instance
     *               whose fields we want to set for testing
     *               purposes.
     */
    public RecordsTransmittalPlan getSpecificRecordsTransmittalPlan(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(recordsTransmittalPlans.size() - 1, Math.max(0, index));

        RecordsTransmittalPlan recordsTransmittalPlan = recordsTransmittalPlans.get(index);
        String id = recordsTransmittalPlan.getPrimaryId().toString();
        return RecordsTransmittalPlan.findRecordsTransmittalPlan(id);
    }

    /**
     * Used to test merge and flush methods.
     * 
     * @param  recordsTransmittalPlan
     * @return Whether a RecordsTransmittalPlan persistence context 
     *         has been modified.
     */
    //TODO Still need to do this... should be good enough for feature-HIDA-128 for now though.
    public boolean modifyRecordsTransmittalPlan(RecordsTransmittalPlan recordsTransmittalPlan) {
        return false;
    }

    /**
     * Persists an initial amount of {@link RecordsTransmittalPlan}s into the
     * database. This database is then used for testing purposes within 
     * {@link RecordsTransmittalPlanIntegrationTest}. Note: Meant to be called
     * once; if you want to add additional objects to the database, use the 
     * RecordsTransmittalPlan classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of RecordsTransmittalPlans
     *                            that you want persisted into the database.
     */
    public void init(int initialDatabaseSize) {
        int from = 0;
        int to = initialDatabaseSize;
        recordsTransmittalPlans = RecordsTransmittalPlan.
                findRecordsTransmittalPlanEntries(from, to);
        
        if (recordsTransmittalPlans == null) {
            throw new IllegalStateException("Find entries implementation for " +
                    "'RecordsTransmittalPlan' illegally returned null");
        }
        
        if (!recordsTransmittalPlans.isEmpty()) {
            return;
        }
        recordsTransmittalPlans = new ArrayList<RecordsTransmittalPlan>();
        
        // Persist RecordsTransmittalPlan objects.
        for (int i = 0; i < initialDatabaseSize; i++) {
            RecordsTransmittalPlan recordsTransmittalPlan = 
                    getNewTransientRecordsTransmittalPlan(i);
            try {
                recordsTransmittalPlan.persist();
            
            // If the persist fails, throw a detailed exception message about
            // what happened.
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            recordsTransmittalPlan.flush();
            recordsTransmittalPlans.add(recordsTransmittalPlan);
        }
    }
}
