package gov.hawaii.digitalarchives.hida.space;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

@ContextConfiguration(locations = {"classpath:/spring/defaultApplicationContext.xml"})
public class WriteNoTxnOpIntegrationTest extends AbstractTestNGSpringContextTests {
    private static final String TEST_SPACE_NAME = "TestSpace";
    private static final String HZ_INSTANCE_NAME = "hzInstance";
	private HazelcastInstance hcInstance;
	private Logger spaceImplLogger;
	private Space space;

	public WriteNoTxnOpIntegrationTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

	@BeforeClass
	public void setUp() {
		hcInstance = (HazelcastInstance) this.applicationContext.getBean(HZ_INSTANCE_NAME);
		space = new SpaceImpl(hcInstance, TEST_SPACE_NAME, spaceImplLogger);
	}

	@AfterMethod
	public void clearAll() {
		for (DistributedObject distObj : hcInstance.getDistributedObjects()) {
			distObj.destroy();
		}
	}

    /**
     * Simple test to see if we can write an object into the space.
     */
	@Test
	public void testWrite() {
		space.write(TestEntry.class, new TestEntry("HIDA", 1), null, Lease.Duration.FOREVER);
		Predicate<String, TestEntry> p = makePredicate("HIDA", 1);
		assertTrue(getMatches(p).size() == 1, "Entry was not written into the space.");
	}

    /**
     * Writing the same exact object to the space n times should result in the
     * space containing n entries that are all equal.
     */
    @Test
    public void testWriteSameObjectMultiple() {
        TestEntry obj = new TestEntry("HIDA", 1);
        final int cntToWrite = 8;
        for (int i = 0; i < cntToWrite; i++) {
            space.write(TestEntry.class, obj, null, Lease.Duration.FOREVER);
        }

        Map<String, TestEntry> map = getTestEntryMap();
        assertEquals(map.size(), cntToWrite, "expected amount of objects were not written to"  +
                " the map.");
    }

    /**
     * Test the expected expiration value of a Lease when a write() is specified
     * with a duration of FOREVER.
     */
    @Test
    public void testLeaseExpireForever() {
        TestEntry obj = new TestEntry("HIDA", 1);

        Lease lease = space.write(TestEntry.class, obj, null, Lease.Duration.FOREVER);

        assertEquals(lease.getExpiration(), Lease.Duration.FOREVER, "Lease does not have correct" +
                " expiration value of FOREVER.");
    }

    /**
     * Test that the expiration of a Lease that is not FOREVER is correct.
     */
    @Test
    public void testLeaseExpire() {
        TestEntry obj = new TestEntry("HIDA", 1);

        long approxExpiration = System.currentTimeMillis() + 5000L;
        Lease lease = space.write(TestEntry.class, obj, null, 5000L);

        assertTrue(lease.getExpiration() != Lease.Duration.FOREVER, "Lease expiration is" +
                " FOREVER, when it was not specified to be so.");
        assertTrue(lease.getExpiration() >= approxExpiration, "Actual lease experiation is" +
                " less than the expected value.");
    }

    /**
     * Simple test to ensure that we are able to renew a lease given that it's
     * possible to do so.
     */
    @Test
    public void testRenew() {
        TestEntry entry = new TestEntry("HiDA", 1);

        Lease lease = space.write(TestEntry.class, entry, null, 5000L);
        lease.renew(Lease.Duration.FOREVER);

        assertEquals(lease.getExpiration(), Lease.Duration.FOREVER, "Lease expiration was not" +
                " updated to correct Duration.FOREVER, as expected.");
    }

    /**
     * Test to ensure that when the key is locked, the lease time is not
     * renewed after a called to renew().
     */
    @Test
    public void testRenewKeyLocked() throws InterruptedException {
        TestEntry entry = new TestEntry("HiDA", 1);

        final NoTxnLease lease = (NoTxnLease) space.write(TestEntry.class, entry, null, 5000L);

        final IMap<String, TestEntry> map = hcInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
        // Key needs to be locked from a different thread.
        Thread t = new Thread() {
            @Override
            public void run() {
                map.lock(lease.getKey().toString());
            }
        };

        t.start();
        // wait a bit to make sure t is running
        Thread.sleep(128L);
        lease.renew(Lease.Duration.FOREVER);
        assertTrue(lease.getExpiration() != Lease.Duration.FOREVER, "We were able to renew the" +
                " lease when the key was locked!");
        t.join();
    }

	@SuppressWarnings("unchecked")
	public Predicate<String, TestEntry> makePredicate(String fooVal, Integer idVal) {
		EntryObject e = new PredicateBuilder().getEntryObject();
		return (Predicate<String, TestEntry>) e.get("foo").equal(fooVal).and(e.get("id").equal(idVal));
	}

	Set<Map.Entry<String, TestEntry>>  getMatches(Predicate<String, TestEntry> predicate) {
		IMap<String, TestEntry> map = hcInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
		return map.entrySet(predicate);
	}

    Map<String, TestEntry> getTestEntryMap() {
        return hcInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
    }
}
