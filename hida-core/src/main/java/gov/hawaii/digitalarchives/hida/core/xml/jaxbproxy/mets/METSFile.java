package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;
import gov.hawaii.digitalarchives.hida.core.xml.namespaces.XLINK;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * METSFile is a Java representation of the &lt;file&gt; element from METS.  Its
 * main purpose is to describe a file object.  In the METS standard, this object
 * can either contain contain other &lt;file&gt; objects, binary data, and
 * others.  For this implementation however METSFile contains just an inner
 * &lt;FLocat&gt; METS element, which contains the {@code URI} of the file.
 *
 * @author Dongie Agnir
 */
public class METSFile {
    /**
     * This is the Java representation of the {@code <FLocat>} element from
     * METS.
     *
     * @author Dongie Agnir
     */
    private static class FLocat {
        @XmlAttribute(name = "href", namespace = XLINK.NS)
        String URI;

        @XmlAttribute(name = "LOCTYPE")
        String URIType;

        /**
         * No-arg ctor for JAXB.  <b>Do not use.</b>
         */
        FLocat() {

        }

        /**
         * Create an instance of {@code FLocat} with the given {@code URI} and
         * {@code URIType}.
         * @param URI
         * @param URIType
         */
        FLocat(final String URI, final String URIType) {
            this.URI = URI;
            this.URIType = URIType;
        }
    }

    @XmlAttribute(name = "ADMID")
    private String ADMID;

    @XmlAttribute(name = "CHECKSUM")
    private String checksum;

    @XmlAttribute(name = "CHECKSUMTYPE")
    private String checksumType;

    @XmlAttribute(name = "ID")
    private String localID;

    @XmlElement(name = "FLocat", namespace = METS.NS)
    FLocat locat;

    @XmlAttribute(name = "MIMETYPE")
    private String mimeType;

    @XmlTransient
    private Object parent;

    @XmlAttribute(name = "SIZE")
    private int size;

    /**
     * No-arg ctor for JAXB.  <b>Do not use.</b>
     */
    METSFile() {

    }

    /**
     * Construct this METSFile with the given values.
     * @param localID The ID of this object.
     * @param ADMID The ID of the amdSec of this file corresponding amdSec.
     * @param mimeType The mimetype of this file.
     * @param checksum The checksum or message digest of the file.
     * @param checksumType The algorithm used to generate the checksum of the
     *                     file, eg {@code SHA-1}.
     * @param size The size of the file in bytes.
     * @param URI The URI of this file.
     * @param URItype The type of the URI given, e.g. {@code ARK}
     */
    METSFile(final String localID, final String ADMID, final String mimeType,
             final String checksum, final String checksumType, int size,
             final String URI, final String URItype) {
        this.localID = localID;
        this.ADMID = ADMID;
        this.mimeType = mimeType;
        this.checksum = checksum;
        this.checksumType = checksumType;
        this.size = size;

        locat = new FLocat(URI, URItype);
    }

    /**
     * This is what JAXB calls a "Class defined" event callback function.  In
     * this case, this method is called after this object has been umarshalled
     * from XML.  For {@code METSFile}, we use this callback to set the
     * {@code parent} object.
     *
     * @param um The instance of {@code Unmarshaller} that unmarshalled this
     *           {@code METSFile}.
     * @param parent The parent of this object.
     */
    public void afterUnmarshal(Unmarshaller um, final Object parent) {
        this.parent = parent;
    }

    /**
     * Returns the ID of this {@code METSFile} wrapped in a {@link METSID}.
     * @return The ID of this {@code METSFile}.
     */
    public METSID getID() {
        return new METSID(localID);
    }

    /**
     * Check whether another object has taken ownership of this
     * {@code METSFile}.
     *
     * @return {@code true} if it has been bound, {@code false} if not.
     */
     boolean isBound() {
        return parent != null;
    }


    /**
     * Sets the parent of this METSFile.
     * @param parent The parent of this METSFile.
     */
    void setParent(final Object parent) {
        this.parent = parent;
    }
}
