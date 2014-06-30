package gov.hawaii.digitalarchives.hida.space;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.hazelcast.transaction.TransactionContext;

/**
 * Implementation of the {@link Transaction} interface.
 * <p>
 * This object enforces the fact that only one {@code TransactionImpl} can
 * exist at any given time in a thread.  Only once a transaction has become
 * <b>invalid</b>(either by committing or aborting it), can a thread create
 * another one.
 *
 * @author Dongie Agnir
 */
public final class TransactionImpl implements Transaction {
    // Use a ThreadLocal object to make sure our instance is unique to our
    // thread.
    private static final ThreadLocal<Transaction> currentTransaction =
        new ThreadLocal<Transaction>() {
        protected Transaction initialValue() {
            return  null;
        };
    };

    private final TransactionContext transactionContext;
    private final long owningThreadId;

    private final String id;
    private boolean valid;

    // Used to contain the operations performed within the transaction.  We use
    // a wildcard for the type because we don't require the type information,
    // and also so we can hold any valid operation.
    //
    // Note that it's a Set to prevent having the same operation twice.  Also,
    // the use of LinkedHashSet is somewhat important because it preserves
    // insert order.
    private final Set<TransactionalOperation<?>> operations =
        new LinkedHashSet<TransactionalOperation<?>>();

    /**
     * Instantiate a new Transaction object.
     *
     * @param transactionContext The Hazelcast TransactionContext this
     * Transaction will use.
     *
     * @return The newly created Transaction.
     * @throws IllegalStateException If there is already a valid Transaction
     * for the thread.
     */
    static Transaction newTransaction(final TransactionContext transactionContext) {
        Transaction txn = currentTransaction.get();

        if (txn == null || !txn.isValid()) {
            txn = new TransactionImpl(transactionContext, Thread.currentThread().getId());
            currentTransaction.remove();
            currentTransaction.set(txn);
            return txn;
        } else {
            throw new IllegalStateException("There is already a Transaction in progress! You" +
                    " must commit or abort it first.");
        }
    }

    /**
     * Private constructor.
     *
     * @param transactionContext The Hazelcast-supplied {@code
     * TransactionContext} that this object will use.
     * @param owningThreadId The ID of the thread that created this {@code
     * Transaction}.
     */
    private TransactionImpl(final TransactionContext transactionContext,
            final long owningThreadId) {
        this.transactionContext = transactionContext;
        this.owningThreadId = owningThreadId;

        this.id = UUID.randomUUID().toString();
        this.valid = true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void commit() {
        assertCalledFromOwner();
        assertIsValid();
        try {
            valid = false;
            transactionContext.commitTransaction();
        } catch (RuntimeException e) {
            String errorMessage = "The transaction could not be commited.";
            transactionContext.rollbackTransaction();
            throw new HidaException(errorMessage, e);
        } finally {
            for (TransactionalOperation<?> op : operations) {
                op.releaseLocks();
            }
            operations.clear();
            currentTransaction.remove();
        }
    }

    @Override
    public void abort() {
        assertCalledFromOwner();
        assertIsValid();
        try {
            valid = false;
            transactionContext.rollbackTransaction();
        } catch (RuntimeException e) {
            String errorMessage = "The transaction could not be rolled back.";
            throw new HidaException(errorMessage, e);
        } finally {
            for (TransactionalOperation<?> op : operations) {
                op.releaseLocks();
            }
            operations.clear();
            currentTransaction.remove();
        }
    }

    /** Accessor for the internal {@code TransactionContext}.
     *
     * @return The Hazelcast TransactionContext.
     */
    TransactionContext getTransactionContext() {
        return transactionContext;
    }

    /**
     * Add an operation to this Transaction.  The operation must already have
     * been performed ({@link TransactionalOperations#doOperation} was called)
     * before adding.
     */
    void addOperation(final TransactionalOperation<?> operation) {
        operations.add(operation);
    }

    /**
     * Used to assert that the calling Thread is the same as the owning Thread.
     *
     * @throws IllegalStateException Thrown if the caller is a thread other
     * than the one that created it.
     */
    private void assertCalledFromOwner() {
        if (Thread.currentThread().getId() != owningThreadId) {
            throw new IllegalStateException("Transaction may not be used by any thread that is " +
                    "not the owner!  Owner ID: " + this.owningThreadId);
        }
    }

    /**
     * Check that this transaction is valid and throw an
     * {@link IllegalStateException} if it isn't.
     */
    private void assertIsValid() {
        if (!isValid()) {
            throw new IllegalStateException("Transaction is no longer active!");
        }
    }
}
