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
 * Integration test class for a ManifestDrive model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class ManifestDriveIntegrationTest extends
        AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the ManifestDrive model class methods.
     */
    @Autowired
    // Marking '@Autowired' so that this ManifestDriveOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    ManifestDriveDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with ManifestDrive objects to be used
     * for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    /**
     * Tests to see if the ManifestDrive count method works correctly.
     */
    @Test
    public void testCountManifestDrives() {
        long count = ManifestDrive.countManifestDrives();

        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'ManifestDrive' incorrectly "
                        + "reported the amount of entries");
    }

    /**
     * Throughly testing the grabbing of a ManifestDrive from the database.
     */
    @Test
    public void testFindManifestDrive() {
        ManifestDrive manifestDrive = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            manifestDrive = dod.getSpecificManifestDrive(i);
            Long id = manifestDrive.getPrimaryId();
            manifestDrive = ManifestDrive.findManifestDrive(id);
            Assert.assertNotNull(manifestDrive,
                    "Find method for 'ManifestDrive' illegally returned null for id '"
                            + id + "'");
            Assert.assertEquals(id, manifestDrive.getPrimaryId(),
                    "Find method for 'ManifestDrive' returned the incorrect identifier");
        }
    }

    /**
     * Tests a ManifestDrive find all method.
     */
    @Test
    public void testFindAllManifestDrives() {
        List<ManifestDrive> result = ManifestDrive.findAllManifestDrives();

        Assert.assertNotNull(result, "Find all method for 'ManifestDrive' "
                + "illegally returned null");

        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for"
                + " 'ManifestDrive' returned the incorrect amount of entries.");
    }

    /**
     * Tests the findManifestDriveEntries method of a ManifestDrive.
     */
    @Test
    public void testFindManifestDriveEntries() {
        long count = ManifestDrive.countManifestDrives();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ManifestDrive> result = ManifestDrive
                .findManifestDriveEntries(firstResult, maxResults);

        Assert.assertNotNull(result, "Find entries method for 'ManifestDrive'"
                + " illegally returned null");

        Assert.assertEquals(count, result.size(), "Find entries method for 'ManifestDrive'"
                        + " returned an incorrect number of entries");
    }

    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        ManifestDrive manifestDrive = dod.getRandomManifestDrive();
        long id = manifestDrive.getPrimaryId();
        manifestDrive = ManifestDrive.findManifestDrive(id);
        Assert.assertNotNull(manifestDrive, "Find method for 'ManifestDrive' "
                + "illegally returned null for id '" + id + "'");
        boolean modified = dod.modifyManifestDrive(manifestDrive);
        Integer currentVersion = manifestDrive.getVersion();
        manifestDrive.flush();

        Assert.assertTrue((currentVersion != null && manifestDrive.getVersion() > currentVersion)
                || !modified, "Version for 'ManifestDrive' failed "
                        + "to increment on flush directive");
    }

    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        ManifestDrive obj = dod.getRandomManifestDrive();
        Long id = obj.getPrimaryId();
        obj = ManifestDrive.findManifestDrive(id);
        boolean modified = dod.modifyManifestDrive(obj);
        Integer currentVersion = obj.getVersion();
        ManifestDrive merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object"
                        + " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null && obj.getVersion() > currentVersion)
                || !modified, "Version for 'ManifestDrive' failed to increment " +
                        "on merge and flush directive");
    }

    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        ManifestDrive manifestDrive = dod
                .getNewTransientManifestDrive(Integer.MAX_VALUE);

        try {
            manifestDrive.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        manifestDrive.flush();

        // Grabbing a count of the ManifestDrives that are currently in the
        // database.
        long count = ManifestDrive.countManifestDrives();

        // The persist should've incremented the count of
        // ManifestDrives in the database.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't "
                + "increment.");
    }

    /**
     * Tests to see if our remove method is working properly.
     */
    @Test
    public void testRemove() {
        ManifestDrive manifestDrive = dod.getRandomManifestDrive();
        long id = manifestDrive.getPrimaryId();
        manifestDrive = ManifestDrive.findManifestDrive(id);
        manifestDrive.remove();
        manifestDrive.flush();

        Assert.assertNull(ManifestDrive.findManifestDrive(id),
                "Failed to remove " + "'ManifestDrive' with identifier '" + id
                        + "'");
    }

}