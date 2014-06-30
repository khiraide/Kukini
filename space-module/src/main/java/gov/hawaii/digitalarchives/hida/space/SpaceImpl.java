package gov.hawaii.digitalarchives.hida.space;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.Assert;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.transaction.TransactionContext;



/**
 * The default implementation of {@link Space}.  Built on top of HazelCast.
 *
 * @author Dongie Agnir
 */
@Configurable
public class SpaceImpl implements Space {
    /**
     * Name of the {@link ExecutorService} used by the space processors, such
     * as {@link CancelLeaseProcessor}.
     */
    static final String SPACE_PROCESSORS_EXECUTOR_SERVICE = "SPACE_PROCESSORS";

    /**
     * The maximum amount of time in milliseconds that an operation will wait
     * on a lock/condition to be notified/signalled before running again.
     * <p>
     * For example, in TransactionalReadOp, if the operation does not find a
     * match (space is empty or all entries being taken), this is the maximum
     * amount of time the operation will wait for a new matching entry to
     * enter the space before just retrying the whole operation again.
     */
    static final long MAX_POLL_DELAY = 500L;

    /**
     * Prefix string for the name of read-write semaphores used by this
     * {@code Space}.
     */
    private static final String READ_WRITE_SEM_PREFIX = "READ_WRITE_SEM_";

    /**
     * Name of the lease cancellation map used by the
     * {@link CancelleaseProcessor}.
     */
    private static final String CANCEL_LEASE_MAP = "CANCEL_LEASE_MAP";

    private final HazelcastInstance hzInstance;
    private final String name;

    private Logger log;

    // Single thread to use when performing operations that don't supply an
    // existing transaction.
    private final ExecutorService singleOpExecutor = Executors.newSingleThreadExecutor();

