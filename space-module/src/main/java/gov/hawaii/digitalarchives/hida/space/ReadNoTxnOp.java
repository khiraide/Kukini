package gov.hawaii.digitalarchives.hida.space;

import gov.hawaii.digitalarchives.hida.space.Space.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;

import org.springframework.util.Assert;

/**
 * Implementation of the {@link Space#read(Class, Predicate, Transaction, long)}
 * operation that does not have a transaction.
 * <p>
 * Note that this class is also not transaction-aware.  That is, it does not
 * take into account transactions that may be ongoing, and any records they may
 * have locked.
 *
 * @author Dongie Agnir
 */
class ReadNoTxnOp<V> implements NoTxnOperation<V>, EntryListener<String, V> {
    private final Predicate<String, V> predicate;
    private final IMap<String, V> clazzMap;
    private final long timeout; 
    private final Logger log;

    private V entry = null;
    private Lock entryLock = new ReentrantLock();
    private Condition entryAvailableCondition = null;

    /**
     * Package-private constructor for this operation.
     *
     * @param hzInstance The Hazelcast Instance this operation will work on.
     * @param clazz The class of the entry to read.
     * @param predicate The predicate that specifies the conditions that the
     * matching entry must meet.
     * @param timeout The timeout for this read operation.
     * @param log The logger to use to for logging.
     */
    ReadNoTxnOp(final IMap<String, V> clazzMap, final Predicate<String, V> predicate,
            final long timeout, Logger log) {
        Assert.notNull(clazzMap, "clazzMap must not be null.");
        Assert.notNull(predicate, "predicate must not be null.");
        Assert.isTrue(timeout >= 0L, "timeout cannot be negative.");
        Assert.notNull(log, "logger must not be null.");

        this.predicate = predicate;
        this.clazzMap = clazzMap;
        this.timeout = timeout;
        this.log = log;
    }

    ReadNoTxnOp(final IMap<String, V> clazzMap, final Predicate<String, V> predicate,
            final long timeout, Logger log, final Lock entryLock) {
        Assert.notNull(clazzMap, "clazzMap must not be null.");
        Assert.notNull(predicate, "predicate must not be null.");
        Assert.isTrue(timeout >= 0L, "timeout cannot be negative.");
        Assert.notNull(log, "logger must not be null.");

        log.warn("CAREFUL!!!  This constructor is meant to be used only in testing.  Do not" +
                " use in production!");

        this.predicate = predicate;
        this.clazzMap = clazzMap;
        this.timeout = timeout;
        this.log = log;

        this.entryLock = entryLock;
    }

    @Override
    public V call() throws Exception {
        // First acquire the lock for ourself.
        entryLock.lock();
        entryAvailableCondition = entryLock.newCondition();
        try {
            // Begin by adding ourself as a listener for the given predicate to
            // the Map.  This is to guarantee that by the time we call
            // IMap.entrySet() below, if we don't find any matches, then we can
            // guarantee that our listener is already setup and good to go.
            // It's important that we do this now; if we register the listener
            // AFTER we determine there aren't any existing entries, an entry
            // could be added before we are registered and we will not
            // catch it.
            String listenerId = clazzMap.addEntryListener(this, predicate, null, true);
            log.debug("Added map listener.  ID = {}", listenerId);
            try {
                // Check the map for entries already in it
                log.debug("Checking map for matching entry.");
                Set<Map.Entry<String, V>> matchingEntries = clazzMap.entrySet(predicate);
                if (!matchingEntries.isEmpty()) {
                    Map.Entry<String, V> first = matchingEntries.iterator().next();

                    V match = first.getValue();
                    log.debug("Existing match found: {}.  Returning.", match);
                    return match;
                }

                if (timeout == Timeout.NO_WAIT) {
                    log.debug("No matching entries found, and timeout is NO_WAIT." +
                            "  Returning null.");
                    return null;
                } else {
                    log.debug("No exisiting matching found.  Now waiting for listener to be" +
                            " notified.");
                    if (timeout == Timeout.WAIT_FOREVER) {
                        entryAvailableCondition.await();
                    } else {
                        entryAvailableCondition.await(timeout, TimeUnit.MILLISECONDS);
                    }
                    log.debug("Listener notified or timeout exceed.  Returning {}", entry);
                    return entry;
                }
            } catch (InterruptedException e) {
                log.error("Read operation interrupted prematurely.", e);
                // Recommended practice rethrow the InterruptedException.
                throw e;
            }finally {
                if (listenerId != null) {
                    log.debug("Removing listener {}", listenerId);
                    clazzMap.removeEntryListener(listenerId);
                }
            }
        } finally {
            entryLock.unlock();
        }
    }

    // Note: These will get called from another Thread.

    //We're only interested when a new matching entry enters the map.
    @Override
    public void entryAdded(EntryEvent<String, V> event) {
        log.debug("Entry added.");
        entryLock.lock();
        try {
            entry = event.getValue();
            entryAvailableCondition.signal();
        } finally {
            entryLock.unlock();
        }
    }

    @Override
    public void entryEvicted(EntryEvent<String, V> event) {
    }

    @Override
    public void entryRemoved(EntryEvent<String, V> event) {
    }

    @Override
    public void entryUpdated(EntryEvent<String, V> event) {
    }
}
