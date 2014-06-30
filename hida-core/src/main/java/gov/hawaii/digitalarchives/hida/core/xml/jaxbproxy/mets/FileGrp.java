package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * FileGrp is a Java representation of the {@code <fileGrp>} element in a METS
 * document.  It contains a list of {@code METSFile} objects.
 *
 * @author Dongie Agnir
 */
public class FileGrp {
    @XmlElement(name = "file", namespace = METS.NS)
    private List<METSFile> files = new ArrayList<METSFile>();

    @XmlAttribute(name = "USE")
    private String use;

    /**
     * No-arg ctor for JAXB.  <b>Do not use.</b>
     */
    FileGrp() {

    }

    /**
     * Construct this {@code FileGrp} with the indicated use, and the given
     * files.
     * @param use A {@code String} indicating the what this group is used for.
     *            Can be {@code null}.
     * @param files The list of files to be added to this FileGroup.
     */
    FileGrp(final String use, final METSFile... files) {
        assert files.length > 0;
        this.use = use;
        for (METSFile file : files) {
            if (!file.isBound()) {
                file.setParent(this);
                this.files.add(file);
            } else {
                //the file has been bound.  That is enough to disqualify the
                //entire list of files as ineligible for this group.  Undo what
                //did and throw.
                for (METSFile ffile : this.files) {
                    ffile.setParent(null);
                }
                throw new IllegalArgumentException("One of the files has"
                                                    + " already been bound!");
            }
        }
    }

    /**
     * @return The use descriptor for this group.
     */
    public String getUse() {
        return use;
    }
}
