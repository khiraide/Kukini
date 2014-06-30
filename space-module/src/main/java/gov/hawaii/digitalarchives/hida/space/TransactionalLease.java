package gov.hawaii.digitalarchives.hida.space;

import com.hazelcast.core.IMap;

/**
 * {@code Lease} implementation to be used with {@link TransactionalWriteOp}.
 *
 * @param <K> The type of the key that identifies the entry in an {@link IMap}.
 * @param <V> The type of the entry that was written.
 */
class TransactionalLease<K, V> implements Lease {
    private final SpaceImpl space;
    private final TransactionalWriteOp<K, V> writeOp;
    private long expiration;

    /**
     * Constructor.
     *
     * @param space The space that created the write operation.
     * @param transaction The transaction that the write operation ran under.
     * @param writeOp The write operation that created this lease.
     * @param expiration The time (in milliseconds) when this lease will expire.
     */
    public TransactionalLease(final SpaceImpl space,  final TransactionalWriteOp<K, V> writeOp,
            final long expiration) {
        this.space = space;
        this.writeOp = writeOp;
        this.expiration = expiration;
    }

    @Override
    public long getExpiration() {
        return expiration;
    }

    @Override
    public void cancel() {
        IMap<String, String> cancelLeaseMap = space.getCancelLeaseMap();
        cancelLeaseMap.put(writeOp.getKey().toString(), space.getMap(writeOp.getEntryClass())
                .getName());
    }

    @Override
    public void renew(long duration) {
        //TODO
        // This requires TransactionalMap.put(K,V,long,TimeUnit), which is not
        // available yet.
        throw new UnsupportedOperationException("This operation is not yet supported.");
    }
}
