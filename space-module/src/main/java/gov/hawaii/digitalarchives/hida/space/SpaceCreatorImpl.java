package gov.hawaii.digitalarchives.hida.space;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.util.Assert;

import com.hazelcast.core.HazelcastInstance;

/**
 * Implementation of the {@link SpaceCreator} interface.
 */
public class SpaceCreatorImpl implements SpaceCreator {
    // All Space instances will share a single HazelcastInstance.
    private final HazelcastInstance hcInstance;

    private Logger log;

    /**
     * Constructor.
     *
     * @param hcInstance The HazelcastInstance for the Spaces created.
     */
    public SpaceCreatorImpl(final HazelcastInstance hcInstance) {
        Assert.notNull(hcInstance, "hcInstance must not be null.");
        this.hcInstance = hcInstance;

        // There needs to be at least one CancelLeaseProcessor running
        // throughout the cluster that will take care of Lease cancellations.
        // One way to ensure this is to always submit the task everytime a
        // SpaceCreator is created.  Since Spaces should only be created via
        // this class, we can guarantee that there's a processor running that
        // will take care of leases that are created by the Spaces, and then
        // cancelled.  When CancelLeaseProcessor runs, it first checks if
        // there's already a lease processor running on the same instance that
        // it's on.  If there is, it returns right away (see
        // CancelLeaseProcessor.call()) so there's no harm in always submitting
        // this task during the creation of SpaceCreatorImpl.  In other words,
        // it is idempotent.
        //
        // Submitting the task to all members is done because tasks are not
        // automatically distributed.  They only run on the members that they
        // are submitted to.  This ensures that if one member leaves/crashes
        // and takes the CancelleaseProcessor with it, the other processors on
        // other members can step in.  Again if this take is submitted to a
        // member that already has one running, it will just return.
        hcInstance.getExecutorService(SpaceImpl.SPACE_PROCESSORS_EXECUTOR_SERVICE)
                .submitToAllMembers(new CancelLeaseProcessor());
    }

    /**
     * Set the logger.
     *
     * @param log The logger.
     */
    @AutowiredLogger
    public void setLog(final Logger log) {
        Assert.notNull(log, "log must not be null.");
        this.log = log;
    }

    @Override
    public Space createSpace(final String spaceName) {
        Assert.notNull(spaceName, "spaceName must not be null.");
        Assert.hasLength(spaceName, "spaceName must not be empty.");

        Space newSpace = new SpaceImpl(hcInstance, spaceName,
                LoggerFactory.getLogger(SpaceImpl.class));

        log.debug("Created new space with name: {}", spaceName);

        return newSpace;
    }
}
