package gov.hawaii.digitalarchives.hida.space;

import java.util.concurrent.Callable;

/**
 * An operation that does not operate on a transaction, and is also not
 * transaction-aware.
 *
 * @author Dongie Agnir
 */
public interface NoTxnOperation<T> extends Callable<T> {
}