    /**
     * Package-private constructor.
     *
     * @param hcInstance The {@code HazelcastInstance} that this Space abstracts
     *        over.
     * @param name The name of this space. Used to logically separate spaces
     *        that share the same {@code HazelcastInstance}.
     * @param log The logger this instance will use to write messages to the
     *        log.
     */
    SpaceImpl(final HazelcastInstance hcInstance, final String name, final Logger log) {
        Assert.notNull(hcInstance, "hcInstance must not be null.");
        Assert.notNull(name, "name must not be null.");
        Assert.hasLength(name, "name must not be empty.");
        Assert.notNull(log, "log must not be null.");
        this.hzInstance = hcInstance;
        this.name = name;
        this.log = log;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public <T> Lease write(final Class<T> clazz, final T entry, Transaction transaction,
            long duration) {
        log.debug("Entering write(clazz = {}, entry = {}, transaction = {}, duration = {})", clazz,
                entry, transaction, duration);

        Lease lease = null;
        String key = makeUniqueEntryId();
        if (transaction == null) {
            lease = writeInternal(key, entry, clazz, duration);
        } else {
            lease = writeInternal(key, entry, clazz, (TransactionImpl) transaction, duration);
        }

        log.debug("Exiting write(): {}", lease);
        return lease;
    }

    @Override
    public <T> T read(Class<T> clazz, Predicate<String, T> predicate,
            Transaction transaction, long timeout) {
        log.debug("Entering read(clazz = {}, predicate = {}, transaction = {}, timeout = {})",
                clazz, predicate, transaction, timeout);

        T entry = null;

        if (transaction == null) {
            entry = readInternal(clazz, predicate, timeout);
        } else {
            entry = readInternal(clazz, predicate, (TransactionImpl) transaction, timeout);
        }

        log.debug("Exiting read(): {}", entry);
        return entry;
    }

    @Override
    public <T> T readIfExists(Class<T> clazz, Predicate<String, T> predicate,
            Transaction transaction, long timeout) {
        log.debug("Entering readIfExists(clazz = {}, predicate = {}, transaction = {}," +
                " timeout = {})", clazz, predicate, transaction, timeout);

        T entry = null;

        log.debug("Exiting readIfExists(): {}", entry);
        return entry;
    }

    @Override
    public <T> T take(Class<T> clazz, Predicate<String, T> predicate,
            Transaction transaction, long timeout) {
        log.debug("Entering take(clazz = {}, predicate = {}, transaction = {}, timeout = {})",
                clazz, predicate, transaction, timeout);

        T entry = null;

        if (transaction == null) {
            entry = takeInternal(clazz, predicate, timeout);
        } else {
            entry = takeInternal(clazz, predicate, (TransactionImpl) transaction, timeout);
        }

        log.debug("Exiting take(): {})", entry);
        return entry;
    }

    @Override
    public <T> T takeIfExists(Class<T> clazz, Predicate<String, T> predicate,
            Transaction transaction, long timeout) {
        log.debug("Entering takeIfExists(clazz = {}, predicate = {}, transaction = {}," +
                " timeout = {})", clazz, predicate, transaction, timeout);

        T entry = null;

        log.debug("Exiting takeIfExists(): {})", entry);
        return entry;
    }

    @Override
    public Transaction beginTransaction() {
        log.debug("Entering beginTransaction()");
        try {
            TransactionContext ctx = hzInstance.newTransactionContext();
            ctx.beginTransaction();
            Transaction txn = TransactionImpl.newTransaction(ctx);
            log.debug("Exiting beginTransaction(): {}", txn);
            return txn;
        } catch (IllegalStateException e) {
            final String errorMessage = "There is already an existing Transaction in progress!";
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    /**
     * Renew the {@code Lease} using the given duration.
     *
     * @param oldLease The original lease to renew.
     * @param duration The requested duration for the new Lease.
     *
     * @return The renewed Lease.
     */
    <K, V> Lease renewLease(final NoTxnLease<K, V>  oldLease, long duration) {
        log.trace("Entering renewLease(oldLease = {}, duration = {})", oldLease, duration);
        V value = oldLease.getValue();
        Class<V> clazz = oldLease.getValueClass();
        IMap<String, V> map = getMap(clazz);
        // Entry still exists in the space.
        String key = oldLease.getKey().toString();
        if (map.tryLock(key)) {
            try {
                if (map.get(key) != null) {
                    Lease newLease = writeInternal(key, value, clazz, duration);
                    log.trace("Exiting renewLease(): {}", newLease);
                    return newLease;
                } else {
                    // The entry is no longer there.
                    String errorMessage = "Entry has been taken from the space.";
                    log.error(errorMessage);
                    throw new HidaException(errorMessage);
                }
            } finally {
                map.unlock(key);
            }
        } else {
            log.error("Could not lock key in order to renew the lease.");
            // Couldn't renew the lease, the return the current lease.
            return oldLease;
        }
    }

    /**
     * Expire the Lease that was not created under a transaction.
     *
     * @param lease The lease to expire.
     */
    <K, V> void expireLease(final NoTxnLease<K, V> lease) {
        log.trace("Entering expireLease(lease = {})", lease);
        if (lease.getExpiration() != Lease.Duration.FOREVER &&
            lease.getExpiration() >= System.currentTimeMillis()) {
            throw new IllegalStateException("Lease is already expired.");
        }
        Class<V> clazz = lease.getValueClass();
        IMap<K, V> map = getMap(clazz);
        map.remove(lease.getKey());
        log.trace("Exiting expireLease()");
    }


     /**
     * Helper method to perform a read operation not under a transaction.
     *
     * @param clazz The class of of the object to read.
     * @param predicate The predicate that specifies the conditions that a
     * matching entry should meet.
     * @param timeout the timeout period for this operation.
     */
    private <T> T readInternal(final Class<T> clazz, final Predicate<String, T> predicate,
            final long timeout) {
        log.trace("Entering readInternal(clazz = {}, predicate = {}, timeout = {})", clazz,
                predicate, timeout);

        Future<T> readFuture = singleOpExecutor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                Transaction txn = SpaceImpl.this.beginTransaction();
                try {
                    return SpaceImpl.this.readInternal(clazz, predicate, (TransactionImpl) txn,
                            timeout);
                } finally {
                    txn.commit();
                }
            }
        });

        try {
            T entry = readFuture.get();
            log.trace("Exiting readInternal(): {}", entry);
            return entry;
        } catch (InterruptedException ie) {
            log.error("Read operation interrupted!");
            throw new HidaException("Read operation interrupted!");
        } catch (ExecutionException e) {
            log.error("Exception occurred during read.", e.getCause());
            throw new HidaException("Error during read operation!");
        }
    }

    /**
     * Internal method to carry out a read operation under a transaction.
     *
     * @param clazz The class of the entry to read.
     * @param predicate The predicate to use for querying the underlying
     *        {@link IMap}.
     * @param txn The transaction the read operation will run under.
     * @param timeout The amount of time this operation should wait for a match.
     *
     * @return The entry that matches the given predicate, or {@code null} if
     *         the operation timed out.
     */
    private <T> T readInternal(final Class<T> clazz, final Predicate<String, T> predicate,
            final TransactionImpl txn, final long timeout) {
        log.trace("Entering readInternal(clazz = {}, predicate = {}, txn = {}, timeout = {}",
                clazz, predicate, txn, timeout);

        TransactionalReadOp<String, T> readOp = new TransactionalReadOp<>(this, txn, clazz,
                predicate, timeout, this.log);
        T read = readOp.run();
        txn.addOperation(readOp);

        log.trace("Exiting readInternal(): {}", read);
        return read;
    }

