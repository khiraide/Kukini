
package gov.hawaii.digitalarchives.hida.space;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.query.Predicate;

/**
 * Implementation of transactional take operation.
 *
 * @author Dongie Agnir
 *
 * @param <K> The type of the key that identifies the entry in an {@link IMap}.
 * @param <V> The type of the entry being taken.
 */
public class TransactionalTakeOp<K,V> implements TransactionalOperation<V>, EntryListener<K, V>{
    private final SpaceImpl space;
    private final Predicate<K,V> predicate;
    private final long timeout;
    private final IMap<K,V> entryMap;
    private final TransactionalMap<K, V> txnMap;
    private final Logger log;

    private final Lock entryAddedLock = new ReentrantLock();
    private final Condition entryAddedCondition = entryAddedLock.newCondition();

    private ISemaphore takenSem = null;

    /**
     * Package-private constructor.
     *
     * @param space The Space that is the owner of this operation.
     * @param txn The transaction under which this take operation will run.
     * @param clazz The type of entry to to take from the space.
     * @param predicate The query predicate that will we used to find matching
     *        entries.
     * @param timeout The timeout value for operation.
     * @param log The logger that will be used by operation to log messages.
     */
    TransactionalTakeOp(final SpaceImpl space, final TransactionImpl txn, Class<V> clazz,
            final Predicate<K,V> predicate, final long timeout, final Logger log) {
        this.space = space;
        this.predicate = predicate;
        this.timeout = timeout;
        this.entryMap = space.getMap(clazz);
        this.txnMap = space.getTransactionalMap(txn, clazz);
        this.log = log;
    }

    @Override
    public V run() {
        final IMap<String, String> cancelLeaseMap = space.getCancelLeaseMap();
        final String listenerId = entryMap.addEntryListener(this, predicate, null, false);
        long timeLeft = timeout;
        V taken = null;
        try {
            Set<K> matches = new HashSet<K>();
            do {
                // Hold this lock until we're ready to wait for new entries.
                // Unlocked in finally block.
                entryAddedLock.lock();
                try {
                    final long t1 = System.currentTimeMillis();
                    matches.clear();

                    // Checking for matches in our own transaction allows us to
                    // take entries that we've written previously in the
                    // transaction.
                    matches.addAll(txnMap.keySet(predicate));
                    log.debug("Got matches {}", matches);

                    Iterator<K> iter = matches.iterator();
                    while (iter.hasNext() && taken == null) {

                        final K key = iter.next();
                        if (!cancelLeaseMap.containsKey(key)) {
                            ISemaphore entrySem = space.getReadWriteSemaphore(key);

                            // Note about the number of permits being acquired here:
                            // per the JavaSpaces specification, if an entry is
                            // being read by another transaction (via the
                            // Space.read() function), then it cannot be taken by
                            // another transaction. The way it's implemented here is
                            // using a semaphore tied to an entry. When it is read,
                            // the transaction reading it acquires a single permit
                            // only. Since the maximum number of permits is
                            // Integer.MAX_VALUE, it essentially allows any
                            // reasonable number of transactions to read the same
                            // entry. If however, we want to take the entry, we need
                            // to have exclusive access to it, so we try and acquire
                            // ALL of the permits, so no other transaction can
                            // acquire the semaphore, either for reading or taking.
                            if (entrySem.tryAcquire(Integer.MAX_VALUE)) {
                                try {
                                    if (entryMap.tryLock(key)) {
                                        try {
                                            taken = txnMap.remove(key);
                                        } finally {
                                            entryMap.unlock(key);
                                        }
                                    } else {
                                        log.warn("Acquired R/W lock, but could not lock entry in Map.");
                                    }
                                } finally {
                                    // If the entry was null, release its semaphore
                                    // now. If it isn't null, save the reference to
                                    // it, and hold on to the lock until
                                    // releaseLocks() is called.
                                    if (taken == null) {
                                        entrySem.release(Integer.MAX_VALUE);
                                    } else {
                                        this.takenSem = entrySem;
                                    }
                                }
                            } else {
                                log.debug("Could not acquire lock for entry {}", key);
                            }
                        }
                    }

                    // We did not take an entry in the above loop.  Time to wait.
                    if (taken == null) {
                        try {
                            long waitTime;
                            // If we found no matches, then we just wait until a
                            // match is added to the IMap.
                            if (matches.isEmpty()) {
                                waitTime = timeLeft;
                                log.debug("No matches were present in the entry IMap.  Waiting {}" +
                                        "ms for a matching entry to be added to the IMap.",
                                        waitTime);
                            } else {
                                // If there were matches, but we could not take
                                // any of them either because they were all
                                // being read or taken by another transaction,
                                // then we wait up to a maximum of
                                // SpaceImpl.MAX_POLL_DELAY. Then we go back to
                                // the start of the loop and check the the keys
                                // again and see if we can acquire any of them
                                // this time around.
                                waitTime = Math.min(timeLeft, SpaceImpl.MAX_POLL_DELAY);
                                log.debug("Found matches but could not take any of them.  Waiting" +
                                        " {}ms before checking again.", waitTime);
                            }
                            entryAddedCondition.await(waitTime, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            log.error("Thread interrupted while waiting.");
                            // Javadocs state that once we've caught the
                            // InterruptedeException, the interrupted flag is
                            // cleared. We call interrupt() to reset the flag so
                            // an outside observer can tell that we've been
                            // interrupted.
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }

                    final long t2 = System.currentTimeMillis();
                    if (timeout != Space.Timeout.WAIT_FOREVER) {
                        timeLeft -= t2 - t1;
                    }
                } finally {
                    entryAddedLock.unlock();
                }
            } while (taken == null && timeLeft > 0L);
            return taken;
        } finally {
            entryMap.removeEntryListener(listenerId);
        }
    }

    @Override
    public void releaseLocks() {
        if (takenSem != null) {
            takenSem.release(Integer.MAX_VALUE);
        }
    }

    // Begin EntryListener methods
    @Override
    public void entryAdded(EntryEvent<K, V> entry) {
        // The operation thread holds this lock for the majority of the time.
        // It's only unlocked when it begins its wait for new entries.
        entryAddedLock.lock();
        try {
            entryAddedCondition.signalAll();
        } finally {
            entryAddedLock.unlock();
        }
    }

    @Override
    public void entryEvicted(EntryEvent<K, V> entry) {
    }

    @Override
    public void entryRemoved(EntryEvent<K, V> entry) {
    }

    @Override
    public void entryUpdated(EntryEvent<K, V> entry) {
    }
    // End EntryListener methods
}
