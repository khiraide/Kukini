package gov.hawaii.digitalarchives.hida.core.util;

/** Class containing static Strings used as prefixes to directory names to
 * indicate their purpose.
 * 
 * @author Micah Takabayashi */
public final class Prefixes
{
    private Prefixes() {}
    
    /** Prefix prepended to all PIP directories created during SIP parsing. */
    public static final String PIP_DIR_PREFIX = "PIP-";
    
    /** Prefix prepended to the name of all Accession directories. */
    public static final String ACCESSION_DIR_PREFIX = "Accession-";
    
    /** Prefix prepended to the name of all error directories. */
    public static final String ERROR_DIR_PREFIX_STRING = "Error-";
}
