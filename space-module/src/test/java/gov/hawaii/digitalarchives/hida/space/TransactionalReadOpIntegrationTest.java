package gov.hawaii.digitalarchives.hida.space;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

/**
 * Test cases for {@link TransactionalReadOp}.
 *
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = "classpath:/spring/defaultApplicationContext.xml")
public class TransactionalReadOpIntegrationTest extends AbstractTestNGSpringContextTests {
    private static final String HZ_INSTANCE_NAME = "hzInstance";
    private static final String TEST_SPACE_NAME = "ReadTestSpace";

    @Autowired
    @Qualifier(HZ_INSTANCE_NAME)
    private HazelcastInstance hzInstance;

    @AutowiredLogger
    private Logger log;

    private Logger spaceImplLogger;
    private SpaceImpl space;

    private TestEntry defaultTestEntry;
    private Predicate<String, TestEntry> defaultPredicate;
    private Transaction txn = null;

    public TransactionalReadOpIntegrationTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    // Suppress the unchecked assignment to defaultPredicate. Safe to ignore
    // because the types should match.
    @SuppressWarnings("unchecked")
    @BeforeClass
    public void setUp() {
        space = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);

        defaultTestEntry = new TestEntry("HiDA", 1);
        EntryObject e = new PredicateBuilder().getEntryObject();
        defaultPredicate = e.get("foo").equal("HiDA").and(e.get("id").equal(1));
    }

    @AfterMethod
    public void reset() {
        if (txn != null && txn.isValid()) {
            txn.abort();
        }

        for (DistributedObject distObj : hzInstance.getDistributedObjects()) {
            distObj.destroy();
        }
    }

    /**
     * Simple test to see if the read operation performs as expected for the
     * simple case of a single existing entry in the map.
     */
    @Test
    public void testSimpleRead() {
        space.getMap(TestEntry.class).put("1", defaultTestEntry);
        txn = space.beginTransaction();
        try {
            TestEntry e = space.read(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
            Assert.assertNotNull(e);
        } finally {
            txn.commit();
        }

        // Check that the entry remains in the map (it was not removed).
        Assert.assertNotNull(space.getMap(TestEntry.class).get("1"));
    }

    /**
     * Test to ensure that that corresponding semaphore gets acquired for the
     * duration of the transaction, and released after the transaction is
     * committed.
     */
    @Test
    public void testEntryLockedAndUnlocked() {
        space.getMap(TestEntry.class).put("1", defaultTestEntry);
        txn = space.beginTransaction();
        ISemaphore readSem = null;
        try {
            TestEntry e = space.read(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
            Assert.assertNotNull(e);
            readSem = space.getReadWriteSemaphore("1");
            // While the transaction is still active, the read operation should
            // be holding on a single permit of the related semaphore.
            Assert.assertEquals(readSem.availablePermits(), Integer.MAX_VALUE - 1);
        } finally {
            txn.commit();
            // The permit acquired during the life of the transaction should
            // have been released.
            Assert.assertEquals(readSem.availablePermits(), Integer.MAX_VALUE);
        }
    }

    /**
     * Test that a read blocked in the waiting state can successfully read an
     * entry written to the space while it was waiting.
     *
     * @throws InterruptedException
     */
    @Test(timeOut = 1000L)
    public void testReadEntryAddedAfter() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Thread t = new Thread() {
            @Override
            public void run() {
                latch.countDown();
                try {
                    Thread.sleep(250L);
                    space.getMap(TestEntry.class).put("1", defaultTestEntry);
                } catch (InterruptedException e) {
                    log.debug("Thread interrupted!");
                }

            }
        };

        txn = space.beginTransaction();
        try {
            t.start();
            // Wait until the thread is ready before issuing the take.
            latch.await();
            TestEntry e = space.read(TestEntry.class, defaultPredicate, txn, 1000L);
            Assert.assertNotNull(e);
        } finally {
            txn.commit();
        }

        t.join();

    }

    /**
     * Test that multiple concurrent readers can successfully read a single
     * matching entry in the space (they don't exclude each other out.)
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testMultipleConcurrentReaders() throws InterruptedException, ExecutionException {
        final int nReaders = 8;
        ExecutorService executor = Executors.newFixedThreadPool(nReaders);
        // Use a barrier to ensure that all read threads have started before
        // they proceed to start reading.
        final CyclicBarrier barrier = new CyclicBarrier(nReaders);

        space.getMap(TestEntry.class).put("1", defaultTestEntry);

        List<Future<Integer>> reads = new ArrayList<Future<Integer>>();
        for (int i = 0; i < nReaders; i++) {
            reads.add(executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Space s = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
                    Transaction t = s.beginTransaction();
                    try {
                        barrier.await();
                        TestEntry e = space.read(TestEntry.class, defaultPredicate, t,
                                Space.Timeout.NO_WAIT);
                        if (e != null) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } finally {
                        t.commit();
                    }
                }
            }));
        }

        // Count the number of threads that were able to take read a non-null
        // entry.
        int readCount = 0;
        for (Future<Integer> f : reads) {
            readCount += f.get();
        }

        Assert.assertEquals(readCount, nReaders);


        // Shutdown the executor. We give a timeout of 2 seconds here, but in
        // reality should take much less time since they aren't performing
        // blocking operations.
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    /**
     * Test that if the only matching entry in the space has been taken under
     * another transaction, it cannot be read by another transaction.
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testEntryBeingTaken() throws InterruptedException, ExecutionException {
        space.getMap(TestEntry.class).put("1", defaultTestEntry);

        // Simulate a "take" on the single entry in the map.
        ISemaphore takeSem = space.getReadWriteSemaphore("1");
        takeSem.acquire(Integer.MAX_VALUE);

        // Spawn a thread that will attempt to read the entry. It should be
        // unable to do so.
        Future<TestEntry> readFuture = Executors.newSingleThreadExecutor().submit(
                new Callable<TestEntry>() {
                    @Override
                    public TestEntry call() throws Exception {
                        Space s = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
                        Transaction t = s.beginTransaction();
                        try {
                            return s.read(TestEntry.class, defaultPredicate, t,
                                    Space.Timeout.NO_WAIT);
                        } finally {
                            t.commit();
                        }
                    }
                });

        Assert.assertNull(readFuture.get());
    }

    /**
     * Test that an entry written in a transaction can be read when using the
     * same transaction.
     */
    @Test
    public void testReadEntryWrittenInTxn() {
        txn = space.beginTransaction();
        try {
            TransactionalMap<String, TestEntry> txnMap = space.getTransactionalMap(
                    (TransactionImpl) txn, TestEntry.class);
            txnMap.put("1", defaultTestEntry);

            TestEntry e = space.read(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
            Assert.assertNotNull(e);
        } finally {
            txn.commit();
        }
    }

    /**
     * Test that a entry that is taken in a transaction, cannot be read, even
     * under the same transaction.
     */
    @Test
    public void testReadEntryTakenInSameTxn() throws InterruptedException {
        space.getMap(TestEntry.class).put("1", defaultTestEntry);
        ISemaphore  entrySem = space.getReadWriteSemaphore("1");
        entrySem.acquire(Integer.MAX_VALUE);

        txn = space.beginTransaction();
        try {
            TestEntry e = space.read(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
            Assert.assertNull(e);
        } finally {
            txn.commit();
        }
    }

    /**
     * Test that a Read will eventually succeed when it first encounters an
     * entry that is being taken, but it is then released (via abort()) within
     * the read's timeout.
     *
     * @throws InterruptedException
     */
    @Test(timeOut = 1500L)
    public void testReadEntryInitiallyLocked() throws InterruptedException {
        space.getMap(TestEntry.class).put("1", defaultTestEntry);
        final CountDownLatch latch = new CountDownLatch(1);

        // Use a separate thread to simulate a separate transaction performing a
        // take. All permits are taken, so initially, the read operation below,
        // which happens after semaphore's already been acquired, should fail to
        // read it, and enter the waiting phase.
        Thread t = new Thread() {
          @Override
          public void run() {
              SpaceImpl s = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
              ISemaphore sem = s.getReadWriteSemaphore("1");
              boolean acquired = false;
              try {
                  sem.acquire(Integer.MAX_VALUE);
                  acquired = true;
                  latch.countDown();
                  Thread.sleep(250L);
              } catch (InterruptedException e) {
              } finally {
                  if (acquired) {
                      sem.release(Integer.MAX_VALUE);
                  }
              }
          }
        };

        t.start();
        latch.await();

        txn = space.beginTransaction();
        try {
            TestEntry e = space.read(TestEntry.class, defaultPredicate, txn, 1000L);
            Assert.assertNotNull(e);
        } finally {
            txn.commit();
        }
    }
}
