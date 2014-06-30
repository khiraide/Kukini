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
 * Integration test class for a DigitalRecord model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class DigitalRecordIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the DigitalRecord model class methods.
     */
    @Autowired // Marking '@Autowired' so that this DigitalRecordOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    DigitalRecordDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with DigitalRecord objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a DigitalRecord from the database.
     */
    @Test
    public void testGetRandomDigitalRecord() {
        // Grabbing a random DigitalRecords from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomDigitalRecord()); 
        }
    }
    
    
    
    /**
     * Tests to see if the DigitalRecord count method works correctly.
     */
    @Test
    public void testCountDigitalRecords() {
        long count = DigitalRecord.countDigitalRecords();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'DigitalRecord' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a DigitalRecord 
     * from the database.
     */
    @Test
    public void testFindDigitalRecord() {
        DigitalRecord digitalRecord = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            digitalRecord = dod.getSpecificDigitalRecord(i);
            String id = digitalRecord.getPrimaryId().toString();
            
            digitalRecord = DigitalRecord.findDigitalRecord(id);
            
            Assert.assertNotNull(digitalRecord, "Find method for 'DigitalRecord' illegally" +
                    " returned null for id '" + id + "'");
            
            Assert.assertEquals(id, digitalRecord.getPrimaryId().toString(),
                    "Find method for 'DigitalRecord' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a DigitalRecord find all method.
     */
    @Test
    public void testFindAllDigitalRecords() {
        List<DigitalRecord> result = DigitalRecord.findAllDigitalRecords();
       
        Assert.assertNotNull(result, "Find all method for 'DigitalRecord' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'DigitalRecord' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findDigitalRecordEntries 
     * method of a DigitalRecord.
     */
    @Test
    public void testFindDigitalRecordEntries() {
        long count = DigitalRecord.countDigitalRecords();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<DigitalRecord> result = DigitalRecord
                .findDigitalRecordEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'DigitalRecord'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'DigitalRecord'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        DigitalRecord digitalRecord = dod.getRandomDigitalRecord();
        String id = digitalRecord.getPrimaryId().toString();
        digitalRecord = DigitalRecord.findDigitalRecord(id);
        Assert.assertNotNull(digitalRecord, "Find method for 'DigitalRecord' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyDigitalRecord(digitalRecord);
        Integer currentVersion = digitalRecord.getVersion();
        digitalRecord.flush();
        
        Assert.assertTrue((currentVersion != null && 
                digitalRecord.getVersion() > currentVersion) || 
                    !modified, "Version for 'DigitalRecord' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        DigitalRecord digitalRecord = dod.getRandomDigitalRecord();
        String id = digitalRecord.getPrimaryId().toString();
        digitalRecord = DigitalRecord.findDigitalRecord(id);
        boolean modified =  dod.modifyDigitalRecord(digitalRecord);
        Integer currentVersion = digitalRecord.getVersion();
        DigitalRecord merged = digitalRecord.merge();
        digitalRecord.flush();
        
        Assert.assertEquals(merged.getPrimaryId().toString(), id, 
                "Identifier of merged object not the same as identifier of original object");
        
        Assert.assertTrue((currentVersion != null && 
                digitalRecord.getVersion() > currentVersion) || 
                    !modified, "Version for 'DigitalRecord' failed to increment " +
                            "on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        DigitalRecord digitalRecord = dod.getNewTransientDigitalRecord(Integer.MAX_VALUE);
        Assert.assertNotNull(digitalRecord, "Data on demand for 'DigitalRecord' failed to " +
                "provide a new transient entity");
   
        try {
            digitalRecord.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        digitalRecord.flush();
        
        // Grabbing a count of the DigitalRecords that are currently in the database.
        long count = DigitalRecord.countDigitalRecords();
        
        // The persist should've incremented the count of 
        // DigitalRecords in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        DigitalRecord digitalRecord = dod.getRandomDigitalRecord();
        String id = digitalRecord.getPrimaryId().toString();
        digitalRecord = DigitalRecord.findDigitalRecord(id);
        digitalRecord.remove();
        digitalRecord.flush();
        
        Assert.assertNull(DigitalRecord.findDigitalRecord(id), "Failed to remove " +
                "'DigitalRecord' with identifier '" + id + "'");
    }
    
}
