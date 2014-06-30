package gov.hawaii.digitalarchives.hida.core.model.digitalobject;
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
 * Integration test class for a {@link Property} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class PropertyIntegrationTest 
    extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the {@link Property} model class methods.
     * Purposely using {@link DefaultFileObjectDataOnDemand} because when a 
     * {@link DefaultFileObject} is persisted, it also persists Property 
     * instances.
     */
    @Autowired // Marking '@Autowired' so that this DefaultFileObjectOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    DefaultFileObjectDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;
    
    /**
     * Initializes our in-memory database with {@link Property} objects to be
     * used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }
    
    
    
    /**
     * Tests to see if the {@link Property} count method works correctly.
     */
    @Test
    public void testCountPropertys() {
        long count = Property.countProperties();
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'Property' incorrectly" +
                " reported the amount of entries");
    }

    
    
    /**
     * Tests a {@link Property} find all method.
     */
    @Test
    public void testFindAllPropertys() {
        List<Property> result = Property.findAllProperties();
        Assert.assertNotNull(result, "Find all method for 'Property" +
                "' illegally returned null");
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for " +
                "'Property' failed to return the correct amount of data.");
    }

    
    
    /**
     * Throughly testing the grabbing of a {@link Property} from the database.
     */
    @Test
    public void testFindProperty() {
        DefaultFileObject obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificDefaultFileObject(i);
            Long id = obj.getSignificantProperties().get(0).getPrimaryId();
            Property obj2 = Property.findProperty(id);
            Assert.assertNotNull(obj2, "Find method for 'Property' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getPrimaryId(), "Find method for 'Property' returned" +
                    " the incorrect identifier");
        }
    }

    
    
    /**
     * Tests the findDefaultFileObjectEntries method of a {@link Property}.
     */
    @Test
    public void testFindPropertyEntries() {
        long count = Property.countProperties();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Property> result = Property
                .findPropertyEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'Property'" +
                " illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for 'Property'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        Property obj = dod.getRandomDefaultFileObject().getSignificantProperties().get(0);
        Long id = obj.getPrimaryId();
        obj = Property.findProperty(id);
        boolean modified =  false;
        Integer currentVersion = obj.getVersion();
        Property merged = obj.merge();
        obj.flush();
        
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object not " +
                "the same as identifier of original object");
        
        Assert.assertTrue((currentVersion != null 
                && obj.getVersion() > currentVersion) || !modified, 
                "Version for 'StorageEntry' failed to increment" +
                " on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {  
        Property obj = dod.getNewTransientDefaultFileObject(Integer.MAX_VALUE)
        .getSignificantProperties().get(0);
        
        Assert.assertNotNull(obj, "Data on demand for 'Property' " +
                "failed to provide a new transient entity");
        
        // This StorangeEntry has not been persisted to the database
        // yet, so it should not have been given a primary id.
        Assert.assertNull(obj.getPrimaryId(), "Expected 'StorageEntry' identifier to be null");
        try {
            obj.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            Assert.fail(msg);
        }
        // Synchronizing our persistence context to the database.
        obj.flush();
        
        // The Property should now have an id since it has been
        // persisted to the database.
        Assert.assertNotNull(obj.getPrimaryId(), "Expected 'Property' " +
                "identifier to no longer be null");
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        Property obj = dod.getRandomDefaultFileObject()
                .getSignificantProperties().get(0);
        Long id = obj.getPrimaryId();
        obj = Property.findProperty(id);
        obj.remove();
        obj.flush();
        Assert.assertNull(Property.findProperty(id), "Failed to" +
                " remove 'StorageEntry' with identifier '" + id + "'");
    }
}
