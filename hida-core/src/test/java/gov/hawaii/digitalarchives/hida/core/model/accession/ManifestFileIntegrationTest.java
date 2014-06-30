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
 * Integration test class for a ManifestFile model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class ManifestFileIntegrationTest extends
        AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the ManifestFile model class methods.
     */
    @Autowired
    // Marking '@Autowired' so that this ManifestFileOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    ManifestFileDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with ManifestFile objects to be used
     * for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    /**
     * Tests to see if the ManifestFile count method works correctly.
     */
    @Test
    public void testCountManifestFiles() {
        long count = ManifestFile.countManifestFiles();

        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'ManifestFile' incorrectly "
                        + "reported the amount of entries");
    }

    /**
     * Throughly testing the grabbing of a ManifestFile from the database.
     */
    @Test
    public void testFindManifestFile() {
        ManifestFile file = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            file = dod.getSpecificManifestFile(i);
            Long id = file.getPrimaryId();
            file = ManifestFile.findManifestFile(id);
            Assert.assertNotNull(file,
                    "Find method for 'ManifestFile' illegally returned null for id '" + id + "'");
            Assert.assertEquals(id, file.getPrimaryId(),
                    "Find method for 'ManifestFile' returned the incorrect identifier");
        }
    }

    /**
     * Tests a ManifestFile find all method.
     */
    @Test
    public void testFindAllManifestFiles() {
        List<ManifestFile> result = ManifestFile.findAllManifestFiles();

        Assert.assertNotNull(result, "Find all method for 'ManifestFile' "
                + "illegally returned null");

        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for"
                + " 'ManifestFile' returned the incorrect amount of entries.");
    }

    /**
     * Tests the findManifestFileEntries method of a ManifestFile.
     */
    @Test
    public void testFindManifestFileEntries() {
        long count = ManifestFile.countManifestFiles();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ManifestFile> result = ManifestFile.findManifestFileEntries(
                firstResult, maxResults);

        Assert.assertNotNull(result, "Find entries method for 'ManifestFile'"
                + " illegally returned null");

        Assert.assertEquals(count, result.size(),
                "Find entries method for 'ManifestFile'"
                        + " returned an incorrect number of entries");
    }

    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        ManifestFile file = dod.getRandomManifestFile();
        long id = file.getPrimaryId();
        file = ManifestFile.findManifestFile(id);
        Assert.assertNotNull(file, "Find method for 'ManifestFile' "
                + "illegally returned null for id '" + id + "'");
        boolean modified = dod.modifyManifestFile(file);
        Integer currentVersion = file.getVersion();
        file.flush();

        Assert.assertTrue(
                (currentVersion != null && file.getVersion() > currentVersion)
                        || !modified, "Version for 'ManifestFile' failed "
                        + "to increment on flush directive");
    }

    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        ManifestFile obj = dod.getRandomManifestFile();
        Long id = obj.getPrimaryId();
        obj = ManifestFile.findManifestFile(id);
        boolean modified = dod.modifyManifestFile(obj);
        Integer currentVersion = obj.getVersion();
        ManifestFile merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object"
                        + " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null 
                && obj.getVersion() > currentVersion)
                || !modified, "Version for 'ManifestFile' failed to increment " +
                        "on merge and flush directive");
    }

    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        ManifestFile file = dod.getNewTransientManifestFile(Integer.MAX_VALUE);

        try {
            file.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        file.flush();

        // Grabbing a count of the ManifestFiles that are currently in the
        // database.
        long count = ManifestFile.countManifestFiles();

        // The persist should've incremented the count of
        // ManifestFiles in the database.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't "
                + "increment.");
    }

    /**
     * Tests to see if our remove method is working properly.
     */
    @Test
    public void testRemove() {
        ManifestFile file = dod.getRandomManifestFile();
        long id = file.getPrimaryId();
        file = ManifestFile.findManifestFile(id);
        file.remove();
        file.flush();

        Assert.assertNull(ManifestFile.findManifestFile(id), "Failed to remove 'ManifestFile' " +
                "with identifier '" + id + "'");
    }

}