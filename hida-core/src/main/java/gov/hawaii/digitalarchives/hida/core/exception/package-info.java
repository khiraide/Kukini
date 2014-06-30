/**
 * This package contains the root Exception and basic hierarchy for the Hawaii
 * Digital Archives Project (HiDA).
 * <p>
 * Note that the HiDA Project prefers unchecked exceptions over checked
 * exceptions.  This is why the root exception, {@link HidaException} extends
 * {@code RuntimeException}, rather than {@code Exception}.  Wherever necessary,
 * we will try to recreate the java version of an exception if it is a checked
 * exception.  One such example is {@link java.io.IOException}, being replaced
 * by {@link HidaIOException}.
 * <p>
 * @author Dongie Agnir
 */
package gov.hawaii.digitalarchives.hida.core.exception;
