package gov.hawaii.digitalarchives.hida.space;

import static org.testng.Assert.*;

import gov.hawaii.digitalarchives.hida.space.Space.Timeout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
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
public class HazelCastIntegrationTest extends AbstractTestNGSpringContextTests {
    private static final String TEST_SPACE_NAME = "TestSpace";
    private static final String HZ_INSTANCE_NAME = "hzInstance";
    @AutowiredLogger
    private Logger log;

    @Autowired
    @Qualifier(HZ_INSTANCE_NAME)
    private HazelcastInstance hzInstance;

    private Logger spaceImplLogger;
    private Space space;

    public HazelCastIntegrationTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    @BeforeClass
    public void setUp() throws Exception {
        space = new SpaceImpl(hzInstance, TEST_SPACE_NAME, spaceImplLogger);
    }

    @AfterClass
    public void shutdown() {
        hzInstance.getLifecycleService().shutdown();
    }

    @AfterMethod
    public void clearAll() {
        // Clear all the maps after each test.
        for (DistributedObject distObj : hzInstance.getDistributedObjects()) {
            distObj.destroy();
        }
    }


    /**
     * Test a non-blocking read for when there are no available entries.
     * @throws InterruptedException
     */
    @Test
    public void testReadNoEntryNoWait() {
        EntryObject e = new PredicateBuilder().getEntryObject();
        @SuppressWarnings("unchecked")
        Predicate<String, TestEntry> predicate = e.get("flag").equal(Boolean.TRUE);
        TestEntry entry = space.read(TestEntry.class, predicate, null, Timeout.NO_WAIT);
        assertNull(entry);
    }

    /**
     * Test the read method when there is an existing entry in the space BEFORE
     * the read method is called, and it is given a NO_WAIT timeout.  The call
     * should return without blocking.
     */
    @Test
    public void testReadEntryPresentNoWait() {
        IMap<String, TestEntry> map = hzInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
        
        TestEntry match =  new TestEntry(Boolean.TRUE);
        map.put("test", match);
        EntryObject e = new PredicateBuilder().getEntryObject();
        @SuppressWarnings("unchecked")
        Predicate<String, TestEntry> predicate = e.get("flag").equal(Boolean.TRUE);
        TestEntry entry = space.read(TestEntry.class, predicate, null, Timeout.NO_WAIT);

        assertNotNull(entry);
        assertEquals(entry.getFlag(), match.getFlag());
    }

    /**
     * Test a blocking read, where the timeout is FOREVER.  That is, it should
     * continue to wait until a matching entry is available.
     */
    @Test(timeOut = 10000L)
    public void testReadNoInitialEntryWaitForever() throws InterruptedException {
        // Use a second thread to insert a matching entry directly into the map
        // after 5 seconds.
        Thread producer = new Thread() {
            @Override
            public void run() {
                IMap<String, TestEntry> map = hzInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.debug("Producer thread interrupted.", e);
                }
                map.put("test", new TestEntry(Boolean.TRUE));
            }
        };

        EntryObject e = new PredicateBuilder().getEntryObject();
        @SuppressWarnings("unchecked")
        Predicate<String, TestEntry> predicate = e.get("flag").equal(Boolean.TRUE);

        final long t1 = System.currentTimeMillis();
        producer.start();
        // This call should take at least 5 seconds
        TestEntry entry = space.read(TestEntry.class, predicate, null, Timeout.WAIT_FOREVER);
        final long t2 = System.currentTimeMillis();

        float elapsedSec = (t2 - t1) / 1000f;
        log.debug("Blocked for {} seconds.", elapsedSec);
        assertTrue(elapsedSec >= 5.0f);
        assertNotNull(entry);
        assertTrue(entry.getFlag());

