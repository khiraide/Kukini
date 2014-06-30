package gov.hawaii.digitalarchives.hida.space;

/**
 * Encapsulates a "lease" on an entry written into a {@link Space}.  Allows for
 * cancelling and renewing the lease.
 *
 * @author Dongie Agnir
 */
public interface Lease {
    /**
     * Constants for {@code Lease} durations.
     */
    public static final class Duration {
        private Duration() {}
        /** Specify that the {@link Space} should keep the entry forever. */
        public static final long FOREVER = 0L;
    }

    /**
     * @return The time in milliseconds that the lease will expire.  This is
     * milliseconds from the UNIX epoch time: January 1, 1970 at 00:00:00.
     */
    public long getExpiration();

    /**
     * If this {@code Lease} is still valid, cancels the lease.
     */
    public void cancel();

    /**
     * If this {@code Lease} is still valid, renews the lease with the given
     * duration.  The duration specified becomes the new lease time for the
     * entry.
     *
     * @param duration Reset the lease time for the entry to this amount in
     *                 milliseconds.
     */
    public void renew(long duration);
}
