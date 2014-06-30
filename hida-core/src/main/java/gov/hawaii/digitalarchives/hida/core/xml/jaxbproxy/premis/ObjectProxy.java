package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.premis;

import gov.hawaii.digitalarchives.hida.core.util.Lists;
import gov.hawaii.digitalarchives.hida.core.xml.namespaces.PREMIS;
import gov.hawaii.digitalarchives.hida.core.xml.namespaces.XSI;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a proxy class for {@link ArchivesObject} and its subclasses
 * ({@link com.hi.dags.archives.object.RepresentationObject} and
 * {@link FileObject}) that is meant to be serializable to XML by JAXB that is
 * then valid against the PREMIS v2.2 schema.
 *
 * @author Dongie Agnir
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "object", namespace = PREMIS.NS)
public class ObjectProxy {
    private static class Fixity {
        @XmlElement(name = "messageDigest", namespace = PREMIS.NS)
        private String digest;

        @XmlElement(name = "messageDigestAlgorithm", namespace = PREMIS.NS)
        private String digestAlgorithm;

        Fixity() {

        }

        Fixity(final String digestAlg, final String digest) {
            this.digestAlgorithm = digestAlg;
            this.digest = digest;
        }

        public String getAlgorithm() {
            return digestAlgorithm;
        }

        public String getDigest() {
            return digest;
        }
    }

    private static class Format {
        private static class FormatDesignation {
            @XmlElement(name = "formatName", namespace = PREMIS.NS)
            private String name;

            @XmlElement(name = "formatVersion", namespace = PREMIS.NS)
            private String version;


            /**
             */
            FormatDesignation() {

            }

            FormatDesignation(final String name, final String version) {
                this.name = name;
                this.version = version;
            }

            public String getName() {
                return name;
            }

            public String getVersion() {
                return version;
            }


        }

        @XmlElement(name = "formatDesignation", namespace = PREMIS.NS)
        private FormatDesignation fmtDesignation;

        /**
         * Empty ctor for JAXB.  Do not use.
         */
        Format() {

        }

        Format(final String name, final String version) {
            fmtDesignation = new FormatDesignation(name, version);
        }

        public String getName() {
            return fmtDesignation.getName();
        }

        public String getVersion() {
            return fmtDesignation.getVersion();
        }
    }

    private static class ObjectCharacteristics {
        @XmlElement(name = "compositionLevel", namespace = PREMIS.NS)
        private int compLevel;

        @XmlElement(name = "fixity", namespace = PREMIS.NS)
        private Fixity fixity;

        @XmlElement(name = "format", namespace = PREMIS.NS)
        private Format fmt;

        @XmlElement(name = "size", namespace = PREMIS.NS)
        private int size;

        public ObjectCharacteristics() {

        }

        public ObjectCharacteristics(int compLevel, int size, Fixity fixity,
                                     Format fmt) {
            this.compLevel = compLevel;
            this.fixity = fixity;
            this.size = size;
            this.fmt = fmt;
        }

        public int getCompLevel() {
            return compLevel;
        }


        public Fixity getFixity() {
                return fixity;
        }

        public Format getFormat() {
            return fmt;
        }

        public int getSize() {
            return size;
        }


        private void setCompositionLivel(final int compLevel) {
            this.compLevel = compLevel;
        }

        private void setFixity(final Fixity fixity) {
            this.fixity = fixity;
        }

        private void setFormat(final Format fmt) {
            this.fmt = fmt;
        }

        private void setSize(final int size) {
            this.size = size;
        }
    }

    private static class ObjID {
        @XmlElement(name = "objectIdentifierType", namespace = PREMIS.NS)
        private String IDType;

        @XmlElement(name = "objectIdentifierValue", namespace = PREMIS.NS)
        private String IDVal;

        /**
         * No-arg ctor for JAXB.  <b>Do not use.</b>
         */
        private ObjID() {

        }

        /**
         * Construct this {@code ObjID} with the given type and value.
         *
         * @param IDType The type of the ID as a string.  Common example is
         *               "ARK".
         * @param IDVal The value of the ID as a string.
         */
        private ObjID(final String IDType, final String IDVal) {
            this.IDType = IDType;
            this.IDVal = IDVal;
        }

