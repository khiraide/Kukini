package gov.hawaii.digitalarchives.hida.core.model.record;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * An 'Organization' type {@code Agent}.
 *
 * @author Dongie Agnir
 */
@Configurable
@Entity
@Table(name = "ORGANIZATION")
public class Organization extends Agent {

    public Organization(String name, String note) {
        super(name, note);
    }
    
    /**
     * Gets the amount of Organization objects that are currently in the database.
     * 
     * @return The amount of Organization objects that are currently in the database.
     */
    public static long countOrganizations() {
        staticMethodLogger().debug("Entering countOrganizations()");
        long count = entityManager().createQuery("SELECT COUNT(o) FROM Organization o",
                Long.class).getSingleResult();
        staticMethodLogger().debug("Exiting countOrganizations(): {}", count);
        return count;
    }
    
    /**
     * Gets all of the Organization objects from the database and puts them in a
     * collection.
     * 
     * @return The collection of Organization objects that are in the database.
     */
    public static List<Organization> findAllOrganizations() {
        staticMethodLogger().debug("Entering findAllOrganizations()");
        List<Organization> organizations = entityManager()
                .createQuery("SELECT o FROM Organization o", Organization.class)
                .getResultList();
        staticMethodLogger().debug("Exiting findAllOrganizations(): {}", organizations);
        return organizations;
    }

    /**
     * Gets an Organization from the database according to its primaryId.
     * 
     * @param primaryId The primary key of a Organization.
     * @return The Organization associated with the primaryId that was passed in or
     *         null if not found.
     */
    public static Organization findOrganization(Long primaryId) {
        staticMethodLogger().debug("Entering findOrganization(primaryId = {})", primaryId);
        if (primaryId == null) {
            staticMethodLogger().debug("Exiting findOrganization(): null)");
            return null;
        }
        Organization organization = entityManager().find(Organization.class, primaryId);
        staticMethodLogger().debug("Exiting findOrganization(): {})", organization);
        return organization;
    }

    /**
     * Gets a certain subset of Organizations based on the range desired.
     * 
     * @param firstResult The beginning of the range of values.
     * @param maxResults The last element of the range of values.
     * @return The collection of Organizations based
     */
    public static List<Organization> findOrganizationEntries(int firstResult, int maxResults) {
        staticMethodLogger().debug("Entering findOrganizationEntries(firstResult = {}, " +
                "maxResults = {})",
                firstResult, maxResults);
        
        List<Organization> organizations = entityManager()
                .createQuery("SELECT o FROM Organization o", Organization.class)
                .setFirstResult(firstResult).setMaxResults(maxResults)
                .getResultList();
        
        staticMethodLogger().debug("Exiting findOrganizationEntries(): {}", organizations);
        return organizations;
    }
    
    
}
