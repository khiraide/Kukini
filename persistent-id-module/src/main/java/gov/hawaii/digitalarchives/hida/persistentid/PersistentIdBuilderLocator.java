package gov.hawaii.digitalarchives.hida.persistentid;


/** Service class used to look up instances of {@link PersistentIdFactory}s.
 * 
 * @author Micah Takabayashi */
public interface PersistentIdBuilderLocator
{
    /** Look up and return a {@link PersistentIdFactory} of the type used to
     * create {@link PersistentId}s with the given scheme. PersistentIdBuilders
     * are Spring beans named after the type of scheme they handle, and are
     * looked up by ID.
     * 
     * @param schemeId The scheme the returned builder should create
     *            PersistentIds for. This is also the name of the
     *            PersistentIdBuilder bean in the Spring context.
     * @return The singleton PersistentIdBuilder that handles PersistentId with
     *         the scheme specified. */
    public PersistentIdFactory createPersistentIdBuilder (String schemeId);
}
