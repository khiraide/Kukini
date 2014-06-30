package gov.hawaii.digitalarchives.hida.space;

import com.hazelcast.query.Predicate;

/**
 * A space provides a type-safe tuplespace.  This object tries to adhere to the
 * canonical API outlined in the JavaSpaces Specification, and attempts to
 * provide almost all of its semantics.  Only minor changes to the interface
 * have been made, mainly in the way in which queries (read, take), are done.
 *
 * @author Dongie Agnir
 */
public interface Space {
    public static class Timeout {
        /**
         * Private constructor to prevent instantiaton.
         */
        private Timeout() {}

        /**
         *Timeout value that means the operation should not block.
         */
        public static final long NO_WAIT = 0L;

        /**
         * Timeout value that indicates the operation should wait as long as it
         * takes to complete.
         */
        public static final long WAIT_FOREVER = Long.MAX_VALUE;
    }

    /**
     * @return The name of this Space.
     */
    public String getName();

    /**
     * Write an entry into this space.
     *
     * @param clazz The type of the  entry.
     * @param entry The entry to write into the space.
     * @param transaction If a transaction is provided, the write operation will
     * take place in the context of the transaction.  If so, the entry will not
     * be visible to others until the transaction is committed.
     * @param duration The lease time for this entry, given in milliseconds.  The
     * Space will hold this entry for this amount of time, then evict it.
     *
     * @return The lease for the entry granted by this Space.
     */
    public <T> Lease write(Class<T> clazz, T entry, Transaction transaction, long duration);

    /**
     * Read an entry from the space that satisfies the conditions in the given
     * predicate.
     * <p>
     * This operation does not remove the entry from the space.
     *
     * @param clazz The type of the entry to read.
     * @param predicate The query predicate that will be used to search the
     * space.
     * @param transaction An optional transaction.  Entries previously written
     * under the transaction will also be eligible for matching.
     * @param timeout The maximum amount of time this method will wait for an
     * eligible entry to become available.
     *
     * @return An entry that matches the conditions given in the predicate.
     * {@code null} if there was no match.
     */
    public <T> T read(Class<T> clazz, Predicate<String, T> predicate, Transaction transaction,
            long timeout);

    /**
     * Read an entry from the space that satisfies the conditions in the given
     * predicate.
     * <p>
     * This operation is similar to {@code read}, but entries locked by other
     * transactions are also visible to this operation.  If the only entries
     * available are locked by a transaction, this operation will wait up to the
     * duration specified by the {@code timeout} for any of those entries to
     * become available.
     * <p>
     * This operation does not remove the entry from the space.
     *
     * @param clazz The class of the type of entry to match on.
     * @param predicate The query predicate that will be used to search the
     * space.
     * @param transaction An optional transaction.  Entries previously written
     * under the transaction will also be eligible for matching.
     * @param timeout If the only eligible entries are locked by other
     * transactions, the  method will wait for up to this amount of time for one
     * of them to be eligible for reading.
     * @return An entry that matches the conditions given in the predicate.
     * {@code null} if there was no match.
     */
    public <T> T readIfExists(Class<T> clazz, Predicate<String, T> predicate, Transaction transaction,
            long timeout);

    /**
     * Removes an entry from the space that satisfies the conditions in the
     * given predicate.
     *
     * @param clazz The type of the entry to match on.
     * @param predicate The query predicate that will be used to search the
     * space.
     * @param transaction An optional transaction.  If one is given, this
     * operation will occur under the transaction and will not be visible to
     * other clients until it is committed.  In addition, entries written under
     * the transaction will also be eligible for matching.
     * @param timeout The maximum amount of time this method will wait for an
     * eligible entry to become available.
     *
     * @return An entry that matches the conditions given in the predicate.
     * {@code null} if there is no match.
     */
    public <T> T take(Class<T> clazz, Predicate<String, T> predicate, Transaction transaction,
            long timeout);


    /**
     * Removes an entry from the space that satisfies the conditions in the
     * given predicate.
     * <p>
     * This is similar to {@code take}, but entries locked by other transactions
     * are also visible to this operation.  If the only entries that satisfy the
     * the predicate are locked by a transaction, this method will wait up to
     * the duration specified by {@code timeout} for one of them to become
     * available before returning.
     *
     * @param clazz The type of the entry to match on.
     * @param predicate The query predicate that will be used to search the
     * space.
     * @param transaction An optional transaction.  If one is given, this
     * operation will occur under the transaction and will not be visible to
     * other clients until it is committed.  In addition, entries written under
     * the transaction will also be eligible for matching.
     * @param timeout If the only eligible entries are locked by other
     * transactions, the  method will wait for up to this amount of time for one
     * of them to be eligible for taking.
     *
     * @return An entry that matches the conditions given in the predicate.
     * {@code null} if there is no match.
     */
    public <T> T takeIfExists(Class<T> clazz, Predicate<String, T> predicate, Transaction transaction,
            long timeout);

    /**
     * Begin a new Transaction for the Space if one is not already in progress.
     *
     * @return The newly created {@code Transaction}.
     */
    public Transaction beginTransaction();
}
