package gov.hawaii.digitalarchives.hida.space;

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
 * Implementation of a transactional read operation for {@link SpaceImpl}.
 * <p>
 * To summarize how it works, the operation does the following:
 * <p>
 * - The operation begins by first by registering itself as an entry listener in
 * the entry {@code IMap} with the given {@code Predicate}.
 * <p>
 * - Before beginning, the operation also holds a lock that it uses in order to
 * be notified when any new matching entries are added to the Map.
 * <p>
 * - The operation loops for the duration of the given {@code timeout} value,
 * first checking for matches in the {@code TransactionalMap}, and for any of
 * the entries that match, it retrieves a semaphore that is used to guard they
 * key-value pair. If it is able to acquire the semaphore it reads the entry,
 * and if it isn't null, returns it to the caller.
 * <p>
 * - If the operation did not get any matches, OR it did get matches but could
 * not acquire any of the semaphores for them, it waits a little bit before
 * starting the process again. It waits on {@code entryAddedLock} for the
 * minimum between the remaining timeout and 500ms (or in other words, a maximum
 * of 500ms). Since the operation registered itself as an {@code EntryListener},
 * whenever it is notified of a new matching entry that enters the {@code IMap},
 * it notifies the reading thread through the {@code entryAddedCondition} which
 * was created from {@code entryAddedLock} . Notice that it can only do this
 * when the read operation relinquishes the lock, which can only happen when it
 * can't read any existing matches in the {@code TransactionalMap}, and waits on
 * the {@code Condition}.
 *
 * @param <K> The type of the key that identifies the entry in an
 *        {@link TransactionalMap}.
 * @param <V> The type of the entry being read.
 */
class TransactionalReadOp<K, V> implements TransactionalOperation<V>, EntryListener<K, V> {
    // The SpaceImpl that created this operation. Store the reference to to it
    // in order to call back to it for methods likes like
    // SpaceImpl.getTransactionalMap.
    private final SpaceImpl space;

    // The Predicate used to query txnMap for the matching entries.
    private final Predicate<K, V> predicate;

    // The timeout value of the operation.
    private final long timeout;

    // The entryMap that is used to register the operation as an EntryListener.
    // Note that it's not actually used for querying the space for entries.
    private final IMap<K, V> entryMap;

    // The TransactionalMap that is used to query for entries that match the
    // predicate. This is used rather than entryMap for two reasons: 1) Querying
    // it returns matches from entryMap, as well as matches that have been
    // written under the transactional previous to this operation. 2) A little
    // unimportant in the context of a read operation, but in general, we
    // perform transactional operations on the TransactionalMap so that it stays
    // in the transactional until the transaction is aborted of committed.
    private final TransactionalMap<K, V> txnMap;
    private final Logger log;

    // The lock that we used to synchronize the "reading" thread and the
    // "listener" thread. This is used by the listener thread to notify the
    // reading thread whenever it is notified by Hazelcast that a matching entry
    // has been written into the IMap.
    private final Lock entryAddedLock = new ReentrantLock();
    private final Condition entryAddedCondition = entryAddedLock.newCondition();

    // This is the semaphore guarding the entry that we read (if any). This
    // is unlocked in releaseLock.
    private ISemaphore readSem = null;

    TransactionalReadOp(final SpaceImpl space, final TransactionImpl txn, Class<V> clazz,
            final Predicate<K, V> predicate, final long timeout, final Logger log) {
        this.space = space;
        this.predicate = predicate;
        this.timeout = timeout;
        this.entryMap = space.getMap(clazz);
        this.txnMap = space.getTransactionalMap(txn, clazz);
        this.log = log;
    }

