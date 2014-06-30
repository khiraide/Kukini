package gov.hawaii.digitalarchives.hida.core.model.record;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.apache.commons.httpclient.URIException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a ProducerInfo model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class ProducerInfoDataOnDemand {

    /**
     * Helper class used to test the ProducerInfo model class methods.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of ProducerInfos to be used for testing purposes.
     */
    private List<ProducerInfo> data;

    
    
    /** 
     * Builds a transient ProducerInfo to be used for testing.
     * 
     * @param index  Used so that each field set by this ProducerInfo is
     *               unique.
     * @return A properly formed ProducerInfo to be used for testing.
     */
    public ProducerInfo getNewTransientProducerInfo(int index) {
        String branch = "branch_" + index;
        String department = "department_" + index;
        String division = "division_" + index;
        ProducerInfo obj = new ProducerInfo(branch, department, division);
        return obj;
    }

    
    
    /**
     * Grab a specific ProducerInfo instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the ProducerInfo instance that you 
     *                  want to grab from the database.
     * @return       The ProducerInfo.
     * @throws URIException 
     */
    public ProducerInfo getSpecificProducerInfo(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        ProducerInfo obj = data.get(index);
        Long id = obj.getPrimaryId();
        return ProducerInfo.findProducerInfo(id);
    }

    
    
    /**
     * Gets a random ProducerInfo from the database.
     * 
     * @return A random ProducerInfo from the database.
     * @throws URIException 
     */
    public ProducerInfo getRandomProducerInfo() {
        
        ProducerInfo obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getPrimaryId();
        return ProducerInfo.findProducerInfo(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj ProducerInfo object.
     * @return Whether a ProducerInfo persistence context 
     *         has been modified.
     */
    public boolean modifyProducerInfo(ProducerInfo obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link ProducerInfo}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link ProducerInfoIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  ProducerInfo classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of ProducerInfos that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = ProducerInfo.findProducerInfoEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for " +
                    "'ProducerInfo' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<ProducerInfo>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            ProducerInfo obj = getNewTransientProducerInfo(i);
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
