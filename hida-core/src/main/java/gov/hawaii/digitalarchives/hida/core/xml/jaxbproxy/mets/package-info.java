@XmlSchema(namespace = METS.NS,
        xmlns = {
                @XmlNs(namespaceURI = METS.NS, prefix = "mets"),
                @XmlNs(namespaceURI = XLINK.NS, prefix = "xlink"),
        },
        elementFormDefault = XmlNsForm.QUALIFIED)

package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;
import gov.hawaii.digitalarchives.hida.core.xml.namespaces.XLINK;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

