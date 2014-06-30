package gov.hawaii.digitalarchives.hida.core.xml.namespaces;

/**
 * This is an interface for aggregating string constants related to the PREMIS
 * namespace.
 *
 * @author Dongie Agnir
 */
public class PREMIS {
    //Qualified Node names
    //Object Types
    public static final String FILE = "premis:file";
    public static final String NS = "info:lc/xmlns/premis-v2";
    //Object and sub-nodes
    public static final String OBJ = "premis:object";

    public static final String OBJ_CHARS = "premis:objectCharacteristics";
    public static final String OBJ_COMP_LEVEL = "premis:compositionLevel";
    public static final String OBJ_DIGEST = "premis:messageDigest";
    public static final String OBJ_DIGEST_ALG = "premis:messageDigestAlgorithm";
    public static final String OBJ_FIXITY = "premis:fixity";
    public static final String OBJ_FMT = "premis:format";

    public static final String OBJ_FMT_DESIGNATION = "premis:formatDesignation";
    public static final String OBJ_FMT_NAME = "premis:formatName";
    public static final String OBJ_ID = "premis:objectIdentifier";
    public static final String OBJ_ID_TYPE = "premis:objectIdentifierType";
    public static final String OBJ_ID_VAL = "premis:objectIdentifierValue";
    public static final String OBJ_SIZE = "premis:size";
    public static final String REP = "premis:representation";
    public static final String SCHEMA_LOC = "http://www.loc.gov/standards/premis/premis.xsd";
    //private to prevent instiation
    private PREMIS() {}

}
