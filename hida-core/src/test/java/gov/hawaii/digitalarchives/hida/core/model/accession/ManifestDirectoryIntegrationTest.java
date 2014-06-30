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
 * Integration test class for a ManifestDirectory model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class ManifestDirectoryIntegrationTest extends
        AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the ManifestDirectory model class methods.
     */
    @Autowired
    // Marking '@Autowired' so that this ManifestDirectoryOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    ManifestDirectoryDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with ManifestDirectory objects to be
     * used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    /**
     * Tests to see if the ManifestDirectory count method works correctly.
     */
    @Test
    public void testCountManifestDirectories() {
        long count = ManifestDirectory.countManifestDirectories();

        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'ManifestDirectory' " +
                "incorrectly reported the amount of entries");
    }

    /**
     * Throughly testing the grabbing of a ManifestDirectory from the database.
     */
    @Test
    public void testFindManifestDirectory() {
        ManifestDirectory directory = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            directory = dod.getSpecificManifestDirectory(i);
            Long id = directory.getPrimaryId();
            directory = ManifestDirectory.findManifestDirectory(id);
            Assert.assertNotNull(directory,
                    "Find method for 'Volume' illegally returned"
                            + " null for id '" + id + "'");
            Assert.assertEquals(id, directory.getPrimaryId(),
                    "Find method for 'Volume' "
                            + "returned the incorrect identifier");
        }
    }

    /**
     * Tests a ManifestDirectory find all method.
     */
    @Test
    public void testFindAllManifestDirectories() {
        List<ManifestDirectory> result = ManifestDirectory
                .findAllManifestDirectories();

        Assert.assertNotNull(result, "Find all method for 'ManifestDirectory' "
                + "illegally returned null");

        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for 'ManifestDirectory'" +
                " returned the incorrect amount of entries.");
    }

    /**
     * Tests the findManifestDirectoryEntries method of a ManifestDirectory.
     */
    @Test
    public void testFindManifestDirectoryEntries() {
        long count = ManifestDirectory.countManifestDirectories();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ManifestDirectory> result = ManifestDirectory
                .findManifestDirectoryEntries(firstResult, maxResults);

        Assert.assertNotNull(result,"Find entries method for 'ManifestDirectory'"
                        + " illegally returned null");

        Assert.assertEquals(count, result.size(),"Find entries method for 'ManifestDirectory'"
                        + " returned an incorrect number of entries");
    }

    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        ManifestDirectory directory = dod.getRandomManifestDirectory();
        long id = directory.getPrimaryId();
        directory = ManifestDirectory.findManifestDirectory(id);
        Assert.assertNotNull(directory, "Find method for 'ManifestDirectory' "
                + "illegally returned null for id '" + id + "'");
        boolean modified = dod.modifyManifestDirectory(directory);
        Integer currentVersion = directory.getVersion();
        directory.flush();

        Assert.assertTrue((currentVersion != null 
                        && directory.getVersion() > currentVersion)
                        || !modified, "Version for 'ManifestDirectory' failed "
                        + "to increment on flush directive");
    }

    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        ManifestDirectory obj = dod.getRandomManifestDirectory();
        Long id = obj.getPrimaryId();
        obj = ManifestDirectory.findManifestDirectory(id);
        boolean modified = dod.modifyManifestDirectory(obj);
        Integer currentVersion = obj.getVersion();
        ManifestDirectory merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object"
                        + " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null 
                && obj.getVersion() > currentVersion)
                || !modified,"Version for 'ManifestDirectory' failed to increment on "
                        + "merge and flush directive");
    }

    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        ManifestDirectory directory = dod
                .getNewTransientManifestDirectory(Integer.MAX_VALUE);
        Assert.assertNotNull(directory,
                "Data on demand for 'ManifestDirectory' failed to "
                        + "provide a new transient entity");
        try {
            directory.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        directory.flush();

        // Grabbing a count of the Directories that are currently in the
        // database.
        long count = ManifestDirectory.countManifestDirectories();

        // The persist should've incremented the count of
        // Directories in the database.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't "
                + "increment.");
    }

    /**
     * Tests to see if our remove method is working properly.
     */
    @Test
    public void testRemove() {
        ManifestDirectory directory = dod.getRandomManifestDirectory();
        long id = directory.getPrimaryId();
        directory = ManifestDirectory.findManifestDirectory(id);
        directory.remove();
        directory.flush();

        Assert.assertNull(ManifestDirectory.findManifestDirectory(id),
                "Failed to remove 'ManifestDirectory' with identifier '"
                        + id + "'");
    }

}
