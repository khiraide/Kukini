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
 * Integration test class for a RecordLanguage model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class RecordLanguageIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the RecordLanguage model class methods.
     */
    @Autowired // Marking '@Autowired' so that this RecordLanguageOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    RecordLanguageDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with RecordLanguage objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a RecordLanguage from the database.
     */
    @Test
    public void testGetRandomRecordLanguage() {
        // Grabbing a random RecordLanguages from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomRecordLanguage()); 
        }
    }
    
    
    
    /**
     * Tests to see if the RecordLanguage count method works correctly.
     */
    @Test
    public void testCountRecordLanguages() {
        long count = RecordLanguage.countRecordLanguages();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'RecordLanguage' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a RecordLanguage 
     * from the database.
     */
    @Test
    public void testFindRecordLanguage() {
        RecordLanguage recordLanguage = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            recordLanguage = dod.getSpecificRecordLanguage(i);
            Long id = recordLanguage.getPrimaryId();
            recordLanguage = RecordLanguage.findRecordLanguage(id);
            Assert.assertNotNull(recordLanguage, "Find method for 'RecordLanguage' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, recordLanguage.getPrimaryId(), "Find method for " +
                    "'RecordLanguage' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a RecordLanguage find all method.
     */
    @Test
    public void testFindAllRecordLanguages() {
        List<RecordLanguage> result = RecordLanguage.findAllRecordLanguages();
       
        Assert.assertNotNull(result, "Find all method for 'RecordLanguage' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'RecordLanguage' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findRecordLanguageEntries 
     * method of a RecordLanguage.
     */
    @Test
    public void testFindRecordLanguageEntries() {
        long count = RecordLanguage.countRecordLanguages();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<RecordLanguage> result = RecordLanguage
                .findRecordLanguageEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'RecordLanguage'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'RecordLanguage'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        RecordLanguage recordLanguage = dod.getRandomRecordLanguage();
        long id = recordLanguage.getPrimaryId();
        recordLanguage = RecordLanguage.findRecordLanguage(id);
        Assert.assertNotNull(recordLanguage, "Find method for 'RecordLanguage' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyRecordLanguage(recordLanguage);
        Integer currentVersion = recordLanguage.getVersion();
        recordLanguage.flush();
        
        Assert.assertTrue((currentVersion != null && 
                recordLanguage.getVersion() > currentVersion) || 
                    !modified, "Version for 'RecordLanguage' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        RecordLanguage obj = dod.getRandomRecordLanguage();
        Long id = obj.getPrimaryId();
        obj = RecordLanguage.findRecordLanguage(id);
        boolean modified =  dod.modifyRecordLanguage(obj);
        Integer currentVersion = obj.getVersion();
        RecordLanguage merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null && obj.getVersion() > 
            currentVersion) || !modified,
                "Version for 'RecordLanguage' failed to increment on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        RecordLanguage recordLanguage = dod.getNewTransientRecordLanguage(Integer.MAX_VALUE);
        Assert.assertNotNull(recordLanguage, "Data on demand for 'RecordLanguage' failed to " +
                "provide a new transient entity");
   
        try {
            recordLanguage.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        recordLanguage.flush();
        
        // Grabbing a count of the RecordLanguages that are currently in the database.
        long count = RecordLanguage.countRecordLanguages();
        
        // The persist should've incremented the count of 
        // RecordLanguages in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        RecordLanguage recordLanguage = dod.getRandomRecordLanguage();
        long id = recordLanguage.getPrimaryId();
        recordLanguage = RecordLanguage.findRecordLanguage(id);
        recordLanguage.remove();
        recordLanguage.flush();
        
        Assert.assertNull(RecordLanguage.findRecordLanguage(id), "Failed to remove " +
                "'RecordLanguage' with identifier '" + id + "'");
    }
    
}
