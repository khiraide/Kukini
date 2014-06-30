package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class is a Java representation of the &lt;div&gt; element from METS.
 * it can contain other {@code Div} elements, and/or {@link Div.Fptr} objects.
 * Once this object has been added to either a {@link StructMap} or a parent
 * {@code Div}, it CANNOT be added again.
 *
 * @author Dongie Agnir
 */
public class Div {
    /**
     * Simple class that represents the &lt;fptr&gt; element from METS.
     */
    private static class Fptr {
        @XmlAttribute(name = "FILEID")
        private String fileID;

        /**
         * No-arg ctor for JAXB.  <b>Do not use.</b>
         */
        Fptr() {

        }

        /**
         * Construct this Fptr with the given FILEID attribute.
         * @param fileID
         */
        private Fptr(final String fileID) {
            this.fileID = fileID;
        }
    }

    @XmlAttribute(name = "ADMID")
    private String ADMID;

    @XmlElement(name = "div", namespace = METS.NS)
    private List<Div> childDivs = new ArrayList<Div>();

    @XmlElement(name = "fptr", namespace = METS.NS)
    private List<Fptr> filePtrs = new ArrayList<Fptr>();

    @XmlAttribute(name = "ID")
    private String localID;

    @XmlTransient
    private Object parent = null;

    /**
     * No-arg ctor for JAXB.  <b>Do not use.</b>
     */
    Div() {

    }

    /**
     * Construct this {@code Div} with the given ID and amdSec ID.
     * @param localID The ID for this {@code Div}.
     * @param ADMID The ID for this {@code Div's} amdSec; can be {@code null}.
     */
    Div(final String localID, final String ADMID) {
        this.localID = localID;
        this.ADMID = ADMID;
    }

    /**
     * Adds a (unbound) {@code Div} as a child.  This {@code Div} takes
     * ownership.
     *
     * @param d The child {@code Div} to add.
     */
    public void addDiv(Div d) {
        if (!d.isBound()) {
            d.setParent(this);
            childDivs.add(d);
        } else {
            throw new IllegalArgumentException("Div object to be added is already bound.");
        }
    }

    /**
     * Adds a {@link METSFile} instance to the {@code Div}.  The {@code Div}
     * takes ownership.
     *
     * @param file {@code METSFile} to be added.
     */
    public void addFile(METSFile file) {
        //Need to take ownership of the file here

        filePtrs.add(new Fptr(file.getID().getValue()));
    }

    /**
     * NOTE: This is what JAXB refers to as "Class defined" event callback.
     * After this object is unmarshalled by JAXB, it calls this method.
     *
     * @param um The Unmarshaller that unmarshalled this object.
     * @param parent The parent object.
     */
    void afterUnmarshal(Unmarshaller um, final Object parent) {
        this.parent = parent;
    }

    /**
     * Returns whether this a parent {@code Object} has taken ownership of this
     * {@code Div}.
     *
     * @return {@code} true of it is part of a parent object, {@code false} if
     * not.
     */
    boolean isBound() {
        return parent != null;
    }

    /**
     * Sets the parent of the this {@code Div}.
     *
     * @param parent The parent {@code Object}.
     */
    void setParent(final Object parent) {
        this.parent = parent;
    }
}
