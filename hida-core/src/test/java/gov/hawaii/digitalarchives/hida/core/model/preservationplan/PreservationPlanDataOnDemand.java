package gov.hawaii.digitalarchives.hida.core.model.preservationplan;

import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a {@link PreservationPlan}. 
 * 
 * @author Keone Hiraide
 */
@Configurable
@Component
public class PreservationPlanDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of PreservationPlans to be used for testing purposes.
     */
    private List<PreservationPlan> data;

    /** 
     * Builds a transient {@link PreservationPlan} to be used for testing.
     * 
     * @return A properly formed PreservationPlan to be used for testing.
     * @param  index Used so that each field set by this PreservationPlan
     *         is unique.
     */
    public PreservationPlan getNewTransientPreservationPlan(int index) {
        PreservationPlan obj = new PreservationPlan();
        setPrimaryId(obj, index);
        setCreatedDate(obj, index);
        setLabel(obj, index);
        setLastModifiedDate(obj, index);
        setFormatPlans(obj, index);
        return obj;
    }
    
    /**
     * Sets a unique {@link PreservationPlanId} as this 
     * {@link PreservationPlan}'s primaryId.
     * 
     * @param obj  The {@link PreservationPlan} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link PreservationPlanId} field
     *               set by this PreservationPlan is unique.
     */
    public void setPrimaryId(PreservationPlan obj, int index) {
        String preservationPlanId = "primaryId_" + index;
        obj.setPrimaryId(preservationPlanId);
    }

    /**
     * Sets a unique creation {@link Date} into this 
     * {@link PreservationPlan}.
     * 
     * @param obj  The {@link PreservationPlan} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link PreservationPlan} field
     *               set by this PreservationPlan is unique.
     */
    public void setCreatedDate(PreservationPlan obj, int index) {
        Date createdDate = new Date();
        obj.setCreatedDate(createdDate);
    }

    /**
     * Sets a unique label of type {@link String} into this 
     * {@link PreservationPlan}.
     * 
     * @param obj  The {@link PreservationPlan} instance
     *             whose fields we want to set for testing
     *             purposes.
     * @param index  Used so that each {@link PreservationPlan} field
     *               set by this {@link PreservationPlan} is unique.
     */
    public void setLabel(PreservationPlan obj, int index) {
        String label = "label_" + index;
        obj.setLabel(label);
    }

    /**
     * Sets a unique last modified {@link Date} into this 
     * {@link PreservationPlan}.
     * 
     * @param obj  The {@link PreservationPlan} instance
     *                           whose fields we want to set for testing
     *                           purposes.
     * @param index  Used so that each {@link PreservationPlan} field
     *               set by this PreservationPlan is unique.
     */
    public void setLastModifiedDate(PreservationPlan obj, int index) {
        Date lastModifiedDate = new Date();
        obj.setLastModifiedDate(lastModifiedDate);
    }
    
    
    
    /**
     * Sets a collection of {@link FormatPlan}s associated with a 
     * {@link PreservationPlan}.
     * 
     * @param obj  The {@link PreservationPlan} instance
     *             whose fields we want to set for testing
     *             purposes.
     * @param index  Used so that each {@link PreservationPlan} field
     *               set by this {@link PreservationPlan} is unique.
     */
    public void setFormatPlans(PreservationPlan obj, int index) {
        Map<String, FormatPlan> formatPlans = new HashMap<String, FormatPlan>();
        FormatPlan formatPlan = new FormatPlan();
        
        // Represents a native format for a formatPlan.
        FileFormat nativeFormat = new FileFormat();
        nativeFormat.setFormatName(".doc");
        nativeFormat.setPronomFormat("FMT/40_" + index);
        
        // Represents a preservation format for a FormatPlan.
        FileFormat preservationFormat = new FileFormat();
        preservationFormat.setFormatName(".docx");
        preservationFormat.setPronomFormat("FMT/412");
        
        // Represents a presentation format for a FormatPlan.
        FileFormat presentationFormat = new FileFormat();
        presentationFormat.setFormatName(".pdf/A");
        presentationFormat.setPronomFormat("FMT/95");
        
        // Represents a thumbnail format for a FormatPlan.
        FileFormat thumbnailFormat = new FileFormat();
        thumbnailFormat.setFormatName(".jpg");
        thumbnailFormat.setPronomFormat("FMT/42");
        
        formatPlan.setPrimaryId(".dxf_" + index);
        formatPlan.setNativeFormat(nativeFormat);
        formatPlan.setPreservationFormat(preservationFormat);
        formatPlan.setPresentationFormat(presentationFormat);
        formatPlan.setThumbnailFormat(thumbnailFormat);
        
        formatPlans.put(formatPlan.getNativeFormat().getPronomFormat(), formatPlan);
        String label = "label_" + index;
        obj.setLabel(label);
        obj.setFormatPlans(formatPlans);
    }

    /**
     * Grab a specific {@link PreservationPlan} instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the PreservationPlan instance that you 
     *                  want to grab from the database.
     * @return       The PreservationPlan instance
     *               whose fields we want to set for testing
     *               purposes.
     */
    public PreservationPlan getSpecificPreservationPlan(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));
        PreservationPlan obj = data.get(index);
        String id = obj.getPrimaryId().toString();
        return PreservationPlan.findPreservationPlan(id);
    }

    /**
     * Gets a random {@link PreservationPlan} from the database.
     * 
     * @return A random PreservationPlan from the database.
     */
    public PreservationPlan getRandomPreservationPlan() {
        PreservationPlan obj = data.get(rnd.nextInt(data.size()));
        String id = obj.getPrimaryId().toString();
        return PreservationPlan.findPreservationPlan(id);
    }

    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj {@link PreservationPlan} object you want to set as being
     *             modified.
     * @return Whether a PreservationPlan persistence context 
     *         has been modified.
     */
    //TODO Still need to do this... should be good enough for this story.
    public boolean modifyPreservationPlan(PreservationPlan obj) {
        return false;
    }

    /**
     * Persists an initial amount of {@link PreservationPlan}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link PreservationPlanIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  {@link PreservationPlan} classes' persist 
     * method.
     * 
     * @param initialDatabaseSize The initial amount of PreservationPlans that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = PreservationPlan.findPreservationPlanEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for "
                    + "'PreservationPlan' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<PreservationPlan>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            PreservationPlan obj = getNewTransientPreservationPlan(i);
            try {
                obj.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            obj.flush();
            data.add(obj);
        }
    }
}
