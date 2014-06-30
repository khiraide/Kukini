package gov.hawaii.digitalarchives.hida.space;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;


/**
 * Unit Tests for Write Operations not under a transaction.
 */
public class WriteNoTxnUnitTest {
    private static final String TEST_SPACE_NAME = "TestSpace";
    private Logger spaceImplLogger;
    private HazelcastInstance mockHcInstance;
    private IMap mockMap;
    private Space space;

    public WriteNoTxnUnitTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    @BeforeMethod
    public void setUp() {
        mockHcInstance = mock(HazelcastInstance.class);
        mockMap = mock(IMap.class);
        space = new SpaceImpl(mockHcInstance, TEST_SPACE_NAME, spaceImplLogger);

        when(mockHcInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName())).thenReturn(mockMap);
        when(mockMap.tryLock(any(String.class))).thenReturn(true);
    }

    /**
     * Test that we are using the correct duration value for IMap.put() when we
     * specify duration length of FOREVER.
     */
    @Test
    public void testWriteForever() {
        TestEntry entry = new TestEntry("HIDA", 1);
        Lease lease = space.write(TestEntry.class, entry, null, Lease.Duration.FOREVER);

        assertTrue(lease.getExpiration() == Lease.Duration.FOREVER);

        verify(mockMap).put(any(String.class), eq(entry), eq(0L), eq(TimeUnit.MILLISECONDS));
    }

    /**
     * Test that the Space is using the correct duration values when we attempt
     * to write an Entry into the space.
     */
    @Test
    public void testWriteDurationCorrect() {
        TestEntry entry = new TestEntry("HIDA", 1);

        final long leaseTime = 5000L;
        long expectedExpire = System.currentTimeMillis() + leaseTime;
        Lease lease = space.write(TestEntry.class, entry, null, leaseTime);
        assertTrue(lease.getExpiration() >= expectedExpire);

        verify(mockMap).put(any(String.class), eq(entry), eq(leaseTime), eq(TimeUnit.MILLISECONDS));
    }

    /**
     * Test expected behavior of a of cancelling a lease that has not yet
     * expired.
     */
    @Test
    public void testCancelLeaseNotExpired() {
        TestEntry entry = new TestEntry("HIDA", 1);

        @SuppressWarnings("unchecked")
        NoTxnLease<String, TestEntry> lease =
        (NoTxnLease<String, TestEntry>)space.write(TestEntry.class, entry, null,
                Lease.Duration.FOREVER);

        lease.cancel();
        verify(mockMap).remove(lease.getKey().toString());
    }

    /**
     * Test correct behavior when attempting to renew a least that is not
     * expired, and still exists in the space.
     */
    @Test
    public void testRenewLeaseNotExpired() {
        TestEntry entry = new TestEntry("HIDA", 1);

        @SuppressWarnings("unchecked")
        NoTxnLease<String, TestEntry> lease =
        (NoTxnLease<String, TestEntry>)space.write(TestEntry.class, entry, null, 50000L); // 50 sec
        final long oldExpire = lease.getExpiration();

        when(mockMap.get(eq(lease.getKey().toString()))).thenReturn(entry);

        final long newDuration = 25000L; // 25 sec
        final long newExpire = System.currentTimeMillis() + newDuration;
        lease.renew(newDuration);
        verify(mockMap).put(eq(lease.getKey().toString()), eq((TestEntry)lease.getValue()),
                eq(newDuration), eq(TimeUnit.MILLISECONDS));

        assertTrue(lease.getExpiration() >= newExpire && lease.getExpiration() < oldExpire,
                "Expiration date for renewed lease was not updated properly.");
    }

    /**
     * Test exptected behavior when attempting to renew a lease that is already
     * expired.  Lease.renew() should throw.\
     */
    @Test(expectedExceptions = IllegalStateException.class)
    public void testRenewLeaseExpired() throws InterruptedException {
        TestEntry entry = new TestEntry("HIDA", 1);

        Lease lease = space.write(TestEntry.class, entry, null, 1L);

        Thread.sleep(5L);

        // This should throw the IllegalStateException shown above.
        lease.renew(5000L);
    }

    /**
     * Test the expected behavior of trying to renew a lease on an object that
     * has already been taken from the space.  specifically, Lease.renew()
     * should throw because the entry no longer exists.
     */
    @Test(expectedExceptions = HidaException.class)
    public void testRenewLeaseEntryRemoved() {
        TestEntry entry = new TestEntry("HIDA", 1);

        @SuppressWarnings("unchecked")
        NoTxnLease<String, TestEntry> lease =
        (NoTxnLease<String, TestEntry>)space.write(TestEntry.class, entry, null,
                Lease.Duration.FOREVER);

        // Map does not contain the lease's key anymore; simulates that it was
        // taken
        when(mockMap.containsKey(eq(lease.getKey().toString()))).thenReturn(false);

        // Should throw becase Space determines that the map does not contain
        // the entry anymore.
        lease.renew(10000L);
    }
}
