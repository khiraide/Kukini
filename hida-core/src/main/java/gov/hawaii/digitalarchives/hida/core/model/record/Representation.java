package gov.hawaii.digitalarchives.hida.core.model.record;

import gov.hawaii.digitalarchives.hida.core.model.digitalobject.FileObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;

/**
 * Model class of a Representation.
 * <p>
 * A 'representation' of a record is the subset of the digital objects
 * associated with it that, taken together can fully (or as close to the
 * original as possible) give an accurate (but derivative) depiction of
 * the original record.
 * </p>
 * <p>
 * For example, given a record which was originally a PDF file, a separate
 * representation of it could be the set of text files that contain the
 * text of each page of the original PDF.
 * </p>
 *
 * @author Dongie Agnir
 */
public class Representation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of the {@link DigitalRecord} this Representation belongs to.
    */
    private String digitalRecordId;

    /**
     * The ID of this {@code Representation}.
     */
    private String id;

    /**
     * The representation in this branch of representations that
     * precedes this one.
     */
    private String previous;

    /**
     * The representation that this representation is derived
     * from.  Note that it is not necessarily the case that the
     * representation returned by {@link #getPrevious()} is the same as
     * the one returned by this method.
     */
    private String source;

    /**
     * The list of {@link FileObject} IDs that comprise this
     * representation.
     */
    private List<String> fileObjects;
    
    @Transient
    private Boolean isNative = null;
    
    @Transient
    private Boolean derivativesProcessed = null;

    
    
    /**
     * Oracle specifications state that the class must have a public 
     * or protected, no-argument constructor. The class may have other 
     * constructors. 
     * http://docs.oracle.com/javaee/7/tutorial/doc/persistence-intro001.htm
     */
    public Representation() {
        
    }
    
    /** Autowired logger to log this class */
    private transient Logger log = null;
    
    
    
    /**
     * Creates a Representation and ensures that the values passed in are not
     * null.
     * 
     * @param id  The ID of this {@code Representation}.
     * @param digitalRecordId  The ID of the {@link DigitalRecord} this
     *                         Representation belongs to.
     * @param previous  The representation in this branch of representations 
     *                  that precedes this one.
     * @param source  The representation that this representation is derived
     *                from.  
     * @param fileObjects  The list of {@link FileObject} IDs that comprise
     *                     this representation.
     */
    public Representation(final String id, final String digitalRecordId,
            final String previous, final String source,
            final List<String> fileObjects) {
        if (digitalRecordId == null) {
            throw new IllegalArgumentException("digitalRecordId cannot be null");
        }
        
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        
        if (previous == null) {
            throw new IllegalArgumentException("previous cannot be null");
        }
        
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        
        if (fileObjects == null) {
            throw new IllegalArgumentException("fileObjects cannot be null");
        }
        
        this.digitalRecordId = digitalRecordId;
        this.id = id;
        this.previous = previous;
        this.source = source;
        this.fileObjects = fileObjects;
    }


    
    /**
     * @return The ID of this {@code Representation}.
     */
    public String getId() {
        return id;
    }

    
    
    /**
     * @return  The ID of the {@link DigitalRecord} this Representation 
     *          belongs to.
     */
    public String getDigitalRecordId() {
        return digitalRecordId;
    }

    
    
    /**
     * @return  The list of {@link FileObject} IDs that comprise this
     * representation.
     */
    public List<String> getFileObjects() {
        return new ArrayList<String>(fileObjects);
    }


    
    /**
     * @return The representation in this branch of representations that
     * precedes this one.
     */
    public String getPrevious() {
        return previous;
    }

    
    
    /** 
     * @return The representation that this representation is derived
     * from. Note that it is not necessarily the case that the
     * representation returned by {@link #getPrevious()} is the same as
     * the one returned by this method.
     */
    public String getSource() {
        return source;
    }
    
    /** @return True if object has been marked as native, false if not native,
     *         and null if unprocessed. */
    public Boolean isNative() {
        return this.isNative;
    }
    
    /** @param isNative True to mark object as native, false for not native, or
     *            null for unprocessed. */
    public void setIsNative(Boolean isNative) {
        this.isNative = isNative;
    }
    
    /** @return True if object has been marked as derivatives processed, false if
     *         not, and null if object has not been processed. */
    public Boolean isDerivativesProcessed() {
        return this.derivativesProcessed;
    }
    
    /** @param derivativesProcessed True to mark object as derivatives processed,
     *            false for not derivatives processed, and null for unprocessed. */
    public void setDerivativesProcessed(Boolean derivativesProcessed) {
        this.derivativesProcessed = derivativesProcessed;
    }
    
    /**
     * Sets the logger for this class.
     * 
     * @param log The logger for this class.
     */
    @AutowiredLogger
    public void setLog(Logger log) {
        this.log = log;
    }
}