        String getIDType() {
            return IDType;
        }

        String getIDVal() {
            return IDVal;
        }
    }

    private static class Storage {
        private static class Location {
            String type;

            String value;

            /**
             * No-arg ctor for JAXB.  <b>Do not use.</b>
             */
            Location() {

            }

            Location(final String type, final String value) {
                this.type = type;
                this.value = value;
            }


            @XmlElement(name = "contentLocationType", namespace = PREMIS.NS)
            public String getType() {
                return type;
            }

            @XmlElement(name = "contentLocationValue", namespace = PREMIS.NS)
            public String getValue() {
                return value;
            }
        }

        @XmlElement(name = "contentLocation", namespace = PREMIS.NS)
        private Location location;

        /**
         * No-arg ctor for JAXB.  <b>Do not use.</b>
        */
        Storage() {

        }

        Storage(String locType, String locVal) {
            location = new Location(locType, locVal);
        }

        public String getContentLocation() {
            return location.getValue();
        }

    }


    @XmlElement(name = "objectIdentifier", namespace = PREMIS.NS)
    private ObjID ID;


    @XmlElement(name = "objectCharacteristics", namespace = PREMIS.NS)
    private ObjectCharacteristics objChars;

    //===------------------------------------------------------------------===//
    // <premis:objectCharacteristics> setters.
    //
    // These can only be used on objects that are either a BITSTREAM or a FILE
    // (type is set during construction), so we throw if our "type" is a
    // REPRESENTATION.
    //
    // TODO:
    // 1. Is IllegalStateException the correct Exception to throw in the above
    // case?
    // 2. Some of the setters take values that actually can't be set at the
    // moment, such as the registry stuff for setFormat.
    //===------------------------------------------------------------------===//

    @XmlElement(name = "relationship", namespace = PREMIS.NS)
    private List<Relationship> relationships = Lists.newArrayList();

    @XmlElement(name = "storage", namespace = PREMIS.NS)
    private Storage storage;

    @XmlAttribute(name = "type", namespace = XSI.NS)
    private ObjectType type;

    /**
     * No-arg ctor for JAXB.  <b>Do not use.</b>
     */
    ObjectProxy() {

    }

    public ObjectProxy(final ObjectType type, String IDType, String IDVal) {
        this.type = type;

        this.ID = new ObjID(IDType, IDVal);
    }

    /**
     * Add a relationship to this object.
     *
     * @param type The type of this relationship, such as "derivation" or
     *             "structural."
     * @param subType The sub type of the relationship, such as "has source."
     * @param objIDType The type of the ID of the related object.
     * @param objIDVal The value of the ID of the related object.
     * @param eventIDType The type of the ID of the related event.
     * @param eventIDVal The value of the ID of the related event.
     */
    public void addRelationship(String type, String subType, String objIDType,
                                String objIDVal, String eventIDType,
                                String eventIDVal) {
        relationships.add(new Relationship(type, subType, objIDType, objIDVal,
                                           eventIDType, eventIDVal));
    }

    /**
     * @return The composition level of the object.
     */
    public int getCompLevel() {
        if (objChars != null) {
            return objChars.getCompLevel();
        }
        return -1;
    }

    /**
     * @return The digest value of object.  (Presumably produced by the
     * algorithm identified by {@link #getDigestAlgorithm()}.
     */
    public String getDigest() {
        if (objChars != null) {
            return objChars.getFixity().getDigest();
        }
        return "";
    }

    /**
     * @return the algorithm presumably used to create the digest message
     * returned by {@link #getDigest()}.
     */
    public String getDigestAlgorithm() {
        if (objChars != null) {
            return objChars.getFixity().getAlgorithm();
        }
        return "";
    }

    /**
     * @return The name of the format of the object.
     */
    public String getFormatName() {
        if (objChars != null) {
            return objChars.getFormat().getName();
        }
        return "";
    }

    public String getID() {
        return ID.getIDVal();
    }

    /**
     * @return The type of the ID.
     */
    public String getIDType() {
        return ID.getIDType();
    }

    /**
     * @return The value of the ID.
     */
    public String getIDValue() {
        return ID.getIDVal();
    }

    /**
     * @return The storage location of the object.
     */
    public String getLocationURI() {
        if (storage != null) {
            return storage.getContentLocation();
        }
        return "";
    }