        // Just in case the producer didn't finish executing yet for odd reason.
        producer.join();
    }

    /**
     * Test that the read operation waits the duration of the timeout (when not
     * WAIT_FOREVER), until returning when there is no entry available.
     */
    @Test
    public void testReadNoEntryWait() {
        EntryObject e = new PredicateBuilder().getEntryObject();
        @SuppressWarnings("unchecked")
        Predicate<String, TestEntry> predicate = e.get("flag").equal(Boolean.TRUE);
        
        // Block for a maximum of 5 seconds.  Since the distributed map is
        // EMPTY, we expect to actually block for at least (within a handful of
        // ms) this amount of time.
        final long timeout = 5000L;
        final long t1 = System.currentTimeMillis();

        TestEntry entry = space.read(TestEntry.class, predicate, null, timeout);
        
        final long t2 = System.currentTimeMillis();

        final float elapsed = ((int) (t2 - t1)) / 1000f;
        log.debug("Elapsed time was {} seconds", elapsed);
        assertTrue((t2 - t1) >= timeout);
        assertNull(entry);
    }
    
    
    /**
     * Test that multiple calls to read with the same template on a space with
     * only a single matching entry will return a valid entry.
     */
    @Test
    public void testReadMultipleSingleMatchingEntry() {
        EntryObject e = new PredicateBuilder().getEntryObject();
        @SuppressWarnings("unchecked")
        Predicate<String, TestEntry> predicate = e.get("flag").equal(Boolean.TRUE);

        IMap<String, TestEntry> map = hzInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
        // sanity check before we proceed
        assertTrue(map.isEmpty(), "Map is not empty to start with!  Test cannot proceed.");

        TestEntry match = new TestEntry(Boolean.TRUE);
        map.put("1", match);

        TestEntry entry1 = space.read(TestEntry.class, predicate, null, Timeout.NO_WAIT);
        assertNotNull(entry1, "First read entry is null.");
        TestEntry entry2 = space.read(TestEntry.class, predicate, null, Timeout.NO_WAIT);
        assertNotNull(entry2, "Second read entry is null.");

        assertTrue(match.getFlag().equals(entry1.getFlag()) &&
                entry1.getFlag().equals(entry2.getFlag()));
    }

    /**
     * Test that multiple readers reading the same template at the same time
     * will all get notified when the a single matching entry appears in the
     * space.
     */
    @Test(timeOut = 10000L)
    public void testMultipleConcurrentReadersSamePredicate() throws InterruptedException,
           ExecutionException {
        // Use size of three so that each of our 3 callables below will have
        // their own thread.
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        EntryObject e = new PredicateBuilder().getEntryObject();
        @SuppressWarnings("unchecked")
        final Predicate<String, TestEntry> predicate = e.get("flag").equal(Boolean.TRUE);
        
        List<Future<TestEntry> > results = new ArrayList<Future<TestEntry> >();
        for (int i = 0; i < 3; ++i) {
            Future<TestEntry> resultFuture = executor.submit(
                    new Callable<HazelCastIntegrationTest.TestEntry>() {

                    @Override
                    public TestEntry call() throws Exception {
                        Space callablesSpace = new SpaceImpl(hzInstance, TEST_SPACE_NAME,
                                spaceImplLogger);
                        return callablesSpace.read(TestEntry.class, predicate, null,
                            Timeout.WAIT_FOREVER);
                    }
            });
            results.add(resultFuture);
        }
        
        // Let the readers run ahead of us first, so they end up a blocked
        // state.
        Thread.sleep(100);
        
        IMap<String, TestEntry> map = hzInstance.getMap(TEST_SPACE_NAME + TestEntry.class.getName());
        map.put("1", new TestEntry(Boolean.TRUE));
        
        for (int i = 0; i < 3; ++i) {
            Future<TestEntry> tef = results.get(i);
            TestEntry te = tef.get();
            assertTrue(te.getFlag().equals(Boolean.TRUE));
        }
    }

    /**
     * Simple class used within the tests.
     */
    public static class TestEntry implements Serializable {
        private Boolean flag;

        public TestEntry(final Boolean flag) {
            this.flag = flag;
        }

        public Boolean getFlag() {
            return flag;
        }

        @Override
        public String toString() {
            return "TestEntry{ flag = " + flag + "}";
        }
    }
}
