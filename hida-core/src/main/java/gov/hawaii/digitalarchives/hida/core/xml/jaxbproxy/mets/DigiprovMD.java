package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * DigiprovMD encapsulates the &lt;digiprovMD&gt; complex type from METS.  Its
 * main purpose is to encapsulate some type of Digital Provenance Metadata
 * conformant to some type of schema external to METS.  The metadata is given
 * as a JAXB compatible class (annoted with {@link XmlRootElement}.
 */
public class DigiprovMD {
    static class MDWrap {
        @XmlAttribute(name = "MDTYPE")
        private String type;

        @XmlElementWrapper(name = "xmlData", namespace = METS.NS)
        //lax = true means that JAXB will use the @XmlRootElement tag of the
        //Object to see which class it will use to (un)marshal this Object.
        @XmlAnyElement(lax = true)
        private List<Object> wrappedMD = new ArrayList<Object>();

        /**
         * Empty ctor for JAXB.  <b>Do not use</b>.
         */
        MDWrap() {

        }

        MDWrap(final String MDType, final Object wrappedMD) {
            assert MDType != null && wrappedMD != null;
            type = MDType;
            this.wrappedMD.add(wrappedMD);
        }


        String getType() {
            return type;
        }

        Object getWrappedMD() {
            return wrappedMD.get(0);
        }
    }

    private String localID;

    @XmlElement(name = "mdWrap", namespace = METS.NS)
    private MDWrap mdWrap;

    /**
     * Empty ctor for JAXB.  <b>Do not use</b>.
     */
    DigiprovMD() {

    }

    /**
     * Construct this Digital Provenance Metadata object with the given ID,
     * metadata type, and the actual metadata.
     *
     * @param localID The local ID of this element.  This must be unique accross
     *                all element IDs in the the document.
     * @param MDType  The type of metada being wrapped.
     * @param MD      The metadata being wrapped.  This class MUST be
     *                annoted with {@link XmlRootElement}, so that JAXB knows
     *                how to marshal and unmarshal it properly.
     */
    DigiprovMD(final String localID, String MDType, Object MD) {
        this.localID = localID;
        mdWrap = new MDWrap(MDType, MD);
    }

    /**
     * @return The ID of this Digital Provenance Metadata element.
     */
    @XmlAttribute(name = "ID")
    public String getID() {
        return localID;
    }

    /**
     * Returns the type of metadata this instance contains.  Use the this info
     * to determine what type to cast the object returned by
     * {@link com.hi.dags.archives.xml.jaxbproxy.mets.DigiprovMD#getMetadata()}.
     *
     * @return A String representing the type of this object.
     */
    public String getMDType() {
        return mdWrap.getType();
    }

    /**
     * The actual metadata as an Object.  The caller needs to know what type
     * to cast this to.
     * @return The Digital Provenance metadata.
     */
    public Object getMetadata() {
        return mdWrap.getWrappedMD();
    }
}