    /**
     * Used to retrieve the object characteristics of this object.  Provides
     * lazy isntantiation.
     */
    private ObjectCharacteristics getObjChars() {
        if (objChars == null) {
            objChars = new ObjectCharacteristics();
        }
        return objChars;
    }

    /**
     * @return The size of the object in bytes.
     */
    public int getSize() {
        if (objChars != null) {
            return objChars.getSize();
        }
        return -1;
    }

    public ObjectType getType() {
        return type;
    }


    /**
     * Set the composition level of this Object.
     *
     * Can only be used when the {@link ObjectType} of this Object is either
     * a {@link ObjectType#BISTREAM} or {@link ObjectType#FILE}.
     *
     * @param compLevel The composition level of this obbject.
     *
     * @throws IllegalStateException Thrown if the type of this object is
     * {@link ObjectType#REPRESENTATION}.
     */
    public void setCompositionLevel(int compLevel) {
        if (type == ObjectType.REPRESENTATION) {
            throw new IllegalStateException("Cannot set composition level on a"
                                                + "  representation object.");
        }
    }


    /**
     * Set the fixity information for this object.
     *
     * Can only be used when the {@link ObjectType} of this Object is either
     * a {@link ObjectType#BISTREAM} or {@link ObjectType#FILE}.
     *
     * @param digestAlgorithm The digest algorithm used (e.g. "SHA-1").
     * @param digest The message digest.
     * @param digestOriginator The originator of the digest.
     *
     * @throws IllegalStateException Thrown if the type of this object is
     * {@link ObjectType#REPRESENTATION}.
     */
    public void setFixity(String digestAlgorithm, String digest,
                          String digestOriginator) {
        if (type == ObjectType.REPRESENTATION) {
            throw new IllegalStateException("Cannot set fixity on a"
                                                + " representation object.");
        }

        getObjChars().setFixity(new Fixity(digestAlgorithm, digest));
    }

    /**
     * Set the format information of this object.
     *
     * Can only be used when the {@link ObjectType} of this Object is either
     * a {@link ObjectType#BISTREAM} or {@link ObjectType#FILE}.
     *
     * @param name The format name.
     * @param version The format version.
     * @param registryName The name of the registry that the format originates
     *                     from.
     * @param registryKey The key of the registry.
     * @param registryRole The role of the registry.
     *
     * @throws IllegalStateException Thrown if the type of this object is
     * {@link ObjectType#REPRESENTATION}.
     */
    public void setFormat(String name, String version, String registryName,
                          String registryKey, String registryRole) {
        if (type == ObjectType.REPRESENTATION) {
            throw new IllegalStateException("Cannot set format on a "
                                                    + " representationobject.");
        }
        getObjChars().setFormat(new Format(name, version));
    }
    //===------------------------------------------------------------------===//
    // End <premis:objectCharacteristics> setters.
    //===------------------------------------------------------------------===//

    /**
     * Set the size (in bytes) of the object.
     *
     * Can only be used when the {@link ObjectType} of this Object is either
     * a {@link ObjectType#BISTREAM} or {@link ObjectType#FILE}.
     *
     * @param size The size of the object in bytes.
     *
     * @throws IllegalStateException Thrown if the type of this object is
     * {@link ObjectType#REPRESENTATION}.
     */
    public void setSize(int size)  {
        if (type == ObjectType.REPRESENTATION) {
            throw new IllegalStateException("Cannot set size on a"
                                                + " representation object.");
        }
        getObjChars().setSize(size);
    }

    /**
     * Set the storage information for this object.
     *
     * Can only be used when the {@link ObjectType} of this Object is either
     * a {@link ObjectType#BISTREAM} or {@link ObjectType#FILE}.
     *
     * @param locationType The type of the location.
     * @param location the URI of the object.
     *
     * @throws IllegalStateException Thrown if the type of this object is
     * {@link ObjectType#REPRESENTATION}.
     */
    public void setStorage(String locationType, String location) {
        if (type == ObjectType.REPRESENTATION) {
            throw new IllegalStateException("Cannot set storage on a"
                                                + " representation object.");
        }
        storage = new Storage(locationType, location);
    }
}