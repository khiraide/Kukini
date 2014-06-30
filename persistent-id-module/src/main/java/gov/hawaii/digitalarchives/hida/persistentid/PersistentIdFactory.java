package gov.hawaii.digitalarchives.hida.persistentid;

/**
 * Interface for creating PersistentId objects.
 *
 * @author Calvin Wong
 *
 */
public interface PersistentIdFactory
{
    /**
     * Creates a new {@link PersistentId} object.
     *
     * @return PersistentId object.
     */
    PersistentId createPersistentId ();
    
    /** Create a {@link PersistentId} object using the existing unique
     * identifier, passed as a String.
     * 
     * @param existingId An existing PersistentId URI, as a String.
     * @return A new PersistentId object based on the input URI. */
    PersistentId createPersistentId (String existingId);
}
