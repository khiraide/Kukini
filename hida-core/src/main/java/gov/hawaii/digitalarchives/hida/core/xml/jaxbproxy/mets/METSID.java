package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

/**
 * This is a simple ID type for use when building the METS Document.
 *
 * Rationale: In some instances, the user creating their METS document will need
 * to provide the ID of one part of the document, when creating another.  For
 * example, when adding a File element, the user (in most cases) will want to
 * provide the ID of the {@link AmdSec} object that contains all of the
 * administrative metadata of that file.  At the same time, we want to to ensure
 * as much asa possible that the user is giving us proper IDs, so rather than
 * having them provide a raw String, they must provide an instance of this
 * object instead.  IDs shall then be generated and assigned internally by
 * {@link METSDoc}, and its child objects will provide their raw String ID
 * wrapped in this object.
 */
public class METSID {
    private final String IDVal;

    /**
     * Package private ctor to allow creation of by classes in the same package.
     * @param IDVal The String representation of the ID.
     */
    METSID(final String IDVal) {
        this.IDVal = IDVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null || rhs.getClass() != this.getClass()) {
            return false;
        }

        return IDVal.equals(((METSID)rhs).getValue());
    }

    /**
     * @return The value of the ID.
     */
    public String getValue() {
        return IDVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public int hashCode() {
        return IDVal.hashCode();
    }

}