    /**
     * Internal method to carry out a take operation  not under a transaction.
     *
     * @param clazz The class of the entry to take.
     * @param predicate The predicate to use for querying the underlying
                        {@link IMap}.
     * @param timeout The amount of time this operation should wait for a match.
     *
     * @return The entry that matches the given predicate, or {@code null} if
     *         the operation timed out.
     */
    private <V> V takeInternal(final Class<V> clazz, final Predicate<String, V> predicate,
        final long timeout) {
        log.trace("Entering takeInternal(clazz = {}, predicate = {}, timeout = {})", clazz,
            predicate, timeout);

        try {
            V entry = null;
            // Create a transaction in a separate thread, run the take
            // operation using the transaction, then immediately commit it. The
            // separate thread is used because the calling method could already
            // have a transaction active on this (the calling) thread.
            Future<V> takeFuture = singleOpExecutor.submit(new Callable<V>() {
               @Override
               public V call() throws Exception {
                   Transaction t = SpaceImpl.this.beginTransaction();
                   try {
                       return SpaceImpl.this.takeInternal(clazz, predicate, (TransactionImpl) t,
                               timeout);
                   } finally {
                       // Single operation.  Commit it right away.
                       t.commit();
                   }
               }
            });

            entry = takeFuture.get();
            log.trace("Exiting takeInternal(): {}", entry);
            return entry;
        } catch (InterruptedException ie) {
            String msg = "Take operation interrupted before timeout expired!";
            log.error(msg, ie);
            throw new HidaException(msg, ie);
        } catch (ExecutionException e) {
            String msg = "Error encountered while performing take operation.";
            log.error(msg, e);
            throw new HidaException(msg, e);
        }
    }

    private <V> V takeInternal(final Class<V> clazz, final Predicate<String, V> predicate,
            final TransactionImpl txn, final long timeout) {
        TransactionalTakeOp<String, V> takeOp = new TransactionalTakeOp<>(this, txn, clazz,
                predicate, timeout, log);
        V taken = takeOp.run();
        txn.addOperation(takeOp);
        return taken;
    }
     /** Helper method that contains the logic for writing an entry into the space
     * without a transaction.
     *
     * @param key The key to use for storing the entry.
     * @param value The value to store.
     * @param clazz The class of the value to write into the space.
     * @param duration The time in milliseconds Hazelcast will keep entry
     * around.
     *
     * @return The lease created.
     */
    private <T> Lease writeInternal(final String key, final T value, Class<T> clazz,
            final long duration) {
        log.trace("Entering writeInternal(key = {}, value = {}, clazz = {}, duration = {}", key,
                value, clazz, duration);
        IMap<String, T> map = getMap(clazz);

        map.put(key, value, duration, TimeUnit.MILLISECONDS);

        long expiration = Lease.Duration.FOREVER;
        if (duration != Lease.Duration.FOREVER) {
            expiration = System.currentTimeMillis() + duration;
        }

        NoTxnLease<String, T> lease = new NoTxnLease<String, T>(this, expiration, key, value,
                clazz);
        log.trace("Exiting writeInternal(): {}", lease);
        return lease;
    }

    private <V> TransactionalLease<String, V> writeInternal(final String key, final V entry,
            final Class<V> clazz, final TransactionImpl txn, long duration) {
        log.trace("Entering writeInternal(key = {}, entry = {}, clazz = {}, txn = {}, " +
            "duration = {}", key, entry, clazz, txn, duration);

        TransactionalWriteOp<String, V> writeOp = new TransactionalWriteOp<String, V>(this, clazz,
                key, entry, txn, duration);
        TransactionalLease<String, V> lease = writeOp.run();
        txn.addOperation(writeOp);

        log.trace("Exiting writeInternal(): {}", lease);
        return lease;
    }

    <K, V> IMap<K, V> getMap(Class<V> clazz) {
        return hzInstance.getMap(name + clazz.getName());
    }

    /**
     * Retrieve the {@link TransactionalMap} associated with the given class.
     *
     * @param txn The transaction whose context ({@link TransactionContext}) the
     *        space will use to retrieve the {@code TransactionalMap}.
     * @param clazz The class of the entry which will be used to retrieve the
     *        appropriate map.
     * @return The {@code TransactionalMap} associated with the transaction that
     *         contains instances of the given class.
     */
    <K,V> TransactionalMap<K,V> getTransactionalMap(TransactionImpl txn, Class<V> clazz) {
        TransactionContext txnContext = txn.getTransactionContext();
        return txnContext.getMap(name + clazz.getName());
    }

    static IMap<String,String> getCancelLeaseMap(final HazelcastInstance hzInstance) {
        return hzInstance.getMap(CANCEL_LEASE_MAP);
    }

    IMap<String, String> getCancelLeaseMap() {
        return getCancelLeaseMap(this.hzInstance);
    }

    /**
     * Retrieve an {@link ISemaphore} that is tied to the given entry key. The
     * semaphore is used to guard the entry when reading and taking. The
     * {@code ISemaphore} is initialized to have maximum permit count of
     * {@code Integer#MAX_VALUE}.
     *
     * @param key The key of the entry.
     * @return The {@code ISemaphore} tied to the given key.
     */
    <K> ISemaphore getReadWriteSemaphore(K key) {
        ISemaphore semaphore = hzInstance.getSemaphore(READ_WRITE_SEM_PREFIX + key);
        // Initialize the semaphore to have Integer.MAX_VALUE, effectively
        // allowing it to have unlimited permits. This is to allow an
        // "unlimited" number of readers to each acquire a permit.

        // Note that calling init() if it's already been previously initialized
        // has no effect.
        semaphore.init(Integer.MAX_VALUE);
        return semaphore;
    }

    private String makeUniqueEntryId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
