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
 * Integration test class for a ProducerInfo model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class ProducerInfoIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the ProducerInfo model class methods.
     */
    @Autowired // Marking '@Autowired' so that this ProducerInfoOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    ProducerInfoDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with ProducerInfo objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a ProducerInfo from the database.
     */
    @Test
    public void testGetRandomProducerInfo() {
        // Grabbing a random producerInfos from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomProducerInfo()); 
        }
    }
    
    
    
    /**
     * Tests to see if the ProducerInfo count method works correctly.
     */
    @Test
    public void testCountProducerInfos() {
        long count = ProducerInfo.countProducerInfoes();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'ProducerInfo' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a ProducerInfo 
     * from the database.
     */
    @Test
    public void testFindProducerInfo() {
        ProducerInfo producerInfo = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            producerInfo = dod.getSpecificProducerInfo(i);
            Long id = producerInfo.getPrimaryId();
            producerInfo = ProducerInfo.findProducerInfo(id);
            Assert.assertNotNull(producerInfo, "Find method for 'ProducerInfo' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, producerInfo.getPrimaryId(), "Find method for" +
                    " 'ProducerInfo' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a ProducerInfo find all method.
     */
    @Test
    public void testFindAllProducerInfos() {
        List<ProducerInfo> result = ProducerInfo.findAllProducerInfoes();
       
        Assert.assertNotNull(result, "Find all method for 'ProducerInfo' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'ProducerInfo' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findProducerInfoEntries 
     * method of a ProducerInfo.
     */
    @Test
    public void testFindProducerInfoEntries() {
        long count = ProducerInfo.countProducerInfoes();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ProducerInfo> result = ProducerInfo
                .findProducerInfoEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'ProducerInfo'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'ProducerInfo'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        ProducerInfo producerInfo = dod.getRandomProducerInfo();
        long id = producerInfo.getPrimaryId();
        producerInfo = ProducerInfo.findProducerInfo(id);
        Assert.assertNotNull(producerInfo, "Find method for 'ProducerInfo' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyProducerInfo(producerInfo);
        Integer currentVersion = producerInfo.getVersion();
        producerInfo.flush();
        
        Assert.assertTrue((currentVersion != null && 
                producerInfo.getVersion() > currentVersion) || 
                    !modified, "Version for 'ProducerInfo' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
         ProducerInfo obj = dod.getRandomProducerInfo();
            Long id = obj.getPrimaryId();
            obj = ProducerInfo.findProducerInfo(id);
            boolean modified =  dod.modifyProducerInfo(obj);
            Integer currentVersion = obj.getVersion();
            ProducerInfo merged = obj.merge();
            obj.flush();
            Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                    " not the same as identifier of original object");
            Assert.assertTrue((currentVersion != null && obj.getVersion() > 
                currentVersion) || !modified,
                    "Version for 'ProducerInfo' failed to increment on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        ProducerInfo producerInfo = dod.getNewTransientProducerInfo(Integer.MAX_VALUE);
        Assert.assertNotNull(producerInfo, "Data on demand for 'ProducerInfo' failed to " +
                "provide a new transient entity");
   
        try {
            producerInfo.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        producerInfo.flush();
        
        // Grabbing a count of the ProducerInfos that are currently in the database.
        long count = ProducerInfo.countProducerInfoes();
        
        // The persist should've incremented the count of 
        // ProducerInfos in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        ProducerInfo producerInfo = dod.getRandomProducerInfo();
        long id = producerInfo.getPrimaryId();
        producerInfo = ProducerInfo.findProducerInfo(id);
        producerInfo.remove();
        producerInfo.flush();
        
        Assert.assertNull(ProducerInfo.findProducerInfo(id), "Failed to remove " +
                "'ProducerInfo' with identifier '" + id + "'");
    }
    
}
