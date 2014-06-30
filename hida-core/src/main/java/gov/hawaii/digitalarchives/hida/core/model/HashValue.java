package gov.hawaii.digitalarchives.hida.core.model;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An implementation of the {@link HashValue} class.  Guarantees
 * the immutability of the HashValue.
 *
 * @author Dongie Agnir
 * @author Keone Hiraide
 */
@Configurable
@Entity
@Table(name = "HASH_VALUE")
public class HashValue {
	
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
	 * The name of the algorithm used to compute the hash.
	 */
	@Column(name="HASH_ALGORITHM")
	private String hashAlgorithm;
	
	/**
	 * The bytes that comprise the hash.  This copy is not backed by
	 * {@code HashValue}'s internal copy.
	 */
	@Column(name="HASH_BYTES")
	private byte[] hashBytes;
	

	/**
	 * The primary id associated with this HashValue as well as its subclasses.
	 */
	@Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "HASH_VALUE_ID")
    private Long id;
	
	
	
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
	public HashValue() {
		
	}
	
	
	
	/**
	 * Creates a HashValue instance, insuring that its values are not 
	 * null.
	 * 
	 * @param algorithm The algorithm for this HashValue.
	 * @param hashBytes The hash bytes for this HashValue.
	 */
	public HashValue(final String algorithm, final byte[] hashBytes) {
		if (algorithm == null) {
			throw new IllegalArgumentException("algorithm cannot be null.");
		}
		
		if (hashBytes == null) {
			throw new IllegalArgumentException("hashBytes cannot be null.");
		}
		
		this.hashAlgorithm = algorithm;
		this.hashBytes = Arrays.copyOf(hashBytes, hashBytes.length);
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
        EntityManager em = new HashValue().entityManager;
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
        Logger log = new HashValue().log;
        if (log == null) {
            throw new IllegalStateException("AutowiredLogger has not been"
                    + " injected!");
        }
        return log;
    }
    
    
    
	/**
	 * Constructs a HashValue with the associated HashValue passed in.
	 * 
	 * @param rhs  Setting the fields of this HashValue 
	 */
	public HashValue(final HashValue rhs) {
	    log.debug("Entering HashValue(rhs = {}", rhs);
		this.hashAlgorithm = rhs.getHashAlgorithm();
		this.hashBytes = rhs.getHashBytes();
	}

	
	
	/**
	 * Gets the amount of HashValue objects that are currently 
	 * in the database.
	 * 
	 * @return The amount of HashValue objects that are 
	 *         currently in the database.
	 */
	public static long countHashValues() {
	    staticMethodLogger().debug("Entering countHashValues()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM HashValue o",
                Long.class).getSingleResult();
        staticMethodLogger().debug("Exiting countHashValues(): {}", count);
        return count;
    }
	
	
	
	/**
	 * Gets all the HashValue objects from the database 
	 * and puts them in a collection. 
	 * 
	 * @return The collection of HashValue that are 
	 *         in the database.
	 */
	public static List<HashValue> findAllHashValues() {
	    staticMethodLogger().debug("Entering findAllHashValues()");
        List<HashValue> hashValue = entityManager()
                .createQuery("SELECT o FROM HashValue o", HashValue.class)
                .getResultList();
        staticMethodLogger().debug("Exiting findAllHashValues(): {}", hashValue);
        return hashValue;
    }
	
	/**
     * Queries for an HashValue from the database based on its primaryId.
     * 
     * @param primaryId  The long primaryId associated with the 
     *                   HashValue that you want to search the database for.
     * @return           The associated HashValue matching the primaryId you 
     *                   were searching for, or null if not found.
     */
	public static HashValue findHashValue(Long primaryId) {
	    staticMethodLogger().debug("Entering findHashValue(primaryId = {})", primaryId);
        if (primaryId == null) {
            staticMethodLogger().debug("Exiting findHashValue(): null)");
            return null;
        }
        HashValue hashValue = entityManager()
                .find(HashValue.class, primaryId);
        staticMethodLogger().debug("Exiting findHashValue(): {})", hashValue);
        return hashValue;
    }
	
	
	
	/**
	 * Gets a certain subset of HashValue based 
	 * on the range desired.
	 * 
	 * @param firstResult  The beginning of the range of values.
	 * @param maxResults   The last element of the range of values.
	 * @return             The collection of HashValue based 
	 *                     on the range desired. 
	 */
	public static List<HashValue> 
	findHashValueEntries(int firstResult, int maxResults) {
	    staticMethodLogger().debug("Entering findHashValueEntries(firstResult = {}, " +
                "maxResults = {})", firstResult, maxResults);
        
        List<HashValue> entries = entityManager()
                .createQuery("SELECT o FROM HashValue o", HashValue.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        staticMethodLogger().debug("Exiting findHashValueEntries(): {}", entries);
        return entries;
    }
	
	
	
	/**
	 * @{inheritDoc}
	 */
	public String getHashAlgorithm() {
	    log.debug("Exiting getHashAlgorithm(): {}", this.hashAlgorithm);
        return this.hashAlgorithm;
    }

	
	
	/**
	 * @{inheritDoc}
	 */
	public byte[] getHashBytes() {
	    log.debug("Exiting getHashBytes(): {}", this.hashBytes);
        return this.hashBytes;
    }

	
	/**
	 * Returns the primaryId associated with this HashValue.
	 * 
	 * @return The primaryId associated with this HashValue.
	 */
	public Long getId() {
	    log.debug("Exiting getId(): {}", this.id);
        return this.id;
    }

	
	
	/**
	 * Gets the version of this HashValue.
	 * 
	 * @return The version of this HashValue.
	 */
	public Integer getVersion() {
	    log.debug("Exiting getVersion(): {}", this.version);
        return this.version;
    }

	
	
	/**
	 * Sets the version of this HashValue.
	 * 
	 * @param version The version of this HashValue.
	 */
	public void setVersion(Integer version) {
	    log.debug("Entering setVersion(version = {})", version);
        this.version = version;
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
