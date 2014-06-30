package gov.hawaii.digitalarchives.hida.core.model;
import static org.testng.Assert.assertNotNull;
import gov.hawaii.digitalarchives.hida.core.model.digitalobject.DefaultFileObject;
import gov.hawaii.digitalarchives.hida.core.model.digitalobject.DefaultFileObjectDataOnDemand;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration test class for a {@link HashValue} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class HashValueIntegrationTest 
	extends AbstractTransactionalTestNGSpringContextTests {

	/**
	 * Helper class used to test the {@link HashValue} model class methods.
	 * Purposely using {@link DefaultFileObjectDataOnDemand} because when a 
	 * {@link DefaultFileObject} is persisted, it also persists HashValues.
	 */
	@Autowired // Marking '@Autowired' so that this DefaultFileObjectOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
	DefaultFileObjectDataOnDemand dod;
	
	// The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;
	
	/**
	 * Initializes our in-memory database with {@link HashValue} objects to be 
	 * used for testing purposes.
	 */
	@BeforeClass(alwaysRun = true)
	public void setup() {
		dod.init(INITIAL_DB_SIZE);
	}
	
	
	
	/**
	 * Tests to see if the HashValue count method works correctly.
	 */
	@Test
    public void testCountHashValues() {
        long count = HashValue.countHashValues();
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'HashValue' " +
        		"incorrectly reported the amount of entries");
    }
	
	
	
	/**
	 * Tests a {@link HashValue} find all method.
	 */
	@Test
    public void testFindAllHashValues() {
        List<HashValue> result = HashValue.findAllHashValues();
        Assert.assertNotNull(result, "Find all method for 'HashValue' illegally" +
        		" returned null");
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
        		" 'HashValue' failed to return the correct amount of data");
    }

	
	
	/**
	 * Throughly testing the grabbing of a {@link HashValue} from the database.
	 */
	@Test
    public void testFindHashValue() {
	    DefaultFileObject obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificDefaultFileObject(i);
            Long id = obj.getHash().getId();
            HashValue obj2 = HashValue.findHashValue(id);
            Assert.assertNotNull(obj2, "Find method for 'HashValue' illegally" +
            		" returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getId(), "Find method for 'HashValue'" +
            		" returned the incorrect identifier");
        }
    }

	
	
	/**
	 * Tests the findHashValueEntries method of a {@link HashValue}.
	 */
	@Test
    public void testFindHashValueEntries() {
        long count = HashValue.countHashValues();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<HashValue> result = HashValue
        		.findHashValueEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'HashValue'" +
        		" illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for 'HashValue'" +
        		" returned an incorrect number of entries");
    }

	
	
	/**
	 * Tests gets for a {@link DefaultFileObject} from the database.
	 */
	@Test
	public void testGetRandomDefaultFileObject() {
		// Grabbing a random DefaultFileObjects from the database.
		for (int i=0; i < INITIAL_DB_SIZE; i++) {
			assertNotNull(dod.getRandomDefaultFileObject()); 
		}
	}
}