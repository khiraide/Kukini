package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.premis;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.PREMIS;

import javax.xml.bind.annotation.XmlElement;

/**
 * Represents a relationship between objects in PREMIS.
 *
 * @author Dongie Agnir
 */
// TODO:
//  1.  Right now, type and subType are just STRINGS.  Should an enum be
//  be created instead?
public class Relationship {
    private static class RelatedEventID {
        @XmlElement(name = "relatedEventIdentifierType",
                namespace = PREMIS.NS)
        private String type;

        @XmlElement(name = "relatedEventIdentifierValue",
                namespace = PREMIS.NS)
        private String value;

        private RelatedEventID() {

        }

        private RelatedEventID(final String type, final String value) {
            this.type = type;
            this.value = value;
        }

        public String getIDType() {
            return type;
        }

        public String getIDValue() {
            return value;
        }
    }

    private static class RelatedObjID {
        @XmlElement(name = "relatedObjectIdentifierType",
                namespace = PREMIS.NS)
        private String type;

        @XmlElement(name = "relatedObjectIdentifierValue",
                namespace = PREMIS.NS)
        private String value;

        private RelatedObjID() {

        }

        private RelatedObjID(final String type, final String value) {
            this.type = type;
            this.value = value;
        }

        public String getIDType() {
            return type;
        }

        public String getIDValue() {
            return value;
        }
    }

    @XmlElement(name = "relatedEventIdentification", namespace = PREMIS.NS)
    private RelatedEventID eventID;

    @XmlElement(name = "relatedObjectIdentification", namespace = PREMIS.NS)
    private RelatedObjID objID;

    @XmlElement(name = "relationshipSubType", namespace = PREMIS.NS)
    private String subType;

    @XmlElement(name = "relationshipType", namespace = PREMIS.NS)
    private String type;

    /**
     * No-arg ctor for JAXB.  <b>Do not use.</b>
     */
    private Relationship() {

    }

    /**
     * Construct this Relationship with the given values.
     *
     * @param type The overall general type of this relationship.  PREMIS
     *             suggests values including "derivation", and "structural".
     * @param subType The more specific type of this relationship.  PREMIS
     *                suggests values including "has source" and "includes."
     * @param objIDType The type of the ID of the related object.
     * @param objIDVal The value of the ID of the related object.
     * @param eventIDType The type of the ID of the related event.
     * @param eventIDVal The value of the ID of the related event.
     */
    Relationship(final String type, final String subType,
                         String objIDType, String objIDVal,
                         String eventIDType, String eventIDVal) {
        this.type = type;
        this.subType = subType;

        objID = new RelatedObjID(objIDType, objIDVal);
        eventID = new RelatedEventID(eventIDType, eventIDVal);
    }

    public String getRelatedEventIDType() {
        return eventID.getIDType();
    }

    public String getRelatedEventIDValue() {
        return eventID.getIDValue();
    }

    public String getRelatedObjectIDType() {
        return objID.getIDType();
    }

    public String getRelatedObjectIDValue() {
        return objID.getIDValue();
    }



    public String getSubType() {
        return subType;
    }

    public String getType() {
        return type;
    }

}