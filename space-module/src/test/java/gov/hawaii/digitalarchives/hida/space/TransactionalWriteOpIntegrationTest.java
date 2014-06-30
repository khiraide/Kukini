package gov.hawaii.digitalarchives.hida.space;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

/**
 * Tests for {@link Space#write(Class, Object, Transaction, long)}
 * implementation.
 *
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = { "classpath:/spring/defaultApplicationContext.xml" })
public class TransactionalWriteOpIntegrationTest extends AbstractTestNGSpringContextTests {
    private static final String HZ_INSTANCE_NAME = "hzInstance";
    private static final String TEST_SPACE_NAME = "testSpace";

    private Logger spaceImplLogger;
    private SpaceImpl space;

    @AutowiredLogger
    private Logger log;

    @Autowired
    @Qualifier(HZ_INSTANCE_NAME)
    private HazelcastInstance hzInstance;

    private TestEntry defaultEntry = new TestEntry("HiDA", 1);
    private Predicate<String, TestEntry> defaultPredicate;
    private Transaction txn = null;

    public TransactionalWriteOpIntegrationTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    @BeforeClass
    public void setUp() {
        space = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
        EntryObject entryObject = new PredicateBuilder().getEntryObject();
        defaultPredicate = entryObject.get("foo").equal("HiDA");

        hzInstance.getExecutorService(SpaceImpl.SPACE_PROCESSORS_EXECUTOR_SERVICE).submit(
                new CancelLeaseProcessor());
    }

    @AfterMethod
    public void cleanUp() {
        if (txn != null && txn.isValid()) {
            txn.abort();
        }

        for (DistributedObject distObj : hzInstance.getDistributedObjects()) {
            if (!distObj.getName().equals(SpaceImpl.SPACE_PROCESSORS_EXECUTOR_SERVICE)) {
                distObj.destroy();
            }
        }
    }

    /**
     * Simple write test to see if entries written using
     * {@link Space#write(Class, Object, Transaction, long)} actually end up in
     * the space as expected.
     */
    @Test
    public void testWrite() {
        txn = space.beginTransaction();
        space.write(TestEntry.class, defaultEntry, txn, Lease.Duration.FOREVER);
        txn.commit();

        Assert.assertFalse(space.getMap(TestEntry.class).isEmpty(),
                "Entry was not written into the space.");
    }

    /**
     * Test that an entry written under the transaction using
     * {@link Space#write(Class, Object, Transaction, long)} can be taken by a
     * subsequent {@link Space#take(Class, Predicate, Transaction, long)} when
     * using the same transaction.
     */
    @Test
    public void testTakeWrittenEntrySameTxn() {
        txn = space.beginTransaction();
        space.write(TestEntry.class, defaultEntry, txn, Lease.Duration.FOREVER);

        TestEntry entry = space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);

        Assert.assertNotNull(entry);
    }

    @Test
    public void testTakeCancelledLease() {
        txn = space.beginTransaction();
        Lease lease = space.write(TestEntry.class, defaultEntry, txn, Lease.Duration.FOREVER);
        lease.cancel();
        txn.commit();

        TestEntry e = space.take(TestEntry.class, defaultPredicate, null, Space.Timeout.NO_WAIT);
        Assert.assertNull(e, "Was able to take entry whose lease was cancelled.");
    }

    @Test
    public void testReadCancelledLease() {
        txn = space.beginTransaction();
        Lease lease = space.write(TestEntry.class, defaultEntry, txn, Lease.Duration.FOREVER);
        lease.cancel();
        txn.commit();

        TestEntry e = space.read(TestEntry.class, defaultPredicate, null, Space.Timeout.NO_WAIT);
        Assert.assertNull(e, "Was able to read entry whose lease was cancelled.");
    }
}
