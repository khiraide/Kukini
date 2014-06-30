package gov.hawaii.digitalarchives.hida.core.model.preservationplan;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The model class for a preservation plan.
 * <p>
 * The preservation plan is the controlling configuration file that
 * drives the creation of derivatives for preservation and/or access.
 * <p>
 * The XML is designed based on PRONOM format IDs and lists the native file
 * format and which file format(s) should be created for preservation purposes
 * and access purposes.
 * 
 * @author Keone Hiraide
 */
@Entity
@Configurable
public class PreservationPlan implements Serializable {
    
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
    private static final long serialVersionUID = -5966043320899527380L;

    /**
     * The primary identifier for a preservation plan.
     */
    @Id
    @NotEmpty
    @Column(name = "PRESERVATION_PLAN_ID", unique = true, nullable = false)
    private String primaryId;
    
    /**
     * Specifies the version field or property of an entity class that serves 
     * as its optimistic lock value. The version is used to ensure 
     * integrity when performing the merge operation and for optimistic 
     * concurrency control. 
     */
    @NotNull
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;
    
    /**
     * The date when this preservation plan was created.
     */
    @NotNull
    @Column(name = "CREATED_DATE", nullable = false)
    private Date createdDate;
    
    /**
     * The date when this preservation plan was last modified.
     */
    @NotNull
    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private Date lastModifiedDate;
    
    /**
     * The label associated with this preservation plan.
     */
    @NotNull
    @NotEmpty
    @Column(name = "LABEL", nullable = false)
    private String label;
    
    /**
     * A join table will be created, holding a foreign key representing a
     * PreservationPlan and a foreign key representing a {@link FormatPlan}. 
     * The FormatPlan value will have its own column in this join table.
     */
    @NotNull
    @OneToMany(cascade = CascadeType.ALL)
    private Map<String, FormatPlan> formatPlans = new HashMap<String, FormatPlan>();

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
     * AutowiredLogger for logging purposes.
     */
    private transient Logger log = null;
    
    /**
     * Used to log static methods of this class. Since
     * Autowired logger cannot be "static", this method allows an 
     * Autowired Logger to be used within static methods.
     * 
     * @return A new logger to do logging operations.
     */
    private static final Logger staticMethodLogger() {
        Logger log = new PreservationPlan().log;
        if (log == null) {
            throw new IllegalStateException("Logger has not been"
                    + " injected!");
        }
        return log;
    }
    
    
    
    /**
     * @return The Primary ID of this {@code PreservationPlan}.
     */
    public String getPrimaryId() {
        log.debug("Entering getPrimaryId()");
        log.debug("Exiting getPrimaryId(): {}", primaryId);
        return primaryId;
    }

    
    
    /**
     * Set the Primary ID of this {@code PreservationPlan}.
     * 
     * @param primaryId The primary ID of this {@code PreservationPlan}.
     */
    public void setPrimaryId(final String primaryId) {
        log.debug("Entering setPrimaryId(primaryId = {})", primaryId);
        this.primaryId = primaryId;
        log.debug("Exiting setPrimaryId()");
    }

    
    
    /**
     * @return The date that this preservation plan was created.
     */
    public Date getCreatedDate() {
        log.debug("Entering getCreatedDate()");
        if (createdDate != null) {
            Date createdDate = new Date(this.createdDate.getTime());
            log.debug("Exiting getCreatedDate(): {}", createdDate);
            return createdDate;
        } else {
            log.debug("Exiting getCreatedDate(): null");
            return null;
        }
    }

    
    
    /**
     * Sets the date that this preservation plan was created.
     * 
     * @param createdDate The date that this preservation plan was created.
     */
    public void setCreatedDate(Date createdDate) {
        log.debug("Entering setCreatedDate(createdDate = {})", createdDate);
        Assert.notNull(createdDate, "createdDate cannot be null");
        this.createdDate = new Date(createdDate.getTime());
        log.debug("Exiting setCreatedDate()");
    }

    
    
    /**
     * @return The date when this preservation plan was last modified.
     */
    public Date getLastModifiedDate() {
        log.debug("Entering getLastModifiedDate()");
        if (this.lastModifiedDate != null) {
            Date lastModifiedDate = new Date(this.lastModifiedDate.getTime());
            log.debug("Exiting getLastModifiedDate(): {}", lastModifiedDate);
            return lastModifiedDate;
        } else {
            log.debug("Exiting getLastModifiedDate(): null");
            return null;
        }
    }

    
    
    /**
     * Sets the date of when this preservation plan was last modified.
     * 
     * @param lastModifiedDate The date of when this preservation plan 
     *                         was last modified.
     */
    public void setLastModifiedDate(Date lastModifiedDate) {
        log.debug("Entering setLastModifiedDate(lastModifiedDate = {})", lastModifiedDate);
        if (lastModifiedDate != null) {
            this.lastModifiedDate = new Date(lastModifiedDate.getTime());
        } else if (this.createdDate != null) {
            // Setting the default value for lastModifiedDate to the createdDate.
            this.lastModifiedDate = this.createdDate;
        }
        else {
            Assert.notNull(lastModifiedDate, "lastModifiedDate cannot be null");
        }
        log.debug("Exiting setLastModifiedDate()");
    }

    
    
    /**
     * @return The label of this preservation plan.
     */
    public String getLabel() {
        log.debug("Entering getLabel()");
        log.debug("Exiting getLabel(): {}", this.label);
        return this.label;
    }

    
    
    /**
     * Sets the label associated with this preservation plan.
     * 
     * @param label The label associated with this preservation plan.
     */
    public void setLabel(String label) {
        log.debug("Entering setLabel(label = {})", label);
        Assert.notNull(label, "label cannot be null");
        Assert.hasLength(label, "label cannot be empty");
        this.label = label;
        log.debug("Exiting setLabel()");
    }

    
    
