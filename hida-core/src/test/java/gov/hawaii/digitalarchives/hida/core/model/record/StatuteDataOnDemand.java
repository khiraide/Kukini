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
 * Helper class for the integration testing of a Statute model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class StatuteDataOnDemand {

    /**
     * Helper class used to test the Statute model class methods.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of Statutes to be used for testing purposes.
     */
    private List<Statute> data;

    
    
    /** 
     * Builds a transient Statute to be used for testing.
     * 
     * @param index  Used so that each field set by this Statute is
     *               unique.
     * @return A properly formed Statute to be used for testing.
     */
    public Statute getNewTransientStatute(int index) {
        String jurisdiction = "jurisdiction_" + index;
        String citation = "citation_" + index;
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
        Statute obj = new Statute(jurisdiction, citation, startDate,
                endDate, note);
        return obj;
    }

    
    
    /**
     * Grab a specific Statute instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the Statute instance that you 
     *               want to grab from the database.
     * @return       The Statute.
     */
    public Statute getSpecificStatute(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        Statute obj = data.get(index);
        Long id = obj.getPrimaryId();
        return Statute.findStatute(id);
    }

    
    
    /**
     * Gets a random Statute from the database.
     * 
     * @return A random Statute from the database.
     */
    public Statute getRandomStatute() {
        
        Statute obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getPrimaryId();
        return Statute.findStatute(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj Statute object.
     * @return Whether a Statute persistence context 
     *         has been modified.
     */
    public boolean modifyStatute(Statute obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link Statute}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link StatuteIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  Statute classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of Statutes that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = Statute.findStatuteEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation " +
                    "for 'Statute' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Statute>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            Statute obj = getNewTransientStatute(i);
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
