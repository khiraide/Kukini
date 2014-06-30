package gov.hawaii.digitalarchives.hida.persistentid;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/** Default implementation of the {@link PersistentIdFactory}. Mints new PIDs
 * using a scheme injected during configuration. Can also construct a new PID
 * object using an existing PID, passed in as a URI string. This is done using a
 * factory specific to the scheme being used, if one exists. If one is not
 * found, a {@link HidaException} is thrown.
 * 
 * @author Micah Takabayashi */
public class DefaultPersistentIdFactory implements PersistentIdFactory
{
    @Autowired
    private PersistentIdBuilderLocator locator;
    
    @AutowiredLogger
    private Logger logger;
    
    /** The scheme to use for generated PIDs.*/
    private String scheme;
    
    /** Injection setter for the default scheme for new PIDs.
     * 
     * @param scheme The scheme to use for minted PIDs. */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    @Override
    public PersistentId createPersistentId ()
    {
        this.logger.debug("Entering createPersistentId()");
        
        // Look up the factory for the default scheme and mint a new ID.
        PersistentIdFactory factory = null;
        try {
            factory = this.locator.createPersistentIdBuilder(this.scheme);
        } catch (Exception e) {
            String errMsg = "No PID factory found for the scheme specified.";
            this.logger.error(errMsg, e);
            throw new HidaException(errMsg, e);
        }
        PersistentId id = factory.createPersistentId();
        
        this.logger.debug("Exiting createPersistentId : {}", id);
        return id;
    }

    @Override
    public PersistentId createPersistentId (String existingId)
    {
        this.logger.debug("Entering createPersistentId(existingID = {})", existingId);
        
        // Parse out the scheme of the input ID.
        Assert.isTrue(existingId.contains(":"), 
                "existingId does not appear to be a well-formed URI (" + existingId + ").");
        String scheme = existingId.substring(0, existingId.indexOf(':'));
        // Use scheme to find the appropriate builder class.
        PersistentIdFactory factory = null;
        try {
            factory = this.locator.createPersistentIdBuilder(scheme);
        } catch (Exception e) {
            String errMsg = "No PID factory found for the scheme specified.";
            this.logger.error(errMsg, e);
            throw new HidaException(errMsg, e);
        }
        PersistentId id = factory.createPersistentId(existingId);
        
        this.logger.debug("Exiting createPersistentId : {}", id);
        return id;
    }
}
