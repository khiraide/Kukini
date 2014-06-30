package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.premis;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Simple enum type annoted with JAXB for the three types of PREMIS objects:
 * file, bitstream, and representation.
 *
 * @author Dongie Agnir
 */

@XmlEnum
public enum ObjectType {
    @XmlEnumValue("premis:bitstream")
    BISTREAM("premis:bitstream"),
    @XmlEnumValue("premis:file")
    FILE("premis:file"),
    @XmlEnumValue("premis:representation")
    REPRESENTATION("premis:representation"); //unused

    private final String typeStr;

    ObjectType(final String typeStr) { this.typeStr = typeStr; }
    public String getStringVal() { return typeStr; }
}
