package gov.hawaii.digitalarchives.hida.core.model;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import gov.hawaii.digitalarchives.hida.core.model.digitalobject.DefaultFileObject;
import gov.hawaii.digitalarchives.hida.core.model.digitalobject.DefaultFileObjectDataOnDemand;

import java.util.List;
import java.util.Set;

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
 * Integration test class for a {@link Event} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class EventIntegrationTest
extends AbstractTransactionalTestNGSpringContextTests {

	/**
	 * Helper class used to test the {@link Event} model class methods.
	 * Purposely using {@link DefaultFileObjectDataOnDemand} because when a 
	 * {@link DefaultFileObject} is persisted, it also persists Events.
	 */
	@Autowired // Marking '@Autowired' so that this DefaultFileObjectOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    DefaultFileObjectDataOnDemand dod;
	
	// The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;
	
	/**
	 * Initializes our in-memory database with {@link Event} objects to be used
	 * for testing purposes.
	 */
	@BeforeClass(alwaysRun = true)
	public void setup() {
		dod.init(INITIAL_DB_SIZE);
	}
	
	
	
	/**
	 * Tests to see if the {@link Event} count method works correctly.
	 */
	@Test
    public void testCountEvents() {
        long count = Event.countEvents();
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'Event' " +
        		"incorrectly reported the count.");
    }

	
	
	/**
	 * Tests a {@link Event} FindAllEvents method.
	 */
	@Test
    public void testFindAllEvents() {
        List<Event> result = Event.findAllEvents();
        Assert.assertNotNull(result, "Find all method for 'Event' illegally" +
        		" returned null");
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for 'Event'" +
        		" failed to return the correct amount of data.");
    }

	
	
	/**
	 * Throughly testing the grabbing of a {@link Event} from the database.
	 */
	@Test
    public void testFindEvent() {
	    DefaultFileObject obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificDefaultFileObject(i);
            String id = obj.getEvents().iterator().next().getPrimaryId().toString();
            Event obj2 = Event.findEvent(id);
            Assert.assertNotNull(obj2, "Find method for 'Event' illegally" +
            		" returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getPrimaryId().toString(), "Find method for 'Event'" +
            		" returned the incorrect identifier");
        }
    }

	
	
	/**
	 * Tests the FindEventEntries method of an {@link Event}.
	 */
	@Test
    public void testFindEventEntries() {
        long count = Event.countEvents();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Event> result = Event
        		.findEventEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'Event'" +
        		" illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for " +
        		"'Event' returned an incorrect number of entries");
    }

	
	
	/**
	 * Testing to see if our flush method is working as intended.
	 */
	@Test
    public void testFlush() {
        Event obj = dod.getRandomDefaultFileObject().getEvents().iterator().next();
        String id = obj.getPrimaryId().toString();
        obj = Event.findEvent(id);
        boolean modified = false;
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue((currentVersion != null && obj.getVersion() 
        		> currentVersion) || !modified, 
        		"Version for 'Event' failed to increment on flush directive");
    }

	
	
	/**
	 * Tests gets for a {@link DefaultFileObject} from the database.
	 */
	@Test
	public void testGetRandomDefaultFileObject() {
		// Grabbing a random RecordsTransmittalPlans from the database.
		for (int i=0; i < INITIAL_DB_SIZE; i++) {
			assertNotNull(dod.getRandomDefaultFileObject()); 
		}
	}

	
	
	/**
	 * Testing if our merge method is working as intended.
	 */
	@Test
    public void testMergeUpdate() {
        Event obj = dod.getRandomDefaultFileObject().getEvents().iterator().next();
        String id = obj.getPrimaryId().toString();
        obj = Event.findEvent(id);
        boolean modified =  false;
        Integer currentVersion = obj.getVersion();
        Event merged = obj.merge();
        obj.flush();
        
        Assert.assertEquals(merged.getPrimaryId().toString(), id, "Identifier of " +
        		"merged object not the same as identifier of original object");
        
        Assert.assertTrue((currentVersion != null 
        		&& obj.getVersion() > currentVersion) || !modified, 
        		"Version for 'Event' failed to increment" +
        		" on merge and flush directive");
    }

	
	
	/**
	 * Testing a persist to the database.
	 */
	@Test
    public void testPersist() {  
        Event obj = dod.getNewTransientDefaultFileObject(Integer.MAX_VALUE)
		.getEvents().iterator().next();
        
        Assert.assertNotNull(obj, "Data on demand for 'Event' " +
        		"failed to provide a new transient entity");
  
        try {
            obj.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            Assert.fail(msg);
        }
        // Synchronizing our persistence context to the database.
        obj.flush();
        
        // Grabbing a count of the DefaultFileObjects that are currently in the database.
        long count = Event.countEvents();
        
        // The persist should've incremented the count of 
        // Events in the DB from 10 to 11.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
        		"increment."); 
    }

	
	
	/**
	 * Tests to see if our remove method is working properly. 
	 */
	@Test
    public void testRemove() {
	    DefaultFileObject digitalObject = dod.getRandomDefaultFileObject();
	    Set<Event> set = digitalObject.getEvents();
	    Event event = set.iterator().next();
	    String id = event.getPrimaryId();
	    event.remove();
        set.remove(event);
        digitalObject.flush();
        event.flush();
        
        Assert.assertNull(Event.findEvent(id), "Failed to" +
        		" remove 'StorageEntry' with identifier '" + id + "'");
    }
}