    /**
     * Run the transactional read operation. Note that this runs a in separate
     * thread than the {@link EntryListener} methods, so the two are
     * synchronized using entryAddedLock.
     *
     * @return The entry that is read.
     */
    @Override
    public V run() {
        long timeLeft = timeout;
        final IMap<String, String> cancelLeaseMap = space.getCancelLeaseMap();
        // The entry that we will read and return to the caller.
        V read = null;
        // Add ourself as a listener. Removed in finally block.
        final String listenerId = entryMap.addEntryListener(this, predicate, null, false);
        try {
            do {
                // Start time of this loop iteration.
                final long t1 = System.currentTimeMillis();

                // Acquire the lock before proceeding so that we can wait on the
                // condition later if we need to. Unlocked in finally block.
                entryAddedLock.lock();
                try {
                    // The set of keys that identify entries in the map that
                    // match our predicate.
                    //
                    // Note that we don't need to check the key set from
                    // entryMap because the set returned by txnMap also contains
                    // those that would be returned from entryMap, but not vice
                    // versa.
                    final Set<K> matches = txnMap.keySet(predicate);

                    final Iterator<K> iter = matches.iterator();
                    while (iter.hasNext() && read == null) {
                        // The identifying key of this particular matching
                        // entry.
                        final K entryKey = iter.next();

                        // Don't try to read the entry if its lease is being cancelled.
                        if (!cancelLeaseMap.containsKey(entryKey)) {
                            // The semaphore that is used to guard access to this
                            // entry.
                            ISemaphore entrySem = space.getReadWriteSemaphore(entryKey);
                            // We acquire a single permit here (as opposed to
                            // Integer.MAX_VALUE in Take). This allows for
                            // practically unlimited readers to share the entry,
                            // while locking out any takers.
                            if (entrySem.tryAcquire()) {
                                try {
                                    read = txnMap.get(entryKey);
                                } finally {
                                    // We don't hold on to the semaphore if it the
                                    // entry that we read is null. We just release
                                    // it right away. Otherwise we will hold on to
                                    // it.
                                    if (read == null) {
                                        entrySem.release();
                                    } else {
                                        this.readSem = entrySem;
                                    }
                                }
                            } else {
                                log.debug("Could not acquire semaphore for key {}.  It is likely "
                                        + "being taken by another Transaction.", entryKey);
                            }
                        }
                    }

                    // Didn't succeed in reading an entry so we now wait.
                    if (read == null) {
                        long waitTime;
                        if (matches.isEmpty()) {
                            // No matches in the Map. Wait the maximum of amount
                            // of timeLeft.
                            waitTime = timeLeft;
                        } else {
                            // There are some matches that we could not read.
                            // Wait up to SpaceImpl.MAX_POLL_DELAY until going
                            // back and seeing if they become eligible for
                            // reading.
                            waitTime = Math.min(timeLeft, SpaceImpl.MAX_POLL_DELAY);
                        }

                        log.debug("Could not read an entry from the IMap.  Waiting {}ms before "
                                + "trying again.", waitTime);
                        try {
                            entryAddedCondition.await(waitTime, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            log.error("Read operation interrupted while waiting.", e);
                            // Reset the interrupted flag before terminating so
                            // an outside observer can see that we were
                            // interrupted.
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }

                    // End of this iteration, now calculate how long it took.
                    final long t2 = System.currentTimeMillis();

                    if (timeLeft != Space.Timeout.WAIT_FOREVER) {
                        timeLeft = timeLeft - (t2 - t1);
                    }
                } finally {
                    entryAddedLock.unlock();
                }
            } while (read == null && timeLeft > 0L);
        } finally {
            entryMap.removeEntryListener(listenerId);
        }
        return read;
    }

    @Override
    public void releaseLocks() {
        if (readSem != null) {
            readSem.release();
        }
    }

    // Begin EntryListener methods
    @Override
    public void entryAdded(EntryEvent<K, V> event) {
        log.debug("Entering entryAdded(event = {})", event);
        // Acquire the lock so that we can interact with the condition. The
        // reading thread will only release the lock when it is ready to wait
        // for new entries to be added for the IMap.
        entryAddedLock.lock();
        try {
            entryAddedCondition.signalAll();
        } finally {
            entryAddedLock.unlock();
        }
        log.debug("Exiting entryAdded()");
    }

    @Override
    public void entryEvicted(EntryEvent<K, V> arg0) {
    }

    @Override
    public void entryRemoved(EntryEvent<K, V> arg0) {
    }

    @Override
    public void entryUpdated(EntryEvent<K, V> arg0) {
    }
    // End EntryListener methods
}
