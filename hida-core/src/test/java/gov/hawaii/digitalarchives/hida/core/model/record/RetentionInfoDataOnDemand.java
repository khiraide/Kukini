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
 * Helper class for the integration testing of a RetentionInfo model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class RetentionInfoDataOnDemand {

    /**
     * Helper class used to test the RetentionInfo model class methods.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of RetentionInfos to be used for testing purposes.
     */
    private List<RetentionInfo> data;

    
    
    /** 
     * Builds a transient RetentionInfo to be used for testing.
     * 
     * @param index  Used so that each field set by this RetentionInfo is
     *               unique.
     * @return A properly formed RetentionInfo to be used for testing.
     */
    public RetentionInfo getNewTransientRetentionInfo(int index) {
        String recordSchedule = "recordSchedule_" + index;
        String retentionNumber = "retentionNumber_" + index;
        String seriesName = "seriesName_" + index;
        RetentionInfo obj = new RetentionInfo(recordSchedule, retentionNumber,
                 seriesName);
        return obj;
    }
    
    /**
     * Grab a specific RetentionInfo instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the RetentionInfo instance that you 
     *                  want to grab from the database.
     * @return       The RetentionInfo.
     */
    public RetentionInfo getSpecificRetentionInfo(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        RetentionInfo obj = data.get(index);
        Long id = obj.getPrimaryId();
        return RetentionInfo.findRetentionInfo(id);
    }

    
    
    /**
     * Gets a random RetentionInfo from the database.
     * 
     * @return A random RetentionInfo from the database.
     */
    public RetentionInfo getRandomRetentionInfo() {
        
        RetentionInfo obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getPrimaryId();
        return RetentionInfo.findRetentionInfo(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj  RetentionInfo object.
     * @return Whether a RetentionInfo persistence context 
     *         has been modified.
     */
    public boolean modifyRetentionInfo(RetentionInfo obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link RetentionInfo}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link RetentionInfoIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  RetentionInfo classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of RetentionInfos that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = RetentionInfo.findRetentionInfoEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for "
                    + "'RetentionInfo' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<RetentionInfo>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            RetentionInfo obj = getNewTransientRetentionInfo(i);
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
