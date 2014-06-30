package gov.hawaii.digitalarchives.hida.core.model.rtp;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Integration test class for a RecordsTransmittalPlan model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class RecordsTransmittalPlanIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the RecordsTransmittalPlan model class methods.
     */
    @Autowired // Marking '@Autowired' so that this RecordsTransmittalPlanOnDemand
               // instance is autowired by Spring's dependency injection facilities
               // to be used as service.
    RecordsTransmittalPlanDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;
    
    /**
     * Initializes our in-memory database with RecordsTransmittalPlan objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    
    
    /**
     * Tests to see if the RecordsTransmittalPlan count method works correctly.
     */
    @Test
    public void testCountRecordsTransmittalPlans() {
        // Grabbing a count of the RTPs that are currently in the database.
        long count = RecordsTransmittalPlan.countRecordsTransmittalPlans(); 
        
        // Look at RecordsTransmittalPlanDataOnDemand.init() method for more info.
        assertTrue(count == INITIAL_DB_SIZE); 
    }
    
    /**
     * Throughly testing the grabbing of a RecordsTransmittalPlan 
     * from the database.
     */
    @Test
    public void testFindRecordsTransmittalPlan() {
        RecordsTransmittalPlan rtp = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            rtp = dod.getSpecificRecordsTransmittalPlan(i);
        
            String PrimaryId = rtp.getPrimaryId().toString();
           
            rtp = RecordsTransmittalPlan.findRecordsTransmittalPlan(PrimaryId);
            
            assertNotNull(rtp, "Find method for 'RecordsTransmittalPlan' illegally returned " +
                    "null for PrimaryId '" + PrimaryId + "'");
            
            assertEquals(PrimaryId, rtp.getPrimaryId().toString(), "Find method for " +
                    "'RecordsTransmittalPlan' returned the incorrect identifier");
        }
    }
    
    
    /**
     * Tests a RecordsTransmittalPlan find all method.
     */
    @Test
    public void testFindAllRecordsTransmittalPlans() {
        // Grabbing all RecordsTransmittalPlans from the database.
        List<RecordsTransmittalPlan> result = RecordsTransmittalPlan
                .findAllRecordsTransmittalPlans();
        assertNotNull(result, "Find all method for 'RecordsTransmittalPlan' " +
                "illegally returned null");
        assertTrue(result.size() == INITIAL_DB_SIZE,
                "Find all method for 'RecordsTransmittalPlan' failed.");
    }

    
    /**
     * Tests the findRecordsTransmittalPlanEntries method of a 
     * RecordsTransmittalPlan.
     */
    @Test
    public void testFindRecordsTransmittalPlanEntries() {
        long count = RecordsTransmittalPlan.countRecordsTransmittalPlans();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<RecordsTransmittalPlan> result = RecordsTransmittalPlan
                .findRecordsTransmittalPlanEntries(firstResult, maxResults);
        
        assertNotNull(result, "Find entries method for 'RecordsTransmittalPlan' " +
                "illegally returned null");
        
        assertEquals(count, result.size(), "Find entries method for 'RecordsTransmittalPlan' " +
                "returned an incorrect number of entries");
    }



    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        RecordsTransmittalPlan rtp = dod.getRandomRecordsTransmittalPlan();
        String PrimaryId = rtp.getPrimaryId().toString();
        rtp = RecordsTransmittalPlan.findRecordsTransmittalPlan(PrimaryId);
        
        assertNotNull(rtp, "Find method for 'RecordsTransmittalPlan' illegally " +
                "returned null for PrimaryId '" + PrimaryId + "'");
        boolean modified =  dod.modifyRecordsTransmittalPlan(rtp);
        Integer currentVersion = rtp.getVersion();
        rtp.flush();
        
        assertTrue((currentVersion != null && rtp.getVersion() 
                > currentVersion) || !modified, "Version for 'RecordsTransmittalPlan' " +
                        "failed to increment on flush directive");
    }
    
    

    /**
     * Gets a RecordsTransmittalPlan from the database.
     */
    @Test
    public void testGetRecordsTransmittalPlan() {
        // Grabbing a random RecordsTransmittalPlans from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomRecordsTransmittalPlan()); 
        }
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        RecordsTransmittalPlan rtp = dod.getRandomRecordsTransmittalPlan();
        String PrimaryId = rtp.getPrimaryId().toString();

        rtp = RecordsTransmittalPlan.findRecordsTransmittalPlan(PrimaryId);
        boolean modified =  dod.modifyRecordsTransmittalPlan(rtp);
        Integer currentVersion = rtp.getVersion();
        RecordsTransmittalPlan merged = rtp.merge();
        rtp.flush();
        
        assertEquals(merged.getPrimaryId().toString(), PrimaryId, 
                "Identifier of merged object not the same as identifier of original object");
        
        assertTrue((currentVersion != null && rtp.getVersion() 
                > currentVersion) || !modified, "Version for 'RecordsTransmittalPlan' " +
                        "failed to increment on merge and flush directive");
    }
    
    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        // Grabbing a count of the RTPs that are currently in the database.
        long count = RecordsTransmittalPlan.countRecordsTransmittalPlans(); 
        
        // Creating a RecordsTransmittalPlan from scratch.
        RecordsTransmittalPlan rtp = dod.getNewTransientRecordsTransmittalPlan(Integer.MAX_VALUE);
        assertNotNull(rtp, "Data on demand for 'RecordsTransmittalPlan' failed to " +
                "provide a new transient entity");
       
        try {
            rtp.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        rtp.flush();

        // The persist should've incremented the count of RTPs in the DB.
        assertEquals(count + 1, RecordsTransmittalPlan.countRecordsTransmittalPlans());
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        RecordsTransmittalPlan rtp = dod.getRandomRecordsTransmittalPlan();
        String primaryId = rtp.getPrimaryId().toString();
        rtp = RecordsTransmittalPlan.findRecordsTransmittalPlan(primaryId);
        rtp.remove();
        rtp.flush();
        
        assertNull(RecordsTransmittalPlan.findRecordsTransmittalPlan(primaryId), "Failed to " +
                "remove 'RecordsTransmittalPlan' with identifier '" + primaryId + "'");
    }
}
