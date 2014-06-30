package gov.hawaii.digitalarchives.hida.core.model.digitalobject;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIdSyntaxException;
import gov.hawaii.digitalarchives.hida.core.model.DigitalSignature;
import gov.hawaii.digitalarchives.hida.core.model.Event;
import gov.hawaii.digitalarchives.hida.core.model.Event.EventType;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of a {@link  DefaultFileObject}. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class DefaultFileObjectDataOnDemand {

    /**
     * Collection of DefaultFileObjects to be used for testing purposes.
     */
    private List<DefaultFileObject> data;

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();
    
    /**
     * Used when setting a field of DefaultFileObject  whose field is of type 
     * {@link java.nio.file.Path}.
     */
    private static final String TEST_RECORD = "/testrecord.txt";
    
    /** 
     * Builds a transient DefaultFileObject to be used for testing.
     * 
     * @return A properly formed DefaultFileObject to be used for testing.
     * @param  index Used so that each field set by this DefaultFileObject
     *         is unique.
     */
    public DefaultFileObject getNewTransientDefaultFileObject(int index) {
        DefaultFileObject defaultFileObject = new DefaultFileObject();
        setPrimaryId(defaultFileObject, index);
        setDigitalSignature(defaultFileObject, index);
        setFilePath(defaultFileObject);
        setFormatInformation(defaultFileObject, index);
        setHash(defaultFileObject, index);
        setRepresentationId(defaultFileObject, index);
        setSourceObjectId(defaultFileObject, index);
        SetSignificantProperties(defaultFileObject, index);
        SetStorageInformation(defaultFileObject, index);
        SetEvents(defaultFileObject, index);
        return defaultFileObject;
    }
    
    
    
    /**
     * Gets a random DefaultFileObject from the database.
     * 
     * @return A random DefaultFileObject from the database.
     */
    public DefaultFileObject getRandomDefaultFileObject() {
        DefaultFileObject defaultFileObject = data.get(rnd.nextInt(data.size()));
        String id = defaultFileObject.getPrimaryId().toString();
        return DefaultFileObject.findDefaultFileObject(id);
    }

    
    
    /**
     * Grab a specific DefaultFileObject instance from the database according 
     * to its index in the collection.
     * 
     * @param index  Index of the DefaultFileObject instance that you want to 
     *               grab from the database.
     * @return       The DefaultFileObject matching the index.
     */
    public DefaultFileObject getSpecificDefaultFileObject(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));

        DefaultFileObject defaultFileObject = data.get(index);
        String id = defaultFileObject.getPrimaryId().toString();
        return DefaultFileObject.findDefaultFileObject(id);
    }

    
    
    /**
     * Persists an initial amount of {@link DefaultFileObject}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link DefaultFileObjectIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  {@link DefaultFileObject} classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of DefaultFileObjects that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        int from = 0;
        int to = initialDatabaseSize;
        data = DefaultFileObject.findDefaultFileObjectEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for " +
                    "'DefaultFileObject' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<DefaultFileObject>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            DefaultFileObject defaultFileObject = getNewTransientDefaultFileObject(i);
            try {
                defaultFileObject.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            defaultFileObject.flush();
            data.add(defaultFileObject);
        }
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  defaultDigitalObject
     * @return Whether a DefaultFileObject persistence context 
     *         has been modified.
     */
    //TODO Still need to do this... should be good enough for this story for now though.
    public boolean modifyDefaultFileObject(DefaultFileObject defaultFileObject) {
        return false;
    }

    
    
    /**
     * Sets a {@link DefaultFileObject} with a unique {@link DigitalSignature}.
     * 
     * @param  defaultFileObject The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param  index Used so that each field set by this 
     *         {@link DefaultFileObject} is unique.
     */
    public void setDigitalSignature(DefaultFileObject defaultFileObject, int index) {
        // Creating transient Default Digital Signature.
        String signer = "signer_" + index;
        String algorithm = "algorithm_" + index;
        String x = "xx_" + index;
        String y = "yy_" + index;
        byte[] signatureBytes = x.getBytes();
        byte[] publicKey = y.getBytes();
        
        
        DigitalSignature defaultDigitalSignature 
            = new DigitalSignature(signer, algorithm, signatureBytes, publicKey);
        
        // Adding this DefaultDigitalSignature to this DefaultFileObject.
        defaultFileObject.setDigitalSignature(defaultDigitalSignature);
    }

    
    
    /**
     * Sets a {@link DefaultFileObject} with a unique collection of 
     * {@link Event}s.
     * 
     * @param  defaultFileObject The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param  index Used so that each field set by this 
     *         {@link DefaultFileObject} is unique.
     */
    public void SetEvents(DefaultFileObject defaultFileObject, int index) {
        EventType type = EventType.CAPTURE;
        Date date = new Date();
        String eventPrimaryId = "http://www.example.com/" + index;
        Event defaultEvent = new Event(eventPrimaryId, 
                type, date, "eventDetail_" + index);
        defaultFileObject.addEvent(defaultEvent);
    }

    
    
    /**
     * Sets a {@link DefaultFileObject} with a unique {@link Path}.
     * 
     * @param  defaultFileObject The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     */
    public void setFilePath(DefaultFileObject defaultFileObject) {
        defaultFileObject.setFilePath(getClass().getResource(TEST_RECORD).getPath());
    }
    
    
    
    /**
     * Sets a {@link DefaultFileObject} with a unique 
     * {@link FormatRegistryEntry}.
     * 
     * @param  defaultFileObject The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param  index Used so that each field set by this 
     *         {@link DefaultFileObject} is unique.
     */
    public void setFormatInformation(DefaultFileObject defaultFileObject, int index) {
        FormatRegistryEntry formatRegistryEntry
        = new FormatRegistryEntry("primaryId_" + index, "assigningRegistry_" + index,
                 "formatName_" + index, "formatVersion_" + index); 
        defaultFileObject.setFormatInformation(formatRegistryEntry);
    }
    
    
    
    /**
     * Sets a {@link DefaultFileObject} with a unique hash.
     * 
     * @param  defaultFileObject The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param  index Used so that each field set by this 
     *         {@link DefaultFileObject} is unique.
     */
    public void setHash(DefaultFileObject defaultFileObject, int index) {
        String algorithm = "algorithm_" + index;
        String x = "xx_" + index;
        byte[] hashBytes = x.getBytes();
        defaultFileObject.setHash(hashBytes, algorithm);
    }
    
    
    
    /**
     * Sets a unique {@link DigitalObjectId} as this
     * {@link DefaultFileObject}'s primaryId.
     * 
     * @param defaultFileObject  The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param index  Used so that each {@link DigitalObjectId} field set by 
     *               this DefaultFileObject is unique.
     */
    public void setPrimaryId(DefaultFileObject defaultFileObject, int index) {
        String primaryId = "http://www.example.com/" + index;
        defaultFileObject.setPrimaryId(primaryId);
    }

    
    
    /**
     * Sets a unique {@link RepresentationId} into this 
     * {@link DefaultFileObject}.
     * 
     * @param defaultFileObject  The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param index  Used so that each {@link RepresentationId} field set by 
     *               this DefaultFileObject is unique.
     */
    public void setRepresentationId(DefaultFileObject defaultFileObject, int index) {
        String representationId = "http://www.example.com/" + index;
        defaultFileObject.setRepresentationId(representationId);
    }

    
    
    /**
     * Sets a unique {@link Property} into this {@link DefaultFileObject}.
     * 
     * @param defaultFileObject  The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param index  Used so that each {@link Property} field set by this 
     *               DefaultFileObject is unique.
     */
    public void SetSignificantProperties(DefaultFileObject defaultFileObject, int index) {
        Property property = new Property();
        property.setName("name_" + index);
        property.setValue("value_" + index);
        defaultFileObject.addSignificantProperty(property);
        property.setDigitalObject(defaultFileObject);
    }

    
    
    /**
     * Sets a unique SourceObjectId into this {@link DefaultFileObject}.
     * 
     * @param defaultFileObject  The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param index  Used so that each SourceObjectId field set by this 
     *               DefaultFileObject is unique.
     */
    public void setSourceObjectId(DefaultFileObject defaultFileObject, int index) {
        String sourceObjectId = "http://www.example.com/" + index;
        defaultFileObject.setSourceObjectId(sourceObjectId);
    }

    
    
    /**
     * Sets a unique {@link StorageEntry} into this {@link DefaultFileObject}.
     * 
     * @param defaultFileObject  The {@link DefaultFileObject} instance whose 
     *                           fields we want to set for testing purposes.
     * @param index  Used so that each {@link StorageEntry} field set by this 
     *               DefaultFileObject is unique.
     */
    public void SetStorageInformation(DefaultFileObject defaultFileObject, int index) {
        URI locationValue = null;
        try {
            locationValue = new URI("http://www.example.com/" + index);
        } catch (URISyntaxException e) {
            throw new HidaIdSyntaxException("Badly formatted URI " +
                    "when localValue was constructed for the setStorageInformation() method " +
                    "in the DefaultFileObjectDataOnDemand class.", e);
        }
        StorageEntry storageEntry 
        = new StorageEntry("locationType_" + index, 
                locationValue, "mediumType" + index, defaultFileObject);
        defaultFileObject.addStorageInformation(storageEntry);
    }
}
