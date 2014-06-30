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
 * Integration test class for a RetentionInfo model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class RetentionInfoIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the RetentionInfo model class methods.
     */
    @Autowired // Marking '@Autowired' so that this RetentionInfoOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    RetentionInfoDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with RetentionInfo objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a RetentionInfo from the database.
     */
    @Test
    public void testGetRandomRetentionInfo() {
        // Grabbing a random retentionInfos from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomRetentionInfo()); 
        }
    }
    
    
    
    /**
     * Tests to see if the RetentionInfo count method works correctly.
     */
    @Test
    public void testCountRetentionInfos() {
        long count = RetentionInfo.countRetentionInfoes();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'RetentionInfo' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a RetentionInfo 
     * from the database.
     */
    @Test
    public void testFindRetentionInfo() {
        RetentionInfo retentionInfo = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            retentionInfo = dod.getSpecificRetentionInfo(i);
            Long id = retentionInfo.getPrimaryId();
            retentionInfo = RetentionInfo.findRetentionInfo(id);
            Assert.assertNotNull(retentionInfo, "Find method for 'RetentionInfo'" +
                    " illegally returned null for id '" + id + "'");
            Assert.assertEquals(id, retentionInfo.getPrimaryId(), "Find method for" +
                    " 'RetentionInfo' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a RetentionInfo find all method.
     */
    @Test
    public void testFindAllRetentionInfos() {
        List<RetentionInfo> result = RetentionInfo.findAllRetentionInfoes();
       
        Assert.assertNotNull(result, "Find all method for 'RetentionInfo' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'RetentionInfo' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findRetentionInfoEntries 
     * method of a RetentionInfo.
     */
    @Test
    public void testFindRetentionInfoEntries() {
        long count = RetentionInfo.countRetentionInfoes();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<RetentionInfo> result = RetentionInfo
                .findRetentionInfoEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'RetentionInfo'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'RetentionInfo'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        RetentionInfo retentionInfo = dod.getRandomRetentionInfo();
        long id = retentionInfo.getPrimaryId();
        retentionInfo = RetentionInfo.findRetentionInfo(id);
        Assert.assertNotNull(retentionInfo, "Find method for 'RetentionInfo' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyRetentionInfo(retentionInfo);
        Integer currentVersion = retentionInfo.getVersion();
        retentionInfo.flush();
        
        Assert.assertTrue((currentVersion != null && 
                retentionInfo.getVersion() > currentVersion) || 
                    !modified, "Version for 'RetentionInfo' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
         RetentionInfo obj = dod.getRandomRetentionInfo();
            Long id = obj.getPrimaryId();
            obj = RetentionInfo.findRetentionInfo(id);
            boolean modified =  dod.modifyRetentionInfo(obj);
            Integer currentVersion = obj.getVersion();
            RetentionInfo merged = obj.merge();
            obj.flush();
            Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                    " not the same as identifier of original object");
            Assert.assertTrue((currentVersion != null && obj.getVersion() > 
                currentVersion) || !modified,
                    "Version for 'RetentionInfo' failed to increment on" +
                    " merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        RetentionInfo retentionInfo = dod.getNewTransientRetentionInfo(Integer.MAX_VALUE);
        Assert.assertNotNull(retentionInfo, "Data on demand for 'RetentionInfo' failed to " +
                "provide a new transient entity");
   
        try {
            retentionInfo.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        retentionInfo.flush();
        
        // Grabbing a count of the RetentionInfos that are currently in the database.
        long count = RetentionInfo.countRetentionInfoes();
        
        // The persist should've incremented the count of 
        // RetentionInfos in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        RetentionInfo retentionInfo = dod.getRandomRetentionInfo();
        long id = retentionInfo.getPrimaryId();
        retentionInfo = RetentionInfo.findRetentionInfo(id);
        retentionInfo.remove();
        retentionInfo.flush();
        
        Assert.assertNull(RetentionInfo.findRetentionInfo(id), "Failed to remove " +
                "'RetentionInfo' with identifier '" + id + "'");
    }
    
}