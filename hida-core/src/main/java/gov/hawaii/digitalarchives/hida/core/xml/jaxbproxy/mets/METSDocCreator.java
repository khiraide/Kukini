package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

/**
 * METSDocCreator is the class through which an instance of {@link METSDoc} is
 * created.  It main purpose is to be the interface through which all elements
 * of the document are created, in order to enforce the validity of the
 * resulting METS document as much as possible.  For example, it ensures that
 * each AmdSec it creates has a unique ID.  Neither {@link METSDoc} or any of
 * its sub elements can be instantiated outside of this package except through
 * this class.
 *
 * @author Dongie Agnir
 */
public class METSDocCreator {
    //Prefix for amdSec IDs
    private static final String AMDSEC_PREF = "AMD";

    //Prefix for div IDs
    private static final String DIV_PREF = "div";

    //prefix for file IDs
    private static final String FILE_PREF = "FILE";

    //Prefix for StructMap IDs
    private static final String STRUCTMAP_PREF = "SMAP";

    //The following are incremental counters for the different elements of a
    //METS document, to be incremented by one each time an object they represent
    //is instantiated.
    //amdSec
    private int amdSecCnt = 0;

    //div
    private int divCnt = 0;
    //The instance of METSDoc that we are building.
    private final METSDoc doc = new METSDoc();
    //file
    private int fileCnt = 0;
    //fileSec
    private int fileSecCnt = 0;
    //structMap
    private int smapCnt = 0;

    /**
     * Construct an instance of MetsDocCreator.
     */
    public METSDocCreator() {

    }

    /**
     * Creates a new instance of {@link AmdSec} with a unique ID, and returns it
     * to the caller.  The created {@link AmdSec} is automatically bound to the
     * document being created.
     * @return The instance of AmdSec that was created.
     */
    public AmdSec createAmdSec() {
        final String ID = AMDSEC_PREF + (amdSecCnt + 1);
        AmdSec sec = new AmdSec(ID);
        ++amdSecCnt;

        doc.addAmdSec(sec);
        return sec;
    }

    /**
     * Creates a new instance of {@link Div}.  This object <b>IS NOT</b> added
     * to to the document automatically.  It must be added to a parent
     * {@link Div}, or used in {@link #createStructMap(Div)}.
     * @param ADMID The ID token of the {@link AmdSec} that it should be
     *              associated with it.  Can be {@code null}.
     * @return
     */
    public Div createDiv(METSID ADMID) {
        Div d;
        String ID = DIV_PREF + (divCnt + 1);
        if (ADMID == null) {
            d = new Div(ID, null);
        } else {
            d = new Div(ID, ADMID.getValue());
        }
        ++divCnt;
        return d;
    }

    /**
     * Create an instance of {@link FileSec} and automatically binds it to the
     * document.  Only one file is allowed per document.  Multiple calls to
     * this method will return the same instance.
     * @return The {@link FileSec} created for the document.
     */
    public FileSec createFileSec() {
        if (doc.getFileSec() != null) {
            return doc.getFileSec();
        }
        FileSec sec = new FileSec();
        doc.setFileSec(sec);
        return sec;
    }

    /**
     * Creates an instance of {@link METSFile}, and returns it to the caller.
     * Note that this object is not automatically added to the document.  It
     * must be added as part of a {@link FileGrp}.
     * @param ADMID The ID token of the corresponding {@code amdSec} for this
     *              file.
     * @param mimeType The mimetype of this file.
     * @param checksum The checksum (or message digest) of the file.
     * @param checksumType The type aka algorithm that was used to produce the
     *                     checksum.
     * @param size The size--in bytes--of the file.
     * @param URI The URI of the file.
     * @param URIType The type of URI (e.g. ARK).
     * @return The newly created {@link METSFile}.
     */
    public METSFile createMETSFile(METSID ADMID, String mimeType,
                                   String checksum, String checksumType,
                                   int size, String URI, String URIType) {
        final String ID = FILE_PREF + (fileCnt + 1);
        METSFile file = new METSFile(ID, ADMID.getValue(), mimeType, checksum,
                                            checksumType, size, URI, URIType);
        ++fileCnt;
        return file;
    }

    /**
     * Creates a new instance of {@link StructMap} with a unique ID and returns
     * it to the user.  The isntance of {@link StructMap} is automatically bound
     * to the document being created.
     * @param topDiv The root Div element to be used in the StructMap.  Must not
     *               be {@code null}.
     * @return The instance of {@link StructMap} that was created.
     * @throws IllegalAccessException Thrown if {@code topDiv} is {@code null}.
     */
    public StructMap createStructMap(Div topDiv) {
        if (topDiv == null) {
            throw new IllegalArgumentException("The given Div cannot be null");
        }
        final String ID = STRUCTMAP_PREF + (smapCnt + 1);
        StructMap map = new StructMap(ID, topDiv);
        ++smapCnt;

        doc.addStructMap(map);
        return map;
    }


    /**
     * @return The document.
     */
    public METSDoc getDocument() {
        return doc;
    }
}
