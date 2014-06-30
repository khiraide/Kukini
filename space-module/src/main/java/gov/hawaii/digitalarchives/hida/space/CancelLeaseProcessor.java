package gov.hawaii.digitalarchives.hida.space;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;

/**
 * This is a long running task that iterates over the queue of cancelled entries
 * and removes them from their maps. One per Hazelcast instance is sufficient
 * because the cancellation queue is shared among all Spaces using the instance.
 * <p>
 * Hazelcast does not "distribute" Callables; they are run on the instance on
 * which it is submitted.  So each JVM that creates a Hazelcast instance should
 * also submit this.  This is done through {@link SpaceCreator}.
 *
 * @author Dongie Agnir
 */
class CancelLeaseProcessor implements Serializable, Callable<Void>,
        EntryListener<String, String> {
    private static final String INSTANCE_LOCK_PREFIX = "INSTANCE_LEASE_CANCEL_PROCESSOR_LOCK_";
    private static final long serialVersionUID = 1L;

    private final Lock entryAddedLock = new ReentrantLock();
    private final Condition entryAddedCondition = entryAddedLock.newCondition();

    @Override
    public Void call() throws Exception {
        HazelcastInstance hzInstance = getInstance();
        IMap<String, String> cancelLeaseMap = SpaceImpl.getCancelLeaseMap(hzInstance);
        ILock instanceLeaseProcessorLock = hzInstance.getLock(INSTANCE_LOCK_PREFIX
                + hzInstance.getName());

        // Don't run if there's already a processor for this instance.
        if (instanceLeaseProcessorLock.tryLock()) {
            try {
                // Take an exclusive lock on the cancellation map, so that only
                // a single processor at a time is working on it.
                ILock mapLock = hzInstance.getLock(cancelLeaseMap.getName());
                mapLock.lock();
                try {
                    processLeaseCancellations(hzInstance);

                    // note: return not actually reached,
                    // processLeaseCancellations is an infinite loop.
                    return null;
                } finally {
                    mapLock.unlock();
                }
            } finally {
                instanceLeaseProcessorLock.unlock();
            }
        } else {
            return null;
        }
    }

    /**
     * @return The HazelcastInstance on this JVM.
     */
    private HazelcastInstance getInstance() {
        return Hazelcast.getAllHazelcastInstances().iterator().next();
    }

    /**
     * Infinite loop that iterates over the cancelled entries and removes them
     * from their maps.
     * <p>
     * This loop never returns.
     *
     * @param hzInstance The HazelcastInstance that this processor is working with.
     * @throws InterruptedException
     */
    private void processLeaseCancellations(final HazelcastInstance hzInstance)
            throws InterruptedException {
        IMap<String, String> cancelLeaseMap = SpaceImpl.getCancelLeaseMap(hzInstance);
        while (true) {
            entryAddedLock.lock();
            try {
                Iterator<Map.Entry<String, String>> iter = cancelLeaseMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> cancellation = iter.next();
                    final String mapName = cancellation.getValue();
                    final String entryKey = cancellation.getKey();
                    IMap<String, ?> entryMap = hzInstance.getMap(mapName);
                    entryMap.delete(entryKey);
                    cancelLeaseMap.delete(entryKey);
                }

                entryAddedCondition.await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } finally {
                entryAddedLock.unlock();
            }
        }
    }

    @Override
    public void entryAdded(EntryEvent<String, String> arg0) {
        entryAddedLock.lock();
        try {
            entryAddedCondition.signalAll();
        } finally {
            entryAddedLock.unlock();
        }

    }

    @Override
    public void entryEvicted(EntryEvent<String, String> arg0) {
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> arg0) {
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> arg0) {
    }
}
