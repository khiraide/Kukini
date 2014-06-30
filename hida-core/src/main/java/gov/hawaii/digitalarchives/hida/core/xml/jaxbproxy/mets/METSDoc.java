package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * METSDoc is a Java representation of a METS document.  Its main purpose is
 * to be serializable to a valid XML representation, and back, with the help of
 * JAXB.  This implementation is quite minimal, and only has support for adding
 * (exported only to classes in the same package), or retrieving its main
 * sections, e.g. AmdSec or FileSec.
 *
 * The creation of a METSDoc is done through {@link METSDocCreator}.
 *
 * @author Dongie Agnir
 */
@XmlRootElement(name = "mets", namespace = METS.NS)
public class METSDoc {
    @XmlElement(name = "amdSec", namespace = METS.NS)
    private List<AmdSec> amdSecs = new ArrayList<AmdSec>();

    @XmlElement(name = "fileSec", namespace = METS.NS)
    private FileSec fileSec = null;

    @XmlElement(name = "structMap", namespace = METS.NS)
    private List<StructMap> structMaps = new ArrayList();

    @XmlAttribute(name = "TYPE")
    private String type;

    /**
     * Construct an empty METS Document Object.  Also required by JAXB.
     */
    METSDoc() {
    }

    /**
     * Adds and an AmdSec to this METSDoc.
     * @param sec The instance of AmdSec to add to this METSDoc.
     */
    void addAmdSec(AmdSec sec) {
        amdSecs.add(sec);
    }

    /**
     * Adds a StructMap to this METSDoc.
     * @param structMap The instance of StructMap to add to this METSDoc.
     */
    void addStructMap(StructMap structMap) {
        structMaps.add(structMap);
    }

    /**
     * Retrieve the {@link AmdSec} objects current contained by the METS
     * Document Object.
     * @return The {@link AmdSec} objects contained in this document as a
     * {@link List}.
     */
    public List<AmdSec> getAmdSecs() {
        return new ArrayList<AmdSec>(amdSecs);
    }

    /**
     * Returns the instance of FileSec contained in this METSDoc.  Each METSDoc
     * can contain at most only a single instance of this object.
     *
     * @return The instance of FileSec contained in this METSDoc, or null of it
     * does not contain one.
     */
    public FileSec getFileSec() {
        return fileSec;
    }

    /**
     * Adds a FileSec to this METSDoc.
     * @param fileSec The isntance of FileSec to add to this METSDoc.
     */
    void setFileSec(final FileSec fileSec) {
        this.fileSec = fileSec;
    }
}
