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
 * Helper class for the integration testing of a {@link RightsBasis} 
 * model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class RightsBasisDataOnDemand {

    /**
     * Helper class used to test the RightsBasis model class methods.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of RightsBasis to be used for testing purposes.
     */
    private List<RightsBasis> data;

    
    
    /** 
     * Builds a transient RightsBasis to be used for testing.
     * 
     * @param index  Used so that each field set by this RightsBasis is
     *               unique.
     * @return A properly formed RightsBasis to be used for testing.
     */
    public RightsBasis getNewTransientRightsBasis(int index) {
        Date startDate = new GregorianCalendar(Calendar.getInstance()
                .get(Calendar.YEAR), Calendar.getInstance()
                .get(Calendar.MONTH), Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH), Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY), Calendar.getInstance()
                .get(Calendar.MINUTE), Calendar.getInstance()
                .get(Calendar.SECOND))
                .getTime();
        Date endDate = new GregorianCalendar(Calendar.getInstance()
                .get(Calendar.YEAR), Calendar.getInstance()
                .get(Calendar.MONTH), Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH), Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY), Calendar.getInstance()
                .get(Calendar.MINUTE), Calendar.getInstance()
                .get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue())
                .getTime();
        String note = "note_" + index;
        RightsBasis rightsBasis = new RightsBasis(startDate, endDate, note);
        return rightsBasis;
    }

    
    
    /**
     * Grab a specific RightsBasis instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the RightsBasis instance that you 
     *                  want to grab from the database.
     * @return       The RightsBasis.
     */
    public RightsBasis getSpecificRightsBasis(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        RightsBasis obj = data.get(index);
        Long id = obj.getPrimaryId();
        return RightsBasis.findRightsBasis(id);
    }

    
    
    /**
     * Gets a random RightsBasis from the database.
     * 
     * @return A random RightsBasis from the database.
     */
    public RightsBasis getRandomRightsBasis() {
        
        RightsBasis obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getPrimaryId();
        return RightsBasis.findRightsBasis(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj RightsBasis object.
     * @return Whether a RightsBasis persistence context 
     *         has been modified.
     */
    public boolean modifyRightsBasis(RightsBasis obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link RightsBasis}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link RightsBasisIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  RightsBasis classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of RightsBasises that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = RightsBasis.findRightsBasisEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation " +
                    "for 'RightsBasis' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<RightsBasis>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            RightsBasis obj = getNewTransientRightsBasis(i);
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