    /**
     * @return The collection of {@link FormatPlan} instances associated
     *         with this preservation plan.
     */
    public Map<String, FormatPlan> getFormatPlans() {
        log.debug("Entering getFormatPlans()");
        log.debug("Exiting getFormatPlans(): {}", this.formatPlans);
        return new HashMap<String, FormatPlan>(this.formatPlans);
    }

    
    
    /**
     * Sets the collection of {@link FormatPlan} instances associated
     * with this preservation plan.
     * 
     * @param formatPlans The collection of {@link FormatPlan} instances 
     *                    associated with this preservation plan.
     */
    public void setFormatPlans(Map<String, FormatPlan> formatPlans) {
        log.debug("Entering setFormatPlans(formatPlans = {})", formatPlans);
        Assert.notNull(formatPlans, "formatPlans cannot be null");
        this.formatPlans = new HashMap<String, FormatPlan>(formatPlans);
        log.debug("Exiting setFormatPlans()");
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
        EntityManager em = new PreservationPlan().entityManager;
        if (em == null) {
            String errorMessage = "Entity manager has not been injected "
                + "(is the Spring Aspects JAR configured as an AJC/AJDT aspects library?'";
            
            staticMethodLogger().error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        staticMethodLogger().debug("Exiting entityManager(): {}", em);
        return em;
    }

    
    
    /**
     * Gets the amount of PreservationPlan objects that are currently in the 
     * database.
     * 
     * @return The amount of PreservationPlan objects that are currently in the
     *         database.
     */
    public static long countPreservationPlans() {
        staticMethodLogger().debug("Entering countPreservationPlans()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM PreservationPlan o",
                Long.class).getSingleResult();
        staticMethodLogger().debug("Exiting countPreservationPlans(): {}", count);
        return count;
    }

    
    
    /**
     * Gets all of the PreservationPlan objects from the database and puts them 
     * in a collection.
     * 
     * @return The collection of PreservationPlans that are in the database.
     */
    public static List<PreservationPlan> findAllPreservationPlans() {
        staticMethodLogger().debug("Entering findAllPreservationPlans()");
        List<PreservationPlan> preservationPlans = entityManager()
                .createQuery("SELECT o FROM PreservationPlan o", PreservationPlan.class)
                .getResultList();
        staticMethodLogger().debug("Exiting findAllPreservationPlans(): {}", preservationPlans);
        return preservationPlans;
    }

    
    
    /**
     * Gets a PreservationPlan from the database according to its primaryId.
     * 
     * @param primaryId The primary key of a PreservationPlan.
     * @return The PreservationPlan associated with the primaryId that was 
     *         passed in or null if not found.
     */
    public static PreservationPlan findPreservationPlan(String primaryId) {
        staticMethodLogger().debug("Entering findPreservationPlan(primaryId = {})", primaryId);
        if (primaryId == null || primaryId.length() <= 0) {
            staticMethodLogger().debug("Exiting findPreservationPlan(): null)");
            return null;
        }
        PreservationPlan preservationPlan = entityManager()
                .find(PreservationPlan.class, primaryId);
        staticMethodLogger().debug("Exiting findPreservationPlan(): {})", preservationPlan);
        return preservationPlan;
    }

    
    
    /**
     * Gets a certain subset of PreservationPlans based on the range desired.
     * 
     * @param firstResult The beginning of the range of values.
     * @param maxResults The last element of the range of values.
     * @return The collection of PreservationPlans based on the range desired.
     */
    public static List<PreservationPlan> findPreservationPlanEntries(int firstResult,
            int maxResults) {
        staticMethodLogger().debug("Entering findPreservationPlanEntries(firstResult = {}, " +
                "maxResults = {})", firstResult, maxResults);
        
        List<PreservationPlan> preservationPlans = entityManager()
                .createQuery("SELECT o FROM PreservationPlan o", PreservationPlan.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        staticMethodLogger().debug("Exiting findPreservationPlanEntries(): {}", 
                preservationPlans);
        
        return preservationPlans;
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
            PreservationPlan attached = PreservationPlan.findPreservationPlan(this.primaryId);
            this.entityManager.remove(attached);
        }
        log.debug("Exiting remove()");
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
     * If you call clear, all currently managed objects of the EntityManager
     * will be detached and the status is not synchronized with the database. As
     * long as the objects are not explicitly attached again, they are standard
     * Java objects, whose change does not have any effect on the database.
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
     * If this object is detached, the merge() method will merge them back into
     * the EntityManager to become managed again. Note that changes to detached
     * entity objects are not stored in the database.
     * 
     * @return The merged PreservationPlan.
     */
    @Transactional
    public PreservationPlan merge() {
        log.debug("Entering merge()");
        if (this.entityManager == null)
            this.entityManager = entityManager();
        PreservationPlan merged = this.entityManager.merge(this);
        this.entityManager.flush();
        log.debug("Exiting merge(): {}", merged);
        return merged;
    }

    
    
    /**
     * Gets the version of this PreservationPlan.
     * 
     * @return The version of this PreservationPlan.
     */
    public Integer getVersion() {
        log.debug("Entering getVersion()");
        log.debug("Exiting getVersion(): {}", this.version);
        return this.version;
    }

    
    
    /**
     * Sets the version of this PreservationPlan.
     * 
     * @param version The version of this PreservationPlan.
     */
    public void setVersion(Integer version) {
        log.debug("Entering setVersion(version = {})", version);
        this.version = version;
        log.debug("Exiting setVersion()");
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
