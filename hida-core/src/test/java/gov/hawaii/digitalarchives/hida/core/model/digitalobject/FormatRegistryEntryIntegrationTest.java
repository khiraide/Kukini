package gov.hawaii.digitalarchives.hida.core.model.digitalobject;

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
 * Integration test class for a {@link FormatRegistryEntry} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class FormatRegistryEntryIntegrationTest
extends AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the {@link FormatRegistryEntry} model class 
     * methods. Purposely using {@link DefaultFileObjectDataOnDemand} because 
     * when a {@link DefaultFileObject} is persisted, it also persists 
     * FormatRegistryEntries.
     */
    @Autowired // Marking '@Autowired' so that this DefaultFileObjectOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    DefaultFileObjectDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;
    
    /**
     * Initializes our in-memory database with {@link FormatRegistryEntry} 
     * objects to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }
    
    
    
    /**
     * Tests to see if the {@link FormatRegistryEntry} count method works 
     * correctly.
     */
    @Test
    public void testCountFormatRegistryEntries() {
        long count = FormatRegistryEntry.countFormatRegistryEntries();
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'FormatRegistryEntry' " +
                "incorrectly reported the count.");
    }

    
    
    /**
     * Tests a {@link FormatRegistryEntry} find all method.
     */
    @Test
    public void testFindAllFormatRegistryEntries() {
        List<FormatRegistryEntry> result = FormatRegistryEntry.findAllFormatRegistryEntries();
        Assert.assertNotNull(result, "Find all method for 'FormatRegistryEntry' illegally" +
                " returned null");
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for " +
                "'FormatRegistryEntry' failed to return the correct amount of data.");
    }

    
    
    /**
     * Throughly testing the grabbing of a {@link FormatRegistryEntry} from 
     * the database.
     */
    @Test
    public void testFindFormatRegistryEntry() {
        DefaultFileObject obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificDefaultFileObject(i);
            String id = obj.getFormatInformation().getPrimaryId();
            FormatRegistryEntry obj2 = FormatRegistryEntry.findFormatRegistryEntry(id);
            Assert.assertNotNull(obj2, "Find method for 'FormatRegistryEntry' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getPrimaryId(), "Find method for 'FormatRegistryEntry'" +
                    " returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests the findFormatRegistryEntries method of a
     * {@link FormatRegistryEntry}.
     */
    @Test
    public void testFindFormatRegistryEntryEntries() {
        long count = FormatRegistryEntry.countFormatRegistryEntries();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<FormatRegistryEntry> result = FormatRegistryEntry
                .findFormatRegistryEntryEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'FormatRegistryEntry'" +
                " illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for " +
                "'FormatRegistryEntry' returned an incorrect number of entries");
    }
}