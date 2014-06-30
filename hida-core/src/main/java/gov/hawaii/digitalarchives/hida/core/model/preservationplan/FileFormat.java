package gov.hawaii.digitalarchives.hida.core.model.preservationplan;
import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.Assert;

/**
 * Model class representing the format names associated within a 
 * {@link FormatPlan}. For example: nativeFormat, preservationFormat, 
 * presentationFormat, and thumbnailFormat.
 * 
 * @author Keone Hiraide
 */
@Configurable
@Entity
public class FileFormat implements Serializable {
    
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
    private static final long serialVersionUID = 6607470780763466847L;
    
    /**
     * The primary key of a Format.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "FORMAT_ID")
    private Long primaryId;
    
    /**
     * The formatName associated with this format.
     */
    @Column(name = "FORMATNAME")
    private String formatName;
    
    /**
     * The pronom format associated with this format.
     */
    @Column(name = "PRONOM_FORMAT")
    private String pronomFormat;
    
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
     * An {@link EntityManager} instance is associated with a persistence 
     * context. A persistence context is a set of entity instances in which 
     * for any persistent entity identity there is a unique entity instance. 
     * Within the persistence context, the entity instances and their life 
     * cycle are  managed. The EntityManager API is used to create and remove 
     * persistent entity instances, to find  entities by their primary key, 
     * and to query over entities. As long as an object is attached to an 
     * EntityManager, all changes to the object will be synchronized with 
     * the database automatically (Meaning that this object is attached to the
     * 'persistence context'). 
     */
    @PersistenceContext
    transient EntityManager entityManager;
    
    /**
     * AutowiredLogger for logging purposes.
     */
    private static transient Logger log = null;
    
    /**
     * Used to log static methods of this class. Since
     * Autowired logger cannot be "static", this method allows an 
     * Autowired Logger to be used within static methods.
     * 
     * @param A logger to do logging operations.
     */
    @AutowiredLogger
    public void setLog(Logger log) {
        FileFormat.log = log;
    }

    /**
     * @return The primary id associated with this format.
     */
    public Long getPrimaryId() {
        log.debug("Entering getPrimaryId()");
        log.debug("Exiting getPrimaryId(): {}", this.primaryId);
        return this.primaryId;
    }

    /**
     * @return The format associated with this FormatName object.
     */
    public String getFormatName() {
        log.debug("Entering getFormatName()");
        log.debug("Exiting getFormatName(): {}", this.formatName);
        return this.formatName;
    }
    
    /**
     * Sets the formatName associated with this FormatName object.
     * @param formatName
     */
    public void setFormatName(String formatName) {
        log.debug("Entering setFormatName(formatName = {})", formatName);
        this.formatName = formatName;
        log.debug("Exiting setFormatName()", formatName);
    }
    
    
    /**
     * @return The pronom format associated with this format.
     */
    public String getPronomFormat() {
        log.debug("Entering getPronomFormat()");
        log.debug("Exiting getPronomFormat(): {}", this.pronomFormat);
        return this.pronomFormat;
    }

    
    
    /**
     * Sets the pronom format associated with this format.
     * @param pronomFormat
     */
    public void setPronomFormat(String pronomFormat) {
        log.debug("Entering setPronomFormat(pronomFormat = {})", pronomFormat);
        this.pronomFormat = pronomFormat;
        log.debug("Exiting setPronomFormat()");
    }
    
    /**
     * Gets the version of this Format.
     * 
     * @return The version of this Format.
     */
    public Integer getVersion() {
        log.debug("Entering getVersion()");
        log.debug("Exiting getVersion(): {}", version);
        return this.version;
    }

    /**
     * Sets the version of this Format.
     * 
     * @param version The version of this Format.
     */
    public void setVersion(Integer version) {
        log.debug("Entering setVersion(version = {})", version);
        Assert.notNull(version, "version cannot be null");
        this.version = version;
        log.debug("Exiting setVersion()");
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
     * http://docs.jboss.org/hibernate/entitymanager/3.6/reference/en/html_single/
     *  
     * @return A new EntityManager to do database operations.
     */
    public static final EntityManager entityManager() {
        log.debug("Entering entityManager()");
        EntityManager em = new FileFormat().entityManager;
        if (em == null) {
            String errorMessage = "Entity manager has not been injected "
                + "(is the Spring Aspects JAR configured as an AJC/AJDT aspects library?'";
            
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        log.debug("Exiting entityManager(): {}", em);
        return em;
    }

    
    
    /**
     * Gets the number of Format objects that are currently 
     * in the database.
     * 
     * @return The number of Format objects that are 
     *         currently in the database.
     */
    public static long countFormats() {
        log.debug("Entering countFormats()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM FileFormat o",
                Long.class).getSingleResult();
        log.debug("Exiting countFormats(): {}", count);
        return count;
    }

    
    
    /**
     * Gets all the Format objects from the database 
     * and puts them in a collection. 
     * 
     * @return The collection of Formats that are 
     *         in the database.
     */
    public static List<FileFormat> findAllFormats() {
        log.debug("Entering findAllFormats()");
        List<FileFormat> format = entityManager().createQuery("SELECT o FROM FileFormat o",
                FileFormat.class).getResultList();
        log.debug("Exiting findAllFormats(): {}", format);
        return format;
    }

    
    
    /**
     * Queries for a Format based on its primaryId.
     * 
     * @param primaryId  The long primaryId associated with this Format.
     * @return The Format associated with the primaryId that was passed in 
     *         or null if not found.
     */
    public static FileFormat findFormat(Long primaryId) {
        log.debug("Entering findFormat(primaryId = {})", primaryId);
        if (primaryId == null) {
            log.debug("Exiting findFormat(): null)");
            return null;
        }
        FileFormat format = entityManager().find(FileFormat.class, primaryId);
        log.debug("Exiting findFormat(): {})", format);
        return format;
    }

    
    
    /**
     * Gets a certain subset of Formats based 
     * on the range desired.
     * 
     * @param firstResult  The beginning of the range of values.
     * @param maxResults   The last element of the range of values.
     * @return             The collection of Formats based 
     *                     on the range desired. 
     */
    public static List<FileFormat> findFormatEntries(int firstResult, int maxResults) {
        log.debug("Entering findFormatEntries(firstResult = {}, " +
                "maxResults = {})", firstResult, maxResults);
        
        List<FileFormat> formats = entityManager()
                .createQuery("SELECT o FROM FileFormat o", FileFormat.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        log.debug("Exiting findFormatEntries(): {}", formats);
        return formats;
    }

    
    
    /**
     *  Assists in implementing Object.toString() methods using reflection.
     *  This class uses reflection to determine the fields to append. Because 
     *  these fields are usually private, the class changes the visibility of 
     *  the fields. This will fail under a security manager,
     *  unless the appropriate permissions are set up correctly. 
     */
    public String toString() {
        log.debug("Entering toString()");
        String toString = ReflectionToStringBuilder.toString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
        log.debug("Exiting toString(): {}", toString);
        return toString;
    }
}