package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.util.Lists;
import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * AmdSec encapsulates the &lt;amdSec&gt; complex type from the METS schema.
 * as the name suggests, this class contains administrative metadata, and
 * breaks down further into four subgroups:
 *
 * <b>techMD</b> - Technical Metadata
 * <b>rightsMD</b> - Rights Metadata
 * <b>sourceMD</b> - Source Metadata
 * <b>digiprovMD</b> Digital Provenance Metadata
 *
 *
 */
public class AmdSec {
    private static final String DMD_SEC_PREF = "DMD";
    @XmlElement(name = "digiprovMD", namespace = METS.NS)
    List<DigiprovMD> digiprovMDs = new ArrayList<DigiprovMD>();

    private int dmdSecIDCnt = 1;

    @XmlAttribute(name = "ID")
    String localID;

    /**
     * Default constructor for AmdSec needed for JAXB.  Do not use.
     */
    private AmdSec() {

    }

    /**
     * Construct this AmdSec with the given ID.
     *
     * @param localID The local (to the document) ID of this Administrative
     *           Metadata Section.
     */
    AmdSec(final String localID) {
        this.localID = localID;
    }

    /**
     * Add a Digital Provenance Metadata section to this Administrative
     * Metadata Section, and returns it to the caller.
     *
     * @param MDType The "type" of the metadata given, as a String.
     * @param MD The Digital Provenance Metadata to be added.  This class must
     *           be properly annoted with
     *           {@link javax.xml.bind.annotation.XmlRootElement};
     */
    public DigiprovMD addDigiprovMD(String MDType, Object MD) {
        if (!isXmlRoot(MD)) {
            throw new IllegalArgumentException("\"MD\" must be annotated with"
                    + " XmlRootElement.");
        }
        final String dmdID = localID + "_" + DMD_SEC_PREF + dmdSecIDCnt;
        DigiprovMD dmd = new DigiprovMD(dmdID, MDType, MD);
        dmdSecIDCnt++;
        digiprovMDs.add(dmd);
        return dmd;
    }

    public List<DigiprovMD> getDigiprovMDs() {
        return Lists.newArrayList(digiprovMDs);
    }

    /**
     * Returns the ID of this AmdSec as an instance of {@link METSID}.
     */
    public METSID getID() {
        return new METSID(localID);
    }

    /**
     * Checks if the given object is annoted with {@link XmlRootElement}.
     * @param o The Object to check.
     * @return true of o is annoted with {@link XmlRootElement}, false if not.
     */
    private boolean isXmlRoot(Object o) {
        return o.getClass().isAnnotationPresent(XmlRootElement.class);
    }
}
