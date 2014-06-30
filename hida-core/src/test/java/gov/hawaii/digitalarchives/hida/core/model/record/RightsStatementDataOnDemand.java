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
 * Helper class for the integration testing of a {@link RightsStatement} 
 * model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class RightsStatementDataOnDemand {

    /**
     * Helper class used to test the RightsStatement model class methods.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of RightsStatement to be used for testing purposes.
     */
    private List<RightsStatement> data;

    
    
    /** 
     * Builds a transient RightsStatement to be used for testing.
     * 
     * @param index  Used so that each field set by this RightsStatement is
     *               unique.
     * @return A properly formed RightsStatement to be used for testing.
     */
    public RightsStatement getNewTransientRightsStatement(int index) {
        String primaryId = "primaryId_" + index;
        RightsBasis basis = new RightsBasis();
        DigitalRecord digitalRecord = null;
        RightsStatement rightsStatement = new RightsStatement(primaryId, basis, digitalRecord);
        return rightsStatement;
    }

    
    
    /**
     * Grab a specific RightsStatement instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the RightsStatement instance that you 
     *                  want to grab from the database.
     * @return       The RightsStatement instance
     *               whose fields we want to set for testing
     *               purposes.
     */
    public RightsStatement getSpecificRightsStatement(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        RightsStatement obj = data.get(index);
        String id = obj.getPrimaryId().toString();
        return RightsStatement.findRightsStatement(id);
    }

    
    
    /**
     * Gets a random RightsStatement from the database.
     * 
     * @return A random RightsStatement from the database.
     */
    public RightsStatement getRandomRightsStatement() {
        
        RightsStatement obj = data.get(rnd.nextInt(data.size()));
        String id = obj.getPrimaryId().toString();
        return RightsStatement.findRightsStatement(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj RightsStatement object.
     * @return Whether a RightsStatement persistence context 
     *         has been modified.
     */
    public boolean modifyRightsStatement(RightsStatement obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link RightsStatement}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link RightsStatementIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  RightsStatement classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of RightsStatements that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = RightsStatement.findRightsStatementEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation " +
                    "for 'RightsStatement' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<RightsStatement>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            RightsStatement obj = getNewTransientRightsStatement(i);
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