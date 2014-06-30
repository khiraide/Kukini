package gov.hawaii.digitalarchives.hida.space;

import org.springframework.beans.factory.annotation.Configurable;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.util.Assert;

import org.slf4j.Logger;

/**
 * A lease object that is not transaction-aware.
 *
 * @author Dongie Agnir
 */
@Configurable
class NoTxnLease<K, V> implements Lease {
    private final SpaceImpl space;
    private long expiration;
    private final K key;
    private final V value;
    private final Class<V> valueClass;

    @AutowiredLogger
    private Logger log;

    NoTxnLease(final SpaceImpl space, final long expiration, final K key,  final V value,
            Class<V> valueClass) {
        Assert.notNull(space, "space must not be null.");
        Assert.isTrue(expiration >= 0L, "expiration time must be positive.");
        Assert.notNull(key, "key must not be null.");
        Assert.notNull(value, "value must not be null.");
        Assert.notNull(valueClass, "valueClass must not be null.");

        this.space = space;
        this.expiration = expiration;
        this.key = key;
        this.value = value;
        this.valueClass = valueClass;
    }
    @Override
    public long getExpiration() {
        log.debug("Entering getExpiration()");
        log.debug("Exiting getExpiration(): {}", expiration);
        return expiration;
    }

    @Override
    public void cancel() {
        log.debug("Entering cancel()");
        if (expiration != Lease.Duration.FOREVER &&
            System.currentTimeMillis() >= expiration) {
            String errorMsg = "Lease has already expired.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        space.expireLease(this);
        log.debug("Exiting cancel()");
    }

    @Override
    public void renew(final long duration) {
        log.debug("Exiting renew(duration = {})", duration);
        Assert.isTrue(duration >= 0L, "duration must not be negative.");
        if (expiration != Lease.Duration.FOREVER &&
            System.currentTimeMillis() >= expiration) {
            String errorMsg = "Lease has already expired.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        Lease newLease = space.renewLease(this, duration);
        this.expiration = newLease.getExpiration();
        log.debug("Exiting renew()");
    }

    /**
     * @return The key of the entry this lease is managing.
     */
    K getKey() {
        log.trace("Entering getKey()");
        log.trace("Exiting getKey(): {}", key);
        return key;
    }

    /**
     * @return The entry this lease is managing.
     */
    V getValue() {
        log.trace("Entering getValue()");
        log.trace("Exiting getValue(): {}", value);
        return value;
    }

    /**
     * @return The class of the value being managed by this Lease.
     */
    Class<V> getValueClass() {
        log.trace("Entering getValueClass()");
        log.trace("Exiting getValueClass(): {}", valueClass);
        return valueClass;
    }
}
