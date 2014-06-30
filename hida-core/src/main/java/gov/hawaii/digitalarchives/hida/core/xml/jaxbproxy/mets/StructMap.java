package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class StructMap {
    @XmlAttribute(name = "ID")
    private String localID;

    @XmlElement(name = "div", namespace = METS.NS)
    private Div topDiv;

    /**
     * No-arg ctor for JAXB.  <b>Do not use.</b>
     */
    StructMap() {

    }

    StructMap(final String localID, final Div topDiv) {
        this.localID = localID;
        this.topDiv = topDiv;
    }

    public Div getDiv() {
        return topDiv;
    }
}
