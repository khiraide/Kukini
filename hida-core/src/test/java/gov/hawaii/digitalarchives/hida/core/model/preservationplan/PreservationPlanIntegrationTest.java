package gov.hawaii.digitalarchives.hida.core.model.preservationplan;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.util.List;
import java.util.Map;

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
 * Integration test class for a PreservationPlan model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class PreservationPlanIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the PreservationPlan model class methods.
     */
    @Autowired // Marking '@Autowired' so that this PreservationPlanOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    PreservationPlanDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with PreservationPlan objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests to see if the PreservationPlan count method works correctly.
     */
    @Test
    public void testCountPreservationPlans() {
        long count = PreservationPlan.countPreservationPlans();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'PreservationPlan' " +
                "incorrectly reported the amount of entries");
    }
    
    
    
    /**
     * Tests a PreservationPlan find all method.
     */
    @Test
    public void testFindAllPreservationPlans() {
        List<PreservationPlan> result = PreservationPlan.findAllPreservationPlans();
       
        Assert.assertNotNull(result, "Find all method for 'PreservationPlan' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'PreservationPlan' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Throughly testing the grabbing of a PreservationPlan 
     * from the database.
     */
    @Test
    public void testFindPreservationPlan() {
        PreservationPlan obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificPreservationPlan(i);
            String id = obj.getPrimaryId().toString();
            
            obj = PreservationPlan.findPreservationPlan(id);
            
            Assert.assertNotNull(obj, "Find method for 'PreservationPlan' illegally" +
                    " returned null for id '" + id + "'");
            
            Assert.assertEquals(id, obj.getPrimaryId().toString(),"Find method for " +
                    "'PreservationPlan' returned the incorrect identifier");
            
            // Getting the format plans associated with this Preservation Plan.
            Map<String, FormatPlan> fps = obj.getFormatPlans();
            Assert.assertNotNull(fps, "getFormatPlans illegally returned null");
            Assert.assertTrue(fps.size() > 0, "The collection size associated" +
                    "with a Preservation Plan is empty.");
            
            // Getting a Format Plan based on its key.
            FormatPlan fp = fps.get("FMT/40_" + i);
            Assert.assertNotNull(fp, "Failed to grab a FormatPlan from a HashMap" +
                    "collection with a known key");
            
            // Checking that each format associated with this Format Plan is correct
            // and can be retrieved.
            Assert.assertEquals(fp.getNativeFormat().getFormatName(), ".doc");
            Assert.assertEquals(fp.getNativeFormat().getPronomFormat(), "FMT/40_" + i);
            
            Assert.assertEquals(fp.getPreservationFormat().getFormatName(), ".docx");
            Assert.assertEquals(fp.getPreservationFormat().getPronomFormat(), "FMT/412");
            
            Assert.assertEquals(fp.getPresentationFormat().getFormatName(), ".pdf/A");
            Assert.assertEquals(fp.getPresentationFormat().getPronomFormat(), "FMT/95");
            
            Assert.assertEquals(fp.getThumbnailFormat().getFormatName(), ".jpg");
            Assert.assertEquals(fp.getThumbnailFormat().getPronomFormat(), "FMT/42");  
        }
    }
    

    
    /**
     * Tests the findPreservationPlanEntries 
     * method of a PreservationPlan.
     */
    @Test
    public void testFindPreservationPlanEntries() {
        long count = PreservationPlan.countPreservationPlans();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<PreservationPlan> result = PreservationPlan
                .findPreservationPlanEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'PreservationPlan'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'PreservationPlan'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        PreservationPlan obj = dod.getRandomPreservationPlan();
        String id = obj.getPrimaryId().toString();
        obj = PreservationPlan.findPreservationPlan(id);
        Assert.assertNotNull(obj, "Find method for 'PreservationPlan' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyPreservationPlan(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        
        Assert.assertTrue((currentVersion != null && 
                obj.getVersion() > currentVersion) || 
                    !modified, "Version for 'PreservationPlan' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        PreservationPlan obj = dod.getRandomPreservationPlan();
        String id = obj.getPrimaryId().toString();
        obj = PreservationPlan.findPreservationPlan(id);
        boolean modified =  dod.modifyPreservationPlan(obj);
        Integer currentVersion = obj.getVersion();
        PreservationPlan merged = obj.merge();
        obj.flush();
        
        Assert.assertEquals(merged.getPrimaryId().toString(), id, "Identifier of merged object not the" +
                " same as identifier of original object");
        
        Assert.assertTrue((currentVersion != null && 
                obj.getVersion() > currentVersion) || 
                    !modified, "Version for 'PreservationPlan' failed to increment " +
                            "on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        PreservationPlan obj = dod.getNewTransientPreservationPlan(Integer.MAX_VALUE);
        Assert.assertNotNull(obj, "Data on demand for 'PreservationPlan' failed to " +
                "provide a new transient entity");
   
        try {
            obj.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        obj.flush();
        
        // Grabbing a count of the PreservationPlans that are currently in the database.
        long count = PreservationPlan.countPreservationPlans();
        
        // The persist should've incremented the count of 
        // PreservationPlans in the database.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        PreservationPlan obj = dod.getRandomPreservationPlan();
        String id = obj.getPrimaryId().toString();
        obj = PreservationPlan.findPreservationPlan(id);
        obj.remove();
        obj.flush();
        
        Assert.assertNull(PreservationPlan.findPreservationPlan(id), "Failed to remove " +
                "'PreservationPlan' with identifier '" + id + "'");
    }
}