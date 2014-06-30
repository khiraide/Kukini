package gov.hawaii.digitalarchives.hida.core.model.digitalobject;
import static org.testng.Assert.assertEquals;
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
 * Integration test class for a {@link DefaultFileObject} model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class DefaultFileObjectIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the {@link DefaultFileObject} model class 
     * methods.
     */
    @Autowired // Marking '@Autowired' so that this DefaultFileObjectOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    DefaultFileObjectDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with {@link DefaultFileObject} 
     * objects to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE); 
    }



    /**
     * Tests to see if the {@link DefaultFileObject} count method works 
     * correctly.
     */
    @Test
    public void testCountDefaultFileObjects() {
        long count = DefaultFileObject.countDefaultFileObjects();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, 
                "Counter for 'DefaultFileObject' incorrectly "
                + "reported the amount of entries");
    }
    
    
    
    /**
     * Tests a {@link DefaultFileObject} find all method.
     */
    @Test
    public void testFindAllDefaultFileObjects() {
        List<DefaultFileObject> result = DefaultFileObject.findAllDefaultFileObjects();
       
        Assert.assertNotNull(result, "Find all method for 'DefaultFileObject' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE,
                "Find all method for 'DefaultFileObject' returned the incorrect "
                + "amount of entries.");
    }

    
    
    /**
     * Throughly testing the grabbing of a {@link DefaultFileObject} from the
     * database.
     */
    @Test
    public void testFindDefaultFileObject() {
        DefaultFileObject obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificDefaultFileObject(i);
           
            String id = obj.getPrimaryId().toString();
            
            obj = DefaultFileObject.findDefaultFileObject(id);
            
            Assert.assertNotNull(obj, "Find method for 'DefaultFileObject' illegally" +
                    " returned null for id '" + id + "'");
            
            Assert.assertEquals(id, obj.getPrimaryId().toString(),
                    "Find method for 'DefaultFileObject' " +
                    "returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests the findDefaultFileObjectEntries method of a 
     * {@link DefaultFileObject}.
     */
    @Test
    public void testFindDefaultFileObjectEntries() {
        long count = DefaultFileObject.countDefaultFileObjects();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<DefaultFileObject> result = DefaultFileObject
                .findDefaultFileObjectEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'DefaultFileObject'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'DefaultFileObject'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        DefaultFileObject obj = dod.getRandomDefaultFileObject();
        String id = obj.getPrimaryId().toString();
        obj = DefaultFileObject.findDefaultFileObject(id);
        Assert.assertNotNull(obj, "Find method for 'DefaultFileObject' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyDefaultFileObject(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        
        Assert.assertTrue((currentVersion != null && 
                obj.getVersion() > currentVersion) || 
                    !modified, "Version for 'DefaultFileObject' failed " +
                            "to increment on flush directive");
    }

    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        DefaultFileObject obj = dod.getRandomDefaultFileObject();
        String id = obj.getPrimaryId().toString();
        obj = DefaultFileObject.findDefaultFileObject(id);
        boolean modified =  dod.modifyDefaultFileObject(obj);
        Integer currentVersion = obj.getVersion();
        DefaultFileObject merged = obj.merge();
        obj.flush();
        
        Assert.assertEquals(merged.getPrimaryId().toString(), id, 
                "Identifier of merged object not the same as identifier of original object");
        
        Assert.assertTrue((currentVersion != null && 
                obj.getVersion() > currentVersion) || 
                    !modified, "Version for 'DefaultFileObject' failed to increment " +
                            "on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        // Grabbing a count of the DefaultFileObjects that are currently in the database.
        long count = DefaultFileObject.countDefaultFileObjects();
        DefaultFileObject obj = dod.getNewTransientDefaultFileObject(Integer.MAX_VALUE);
        Assert.assertNotNull(obj, "Data on demand for 'DefaultFileObject' failed to " +
                "provide a new transient entity");
   
        try {
            obj.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        obj.flush();
        
        // The persist should've incremented the count of 
        // DefaultFileObjects in the DB.
        assertEquals(count + 1, DefaultFileObject.countDefaultFileObjects(),  
                "The counter after a persist didn't increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        DefaultFileObject obj = dod.getRandomDefaultFileObject();
        String id = obj.getPrimaryId().toString();
        obj = DefaultFileObject.findDefaultFileObject(id);
        obj.remove();
        obj.flush();
        
        Assert.assertNull(DefaultFileObject.findDefaultFileObject(id), "Failed to remove " +
                "'DefaultFileObject' with identifier '" + id + "'");
    }
}
