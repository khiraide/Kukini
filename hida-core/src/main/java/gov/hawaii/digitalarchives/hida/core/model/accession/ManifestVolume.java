package gov.hawaii.digitalarchives.hida.core.model.accession;

import java.io.Serializable;
import java.util.Collections;
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
import javax.persistence.OneToMany;
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represent a "ManifestVolume" or "partition" in a physical drive. This is used
 * to contain instances of {@link ManifestDirectory}.
 * 
 * @author Dongie Agnir
 */
@Entity
@Configurable
@Table(name = "MANIFEST_VOLUME")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class ManifestVolume implements Serializable {
    
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
    private static final long serialVersionUID = 3577451820024143214L;

    /**
     * The primary id associated with this ManifestVolume.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MANIFEST_VOLUME_ID")
    private Long primaryId;

    /**
     * The version associated with this ManifestVolume.
     */
    @Version
    @Column(name = "VERSION")
    private Integer version;

    /**
     * The name of this ManifestVolume.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Many ManifestVolumes objects are associated with a single manifestDrive
     * object. A ManifestDrive object's primary key will be used as a foreign
     * key in this ManifestVolume object/table.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "MANIFEST_DRIVE_ID")
    private ManifestDrive manifestDrive;

    /**
     * A ManifestVolume object has many Directories. A ManifestVolume object's
     * primary key will be used as as a foreign key in the Directory
     * object/table.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "volume")
    private List<ManifestDirectory> directories;
    
    /**
     * AutowiredLogger for logging purposes.
     */
    private transient Logger log = null;

    /**
     * Oracle specifications state that the class must have a public or
     * protected, no-argument constructor. The class may have other
     * constructors.
     * http://docs.oracle.com/javaee/7/tutorial/doc/persistence-intro001.htm
     */
    public ManifestVolume() {

    }

    
    
    /**
     * Creates a ManifestVolume and ensures that its' fields are valid.
     * 
     * @param name           The name of this ManifestVolume.
     * @param directories    The collection of {@link ManifestDirectory}
     *                       instances that are associated with this 
     *                       ManifestVolume.
     * @param manifestDrive  The {@link ManifestDrive} that is associated with
     *                       this ManifestVolume.
     */
    public ManifestVolume(final String name, final List<ManifestDirectory> directories,
            final ManifestDrive manifestDrive) {
        Assert.notNull(name, "name cannot be null");
        Assert.hasLength(name, "name cannot be empty");
        Assert.notNull(directories, "directories cannot be null");
        Assert.notNull(manifestDrive, "manifestDrive cannot be null");
        
        this.name = name;
        this.directories = directories;
        this.manifestDrive = manifestDrive;
    }

    /**
     * @return The name of this ManifestVolume.
     */
    public String getName() {
        //log.debug("Entering getName()");
        //log.debug("Exiting getName(): {}", this.name);
        return this.name;
    }

    /**
     * @return Gets the collection of Directory instances associated with this
     *         ManifestVolume.
     */
    public List<ManifestDirectory> getDirectories() {
        //log.debug("Entering getDirectories()");
        //log.debug("Exiting getDirectories(): {}", Collections.unmodifiableList(directories));
        return Collections.unmodifiableList(directories);
    }

    /**
     * @return The ManifestDrive associated with this ManifestVolume.
     */
    public ManifestDrive getManifestDrive() {
        //log.debug("Entering getManifestDrive()");
        //log.debug("Exiting getManifestDrive(): {}", this.manifestDrive);
        return this.manifestDrive;
    }

    /**
     * @return The primary id associated with this ManifestVolume.
     */
    public Long getPrimaryId() {
        //log.debug("Entering getPrimaryId()");
        //log.debug("Exiting getPrimaryId(): {}", this.primaryId);
        return this.primaryId;
    }

    /**
     * @return The version associated with this ManifestVolume.
     */
    public Integer getVersion() {
        //log.debug("Entering getVersion()");
        //log.debug("Exiting getVersion(): {}", this.version);
        return this.version;
    }

    /**
     * Sets the version of this ManifestVolume.
     * 
     * @param version The version of this ManifestVolume that you want to set.
     */
    public void setVersion(Integer version) {
        //log.debug("Entering setVersion(version = {}", version);
        this.version = version;
        //log.debug("Exiting setVersion()");
    }

    /**
     * Assists in implementing Object.toString() methods using reflection. This
     * class uses reflection to determine the fields to append. Because these
     * fields are usually private, the class changes the visibility of the
     * fields. This will fail under a security manager, unless the appropriate
     * permissions are set up correctly.
     */
    @Override
    public String toString() {
        //log.debug("Entering toString()");
        String toString = ReflectionToStringBuilder
                .toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        //log.debug("Exiting toString(): {}", toString);
        return toString;
    }

    /**
     * An {@link EntityManager} instance is associated with a persistence
     * context. A persistence context is a set of entity instances in which for
     * any persistent entity identity there is a unique entity instance. Within
     * the persistence context,the entity instances and their life cycle are
     * managed. The EntityManager API is used to create and remove persistent
     * entity instances, to find entities by their primary key, and to query
     * over entities. As long as an object is attached to an EntityManager, all
     * changes to the object will be synchronized with the database
     * automatically (Meaning that this object is attached to the 'persistence
     * context').
     */
    @PersistenceContext
    transient EntityManager entityManager;

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
        EntityManager em = new ManifestVolume().entityManager;
        if (em == null) {
            String errorMessage = "Throwing IllegalStateException in "
                    + "entityManager(): 'Entity manager has not been injected "
                + "(is the Spring Aspects JAR configured as an AJC/AJDT aspects library?'";
            
            staticMethodLogger().error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        staticMethodLogger().debug("Exiting entityManager(): {}", em);
        return em;
    }
    
    /**
     * Used to log static methods of this class. Since Autowired logger cannot 
     * be "static", this method allows an Autowired Logger to be used within 
     * static methods.
     * 
     * @return A new logger to do logging operations.
     */
    private static final Logger staticMethodLogger() {
        Logger log = new ManifestVolume().log;
        if (log == null) {
            throw new IllegalStateException("AutowiredLogger has not been"
                    + " injected!");
        }
        return log;
    }

    /**
     * Gets the amount of ManifestVolume objects that are currently in the
     * database.
     * 
     * @return The amount of ManifestVolume objects that are currently in the
     *         database.
     */
    public static long countManifestVolumes() {
        staticMethodLogger().debug("Entering countManifestVolumes()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM ManifestVolume o",
                Long.class).getSingleResult();
        staticMethodLogger().debug("Exiting countManifestVolumes(): {}", count);
        return count;
    }

    /**
     * Gets all of the ManifestVolume objects from the database and puts them 
     * in a collection.
     * 
     * @return The collection of ManifestVolume objects that are in the
     *         database.
     */
    public static List<ManifestVolume> findAllManifestVolumes() {
        staticMethodLogger().debug("Entering findAllManifestVolumes()");
        List<ManifestVolume> manifestVolumes = entityManager()
                .createQuery("SELECT o FROM ManifestVolume o", ManifestVolume.class)
                .getResultList();
        staticMethodLogger().debug("Exiting findAllManifestVolumes(): {}", manifestVolumes);
        return manifestVolumes;
    }

    /**
     * Gets a ManifestVolume from the database according to its primaryId.
     * 
     * @param primaryId The primary key of a ManifestVolume.
     * @return The ManifestVolume associated with the id that was passed in or
     *         null if not found.
     */
    public static ManifestVolume findManifestVolume(Long primaryId) {
        staticMethodLogger().debug("Entering findManifestVolume(primaryId = {})", primaryId);
        if (primaryId == null) {
            staticMethodLogger().debug("Exiting findManifestVolume(): null)");
            return null;
        }
        ManifestVolume manifestVolume = entityManager().find(ManifestVolume.class, primaryId);
        staticMethodLogger().debug("Exiting findManifestVolume(): {})", manifestVolume);
        return manifestVolume;
    }

    /**
     * Gets a certain subset of ManifestVolumes based on the range desired.
     * 
     * @param firstResult The beginning of the range of values.
     * @param maxResults The last element of the range of values.
     * @return The collection of ManifestVolumes based on the range desired.
     */
    public static List<ManifestVolume> findManifestVolumeEntries(
            int firstResult, int maxResults) {
        staticMethodLogger().debug("Entering findManifestVolumeEntries(firstResult = {}, " +
                "maxResults = {})", firstResult, maxResults);
        
        List<ManifestVolume> manifestVolumes = entityManager()
                .createQuery("SELECT o FROM ManifestVolume o", ManifestVolume.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        staticMethodLogger().debug("Exiting findManifestVolumeEntries(): {}", manifestVolumes);
        return manifestVolumes;
    }

    /**
     * Persists this object to the persistence context.
     */
    @Transactional
    public void persist() {
        //log.debug("Entering persist()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        this.entityManager.persist(this);
        //log.debug("Exiting persist()");
    }

    /**
     * Removes this object from the persistence context.
     */
    @Transactional
    public void remove() {
        //log.debug("Entering remove()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            ManifestVolume attached = ManifestVolume.findManifestVolume(this.primaryId);
            this.entityManager.remove(attached);
        }
        //log.debug("Exiting remove()");
    }

    /**
     * Synchronize the persistence context to the underlying database.
     */
    @Transactional
    public void flush() {
        //log.debug("Entering flush()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        this.entityManager.flush();
        //log.debug("Exiting flush()");
    }

    /**
     * If you call clear, all currently managed objects of the EntityManager
     * will be detached and the status is not synchronized with the database. 
     * As long as the objects are not explicitly attached again, they are 
     * standard Java objects, whose change does not have any effect on the 
     * database.
     */
    @Transactional
    public void clear() {
        //log.debug("Entering clear()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        this.entityManager.clear();
        //log.debug("Exiting clear()");
    }

    /**
     * If this object is detached, the merge() method will merge them back into
     * the EntityManager to become managed again. Note that changes to detached
     * entity objects are not stored in the database.
     * 
     * @return The merged ManifestVolume.
     */
    @Transactional
    public ManifestVolume merge() {
        //log.debug("Entering merge()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        ManifestVolume merged = this.entityManager.merge(this);
        this.entityManager.flush();
        //log.debug("Exiting merge(): {}", merged);
        return merged;   
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