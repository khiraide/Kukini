package gov.hawaii.digitalarchives.hida.core.model.record;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Integration test class for a Statute model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class StatuteIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the Statute model class methods.
     */
    @Autowired // Marking '@Autowired' so that this StatuteOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    StatuteDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with Statute objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a Statute from the database.
     */
    @Test
    public void testGetRandomStatute() {
        // Grabbing a random Statutes from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomStatute()); 
        }
    }
    
    
    
    /**
     * Tests to see if the Statute count method works correctly.
     */
    @Test
    public void testCountStatutes() {
        long count = Statute.countStatutes();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'Statute' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a Statute 
     * from the database.
     */
    @Test
    public void testFindStatute() {
        Statute statute = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            statute = dod.getSpecificStatute(i);
            Long id = statute.getPrimaryId();
            statute = Statute.findStatute(id);
            Assert.assertNotNull(statute, "Find method for 'Statute' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, statute.getPrimaryId(), "Find method for " +
                    "'Statute' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a Statute find all method.
     */
    @Test
    public void testFindAllStatute() {
        List<Statute> result = Statute.findAllStatutes();
       
        Assert.assertNotNull(result, "Find all method for 'Statute' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'Statute' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findStatuteEntries 
     * method of a Statute.
     */
    @Test
    public void testFindStatuteEntries() {
        long count = Statute.countStatutes();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Statute> result = Statute
                .findStatuteEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'Statute'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'Statute'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        Statute statute = dod.getRandomStatute();
        long id = statute.getPrimaryId();
        statute = Statute.findStatute(id);
        Assert.assertNotNull(statute, "Find method for 'Statute' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyStatute(statute);
        Integer currentVersion = statute.getVersion();
        statute.flush();
        
        Assert.assertTrue((currentVersion != null && 
                statute.getVersion() > currentVersion) || 
                    !modified, "Version for 'Statute' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        Statute obj = dod.getRandomStatute();
        Long id = obj.getPrimaryId();
        obj = Statute.findStatute(id);
        boolean modified =  dod.modifyStatute(obj);
        Integer currentVersion = obj.getVersion();
        Statute merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null && obj.getVersion() > 
            currentVersion) || !modified,
                "Version for 'Statute' failed to increment on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        Statute statute = dod.getNewTransientStatute(Integer.MAX_VALUE);
        Assert.assertNotNull(statute, "Data on demand for 'Statute' failed to " +
                "provide a new transient entity");
   
        try {
            statute.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        statute.flush();
        
        // Grabbing a count of the Statute that are currently in the database.
        long count = Statute.countStatutes();
        
        // The persist should've incremented the count of 
        // Statute in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        Statute statute = dod.getRandomStatute();
        long id = statute.getPrimaryId();
        statute = Statute.findStatute(id);
        statute.remove();
        statute.flush();
        
        Assert.assertNull(Statute.findStatute(id), "Failed to remove " +
                "'Statute' with identifier '" + id + "'");
    }
}