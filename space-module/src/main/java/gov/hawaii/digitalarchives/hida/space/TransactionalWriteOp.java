
package gov.hawaii.digitalarchives.hida.space;

import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;

/**
 * Implementation of transactional write operation.
 * <p>
 * <b>Note:</b> This implementation does not yet support lease durations other
 * than {@link Lease.Duration#FOREVER}.
 *
 * @author Dongie Agnir
 *
 * @param <K> The type of the key that identifies the entry in an {@link IMap}.
 * @param <V> The type of the entry being written.
 */
class TransactionalWriteOp<K,V> implements TransactionalOperation<TransactionalLease<K,V>> {
    private final SpaceImpl space;
    private final Class<V> entryClass;
    private final K entryKey;
    private final V entry;
    private final TransactionImpl transaction;
    private final long duration;

    /**
     * Constructor
     *
     * @param space The space that is the owner of this operation.
     * @param entryClass The type of the entry to be written.
     * @param entryKey The key to use to identify the entry in the space.
     * @param entry The entry to be written.
     * @param transaction The transaction under which this operation will run.
     * @param duration The requested lease duration.
     */
    TransactionalWriteOp(final SpaceImpl space, final Class<V> entryClass, final K entryKey,
            final V entry, final TransactionImpl transaction, final long duration) {
        this.space = space;
        this.entryClass = entryClass;
        this.entryKey = entryKey;
        this.transaction = transaction;
        this.entry = entry;

        this.duration = duration;

        if (this.duration != Lease.Duration.FOREVER) {
            throw new UnsupportedOperationException("Durations other than FOREVER are currently " +
                    "not supported for Write operations under a transaction.");
        }
    }


    @Override
    public TransactionalLease<K, V> run() {
        TransactionalMap<K, V> txnMap = space.getTransactionalMap(transaction, entryClass);
        txnMap.put(entryKey, entry);
        return new TransactionalLease<K, V>(space, this, Long.MAX_VALUE);
    }

    @Override
    public void releaseLocks() {
        // NO-OP, operation does not take any locks.
    }

    /**
     * @return The key of the entry.
     */
    public K getKey() {
        return entryKey;
    }

    /**
     * @return The class of the entry.
     */
    public Class<V> getEntryClass() {
        return entryClass;
    }

    /**
     * @return The entry.
     */
    public V getEntry() {
        return entry;
    }

}
