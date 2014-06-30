package gov.hawaii.digitalarchives.hida.core.model.accession;

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
 * Integration test class for a Accession model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class AccessionIntegrationTest extends
        AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the Accession model class methods.
     */
    @Autowired
    // Marking '@Autowired' so that this AccessionOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    AccessionDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with Accession objects to be used for
     * testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    /**
     * Tests to see if the Accession count method works correctly.
     */
    @Test
    public void testCountAccessions() {
        long count = Accession.countAccessions();

        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'Accession' incorrectly "
                + "reported the amount of entries");
    }

    /**
     * Throughly testing the grabbing of a Accession from the database.
     */
    @Test
    public void testFindAccession() {
        Accession accession = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            accession = dod.getSpecificAccession(i);
            String id = accession.getPrimaryId().toString();
    
            accession = Accession.findAccession(id);
    
            Assert.assertNotNull(accession, "Find method for 'Accession' illegally"
                    + " returned null for id '" + id + "'");
    
            Assert.assertEquals(id, accession.getPrimaryId().toString(),
                    "Find method for 'Accession' "
                            + "returned the incorrect identifier");
        }
    }

    /**
     * Tests a Accession find all method.
     */
    @Test
    public void testFindAllAccessions() {
        List<Accession> result = Accession.findAllAccessions();

        Assert.assertNotNull(result, "Find all method for 'Accession' "
                + "illegally returned null");

        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, 
                "Find all method for 'Accession' returned the incorrect amount of entries.");
    }

    /**
     * Tests the findAccessionEntries method of a Accession.
     */
    @Test
    public void testFindAccessionEntries() {
        long count = Accession.countAccessions();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Accession> result = Accession.findAccessionEntries(firstResult,
                maxResults);

        Assert.assertNotNull(result, "Find entries method for 'Accession'"
                + " illegally returned null");

        Assert.assertEquals(count, result.size(),
                "Find entries method for 'Accession'"
                        + " returned an incorrect number of entries");
    }

    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        Accession accession = dod.getRandomAccession();
        String id = accession.getPrimaryId().toString();
        accession = Accession.findAccession(id);
        Assert.assertNotNull(accession, "Find method for 'Accession' "
                + "illegally returned null for id '" + id + "'");
        boolean modified = dod.modifyAccession(accession);
        Integer currentVersion = accession.getVersion();
        accession.flush();

        Assert.assertTrue(
                (currentVersion != null && accession.getVersion() > currentVersion)
                        || !modified, "Version for 'Accession' failed "
                        + "to increment on flush directive");
    }

    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        Accession accession = dod.getRandomAccession();
        String id = accession.getPrimaryId().toString();
        accession = Accession.findAccession(id);
        boolean modified = dod.modifyAccession(accession);
        Integer currentVersion = accession.getVersion();
        Accession merged = accession.merge();
        accession.flush();

        Assert.assertEquals(merged.getPrimaryId().toString(), id,
                "Identifier of merged object not the"
                        + " same as identifier of original object");

        Assert.assertTrue(
                (currentVersion != null && accession.getVersion() > currentVersion)
                        || !modified,
                "Version for 'Accession' failed to increment "
                        + "on merge and flush directive");
    }

    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        Accession accession = dod.getNewTransientAccession(Integer.MAX_VALUE);
        Assert.assertNotNull(accession,
                "Data on demand for 'Accession' failed to "
                        + "provide a new transient entity");

        try {
            accession.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        accession.flush();

        // Grabbing a count of the Accessions that are currently in the
        // database.
        long count = Accession.countAccessions();

        // The persist should've incremented the count of
        // Accessions in the database.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't "
                + "increment.");
    }

    /**
     * Tests to see if our remove method is working properly.
     */
    @Test
    public void testRemove() {
        Accession accession = dod.getRandomAccession();
        String id = accession.getPrimaryId().toString();
        accession = Accession.findAccession(id);
        accession.remove();
        accession.flush();

        Assert.assertNull(Accession.findAccession(id), "Failed to remove "
                + "'Accession' with identifier '" + id + "'");
    }

}
