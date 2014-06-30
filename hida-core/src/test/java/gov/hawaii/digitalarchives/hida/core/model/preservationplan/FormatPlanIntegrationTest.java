package gov.hawaii.digitalarchives.hida.core.model.preservationplan;

import static org.testng.Assert.assertNotNull;

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
 * Integration test class for a {@link FormatPlan} model class.
 * 
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
@Configurable
public class FormatPlanIntegrationTest 
    extends AbstractTransactionalTestNGSpringContextTests {


    /**
     * Helper class used to test the {@link FormatPlan} model class 
     * methods. Purposely using {@link PreservationPlanDataOnDemand} because 
     * when a {@link PreservationPlan} is persisted, it also persists 
     * FormatPlans.
     */
    @Autowired // Marking '@Autowired' so that this PreservationPlanOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    PreservationPlanDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests.
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with {@link FormatPlan} objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }
    
    
    
    /**
     * Tests to see if the {@link FormatPlan} count method works 
     * correctly.
     */
    @Test
    public void testCountFormatPlans() {
        long count = FormatPlan.countFormatPlans();
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'FormatPlan' " +
                "incorrectly reported the amount of entries");
    }
    
    
    
    /**
     * Tests a {@link FormatPlan} find all method.
     */
    @Test
    public void testFindAllFormatPlans() {
        List<FormatPlan> result = FormatPlan.findAllFormatPlans();
        
        Assert.assertNotNull(result, "Find all method for 'FormatPlan'" +
                " illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for " +
                "'FormatPlan' failed to return any data");
    }

    
    
    /**
     * Throughly testing the grabbing of a {@link FormatPlan} from the 
     * database.
     */
    @Test
    public void testFindFormatPlan() {
        PreservationPlan obj = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            obj = dod.getSpecificPreservationPlan(i);
            String id = obj.getFormatPlans().get("FMT/40_" + i).getPrimaryId();
            FormatPlan obj2 = FormatPlan.findFormatPlan(id);
            Assert.assertNotNull(obj2, "Find method for 'FormatPlan' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, obj2.getPrimaryId(), "Find method for 'FormatPlan'" +
                    " returned the incorrect identifier");
        }
    }
    

    
    /**
     * Tests the findFormatPlanEntries method of a 
     * {@link FormatPlan}.
     */
    @Test
    public void testFindFormatPlanEntries() {
        long count = FormatPlan.countFormatPlans();
        if (count > 20) 
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<FormatPlan> result = FormatPlan
                .findFormatPlanEntries(firstResult, maxResults);
        Assert.assertNotNull(result, "Find entries method for 'FormatPlan'" +
                " illegally returned null");
        Assert.assertEquals(count, result.size(), "Find entries method for " +
                "'FormatPlan' returned an incorrect number of entries");
    }

    
    
    /**
     * Tests gets for a {@link PreservationPlan} from the database.
     */
    @Test
    public void testGetRandomPreservationPlan() {
        // Grabbing a random PreservationPlans from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomPreservationPlan()); 
        }
    }
}
