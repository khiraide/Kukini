package gov.hawaii.digitalarchives.hida.space;


import com.hazelcast.transaction.TransactionContext;

public interface TransactionalOperation<R> {
    /**
     * Run the operation.
     */
    public R run();

    /**
     * Release any locks this operation held during the life of the
     * Transaction.  Should be by the Transaction only after if has aborted or
     * committed.
     */
    public void releaseLocks();
}
