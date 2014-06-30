package gov.hawaii.digitalarchives.hida.core.model.record;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.apache.commons.httpclient.URIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Integration test class for a MetadataEntry model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class MetadataEntryIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the MetadataEntry model class methods.
     */
    @Autowired // Marking '@Autowired' so that this MetadataEntryOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    MetadataEntryDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;
    
    

    /**
     * Initializes our in-memory database with MetadataEntry objects
     * to be used for testing purposes.
     * @throws URIException Badly formed DigitalObjectId.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() throws URIException {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a MetadataEntry from the database.
     */
    @Test
    public void testGetRandomMetadataEntry() {
        // Grabbing a random metadataEntries from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomMetadataEntry()); 
        }
    }
    
    
    
    /**
     * Tests to see if the MetadataEntry count method works correctly.
     */
    @Test
    public void testCountMetadataEntries() {
        long count = MetadataEntry.countMetadataEntries();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'MetadataEntry' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a MetadataEntry 
     * from the database.
     */
    @Test
    public void testFindMetadataEntry() {
        MetadataEntry metadataEntry = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            metadataEntry = dod.getSpecificMetadataEntry(i);
            Long id = metadataEntry.getPrimaryId();
            metadataEntry = MetadataEntry.findMetadataEntry(id);
            Assert.assertNotNull(metadataEntry, "Find method for 'MetadataEntry' illegally returned " +
                    "'null for id '" + id + "'");
            Assert.assertEquals(id, metadataEntry.getPrimaryId(), "Find method for 'MetadataEntry' " +
                    "returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a MetadataEntry find all method.
     */
    @Test
    public void testFindAllMetadataEntries() {
        List<MetadataEntry> result = MetadataEntry.findAllMetadataEntries();
       
        Assert.assertNotNull(result, "Find all method for 'MetadataEntry' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'MetadataEntry' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findMetadataEntryEntries 
     * method of a MetadataEntry.
     */
    @Test
    public void testFindMetadataEntryEntries() {
        long count = MetadataEntry.countMetadataEntries();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<MetadataEntry> result = MetadataEntry
                .findMetadataEntryEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'MetadataEntry'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'MetadataEntry'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        MetadataEntry metadataEntry = dod.getRandomMetadataEntry();
        long id = metadataEntry.getPrimaryId();
        metadataEntry = MetadataEntry.findMetadataEntry(id);
        Assert.assertNotNull(metadataEntry, "Find method for 'MetadataEntry' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyMetadataEntry(metadataEntry);
        Integer currentVersion = metadataEntry.getVersion();
        metadataEntry.flush();
        
        Assert.assertTrue((currentVersion != null && 
                metadataEntry.getVersion() > currentVersion) || 
                    !modified, "Version for 'MetadataEntry' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
         MetadataEntry obj = dod.getRandomMetadataEntry();
            Long id = obj.getPrimaryId();
            obj = MetadataEntry.findMetadataEntry(id);
            boolean modified =  dod.modifyMetadataEntry(obj);
            Integer currentVersion = obj.getVersion();
            MetadataEntry merged = obj.merge();
            obj.flush();
            Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                    " not the same as identifier of original object");
            Assert.assertTrue((currentVersion != null && obj.getVersion() > 
                currentVersion) || !modified,
                    "Version for 'MetadataEntry' failed to increment on merge and flush " +
                    "directive");
    }

    
    
    /**
     * Testing a persist to the database.
     * @throws URIException Badly formed DigitalObjectId.
     */
    @Test
    public void testPersist() throws URIException {
        MetadataEntry metadataEntry = dod.getNewTransientMetadataEntry(Integer.MAX_VALUE);
        Assert.assertNotNull(metadataEntry, "Data on demand for 'MetadataEntry' failed to " +
                "provide a new transient entity");
   
        try {
            metadataEntry.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        metadataEntry.flush();
        
        // Grabbing a count of the MetadataEntries that are currently in the database.
        long count = MetadataEntry.countMetadataEntries();
        
        // The persist should've incremented the count of 
        // MetadataEntrys in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        MetadataEntry metadataEntry = dod.getRandomMetadataEntry();
        long id = metadataEntry.getPrimaryId();
        metadataEntry = MetadataEntry.findMetadataEntry(id);
        metadataEntry.remove();
        metadataEntry.flush();
        
        Assert.assertNull(MetadataEntry.findMetadataEntry(id), "Failed to remove " +
                "'MetadataEntry' with identifier '" + id + "'");
    }
    
}
