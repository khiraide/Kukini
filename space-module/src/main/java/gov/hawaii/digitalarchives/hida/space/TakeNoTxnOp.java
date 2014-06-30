package gov.hawaii.digitalarchives.hida.space;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import com.hazelcast.core.IMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.EntryListener;
import com.hazelcast.query.Predicate;
import com.hazelcast.core.EntryEvent;

import org.slf4j.Logger;

/**
* Basic implementation of the Space.take operation.  In an implementation where
* it's not possible to have transactions, this works just fine.  <b>DO NOT</b>
* use this version if it's possible to have transactions in because it does not
* take into account entries that are being read by an external transaction.
* <p>
* Due to bugs in Hazelcast regarding transactions and semaphores, this is
* provided for cases where we don't use transactions.
*
* @author Dongie Agnir
*/
public class TakeNoTxnOp<V> implements NoTxnOperation<V>, EntryListener<String, V> {
    private final Predicate<String, V> predicate;
    private final IMap<String, V> clazzMap;
    private final long timeout;
    private final Logger log;

    // Lock and Condition object used in place of plain Object/this for easier
    // unit testing.
    private final Lock addedLock = new ReentrantLock();
    private final Condition addedSignal;

    public TakeNoTxnOp(final IMap<String, V> clazzMap, final Predicate<String, V> predicate,
            final long timeout, Logger log) {
        this.clazzMap = clazzMap;
        this.predicate = predicate;
        this.timeout = timeout;
        this.log = log;
        this.addedSignal = addedLock.newCondition();
    }

    @Override
    public V call() throws Exception {
        // Register the listener in order to signal when a new matching entry
        // enters the map.
        String listenerId = clazzMap.addEntryListener(this, predicate, null, false);
        try {
            long timeLeft = timeout;
            V taken = null;
            do {
                addedLock.lock();
                try {
                    long t1 = System.currentTimeMillis();
                    // Get the matches already in the map
                    Set<String> matches = clazzMap.keySet(this.predicate);
                    log.debug("Got matches {}", matches);

                    // Iterate over the keys and try to remove them.
                    // IMap.remove is an atomic operation.
                    for (String key : matches) {
                        taken = clazzMap.remove(key);
                        if (taken != null) {
                            break;
                        }
                    }

                    if (taken == null) {
                        if (timeout == Space.Timeout.WAIT_FOREVER) {
                            addedSignal.await();
                        } else {
                            addedSignal.await(timeLeft, TimeUnit.MILLISECONDS);
                        }
                    }

                    long t2 = System.currentTimeMillis();
                    final long elapsed = t2 - t1;

                    if (timeout != Space.Timeout.WAIT_FOREVER) {
                            timeLeft -= elapsed;
                    }
                } finally {
                    addedLock.unlock();
                }
            } while (taken == null && timeLeft > 0L);

            return taken;
        }catch (InterruptedException e) {
           log.error("Take operation interrupted externally while waiting for entries!", e);
           throw e;
        } finally {
            // Remove our listener before exiting.
            clazzMap.removeEntryListener(listenerId);
        }
    }

    @Override
    public void entryAdded(EntryEvent<String, V> event) {
        log.debug("Entering entryAdded()");
        addedLock.lock();
        try {
            addedSignal.signalAll();
        } finally {
            addedLock.unlock();
        }
        log.debug("Exiting entryAdded()");
    }

    @Override
    public void entryRemoved(EntryEvent<String, V> event) {
    }

    @Override
    public void entryUpdated(EntryEvent<String, V> event) {
    }

    @Override
    public void entryEvicted(EntryEvent<String, V> event) {
    }
}
