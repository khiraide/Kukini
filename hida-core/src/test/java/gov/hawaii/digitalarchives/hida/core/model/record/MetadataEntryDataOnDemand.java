package gov.hawaii.digitalarchives.hida.core.model.record;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.apache.commons.httpclient.URIException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a MetadataEntry model class. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class MetadataEntryDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of MetadataEntries to be used for testing purposes.
     */
    private List<MetadataEntry> data;

    
    
    /** 
     * Builds a transient MetadataEntry to be used for testing.
     * 
     * @param index  Used so that each field set by this MetadataEntry is
     *               unique.
     * @return A properly formed MetadataEntry to be used for testing.
     * @throws URIException Badly formed DigitalObjectId.
     */
    public MetadataEntry getNewTransientMetadataEntry(int index) throws URIException {
        String name = "name_" + index;
        String objectExtractedFrom = "http://www.example.com/";
        String value = "value_" + index;
        MetadataEntry obj = new MetadataEntry(name, value, objectExtractedFrom);
        return obj;
    }

    
    
    /**
     * Grab a specific MetadataEntry instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the MetadataEntry instance that you 
     *                  want to grab from the database.
     * @return       The MetadataEntry.
     */
    public MetadataEntry getSpecificMetadataEntry(int index) {
        index = Math.min(data.size() - 1, Math.max(0, index));
        MetadataEntry obj = data.get(index);
        Long id = obj.getPrimaryId();
        return MetadataEntry.findMetadataEntry(id);
    }

    
    
    /**
     * Gets a random MetadataEntry from the database.
     * 
     * @return A random MetadataEntry from the database.
     * @throws URIException 
     */
    public MetadataEntry getRandomMetadataEntry() {
        MetadataEntry obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getPrimaryId();
        return MetadataEntry.findMetadataEntry(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj MetadataEntry object.
     * @return Whether a MetadataEntry persistence context 
     *         has been modified.
     */
    public boolean modifyMetadataEntry(MetadataEntry obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link MetadataEntry}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link MetadataEntryIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  MetadataEntry classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of MetadataEntries that
     *                            you want persisted into the database. 
     * @throws URIException Badly formed DigitalObjectId.
     */
    public void init(int initialDatabaseSize) throws URIException {
        data = MetadataEntry.findMetadataEntryEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'MetadataEntry'" +
                    " illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<MetadataEntry>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            MetadataEntry obj = getNewTransientMetadataEntry(i);
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
