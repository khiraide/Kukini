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
 * Integration test class for a RightsBasis model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class RightsBasisIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the RightsBasis model class methods.
     */
    @Autowired // Marking '@Autowired' so that this RightsBasisOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    RightsBasisDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with RightsBasis objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a RightsBasis from the database.
     */
    @Test
    public void testGetRandomRightsBasis() {
        // Grabbing a random RightsBasiss from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomRightsBasis()); 
        }
    }
    
    
    
    /**
     * Tests to see if the RightsBasis count method works correctly.
     */
    @Test
    public void testCountRightsBasiss() {
        long count = RightsBasis.countRightsBasis();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'RightsBasis' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a RightsBasis 
     * from the database.
     */
    @Test
    public void testFindRightsBasis() {
        RightsBasis rightsBasis = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            rightsBasis = dod.getSpecificRightsBasis(i);
            Long id = rightsBasis.getPrimaryId();
            rightsBasis = RightsBasis.findRightsBasis(id);
            Assert.assertNotNull(rightsBasis, "Find method for 'RightsBasis' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, rightsBasis.getPrimaryId(), "Find method for " +
                    "'RightsBasis' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a RightsBasis find all method.
     */
    @Test
    public void testFindAllRightsBasis() {
        List<RightsBasis> result = RightsBasis.findAllRightsBasis();
       
        Assert.assertNotNull(result, "Find all method for 'RightsBasis' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'RightsBasis' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findRightsBasisEntries 
     * method of a RightsBasis.
     */
    @Test
    public void testFindRightsBasisEntries() {
        long count = RightsBasis.countRightsBasis();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<RightsBasis> result = RightsBasis
                .findRightsBasisEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'RightsBasis'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'RightsBasis'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        RightsBasis rightsBasis = dod.getRandomRightsBasis();
        long id = rightsBasis.getPrimaryId();
        rightsBasis = RightsBasis.findRightsBasis(id);
        Assert.assertNotNull(rightsBasis, "Find method for 'RightsBasis' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyRightsBasis(rightsBasis);
        Integer currentVersion = rightsBasis.getVersion();
        rightsBasis.flush();
        
        Assert.assertTrue((currentVersion != null && 
                rightsBasis.getVersion() > currentVersion) || 
                    !modified, "Version for 'RightsBasis' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        RightsBasis obj = dod.getRandomRightsBasis();
        Long id = obj.getPrimaryId();
        obj = RightsBasis.findRightsBasis(id);
        boolean modified =  dod.modifyRightsBasis(obj);
        Integer currentVersion = obj.getVersion();
        RightsBasis merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null && obj.getVersion() > 
            currentVersion) || !modified,
                "Version for 'RightsBasis' failed to increment on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        RightsBasis rightsBasis = dod.getNewTransientRightsBasis(Integer.MAX_VALUE);
        Assert.assertNotNull(rightsBasis, "Data on demand for 'RightsBasis' failed to " +
                "provide a new transient entity");
   
        try {
            rightsBasis.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        rightsBasis.flush();
        
        // Grabbing a count of the RightsBasis that are currently in the database.
        long count = RightsBasis.countRightsBasis();
        
        // The persist should've incremented the count of 
        // RightsBasis in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        RightsBasis rightsBasis = dod.getRandomRightsBasis();
        long id = rightsBasis.getPrimaryId();
        rightsBasis = RightsBasis.findRightsBasis(id);
        rightsBasis.remove();
        rightsBasis.flush();
        
        Assert.assertNull(RightsBasis.findRightsBasis(id), "Failed to remove " +
                "'RightsBasis' with identifier '" + id + "'");
    }
    
}
