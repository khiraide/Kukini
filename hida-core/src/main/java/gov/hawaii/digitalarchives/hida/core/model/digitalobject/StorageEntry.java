package gov.hawaii.digitalarchives.hida.core.model.digitalobject;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIdSyntaxException;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Model class for encapsulating information about where a digital
 * object is stored.
 * <p>
 * @author Dongie Agnir
 * @author Keone Hiraide
 */
@Configurable
@Entity
@Table(name = "STORAGE_ENTRY")
public class StorageEntry implements Serializable {
	
	/**
	 * Autogenerated for this entity to be serializable. Implementing
	 * Serializable isn't strictly necessary as far as the JPA specification
	 * is concerned. However, it is needed if you're going to use caching or
	 * EJB remoting, both of which require objects to be Serializable.
	 * Caching is a key component in achieving optimal performance in any
	 * JPA application, so implementing the Serializable interface is a good
	 * habit to adopt. Note: This was taken from the book titled, 
	 * "Spring Persistence with Hibernate", 2010, Fisher and Murphy. 
	 */
	private static final long serialVersionUID = -1152654836284318781L;



	/**
     * Many StorageEntry objects are associated with a single DigitalObject object.
     * A DigitalObject object's primary key will be used as as a foreign key
     * in this StorageEntry object/table.
     */
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "DIGITAL_OBJECT_ID")
	private DigitalObject digitalObject;
	
	
	
	/**
	 * An {@link EntityManager} instance is associated with a persistence 
	 * context. A persistence context is a set of entity instances in which 
	 * for any persistent entity identity there is a unique entity instance. 
	 * Within the persistence context, the entity instances and their life cycle 
	 * are  managed. The EntityManager API is used to create and remove 
	 * persistent entity instances, to find  entities by their primary key, 
	 * and to query over entities. As long as an object is attached to an 
	 * EntityManager, all changes to the object will be synchronized with 
	 * the database automatically (Meaning that this object is attached to the
	 * 'persistence context'). 
	 */
	@PersistenceContext
    transient EntityManager entityManager;
	
	
	
	/**
	 * The primary id associated with this StorageEntry as well as its
	 * subclasses.
	 */
	@Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "STORAGE_ENTRY_ID")
    private Long primaryId;
	
	
	/**
	 * The type of the location where the file is stored. Values
	 * should be taken from a controlled vocabulary.
	 */
	@Column(name="LOCATION_TYPE")
	private String locationType;

	
	/**
	 * The absolute path of the digital object, where it is stored.
	 */
	@Column(name="LOCATION_VALUE")
	private String locationValue;
	
	
	/**
	 * The type of the medium being used to store the digital object.
	 * Values should be taken from a controlled vocabulary.
	 */
	@Column(name="MEDIUM_TYPE")
	private String mediumType;

	
	
	/**
	 * Specifies the version field or property of an entity class that serves 
	 * as its optimistic lock value. The version is used to ensure 
	 * integrity when performing the merge operation and for optimistic 
	 * concurrency control. 
	 */
	@Version
    @Column(name = "VERSION")
    private Integer version;
	
	/**
     * AutowiredLogger for logging purposes.
     */
    private transient Logger log = null;
	
	
    
	/**
     * Oracle specifications state that the class must have a public 
     * or protected, no-argument constructor. The class may have other 
     * constructors. 
     * http://docs.oracle.com/javaee/7/tutorial/doc/persistence-intro001.htm
     */
	public StorageEntry() {
		
	}

	
	
	/**
	 * Constructor that will form a StorageEntry, ensuring that its
	 * values are not null.
	 * 
	 * @param locationType   The type of the location where the file is stored.
	 *                       Values should be taken from a 
	 *                       controlled vocabulary.
	 * @param locationValue  The absolute path of the digital object, 
	 *                       where it is stored.
	 * @param mediumType     The type of the medium being used to store 
	 *                       the digital object.
	 * @param digitalObject  The {@link DigitalObject} instance that this
	 *                       StorageEntry is associated with.
	 */
	public StorageEntry(final String locationType, final URI locationValue,
			final String mediumType, final DigitalObject digitalObject) {
	    Assert.notNull(locationType, "locationType cannot be null");
        Assert.hasLength(locationType, "locationType cannot be empty");	
		
        Assert.notNull(locationValue, "locationValue cannot be null");
		
        Assert.notNull(mediumType, "mediumType cannot be null");
        Assert.hasLength(mediumType, "mediumType cannot be empty");
		
        Assert.notNull(digitalObject, "digitalObject cannot be null");
		
		this.locationType = locationType;
		this.locationValue = locationValue.toString();
		this.mediumType = mediumType;
		this.digitalObject = digitalObject;
	}

	
	
    /**
     * A new entity manager is created for every persistence related function. 
     * This pattern is called an “entitymanger-per-request” pattern. 
     * In this model, a request from the client is sent to the server 
     * (where the JPA persistence layer runs), a new EntityManager is opened, 
     * and all database operations are executed in this unit of work. Once the 
     * work has been completed (and the response for the client has been
     * prepared), the persistence context is flushed and closed, as well 
     * as the entity manager object. You would also use a single database 
     * transaction to serve the clients request. The relationship between 
     * the two is one-to-one and this model is a perfect fit for many 
     * applications. 
     * 
     * http://docs.jboss.org/hibernate/entitymanager/
     *  
     * @return A new EntityManager to do database operations.
     */
    public static final EntityManager entityManager() {
        staticMethodLogger().debug("Entering entityManager()");
        EntityManager em = new StorageEntry().entityManager;
        if (em == null) {
            staticMethodLogger().error("Throwing IllegalStateException in "
                    + "entityManager(): 'Entity manager has not been injected "
                + "(is the Spring Aspects JAR configured as an AJC/AJDT aspects library?'");
            
            throw new IllegalStateException(
                    "Entity manager has not been injected"
                            + " (is the Spring Aspects JAR configured as an " +
                            "AJC/AJDT aspects library?)");
        }
        staticMethodLogger().debug("Exiting entityManager(): {}", em);
        return em;
    }

    
    
    /**
     * Used to log static methods of this class. Since
     * Autowired logger cannot be "static", this method allows an 
     * Autowired Logger to be used within static methods.
     * 
     * @return A new logger to do logging operations.
     */
    private static final Logger staticMethodLogger() {
        Logger log = new StorageEntry().log;
        if (log == null) {
            throw new IllegalStateException("AutowiredLogger has not been"
                    + " injected!");
        }
        return log;
    }
    
    
    
	/**
	 * Gets the amount of StorageEntry objects that are currently 
	 * in the database.
	 * 
	 * @return The amount of StorageEntry objects that are 
	 *         currently in the database.
	 */
	public static long countStorageEntries() {
	    staticMethodLogger().debug("Entering countStorageEntries()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM StorageEntry o",
                Long.class).getSingleResult();
        staticMethodLogger().debug("Exiting countStorageEntries(): {}", count);
        return count;
    }

	
	
	/**
	 * Gets all the StorageEntry objects from the database 
	 * and puts them in a collection. 
	 * 
	 * @return The collection of StorageEntries that are 
	 *         in the database.
	 */
	public static List<StorageEntry> findAllStorageEntries() {
	    staticMethodLogger().debug("Entering findAllStorageEntries()");
        List<StorageEntry> storageEntries = entityManager()
                .createQuery("SELECT o FROM StorageEntry o", StorageEntry.class)
                .getResultList();
        staticMethodLogger().debug("Exiting findAllStorageEntries(): {}", storageEntries);
        return storageEntries;
    }
	
	
	
	/**
     * Queries for a StorageEntry based on its primaryId.
     * 
     * @param primaryId  The long primaryId associated with this StorageEntry.
     * @return    The StorageEntry that is associated with the primaryId being 
     *            searched for.
     */
	public static StorageEntry findStorageEntry(Long primaryId) {
	    staticMethodLogger().debug("Entering findStorageEntry(primaryId = {})", primaryId);
        if (primaryId == null) {
            staticMethodLogger().debug("Exiting findStorageEntry(): null)");
            return null;
        }
        StorageEntry storageEntry = entityManager().find(StorageEntry.class, primaryId);
        staticMethodLogger().debug("Exiting findStorageEntry(): {})", storageEntry);
        return storageEntry;
    }
	
	
	
	/**
	 * Gets a certain subset of StorageEntries based 
	 * on the range desired.
	 * 
	 * @param firstResult  The beginning of the range of values.
	 * @param maxResults   The last element of the range of values.
	 * @return             The collection of StorageEntries based 
	 *                     on the range desired. 
	 */
	public static List<StorageEntry> 
	findStorageEntryEntries(int firstResult, int maxResults) {
	    staticMethodLogger().debug("Entering findStorageEntryEntries(firstResult = {}, " +
                "maxResults = {})", firstResult, maxResults);
        
        List<StorageEntry> storageEntries = entityManager()
                .createQuery("SELECT o FROM StorageEntry o", StorageEntry.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        staticMethodLogger().debug("Exiting findStorageEntryEntries(): {}", storageEntries);
        return storageEntries;
    }

	
	
	/**
	 * If you call clear, all currently managed objects of the EntityManager 
	 * will be detached and the status is not synchronized with the database. 
	 * As long as the objects are not explicitly attached again, they are 
	 * standard Java objects, whose change does not have any effect on 
	 * the database.
	 */
	@Transactional
    public void clear() {
	    log.debug("Entering clear()");
        if (this.entityManager == null) 
            this.entityManager = entityManager();
        this.entityManager.clear();
        log.debug("Exiting clear()");
    }

	
	
	/**
	 * Synchronize the persistence context to the underlying database. 
	 */
	@Transactional
    public void flush() {
	    log.debug("Entering flush()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        this.entityManager.flush();
        log.debug("Exiting flush()");
    }
	
	
	
	/**
	 * Gets the DigitalObject associated with this class. 
	 * 
	 * @return The DigitalObject associated with this class.
	 */
	public DigitalObject getDigitalObject() {
	    log.debug("Entering getDigitalObject()");
	    log.debug("Exiting getDigitalObject(): {}", this.digitalObject);
        return this.digitalObject;
    }

	
	
	/**
	 * Gets the primary id associated with this class.
	 * 
	 * @return the primary id associated with this class.
	 */
	public Long getPrimaryId() {
	    log.debug("Entering getPrimaryId()");
	    log.debug("Exiting getPrimaryId(): {}", this.primaryId);
        return this.primaryId;
    }

	
	
	/**
	 * @return The type of the location where the file is stored.  Values
	 * should be taken from a controlled vocabulary.
	 */
	public String getLocationType() {
	    log.debug("Entering getLocationType()");
	    log.debug("Exiting getLocationType(): {}", this.locationType);
        return this.locationType;
    }

	
	
	/**
	 * @return The absolute path of the digital object, where it is stored.
	 */
	public URI getLocationValue() {
	    log.debug("Entering getLocationValue()");
		URI locationValueURI = null;
		try {
			locationValueURI = new URI(this.locationValue);
		} catch (URISyntaxException e) {
		    String errorMessage = "Throwing HidaIdSyntaxException in getLocationValue(): Badly " +
                    "formatted URI when locationValueURI was constructed for the " +
                    "getLocationValue() method in the StorageEntry class.";
		    log.error(errorMessage, e);
			throw new HidaIdSyntaxException(errorMessage, e);
		}
		log.debug("Exiting getLocationValue(): {}", locationValueURI);
        return locationValueURI;
    }
	
	
	
	/**
	 * @return The type of the medium being used to store the digital object.
	 * Values should be taken from a controlled vocabulary.
	 */
	public String getMediumType() {
	    log.debug("Entering getMediumType()");
	    log.debug("Exiting getMediumType(): {}", this.mediumType);
        return this.mediumType;
    }
	
	
	/**
	 * Gets the version of this StorageEntry.
	 * 
	 * @return The version of this StorageEntry.
	 */
	public Integer getVersion() {
	    log.debug("Entering getVersion()");
	    log.debug("Exiting getVersion(): {}", this.version);
        return this.version;
    }

	
	
	/**
	 * If this object is detached, the merge() method will merge them back into
	 * the EntityManager to become managed again. Note that changes to detached
	 * entity objects are not stored in the database.
	 * 
	 * @return The merged StorageEntry.
	 */
	@Transactional
    public StorageEntry merge() {
	    log.debug("Entering merge()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        StorageEntry merged = this.entityManager.merge(this);
        this.entityManager.flush();
        log.debug("Exiting merge(): {}", merged);
        return merged;
    }

	
	
	/**
	 * Persists this object to the persistence context. 
	 */
	@Transactional
    public void persist() {
	    log.debug("Entering persist()");
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.persist(this);
        log.debug("Exiting persist()");
    }

	
	
	/**
	 * Removes this object from the persistence context. 
	 */
	@Transactional
    public void remove() {
	    log.debug("Entering remove()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            StorageEntry attached = StorageEntry.findStorageEntry(this.primaryId);
            this.entityManager.remove(attached);
        }
        log.debug("Exiting remove()");
    }

	
	
	/**
	 * Sets the version of this StorageEntry.
	 * 
	 * @param version The version of this StorageEntry.
	 */
	public void setVersion(Integer version) {
	    log.debug("Entering setversion(version = {})", version);
        this.version = version;
        log.debug("Exiting setVersion()");
    }

	
	
	/**
	 *  Assists in implementing Object.toString() methods using reflection.
	 *  This class uses reflection to determine the fields to append. Because 
	 *  these fields are usually private, the class changes the visibility of 
	 *  the fields. This will fail under a security manager,
	 *  unless the appropriate permissions are set up correctly. 
	 */
	@Override
	public String toString() {
	    log.debug("Entering toString()");
        String toString = ReflectionToStringBuilder.toString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
        log.debug("Exiting toString(): {}", toString);
        return toString;
    }
	
	/**
     * Sets the logger for this class.
     * 
     * @param log The logger for this class.
     */
    @AutowiredLogger
    public void setLog(Logger log) {
        this.log = log;
    }
}
