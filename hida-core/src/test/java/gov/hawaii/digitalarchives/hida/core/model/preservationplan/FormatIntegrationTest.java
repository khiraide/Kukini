package gov.hawaii.digitalarchives.hida.core.model.preservationplan;

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
 * Integration test class for a {@link FileFormat} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class FormatIntegrationTest 
    extends AbstractTransactionalTestNGSpringContextTests {


    /**
     * Helper class used to test the {@link FileFormat} model class 
     * methods. Purposely using {@link PreservationPlanDataOnDemand} because 
     * when a {@link PreservationPlan} is persisted, it also persists 
     * Formats.
     */
    @Autowired // Marking '@Autowired' so that this PreservationPlanOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    PreservationPlanDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with {@link FileFormat} objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }
    
    
    
    /**
     * Tests to see if the {@link FileFormat} count method works 
     * correctly.
     */
    @Test
    public void testCountFormats() {
        long count = FileFormat.countFormats();
        Assert.assertEquals(count, INITIAL_DB_SIZE * 4, "Counter for 'Format' " +
                "incorrectly reported the amount of entries");
    }
    
    
    
    /**
     * Tests a {@link FileFormat} find all method.
     */
    @Test
    public void testFindAllFormats() {
        List<FileFormat> result = FileFormat.findAllFormats();
        
        Assert.assertNotNull(result, "Find all method for 'Format'" +
                " illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE * 4, "Find all method for " +
                "'Format' failed to return any data");
    }

    
    
    /**
     * Throughly testing the grabbing of a {@link FileFormat} from the 
     * database.
     */
    @Test
    public void testFindFormat() {
        PreservationPlan obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificPreservationPlan(i);
            // Getting a random Format object, just so we can test its findFormat method.
            Long id = obj.getFormatPlans().get("FMT/40_" + i).getNativeFormat().getPrimaryId();
            FileFormat obj2 = FileFormat.findFormat(id);
            Assert.assertNotNull(obj2, "Find method for 'FormatPlan' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getPrimaryId(), "Find method for 'FormatPlan'" +
                    " returned the incorrect identifier");
        }
    }
    

    
    /**
     * Tests the findFormatEntries method of a 
     * {@link FileFormat}.
     */
    @Test
    public void testFindFormatEntries() {
        long count = FileFormat.countFormats();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<FileFormat> result = FileFormat
                .findFormatEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'Format'" +
                " illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for " +
                "'Format' returned an incorrect number of entries");
    }
}