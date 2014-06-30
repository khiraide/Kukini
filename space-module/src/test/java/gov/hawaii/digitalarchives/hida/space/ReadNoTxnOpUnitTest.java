package gov.hawaii.digitalarchives.hida.space;

import static org.mockito.Mockito.*;
import gov.hawaii.digitalarchives.hida.space.Space.Timeout;

import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;

import org.mockito.InOrder;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for ReadNoTxnOp.  These tests do not interact with an actual
 * Hazelcast instance, but uses mocks instead.  We're only interested in what
 * ReadNoTxnOp itself is doing.
 */
public class ReadNoTxnOpUnitTest {

    private HazelcastInstance mockHcInst;
    private IMap mockMap;
    private Predicate<String, Object> mockPredicate;
    private Logger mockLogger;
    private ReadNoTxnOp<Object> readOp;
    private Lock mockLock;
    private Condition mockCondition;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp() {
        mockHcInst = mock(HazelcastInstance.class);
        mockMap = mock(IMap.class);
        mockPredicate = (Predicate<String, Object>) mock(Predicate.class);
        mockLogger = mock(Logger.class);
        mockLock = mock(Lock.class);
        mockCondition = mock(Condition.class);

        when(mockLock.newCondition()).thenReturn(mockCondition);
        when(mockHcInst.getMap(any(String.class))).thenReturn(mockMap);
    }

    /**
     * Verify the behavior of ReadNoTxnOp when it is asked to not wait on
     * entries, and there are no matching entries in the map when it is run.
     * Specifically, it should not wait on a lock.
     */
    @Test
    public void testNoEntriesNoWait() throws Exception {
        when(mockMap.entrySet(mockPredicate)).thenReturn(new HashSet());

        readOp = new ReadNoTxnOp<>(mockMap, mockPredicate, Timeout.NO_WAIT,
                mockLogger, mockLock);
        readOp.call();

        verify(mockCondition, never()).await();
        verify(mockCondition, never()).await(anyLong(), any(TimeUnit.class));
    }

    /**
     * Verify the behavior of ReadNoTxnOp when it is asked to wait forever for a
     * valid entry, and there are no matching entries in the map when it is run.
     * It should attempt to wait forever on condition lock.
     */
    @Test
    public void testNoEntriesWaitForever() throws Exception {
        when(mockMap.entrySet(mockPredicate)).thenReturn(new HashSet());

        readOp = new ReadNoTxnOp<>(mockMap, mockPredicate, Timeout.WAIT_FOREVER,
                mockLogger, mockLock);

        readOp.call();

        // The operations below will be called in order when it attempts to
        // wait.
        InOrder inOrder = inOrder(mockMap, mockLock, mockCondition);
        inOrder.verify(mockLock).lock();
        inOrder.verify(mockLock).newCondition();
        inOrder.verify(mockMap).addEntryListener(any(EntryListener.class), any(Predicate.class),
                any(String.class), anyBoolean());
        // await() will be called, and not the await(long, TimeUnit) version
        // because await() will block as long as it takes (ie forever).
        inOrder.verify(mockCondition).await();
    }

    /**
     * Verify the behavior of ReadNoTxnOp when it is asked to wait a specified
     * amount of time (but not forever), for a matching entry, and there are no
     * matching entries in the map.  It should wait on the condition for the
     * given timeout.
     */
    @Test
    public void testNoEntriesWaitNotForever() throws Exception {
        when(mockMap.entrySet(mockPredicate)).thenReturn(new HashSet());

        final long timeout = 5000L;
        readOp = new ReadNoTxnOp<>(mockMap, mockPredicate, timeout, mockLogger,
                mockLock);

        readOp.call();

        // The operations below will be called in order when it attempts to
        // wait.
        InOrder inOrder = inOrder(mockMap, mockLock, mockCondition);
        inOrder.verify(mockLock).lock();
        inOrder.verify(mockLock).newCondition();
        inOrder.verify(mockMap).addEntryListener(any(EntryListener.class), any(Predicate.class),
                any(String.class), anyBoolean());
        // await(long, TimeUnit) variation will be called here because we want
        // to stop waiting after the specified time.
            inOrder.verify(mockCondition).await(timeout, TimeUnit.MILLISECONDS);
    }
}
