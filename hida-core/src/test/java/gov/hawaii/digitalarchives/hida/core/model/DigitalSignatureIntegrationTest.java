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
 * Integration test class for a {@link DigitalSignature} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class DigitalSignatureIntegrationTest 
	extends AbstractTransactionalTestNGSpringContextTests {


	/**
	 * Helper class used to test the {@link DigitalSignature} model class 
	 * methods. Purposely using {@link DefaultFileObjectDataOnDemand} because 
	 * when a {@link DefaultFileObject} is persisted, it also persists 
	 * DigitalSignatures.
	 */
	@Autowired // Marking '@Autowired' so that this DefaultFileObjectOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
	DefaultFileObjectDataOnDemand dod;
	
	// The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;

	/**
	 * Initializes our in-memory database with {@link DigitalSignature} objects
	 * to be used for testing purposes.
	 */
	@BeforeClass(alwaysRun = true)
	public void setup() {
		dod.init(INITIAL_DB_SIZE);
	}
	
	
	
	/**
	 * Tests to see if the {@link DigitalSignature} count method works 
	 * correctly.
	 */
	@Test
    public void testCountDigitalSignatures() {
        long count = DigitalSignature.countDigitalSignatures();
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'DigitalSignature' " +
        		"incorrectly reported the amount of entries");
    }
	
	
	
	/**
	 * Tests a {@link DigitalSignature} find all method.
	 */
	@Test
    public void testFindAllDigitalSignatures() {
        List<DigitalSignature> result = DigitalSignature
        		.findAllDigitalSignatures();
        
        Assert.assertNotNull(result, "Find all method for 'DigitalSignature'" +
        		" illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for " +
        		"'DigitalSignature' failed to return any data");
    }

	
	
	/**
	 * Throughly testing the grabbing of a {@link DigitalSignature} from the 
	 * database.
	 */
	@Test
    public void testFindDigitalSignature() {
	    DefaultFileObject obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificDefaultFileObject(i);
            Long id = obj.getDigitalSignature().getPrimaryId();
            DigitalSignature obj2 = DigitalSignature.findDigitalSignature(id);
            Assert.assertNotNull(obj2, "Find method for 'DigitalSignature' illegally" +
            		" returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getPrimaryId(), "Find method for 'DigitalSignature'" +
            		" returned the incorrect identifier");
        }
    }
	

	
	/**
	 * Tests the findDigitalSignatureEntries method of a 
	 * {@link DigitalSignature}.
	 */
	@Test
    public void testFindDigitalSignatureEntries() {
        long count = DigitalSignature.countDigitalSignatures();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<DigitalSignature> result = DigitalSignature
        		.findDigitalSignatureEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'DigitalSignature'" +
        		" illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for " +
        		"'DigitalSignature' returned an incorrect number of entries");
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