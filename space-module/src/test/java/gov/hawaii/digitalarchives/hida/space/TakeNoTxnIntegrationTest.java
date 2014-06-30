package gov.hawaii.digitalarchives.hida.space;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

@ContextConfiguration(locations = {"classpath:/spring/defaultApplicationContext.xml"})
@Test
public class TakeNoTxnIntegrationTest extends AbstractTestNGSpringContextTests {
    private static final String TEST_SPACE_NAME = "TestSpace";
    private static final String HZ_INSTANCE_NAME = "hzInstance";
    private final int nInstances = 2;
    private HazelcastInstance instances[] = new HazelcastInstance[nInstances];
    private Logger spaceImplLogger;
    private IMap<String, TestEntry> map;
    private Space space;

    private Predicate<String, TestEntry> defaultPredicate;

    @AutowiredLogger
    private Logger log;

    public TakeNoTxnIntegrationTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    @BeforeClass
    public void setUp() {
        for (int i = 0; i < instances.length; i++) {
            // Note, the hzInstace is declared with prototype scope, so this
            // call actually creates a new bean instance each time.
            this.instances[i] = (HazelcastInstance) this.applicationContext
                    .getBean(HZ_INSTANCE_NAME);
        }
        this.map = instances[0].getMap(TEST_SPACE_NAME + TestEntry.class.getName());
        this.space = new SpaceImpl(instances[0], TEST_SPACE_NAME, spaceImplLogger);

        EntryObject e = new PredicateBuilder().getEntryObject();
        this.defaultPredicate = e.get("foo").equal("HIDA");
    }

    @AfterClass
    public void shutdown() {
        for (HazelcastInstance i : instances) {
            i.getLifecycleService().shutdown();
        }
    }

    @AfterMethod
    public void reset() {
        map.clear();
    }


    /**
     * Test that the Take operation will wait (until the timeout) for a
     * matching entry when it encounters an empty map.
     */
    @Test(timeOut = 5000L)
    public void testWaitForEntry() throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                map.put("1", new TestEntry("HIDA"));
                log.debug("Entry added from thread.");
            }
        };

        t.start();

        TestEntry taken = space.take(TestEntry.class, defaultPredicate, null, Space.Timeout.WAIT_FOREVER);
        assertNotNull(taken);
        assertTrue(map.isEmpty());
        t.join();
    }

    /**
     * Test that when multiple concurrent operations are trying to Take on the
     * same predicate, and there is only a single match in the map, only one of
     * them will successfully take the entry.
     */
    public void testMultipleTakersSingleMatch() throws InterruptedException, ExecutionException {
        final int nTakers = 4;
        ExecutorService executor = Executors.newFixedThreadPool(nTakers);

        Future takeFutures[] = new Future[nTakers];
        for (int i = 0; i < takeFutures.length; i++) {
            final int ii = i;
            takeFutures[i] = executor.submit(new Callable<TestEntry>() {
                @Override
                public TestEntry call() throws Exception {
                    Space s = new SpaceImpl(instances[ii % nInstances], TEST_SPACE_NAME,
                            spaceImplLogger);
                    return s.take(TestEntry.class, defaultPredicate, null, 5000L);
                }
            });
        }

        Thread.sleep(500L);

        map.put("1", new TestEntry("HIDA"));

        List<TestEntry> entries = new ArrayList<TestEntry>();

        for (Future f : takeFutures) {
            TestEntry taken = null;
            taken = ((Future<TestEntry>)f).get();
            if ( taken != null) {
                entries.add(taken);
            }
        }

        assertEquals(entries.size(), 1);
        assertTrue(map.isEmpty());
    }

    /**
     * Test that when there are multiple concurrent takers, and there are an
     * equal number of matches, all takers are able to take an entry, and no
     * two takers take the same entry.
     */
    public void testMultipleTakersEqualMatches() throws InterruptedException, ExecutionException {
        multipleTakesAndMatchesCommon(16, 16, 1000L);
    }

    /**
     * Test that when there are multiple takers, and the space does not have a
     * matching entry for all of them, only the expected amount of takers will
     * get an entry, and of those entries, none of them will be the same one.
     */
    public void testMultipleTakersLessMatches() throws InterruptedException, ExecutionException {
        multipleTakesAndMatchesCommon(16, 8, 1000L);
    }

    /**
     * Test that when there are more matches than takers, all takers will get a
     * unique match, and the after all takes, the space stil contains #matches
     * - #takers entries.
     */
    public void testMultipleTakersMoreMatches() throws InterruptedException, ExecutionException {
        multipleTakesAndMatchesCommon(16, 32, 1000L);
    }


    /**
     * Common method for the testMultiple* tests.  Provides the actual logic of tests.
     *
     * @param nTakes The number of takers.
     * @param nMatches The number of matches to populate the matp with.
     * @param timeout The timeout for each take operation.
     */
    private void multipleTakesAndMatchesCommon(final int nTakers, final int nMatches,
        final long timeout) throws InterruptedException, ExecutionException {
        Assert.isTrue(timeout != Space.Timeout.WAIT_FOREVER,
                "timeout cannot be Timeout.WAIT_FOREVER");

        for (int i = 0; i < nMatches; i++) {
            map.put("" + i, new TestEntry("HIDA", i));
        }

        ExecutorService executor = Executors.newFixedThreadPool(nTakers);

        Future takeFutures[] = new Future[nTakers];

        for (int i = 0; i < nTakers; i++) {
            final int ii = i;
            takeFutures[i] = executor.submit(new Callable<TestEntry>() {
                @Override
                public TestEntry call() throws Exception {
                    Space s = new SpaceImpl(instances[ii % nInstances], TEST_SPACE_NAME,
                            spaceImplLogger);
                    return space.take(TestEntry.class, defaultPredicate, null, timeout);
                }
            });
        }

        Map<Integer, TestEntry> takenEntries = new HashMap<Integer, TestEntry>();

        for (Future f : takeFutures) {
            TestEntry e = (TestEntry) f.get();
            if (e != null) {
                takenEntries.put(e.getId(), e);
            }
        }

        final int expectedTaken = (nMatches > nTakers) ? nTakers : nMatches;

        assertEquals(takenEntries.size(), expectedTaken);


        if (nTakers >= nMatches) {
            assertTrue(map.isEmpty());
        } else {
            assertEquals(map.size(), nMatches - nTakers);
        }
    }
}
