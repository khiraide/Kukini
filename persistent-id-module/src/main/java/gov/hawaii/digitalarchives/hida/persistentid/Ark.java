package gov.hawaii.digitalarchives.hida.persistentid;


/** Interface for classes representing ARK (Archival Resource Key) identifiers.
 * 
 * @author Micah Takabayashi */
public interface Ark extends PersistentId
{
    /** ARK scheme identifier. */
    public static final String SCHEME = "ark";
    
    /** @return The NAAN (Name Assigning Authority Number) portion of this ARK. */
    public String getNaan ();

    /** @return The ARK name of this ARK. */
    public String getArkName ();

    /** @return The qualifier of this ARK. Note that the qualifier is optional and
     *         may be empty. */
    public String getQualifier ();
}
