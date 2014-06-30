package gov.hawaii.digitalarchives.hida.space;

/**
 * A transaction allows for grouping operations on a {@link Space} together so
 * that they are all performed atomically.
 *
 * @author Dongie Agnir
 */
public interface Transaction {
    /**
     * @return The ID of this Transaction.
     */
    public String getId();

    /**
     * @return Whether this Transaction is still valid or not.
     */
    public boolean isValid();

    /**
     * Abort the transaction.
     */
    public void abort();

    /**
     * Commit the transaction.
     */
    public void commit();
}
