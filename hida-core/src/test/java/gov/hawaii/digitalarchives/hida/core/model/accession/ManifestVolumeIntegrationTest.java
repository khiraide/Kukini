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
 * Integration test class for a ManifestVolume model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class ManifestVolumeIntegrationTest extends
        AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the ManifestVolume model class methods.
     */
    @Autowired
    // Marking '@Autowired' so that this ManifestVolumeOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    ManifestVolumeDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with ManifestVolume objects to be used
     * for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    /**
     * Tests to see if the ManifestVolume count method works correctly.
     */
    @Test
    public void testCountManifestVolumes() {
        long count = ManifestVolume.countManifestVolumes();

        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'ManifestVolume' " +
                "incorrectly reported the amount of entries");
    }

    /**
     * Throughly testing the grabbing of a ManifestVolume from the database.
     */
    @Test
    public void testFindManifestVolume() {
        ManifestVolume volume = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            volume = dod.getSpecificManifestVolume(i);
            Long id = volume.getPrimaryId();
            volume = ManifestVolume.findManifestVolume(id);
            Assert.assertNotNull(volume,
                    "Find method for 'ManifestVolume' illegally "
                            + "returned null for id '" + id + "'");
            Assert.assertEquals(id, volume.getPrimaryId(),
                    "Find method for 'ManifestVolume'"
                            + " returned the incorrect identifier");
        }
    }

    /**
     * Tests a ManifestVolume find all method.
     */
    @Test
    public void testFindAllManifestVolumes() {
        List<ManifestVolume> result = ManifestVolume.findAllManifestVolumes();

        Assert.assertNotNull(result, "Find all method for 'ManifestVolume' "
                + "illegally returned null");

        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for"
                + " 'ManifestVolume' returned the incorrect amount of entries.");
    }

    /**
     * Tests the findManifestVolumeEntries method of a ManifestVolume.
     */
    @Test
    public void testFindManifestVolumeEntries() {
        long count = ManifestVolume.countManifestVolumes();
        if (count > 20)
            count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ManifestVolume> result = ManifestVolume.findManifestVolumeEntries(
                firstResult, maxResults);

        Assert.assertNotNull(result, "Find entries method for 'ManifestVolume'"
                + " illegally returned null");

        Assert.assertEquals(count, result.size(),
                "Find entries method for 'ManifestVolume'"
                        + " returned an incorrect number of entries");
    }

    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        ManifestVolume volume = dod.getRandomManifestVolume();
        long id = volume.getPrimaryId();
        volume = ManifestVolume.findManifestVolume(id);
        Assert.assertNotNull(volume, "Find method for 'ManifestVolume' "
                + "illegally returned null for id '" + id + "'");
        boolean modified = dod.modifyManifestVolume(volume);
        Integer currentVersion = volume.getVersion();
        volume.flush();

        Assert.assertTrue(
                (currentVersion != null && volume.getVersion() > currentVersion)
                        || !modified, "Version for 'ManifestVolume' failed "
                        + "to increment on flush directive");
    }

    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        ManifestVolume obj = dod.getRandomManifestVolume();
        Long id = obj.getPrimaryId();
        obj = ManifestVolume.findManifestVolume(id);
        boolean modified = dod.modifyManifestVolume(obj);
        Integer currentVersion = obj.getVersion();
        ManifestVolume merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id,
                "Identifier of merged object"
                        + " not the same as identifier of original object");
        Assert.assertTrue(
                (currentVersion != null && obj.getVersion() > currentVersion)
                        || !modified,
                "Version for 'ManifestVolume' failed to increment on merge "
                        + "and flush directive");
    }

    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        ManifestVolume volume = dod
                .getNewTransientManifestVolume(Integer.MAX_VALUE);
        Assert.assertNotNull(volume,
                "Data on demand for 'ManifestVolume' failed to "
                        + "provide a new transient entity");

        try {
            volume.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        volume.flush();

        // Grabbing a count of the Volumes that are currently in the database.
        long count = ManifestVolume.countManifestVolumes();

        // The persist should've incremented the count of
        // ManifestVolumes in the database.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't "
                + "increment.");
    }

    /**
     * Tests to see if our remove method is working properly.
     */
    @Test
    public void testRemove() {
        ManifestVolume volume = dod.getRandomManifestVolume();
        long id = volume.getPrimaryId();
        volume = ManifestVolume.findManifestVolume(id);
        volume.remove();
        volume.flush();

        Assert.assertNull(ManifestVolume.findManifestVolume(id),
                "Failed to remove " + "'ManifestVolume' with identifier '" + id
                        + "'");
    }
}