package gov.hawaii.digitalarchives.hida.core.model.record;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The Statute class which roughly corresponds to the {@code <PREMIS:statuteInformation>}
 * elemenent in the HiDA METS Schema. Preserves immutability.
 * <p>
 * Note: package-private.
 * 
 * @author Dongie Agnir
 * TODO: enforce non-null
 */
@Entity
@Configurable
@Table(name = "STATUTE")
class Statute extends RightsBasis {
    
    /**
     * The jurisdiction associated with this Statute.
     */
    @Column(name = "JURISDICTION")
    private String jurisdiction;
    

    /**
     * The citation associated with this Statute.
     */
    @Column(name = "CITATION")
    private String citation;    
    
    /**
     * Oracle specifications state that the class must have a public 
     * or protected, no-argument constructor. The class may have other 
     * constructors. 
     * http://docs.oracle.com/javaee/7/tutorial/doc/persistence-intro001.htm
     */
    public Statute() {
        
    }
    
    

    /**
     * Constructs a properly formed Statute.
     * 
     * @param jurisdiction The jurisdiction associated with this Statute.
     * @param citation The citation associated with this Statute.
     * @param startDate The start date associated with this Statute.
     * @param endDate The end date associated with this Statute.
     * @param note The note associated with this Statute.
     */
    public Statute(final String jurisdiction,final String citation, Date startDate,
                        Date endDate, String note) {
        super(startDate, endDate, note);

        Assert.notNull(jurisdiction, "jurisdiction cannot be null.");
        Assert.notNull(citation, "citation cannot be null.");

        this.jurisdiction = jurisdiction;

        this.citation = citation;
    }


    
    /**
     * Gets the jurisdiction associated with this Statute.
     * 
     * @return The jurisdiction associated with this Statute.
     */
    public String getJurisdiction() {
        log.debug("Entering getJurisdiction()");
        log.debug("Exiting getJurisdiction(): {}", jurisdiction);
        return jurisdiction;
    }


    
    /**
     * Gets the citation associated with this Statute.
     * 
     * @return The citation associated with this Statute.
     */
    public String getCitation() {
        log.debug("Entering getCitation()");
        log.debug("Exiting getCitation(): {}", citation);
        return citation;
    }
    
    
    
    /**
     * Gets the amount of Statute objects that are currently 
     * in the database.
     * 
     * @return The amount of Statute objects that are 
     *         currently in the database.
     */
    public static long countStatutes() {
        staticMethodLogger().debug("Entering countStatutes()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM Statute o",
                Long.class).getSingleResult();
        staticMethodLogger().debug("Exiting countStatutes(): {}", count);
        return count;
    }

    
    
    /**
     * Gets all of the Statute objects from the database 
     * and puts them in a collection. 
     * 
     * @return The collection of Statutes that are 
     *         in the database.
     */
    public static List<Statute> findAllStatutes() {
        staticMethodLogger().debug("Entering findAllStatutes()");
        List<Statute> statutes = entityManager()
                .createQuery("SELECT o FROM Statute o", Statute.class)
                .getResultList();
        staticMethodLogger().debug("Exiting findAllStatutes(): {}", statutes);
        return statutes;
    }

    
    
    /**
     * Queries for an Statute based on its primaryId.
     * 
     * @param primaryId  The long primaryId associated with the 
     *                   Statute that you want to search the database 
     *                   for.
     * @return  The associated Statute matching the primaryId you were
     *          searching for, or null if not found.
     */
    public static Statute findStatute(Long primaryId) {
        staticMethodLogger().debug("Entering findStatute(primaryId = {})", primaryId);
        if (primaryId == null) {
            staticMethodLogger().debug("Exiting findStatute(): null)");
            return null;
        }
        Statute statute = entityManager().find(Statute.class, primaryId);
        staticMethodLogger().debug("Exiting findStatute(): {})", statute);
        return statute;
    }

    
    
    /**
     * Gets a certain subset of Statutes based 
     * on the range desired.
     * 
     * @param firstResult  The beginning of the range of values.
     * @param maxResults   The last element of the range of values.
     * @return             The collection of Statutes based 
     *                     on the range desired. 
     */
    public static List<Statute> findStatuteEntries(int firstResult, int maxResults) {
        staticMethodLogger().debug("Entering findStatuteEntries(firstResult = {}, " +
                "maxResults = {})",
                firstResult, maxResults);
        
        List<Statute> statutes = entityManager()
                .createQuery("SELECT o FROM Statute o", Statute.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        staticMethodLogger().debug("Exiting findStatuteEntries(): {}", statutes);
        return statutes;
    }

    
    
    /**
     * If this object is detached, the merge() method will merge them back into
     * the EntityManager to become managed again. Note that changes to detached
     * entity objects are not stored in the database.
     * 
     * @return The merged Statute.
     */
    @Transactional
    public Statute merge() {
        log.debug("Entering merge()");
        if (this.entityManager == null) 
            this.entityManager = entityManager();
        Statute merged = this.entityManager.merge(this);
        this.entityManager.flush();
        log.debug("Exiting merge(): {}", merged);
        return merged;
    }
}
