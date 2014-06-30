package gov.hawaii.digitalarchives.hida.core.util;

/** An enum of values indicating whether or the output for a given business
 * module should be compressed, uncompressed, or left in its original state.
 * 
 * @author Micah Takabayashi */
public enum CompressOnExit
{
    /** Output bags will be compressed. */
    COMPRESS,
    /** Output bags will be uncompressed. */
    UNCOMPRESS,
    /** Output bags will be left in their original compressed/uncompressed state. */
    RESTORE
}
