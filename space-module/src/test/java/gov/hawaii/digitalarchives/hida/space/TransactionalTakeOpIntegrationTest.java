
package gov.hawaii.digitalarchives.hida.space;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

/**
 * Test cases for {@link TransactionalTakeOp}.
 *
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = {"classpath:/spring/defaultApplicationContext.xml"})
public class TransactionalTakeOpIntegrationTest extends AbstractTestNGSpringContextTests {
    private static final String HZ_INSTANCE_NAME = "hzInstance";
    private static final String TEST_SPACE_NAME = "TxnTakeTestSpace";
    @Autowired
    @Qualifier(HZ_INSTANCE_NAME)
    private HazelcastInstance hzInstance;

    private Logger spaceImplLogger;
    private SpaceImpl space;

    private Predicate<String, TestEntry> defaultPredicate;
    private TestEntry defaultEntry;

    private Transaction txn = null;

    public TransactionalTakeOpIntegrationTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    // Suppress the unchecked warning resulting in casting the predicate below.
    // It should be safe because they match the types returned by TestEntry.
    @SuppressWarnings("unchecked")
    @BeforeClass
    public void setUp() {
        space = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);

        EntryObject e = new PredicateBuilder().getEntryObject();
        defaultPredicate = (Predicate<String,TestEntry>) e.get("foo").equal("HiDA");
        defaultEntry = new TestEntry("HiDA", 1);
    }

    @AfterMethod
    public void cleanUp() {
        if (txn != null && txn.isValid()) {
            txn.abort();
        }

        for (DistributedObject distObj : hzInstance.getDistributedObjects()) {
            distObj.destroy();
        }
    }

    /**
     * Simple test to ensure that a take can actually successfully take a
     * matching entry in a Space.
     */
    @Test
    public void testTakeOpEntryExists() {
        IMap<String, TestEntry> entryMap = space.getMap(TestEntry.class);
        entryMap.put("HiDA", defaultEntry);

        txn = space.beginTransaction();

        TestEntry entry = space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
        txn.commit();

        Assert.assertNotNull(entry);
        Assert.assertTrue(entryMap.isEmpty());
    }

    /**
     * Simple test to ensure that a Take operation will return {@code null} when
     * there are no matching entries after its timeout.
     */
    @Test
    public void testTakeOpEntryNotExists() {
        txn = space.beginTransaction();
        TestEntry entry = space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);

        Assert.assertNull(entry);
    }

    /**
     * Test to ensure that the {@link ISemaphore} held by a successful take is
     * unlocked when the transaction is committed.
     */
    @Test
    public void testSemaphoreUnlockedAfterCommit() {
        space.getMap(TestEntry.class).put("1", defaultEntry);

        txn = space.beginTransaction();
        try {
            space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
        } finally {
            txn.commit();
        }

        Assert.assertFalse(space.getMap(TestEntry.class).isLocked("1"));
        ISemaphore rwSem = space.getReadWriteSemaphore("1");
        Assert.assertEquals(rwSem.availablePermits(), Integer.MAX_VALUE);
    }

    /**
     * Test to ensure that the {@link ISemaphore} held by a successful take is
     * unlocked when the transaction is aborted.
     */
    @Test
    public void testSemaphoreUnlockedAfterAbort() {
        space.getMap(TestEntry.class).put("1", defaultEntry);

        txn = space.beginTransaction();
        try {
            space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
        } finally {
            txn.abort();
        }

        Assert.assertFalse(space.getMap(TestEntry.class).isLocked("1"));
        ISemaphore rwSem = space.getReadWriteSemaphore("1");
        Assert.assertEquals(rwSem.availablePermits(), Integer.MAX_VALUE);
    }

    /**
     * Test that a take operation will return a match after if at first it
     * encounters no matches, and a match is added while it's waiting.
     *
     * @throws InterruptedException
     */
    @Test(timeOut = 2000L)
    public void testTakeEntryAddedAfter() throws InterruptedException {
        try {
            txn = space.beginTransaction();
            final CountDownLatch latch = new CountDownLatch(1);

            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        latch.countDown();
                        Thread.sleep(500L);
                        space.getMap(TestEntry.class).put("1", defaultEntry);
                    } catch (Exception ignored) {

                    }
                }
            };

            t.start();
            latch.await();
            TestEntry entry = space.take(TestEntry.class, defaultPredicate, txn, 1000L);
            Assert.assertNotNull(entry);
            t.join();
        } finally {
            txn.abort();
        }
    }

    /**
     * Test that an entry being read by another transaction cannot be taken from
     * the space.
     *
     * @throws InterruptedException
     */
    @Test(timeOut = 2000L)
    public void testTakeEntryBeingRead() throws InterruptedException {
        Transaction txn = space.beginTransaction();
        space.getMap(TestEntry.class).put("1", defaultEntry);
        try {
            final CountDownLatch readerLatch = new CountDownLatch(1);
            final CountDownLatch testLatch = new CountDownLatch(1);
            Thread t = new Thread() {
              @Override
              public void run() {
                  SpaceImpl s = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
                  ISemaphore rwSemaphore = s.getReadWriteSemaphore("1");
                  rwSemaphore.tryAcquire();
                  readerLatch.countDown();
                  try {
                      testLatch.await();
                  } catch (InterruptedException ignored) {

                  }
              }
            };

            t.start();
            readerLatch.await();
            TestEntry e = space.take(TestEntry.class, defaultPredicate, txn, 1000L);
            Assert.assertNull(e);

            testLatch.countDown();
        } finally {
            txn.abort();
        }
    }

    /**
     * Test that the take operation will block its full timeout, and be able to
     * take an entry that is released by another transaction within the timeout
     * window.
     * <p>
     * Explanation: This test starts by holding the semaphore for a single entry
     * in the Map in a different thread. Once the semaphore is held, the test
     * issues a take.
     *
     * @throws InterruptedException
     */
    @Test(timeOut = 1000L)
    public void testTakeEntryWaitForRead() throws InterruptedException {
        txn = space.beginTransaction();
        final CountDownLatch latch =  new CountDownLatch(1);
        try {
            space.getMap(TestEntry.class).put("1", defaultEntry);
            // Create a thread to simulate an external transaction that is
            // "reading" an entry.
            Thread t = new Thread() {
                @Override
                public void run() {
                    ISemaphore rwSemaphore = space.getReadWriteSemaphore("1");
                    try {
                        // Simulate a read operation on the space by acquiring a
                        // SINGLE permit for the semaphore.
                        rwSemaphore.acquire();
                        // Signal the thread when we've acquired the semaphore
                        latch.countDown();
                        Thread.sleep(250L);
                    } catch (InterruptedException e) {
                    } finally {
                        rwSemaphore.release();
                    }
                }
            };

            t.start();
            // Wait for the other Thread to acquire the semaphore first.
            latch.await();
            TestEntry e = space.take(TestEntry.class, defaultPredicate, txn, 1000L);
            // The thread is configured to release the lock after ~250 ms. Since
            // the timeout for the take is 1000 ms, it should be able to take
            // and return the entry after it's released by the other thread.
            Assert.assertNotNull(e);
            t.join();
        } finally {
            txn.abort();
        }
    }

    /**
     * Test that an entry written under the transaction, can be taken by a take
     * operation using the same transaction.
     */
    @Test
    public void testTakeEntryWrittenInTxn() {
        txn = space.beginTransaction();
        space.getTransactionalMap((TransactionImpl) txn, TestEntry.class).put("1", defaultEntry);

        TestEntry e = space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
        Assert.assertNotNull(e);

        txn.abort();
    }

    /**
     * Test that the same entry in the Space cannot be taken more than once by
     * the same transaction.
     */
    @Test
    public void testTakeSameEntryTwice() {
        // The single matching entry in the space.
        space.getMap(TestEntry.class).put("1", defaultEntry);
        txn = space.beginTransaction();
        try {
            TestEntry e = space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
            Assert.assertNotNull(e);
            // Try to take again, returned should be null this time.
            e = space.take(TestEntry.class, defaultPredicate, txn, Space.Timeout.NO_WAIT);
            Assert.assertNull(e);
        } finally {
            txn.commit();
        }
    }

    /**
     * Test that only a single take operation can take a single entry.
     *
     * @throws InterruptedException
     */
    @Test
    public void testMultipleTakesSingleEntry() throws InterruptedException {
        multipleTakersCommon(1, 8, Space.Timeout.NO_WAIT);
    }

    /**
     * Test that when there are equal number of matches and take operations,
     * each take operation will be able to take an entry.
     *
     * @throws InterruptedException
     */
    @Test
    public void testMultipleTakersEqualMatches() throws InterruptedException {
        multipleTakersCommon(16, 16, Space.Timeout.NO_WAIT);
    }

    /**
     * Test that when there are more matches than takers, all takers will be
     * able to take a unique match.
     *
     * @throws InterruptedException
     */
   @Test
    public void testMultipleTakersMoreMatches() throws InterruptedException {
        multipleTakersCommon(16, 8, Space.Timeout.NO_WAIT);
    }

    /**
     * Test that when there are less matches than takers, exactly the same
     * number of takers as matches will return a unique, non-null entry.
     *
     * @throws InterruptedException
     */
   @Test
   public void testMultipleTakersLessMatches() throws InterruptedException {
       multipleTakersCommon(8, 16, Space.Timeout.NO_WAIT);
   }

    /**
     * Test in which multiple takers initially encounter an empty {@code Space},
     * and enter the waiting state. After all takers are waiting, matches are
     * then added to the Space.
     *
     * @throws InterruptedException
     */
   @Test
   public void testMultipleTakersMatchesAddedAfter() throws InterruptedException {
       final int nTakers = 16;
       final int nMatches = 16;
       final CountDownLatch beforeTakeLatch = new CountDownLatch(nTakers);
       final CountDownLatch afterTakeLatch = new CountDownLatch(nTakers);

       final Map<Integer, TestEntry> takenMap = new ConcurrentHashMap<Integer, TestEntry>();

       ExecutorService executor = Executors.newFixedThreadPool(nTakers);

       for (int i = 0; i < nTakers; i++) {
           executor.submit(new Runnable() {
               @Override
               public void run() {
                   SpaceImpl space = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
                   Transaction txn = space.beginTransaction();
                   try {
                        // Signal to the test thread that we're about to start
                        // our take.
                       beforeTakeLatch.countDown();
                       TestEntry e = space.take(TestEntry.class, defaultPredicate, txn, 10000L);
                       takenMap.put(e.getId(), e);
                   } finally {
                       txn.commit();
                       afterTakeLatch.countDown();
                   }
               }
           });
       }

       // Wait for all takers to start their take operations.
       beforeTakeLatch.await();
       for (int i = 0; i < nMatches; i++) {
           space.getMap(TestEntry.class).put(Integer.valueOf(i), new TestEntry("HiDA", i));
       }

        // Wait for all takers to finish their take operations and commit their
        // transactions.
       afterTakeLatch.await();

       Assert.assertEquals(takenMap.size(), nMatches);
   }

   /**
    * Common method used by tests that test multiple concurrent take operations.
    *
    * @param nMatches The number of matches to pre-populate the {@code Space}.
    * @param nTakers The number of concurrent take operations to start.
    * @param timeout The timeout value for the take operations.
    *
    * @throws InterruptedException
    */
    private void multipleTakersCommon(final int nMatches, final int nTakers, final long timeout)
            throws InterruptedException {
        if (timeout == Space.Timeout.WAIT_FOREVER) {
            throw new IllegalArgumentException("Woops!  WAIT_FOREVER not allowed.  Bad things " +
                    "will happen!");
        }
        final Map<Integer, TestEntry> takenMap = new ConcurrentHashMap<Integer, TestEntry>();
        final CountDownLatch takersLatch = new CountDownLatch(nTakers);
        final CountDownLatch testLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(nTakers);
        for (int i = 0; i < nMatches; i++) {
            space.getMap(TestEntry.class).put(Integer.valueOf(i), new TestEntry("HiDA",
                    Integer.valueOf(i)));
        }

        for (int i = 0; i < nTakers; i++) {
            executor.submit(new Callable<Void>() {
               @Override
               public Void call() throws Exception {
                   Space space = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
                   Transaction takeTxn = null;
                   try {
                       takeTxn = space.beginTransaction();
                       TestEntry e = space.take(TestEntry.class, defaultPredicate, takeTxn,
                               timeout);
                       if (e != null) {
                           takenMap.put(e.getId(), e);
                       }
                        // Signal back to the test that we've performed our take
                        // operation.
                       takersLatch.countDown();
                        // Wait until the test signals that we can proceed. We
                        // want to keep the transaction alive until then.
                       testLatch.await();
                   } finally {
                       if (takeTxn != null) {
                           takeTxn.commit();
                       }
                   }
                   return null;
               }
            });
        }

        takersLatch.await();
        testLatch.countDown();

        // If there are more takers, then only nMatches will be available,
        // otherwise the number of matches taken will be the same as nTakers.
        final int nExpectedTaken = nTakers > nMatches ? nMatches : nTakers;

       Assert.assertEquals(takenMap.size(), nExpectedTaken);

       final int nExpectedLeftInMap = nMatches - nExpectedTaken;

        // Initiate shutdown, and wait up to 1 minute. Should take much less
        // time than that though.
       executor.shutdown();
       executor.awaitTermination(1, TimeUnit.MINUTES);
       Assert.assertEquals(space.getMap(TestEntry.class).size(), nExpectedLeftInMap, "TestEntry " +
               "IMap does not contain the  expected number of matches after running the take " +
               "operation(s)!");
    }

}
