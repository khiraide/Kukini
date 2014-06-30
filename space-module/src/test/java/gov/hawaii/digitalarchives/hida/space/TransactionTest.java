package gov.hawaii.digitalarchives.hida.space;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;

/**
 * Unit tests for {@link TransactionImpl}.
 */
@ContextConfiguration(locations = { "classpath:/spring/applicationContext.xml" })
public class TransactionTest extends AbstractTestNGSpringContextTests {
    private static final String SPACE_NAME = "default";
    private HazelcastInstance hcInstance;
    private Logger spaceImplLogger;
    private TransactionContext txnContext;
    private SpaceImpl space;
    private Transaction txn;
    private ExecutorService executor;

    public TransactionTest() {
        spaceImplLogger = LoggerFactory.getLogger(SpaceImpl.class);
    }

    @BeforeMethod
    public void setUp() {
        hcInstance = mock(HazelcastInstance.class);
        txnContext = mock(TransactionContext.class);
        when(hcInstance.newTransactionContext()).thenReturn(txnContext);
        space = new SpaceImpl(hcInstance, SPACE_NAME, spaceImplLogger);
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterMethod
    public void rollback() {
        if (txn != null && txn.isValid()) {
            txn.abort();
        }
    }

    /**
     * Test that you can only create one Transaction per thread for a given
     * space.
     */
    @Test
    public void testOnlyOneActivePerThread() {
        txn = space.beginTransaction();

        try {
            space.beginTransaction();
            fail("The second beginTransaction() call should have thrown!");
        } catch (IllegalStateException e) {
            // gulp
        }
    }

    /**
     * Test that Transactions created from different threads are different
     * instances.
     */
    @Test
    public void separateTxnPerThread() throws InterruptedException, ExecutionException {
        txn = space.beginTransaction();
        Future<Transaction> f = executor.submit(new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
                Transaction txn2 = space.beginTransaction();
                txn2.abort();
                return txn2;
            }
        });

        assertFalse(f.get() == txn, "Call to beginTransaction() in a separate thread returned"
                + " the same Transaction instance.");
    }

    /**
     * Verify that a thread that did not create a Transaction cannot commit it.
     */
    @Test
    public void testNoCommitFromNonOwningThread() throws InterruptedException, ExecutionException {
        txn = space.beginTransaction();

        Future<Boolean> f = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean couldCommit;
                try {
                    // Expect this to throw.
                    // Note: we can't just call fail() here because this thread
                    // is separate from the one running our test case. We just
                    // return a boolean instead.
                    txn.commit();
                    couldCommit = true;
                } catch (Exception e) {
                    couldCommit = false;
                }

                return couldCommit;
            }
        });

        // The future returns whether the commit was successful. Expect this to
        // be false because the commit() call happens from a different thread.
        assertFalse(f.get(), "Thread that didn't own Transaction was able to commit it!");
        // The transaction should still be valid because the commit() (and
        // abort()) should catch the fact that the thread calling it isn't
        // allowed to and doesn't take any further action before throwing.
        assertTrue(txn.isValid());
    }

    enum TxnAction {
        COMMIT, ABORT
    }

    /**
     * Test that the locks held by {@link TransactionalOperation}s were released
     * after successful commit.
     */
    @Test
    public void testLocksReleasedCommitOk() {
        testCommitAbortCommon(TxnAction.COMMIT, false);
    }

    /**
     * Test that the locks held by {@link TransactionalOperation}s were released
     * after failed commit.
     */
    @Test
    public void testLocksReleasedCommitException() {
        testCommitAbortCommon(TxnAction.COMMIT, true);
    }

    /**
     * Test that the locks held by {@link TransactionalOperation}s were released
     * after successful abort.
     */
    @Test
    public void testLocksReleasedAbortOk() {
        testCommitAbortCommon(TxnAction.ABORT, false);
    }

    /**
     * Test that the locks held by {@link TransactionalOperation}s were released
     * after failed commit.
     */
    @Test
    public void testLocksReleasedAbortException() {
        testCommitAbortCommon(TxnAction.ABORT, true);
    }

    /**
     * Common function used by the testLocksReleased* methods. The logic for
     * these tests are largely the same, so they've been pulled out into this
     * function for convenience.
     *
     * @param action The action that should b performed.
     * @param shouldThrow Whether the action specified should throw or not.
     */
    private void testCommitAbortCommon(TxnAction action, boolean shouldThrow) {
        final int nOps = 8;

        // Throw an exception when TransactionImpl tries to call
        // commitTransaction or abortTransaction.
        if (shouldThrow) {
            switch (action) {
            case COMMIT:
                doThrow(new RuntimeException()).when(txnContext).commitTransaction();
                break;
            case ABORT:
                doThrow(new RuntimeException()).when(txnContext).rollbackTransaction();
                break;
            }
        }

        txn = space.beginTransaction();
        List<TransactionalOperation<?>> ops = new ArrayList<TransactionalOperation<?>>();

        // Use a mock for TransactionalOperations so we can verify that
        // TransactionImpl calls releaseLocks() on it after committing/aborting.
        for (int i = 0; i < nOps; i++) {
            TransactionalOperation<?> mockOperation = mock(TransactionalOperation.class);
            ops.add(mockOperation);
            ((TransactionImpl) txn).addOperation(mockOperation);
        }

        // Do the actual operation.
        try {
            switch (action) {
            case COMMIT:
                txn.commit();
                break;
            case ABORT:
                txn.abort();
                break;
            }

            if (shouldThrow) {
                fail("Operation did not throw as expected.");
            }
        } catch (Exception ignored) {
            if (!shouldThrow) {
                fail("Operation threw an unexpected exception.");
            }
        }

        // Verify that the TransactionImpl called releaseLocks() for all the
        // operations. This should happen regardless of whether the commit
        // failed and succeeded.
        for (TransactionalOperation<?> op : ops) {
            verify(op).releaseLocks();
        }
    }
}
